package dk.dma.msinm.common.repo;

import dk.dma.msinm.common.settings.annotation.Setting;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

/**
 * A repository service.
 * Streams files from the repository
 */
@javax.ws.rs.Path("/repo")
@Singleton
@Lock(LockType.READ)
public class RepositoryService {

    /** The number of hashed sub-folders to use **/
    public enum HashFolderLevels { ONE, TWO }

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

}
