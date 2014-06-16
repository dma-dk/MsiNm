package dk.dma.msinm.common.repo;

import dk.dma.msinm.common.settings.annotation.Setting;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.slf4j.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A repository service.
 * Streams files from the repository
 */
@javax.ws.rs.Path("/repo")
@Singleton
@Lock(LockType.READ)
@SecurityDomain("msinm-policy")
@PermitAll
public class RepositoryService {

    /** The number of hashed sub-folders to use **/
    public enum HashFolderLevels { ONE, TWO }

    @Context
    ServletContext servletContext;

    @Inject
    @Setting(value = "repoRootPath", defaultValue = "${user.home}/.msinm/repo", substituteSystemProperties = true)
    Path repoRoot;

    @Inject
    @Setting(value = "repoCacheTimeoutMinutes", defaultValue = "5")
    Long cacheTimeout;

    @Inject
    Logger log;

    /**
     * Initializes the repository
     */
    @PostConstruct
    public void init() {
        // Create the repo root directory
        if (!Files.exists(repoRoot)) {
            try {
                Files.createDirectories(repoRoot);
            } catch (IOException e) {
                log.error("Error creating repository dir " + repoRoot, e);
            }
        }
    }

    /**
     * Returns the repository root
     * @return the repository root
     */
    public Path getRepoRoot() {
        return repoRoot;
    }


    /**
     * Creates two levels of sub-folders within the {@code rootFolder} based on
     * a hash of the {@code fileName}.
     * If the sub-folder does not exist, it is created.
     *
     * @param levels the number of hashed sub folder to use
     * @param rootFolder the root folder within the repository root
     * @param fileName the file name
     * @return the sub-folder associated with the file name
     */
    public Path getHashedSubfolder(HashFolderLevels levels, String rootFolder, String fileName) throws IOException {
        int hashCode = fileName.hashCode();
        int mask = 255;

        Path folder = getRepoRoot();

        // Add the root folder
        if (StringUtils.isNotBlank(rootFolder)) {
            folder = folder.resolve(rootFolder);
        }

        // Add one or two levels of hashed sub-folders
        folder = folder.resolve(String.format("%03d", hashCode & mask));
        if (levels == HashFolderLevels.TWO) {
            folder = folder.resolve(String.format("%03d", (hashCode >> 8) & mask));
        }

        // Create the folder if it does not exist
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }
        return folder;
    }


    /**
     * Streams the file specified by the path
     * @param path the path
     * @param request the servlet request
     * @return the response
     */
    @GET
    @javax.ws.rs.Path("/{file:.+}")
    public Response streamFile(@PathParam("file") String path,
                               @Context Request request) throws IOException {

        Path f = repoRoot.resolve(path);

        if (Files.notExists(f) || Files.isDirectory(f)) {
            log.warn("Failed streaming file: " + f);
            throw new WebApplicationException(404);
        }

        // Set expiry 10 min
        Date expirationDate = new Date(System.currentTimeMillis() + 1000L * 60L * cacheTimeout);

        // For some reason unknown, this does not work
        String mt = new MimetypesFileTypeMap().getContentType(f.toFile());

        // Check for an ETag match
        EntityTag etag = new EntityTag("" + Files.getLastModifiedTime(f).toMillis() + "_" + Files.size(f), true);
        Response.ResponseBuilder responseBuilder = request.evaluatePreconditions(etag);
        if (responseBuilder != null) {
            // Etag match
            log.trace("File unchanged. Return code 304");
            return responseBuilder
                    .expires(expirationDate)
                    .build();
        }

        log.trace("Streaming file: " + f);
        return Response
                .ok(f.toFile(), mt)
                .expires(expirationDate)
                .tag(etag)
                .build();
    }


    /**
     * Handles upload of files
     *
     * @param request the request
     */
    @POST
    @javax.ws.rs.Path("/{folder:.+}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({ "admin" })
    public List<String> uploadFile(@PathParam("folder") String path, @Context HttpServletRequest request) throws FileUploadException, IOException {

        Path folder = repoRoot.resolve(path);

        if (Files.exists(folder) && !Files.isDirectory(folder)) {
            log.warn("Failed streaming file to folder: " + folder);
            throw new WebApplicationException("Invalid upload folder: " + path, 403);

        } else if (Files.notExists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                log.error("Error creating repository folder " + folder, e);
                throw new WebApplicationException("Invalid upload folder: " + path, 403);
            }
        }


        List<String> result = new ArrayList<>();
        FileItemFactory factory = newDiskFileItemFactory(servletContext);
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);

        for (FileItem item : items) {
            if (!item.isFormField()) {
                File destFile = folder.resolve(item.getName()).toFile();
                log.info("File " + item.getName() + " is uploaded to " + destFile);
                try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destFile))) {
                    InputStream in = new BufferedInputStream(item.getInputStream());
                    byte[] buffer = new byte[1024];
                    int len = in.read(buffer);
                    while (len != -1) {
                        out.write(buffer, 0, len);
                        len = in.read(buffer);
                    }
                    out.flush();
                }

                // Return the repo-relative path as a result
                result.add(Paths.get(path, item.getName()).toString());
            }
        }

        return result;
    }


    /**
     * Creates a new DiskFileItemFactory. See:
     * http://commons.apache.org/proper/commons-fileupload/using.html
     * @return the new DiskFileItemFactory
     */
    public static DiskFileItemFactory newDiskFileItemFactory(ServletContext servletContext) {
        FileCleaningTracker fileCleaningTracker = FileCleanerCleanup.getFileCleaningTracker(servletContext);
        DiskFileItemFactory factory = new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, null);
        factory.setFileCleaningTracker(fileCleaningTracker);
        return factory;
    }
}
