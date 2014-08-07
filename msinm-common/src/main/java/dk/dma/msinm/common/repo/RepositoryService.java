/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msinm.common.repo;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.common.util.WebUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A repository service.<br>
 * Streams files from the repository and facilitates uploading files to the repository.
 * <p>
 *     The repository is public in as much as everybody can download all files.<br>
 *
 *     However only "admin" users can upload files to the entire repository.<br>
 *
 *     Registered users, with the "user" role, can upload files to a sub-root of
 *     the repository, the {@code repoTempRoot}. Files upload to the this part
 *     of the repository will be deleted after 24 hours.
 * </p>
 */
@javax.ws.rs.Path("/repo")
@Singleton
@Lock(LockType.READ)
@SecurityDomain("msinm-policy")
@PermitAll
public class RepositoryService {

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

    @Inject
    FileTypes fileTypes;

    @Inject
    ThumbnailService thumbnailService;

    @Inject
    MsiNmApp app;

    /**
     * Initializes the repository
     */
    @PostConstruct
    public void init() {
        // Create the repo root directory
        if (!Files.exists(getRepoRoot())) {
            try {
                Files.createDirectories(getRepoRoot());
            } catch (IOException e) {
                log.error("Error creating repository dir " + getRepoRoot(), e);
            }
        }

        // Create the repo "temp" root directory
        if (!Files.exists(getTempRepoRoot())) {
            try {
                Files.createDirectories(getTempRepoRoot());
            } catch (IOException e) {
                log.error("Error creating repository dir " + getTempRepoRoot(), e);
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
     * Returns the repository "temp" root
     * @return the repository "temp" root
     */
    public Path getTempRepoRoot() {
        return getRepoRoot().resolve("temp");
    }


    /**
     * Creates a URI from the repo file
     * @param repoFile the repo file
     * @return the URI for the file
     */
    public String getRepoUri(Path repoFile) {
        Path filePath = getRepoRoot().relativize(repoFile);
        return "/rest/repo/file/" + WebUtils.encodeURI(filePath.toString().replace('\\', '/'));
    }

    /**
     * Creates a path from the repo file relative to the repo root
     * @param repoFile the repo file
     * @return the path for the file
     */
    public String getRepoPath(Path repoFile) {
        Path filePath = getRepoRoot().relativize(repoFile);
        return filePath.toString().replace('\\', '/');
    }

    /**
     * Creates two levels of sub-folders within the {@code rootFolder} based on
     * a MD5 hash of the {@code target}.
     * If the sub-folder does not exist, it is created.
     *
     * @param rootFolder the root folder within the repository root
     * @param target the target name used for the hash
     * @param includeTarget whether to create a sub-folder for the target or not
     * @return the sub-folder associated with the target
     */
    public Path getHashedSubfolder(String rootFolder, String target, boolean includeTarget) throws IOException {
        byte[] bytes = target.getBytes("utf-8");

        // MD5 hash the ID
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("This should never happen");
        }
        md.update(bytes);
        bytes = md.digest();
        String hash = String.valueOf(Integer.toHexString(bytes[0] & 0xff));
        while (hash.length() < 2) {
            hash = "0" + hash;
        }

        Path folder = getRepoRoot();

        // Add the root folder
        if (StringUtils.isNotBlank(rootFolder)) {
            folder = folder.resolve(rootFolder);
        }

        // Add two hashed sub-folder levels
        folder = folder
                .resolve(hash.substring(0, 1))
                .resolve(hash.substring(0, 2));

        // Check if we should create a sub-folder for the target as well
        if (includeTarget) {
            folder = folder.resolve(target);
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
    @javax.ws.rs.Path("/file/{file:.+}")
    public Response streamFile(@PathParam("file") String path,
                               @Context Request request) throws IOException {

        Path f = repoRoot.resolve(path);

        if (Files.notExists(f) || Files.isDirectory(f)) {
            log.warn("Failed streaming file: " + f);
            throw new WebApplicationException(404);
        }

        // Set expiry to cacheTimeout minutes
        Date expirationDate = new Date(System.currentTimeMillis() + 1000L * 60L * cacheTimeout);

        String mt = fileTypes.getContentType(f);

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
     * Returns the thumbnail to use for the file specified by the path
     * @param path the path
     * @param size the icon size, either 32, 64 or 128
     * @return the thumbnail to use for the file specified by the path
     */
    @GET
    @javax.ws.rs.Path("/thumb/{file:.+}")
    public Response getThumbnail(@PathParam("file") String path,
                                 @QueryParam("size") @DefaultValue("64") int size) throws IOException, URISyntaxException {

        IconSize iconSize = IconSize.getIconSize(size);
        Path f = repoRoot.resolve(path);

        if (Files.notExists(f) || Files.isDirectory(f)) {
            log.warn("Failed streaming file: " + f);
            throw new WebApplicationException(404);
        }

        // Check if we can generate a thumbnail for image files
        String thumbUri = null;
        Path thumbFile = thumbnailService.getThumbnail(f, iconSize);
        if (thumbFile != null) {
            thumbUri = app.getBaseUri() + getRepoUri(thumbFile);
        } else {
            // Fall back to file type icons
            thumbUri = app.getBaseUri() + "/" + fileTypes.getIcon(f, iconSize);
        }

        log.trace("Redirecting to thumbnail: " + thumbUri);
        return Response
                .temporaryRedirect(new URI(thumbUri))
                .build();
    }

    /**
     * Returns a list of files in the folder specified by the path
     * @param path the path
     * @return the list of files in the folder specified by the path
     */
    @GET
    @javax.ws.rs.Path("/list/{folder:.+}")
    @Produces("application/json;charset=UTF-8")
    @NoCache
    public List<RepoFileVo> listFiles(@PathParam("folder") String path) throws IOException {

        List<RepoFileVo> result = new ArrayList<>();
        Path folder = repoRoot.resolve(path);

        if (Files.exists(folder) && Files.isDirectory(folder)) {

            // Filter out directories, hidden files, thumbnails and map images
            DirectoryStream.Filter<Path> filter = file ->
                    Files.isRegularFile(file) &&
                    !file.getFileName().toString().startsWith(".") &&
                    !file.getFileName().toString().matches(".+_thumb_\\d{1,3}\\.\\w+") && // Thumbnails
                    !file.getFileName().toString().matches("map_\\d{1,3}\\.png"); // Map image

            Files.newDirectoryStream(folder, filter)
                    .forEach(f -> {
                        RepoFileVo vo = new RepoFileVo();
                        vo.setName(f.getFileName().toString());
                        vo.setPath(WebUtils.encodeURI(path + "/" + f.getFileName().toString()));
                        vo.setDirectory(Files.isDirectory(f));
                        result.add(vo);
                    });
        }
        return result;
    }

    /**
     * Handles upload of files
     *
     * @param path the folder to upload to
     * @param request the request
     */
    @POST
    @javax.ws.rs.Path("/upload/{folder:.+}")
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
                // Argh - IE includes the path in the item.getName()!
                String fileName = Paths.get(item.getName()).getFileName().toString();
                File destFile = getUniqueFile(folder, fileName).toFile();
                log.info("File " + fileName + " is uploaded to " + destFile);
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
                result.add(Paths.get(path, fileName).toString());
            }
        }

        return result;
    }

    /**
     * Returns a unique file name in the given folder.
     * If the given file name is not unique, a new is constructed
     * by adding a number to the file name
     * @param folder the folder
     * @param name the file name
     * @return the new unique file
     */
    private Path getUniqueFile(Path folder, String name) {
        Path file = folder.resolve(name);
        if (Files.exists(file)) {
            for (int x = 2; true; x++) {
                String fileName =
                        FilenameUtils.removeExtension(name) +
                        " " + x + "." +
                        FilenameUtils.getExtension(name);
                file = folder.resolve(fileName);
                if (!Files.exists(file)) {
                    break;
                }
            }
        }
        return file;
    }

    /**
     * Returns a new unique "temp" directory. Please note, the directory has not yet been created.
     *
     * @return a new unique "temp" directory
     */
    @GET
    @javax.ws.rs.Path("/new-temp-dir")
    @Produces("application/json;charset=UTF-8")
    public RepoFileVo getNewTempDir() {

        // Construct a unique directory name
        String name = UUID.randomUUID().toString();// test "f823b7d5-9559-4a76-b3a3-6d32f2bf55f2";

        RepoFileVo dir = new RepoFileVo();
        dir.setName(name);
        dir.setPath(WebUtils.encodeURI("temp/" + name));
        dir.setDirectory(false);
        return dir;
    }

    /**
     * Handles upload of files to the "temp" repo root.
     * Only the "user" role is required to upload to the "temp" root
     *
     * @param path the folder to upload to
     * @param request the request
     */
    @POST
    @javax.ws.rs.Path("/upload-temp/{folder:.+}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({ "user" })
    public List<String> uploadTempFile(@PathParam("folder") String path, @Context HttpServletRequest request) throws FileUploadException, IOException {

        // Check that the specified folder is indeed under the "temp" root
        Path folder = repoRoot.resolve(path);
        if (!folder.toAbsolutePath().startsWith(getTempRepoRoot().toAbsolutePath())) {
            log.warn("Failed streaming file to temp root folder: " + folder);
            throw new WebApplicationException("Invalid upload folder: " + path, 403);
        }

        return uploadFile(path, request);
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


    /**
     * Every hour, check the repo "temp" root, and delete old files and folders
     */
    @Schedule(persistent = false, second = "50", minute = "22", hour = "*", dayOfWeek = "*", year = "*")
    public void cleanUpTempRoot() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        File[] files = getTempRepoRoot().toFile().listFiles();
        if (files != null && files.length > 0) {
            Arrays.asList(files).forEach(f -> checkDeletePath(f, cal.getTime()));
        }
    }

    /**
     * Recursively delete one day old files and folders
     * @param file the current root file or folder
     * @param date the expiry date
     */
    private void checkDeletePath(File file, Date date) {
        if (FileUtils.isFileOlder(file, date)) {
            log.info("Deleting expired temp file or folder: " + file);
            FileUtils.deleteQuietly(file);
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                Arrays.asList(files).forEach(f -> checkDeletePath(f, date));
            }
        }
    }

    /**
     * Moves the repository from the repoPath the the newRepoPath.
     * If the directory specified by the repoPath does not exists, false is returned.
     * @param repoPath the repository path
     * @param newRepoPath the new repository path
     */
    public boolean moveRepoFolder(String repoPath, String newRepoPath) throws IOException {
        Path from = getRepoRoot().resolve(repoPath);
        Path to = getRepoRoot().resolve(newRepoPath);
        if (Files.exists(from)) {
            FileUtils.copyDirectory(from.toFile(), to.toFile());
            return true;
        }
        return false;
    }
}
