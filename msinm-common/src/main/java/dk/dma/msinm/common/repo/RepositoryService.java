package dk.dma.msinm.common.repo;

import dk.dma.msinm.common.settings.annotation.Setting;
import org.slf4j.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PostConstruct;
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
public class RepositoryService {

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
     * Streams the file specified by the path
     * @param path the path
     * @param request the servlet request
     * @return
     * @throws IOException
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
