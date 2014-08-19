package dk.dma.msinm.common.repo;

import dk.dma.msinm.common.settings.annotation.Setting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Creates thumbnails
 * <p>
 *     An ExecutorService is used to limit load on the system
 * </p>
 */
@Named
@ApplicationScoped
@Lock(LockType.READ)
public class ThumbnailService {

    private static final int EXECUTOR_POOL_SIZE = 2;

    @Inject
    Logger log;

    @Inject
    @Setting(value = "vipsPath")
    String vipsCmd;

    Set<String> vipsFileTypes = new HashSet<>();

    @Inject
    FileTypes fileTypes;

    private ExecutorService processPool;

    @PostConstruct
    private void init() {
        processPool = Executors.newFixedThreadPool(EXECUTOR_POOL_SIZE);

        // Enlist image types supported by vips (avoid gif)
        vipsFileTypes.add("image/bmp");
        vipsFileTypes.add("image/jpeg");
        vipsFileTypes.add("image/jpg");
        vipsFileTypes.add("image/tiff");
        vipsFileTypes.add("image/tif");
        vipsFileTypes.add("image/png");
    }

    @PreDestroy
    private void closeDown() {
        if (processPool != null && !processPool.isShutdown()) {
            processPool.shutdown();
            processPool = null;
        }
    }


    /**
     * Returns or creates the a thumbnail for the given file if it is an image.
     * Otherwise, null is returned
     *
     * @param file the file to create a thumbnail for
     * @param size the size of the thumbnail
     * @return the thumbnail file or null if none was found or created
     */
    public Path getThumbnail(final Path file, final IconSize size) {

        // Check that the file exists
        if (!Files.isRegularFile(file)) {
            log.warn("File does not exist: " + file);
            return null;
        }

        final String type = fileTypes.getContentType(file);
        if (type == null || !type.startsWith("image")) {
            log.debug("File not an image: " + file);
            return null;
        }

        // Submit the thumbnail creation job to the process pool.
        // Please note, this is used as a way of restricting load on the system,
        // not per se as a way of executing the task asynchronously.
        try {
            return processPool.submit(() -> createThumbnail(file, type, size)).get();
        } catch (InterruptedException | ExecutionException e) {
            log.debug("Error creating thumbnail");
        }

        return null;
    }

    /**
     * Returns or creates the a thumbnail for the given file if it is an image.
     * Otherwise, null is returned
     *
     * @param file the file to create a thumbnail for
     * @param type the type of image
     * @param size the size of the thumbnail
     * @return the thumbnail file or null if none was found or created
     */
    public Path createThumbnail(Path file, String type, IconSize size) {

        try {
            // Construct the thumbnail name by appending "_thumb_size" to the file name
            String thumbName = String.format("%s_thumb_%d.%s",
                    FilenameUtils.removeExtension(file.getFileName().toString()),
                    size.getSize(),
                    FilenameUtils.getExtension(file.getFileName().toString()));

            // Check if the thumbnail already exists
            Path thumbFile = file.getParent().resolve(thumbName);
            if (Files.isRegularFile(thumbFile) &&
                    Files.getLastModifiedTime(thumbFile).toMillis() >= Files.getLastModifiedTime(file).toMillis()) {
                return thumbFile;
            }

            // Check whether to use VIPS or java
            if (StringUtils.isNotBlank(vipsCmd) &&
                    vipsFileTypes.contains(type.toLowerCase())) {
                // Use VIPS
                createThumbnailUsingVips(file, thumbFile, size);

            } else {
                // Use java APIs
                createThumbnailUsingJava(file, thumbFile, size);
            }

            return thumbFile;

        } catch (IOException e) {
            // Alas, no thumbnail
            return null;
        }
    }

    /**
     * Creates a thumbnail for the image file using libvips
     *
     * @param file      the image file
     * @param thumbFile the resulting thumbnail file
     * @param size      the size of the thumbnail
     */
    private void createThumbnailUsingVips(Path file, Path thumbFile, IconSize size) throws IOException {

        try {

            // Example command: vipsthumbnail -s 64 -p bilinear -o thumb.png image2.jpg
            final Process proc = new ProcessBuilder(
                    vipsCmd,
                    "-s", String.valueOf(size.getSize()),
                    "-p", "bilinear",
                    "-o", thumbFile.toString(),
                    file.toString())
                    .directory(new File(System.getProperty("user.dir")))
                    .inheritIO()
                    .start();

            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                log.debug(line);
            }

            //Wait to get exit value
            int exitValue = proc.waitFor();
            log.debug("Exit Value is " + exitValue);

            // Update the timestamp of the thumbnail file to match the change date of the image file
            Files.setLastModifiedTime(thumbFile, Files.getLastModifiedTime(file));

        } catch (IOException | InterruptedException e) {
            log.error("Error creating thumbnail for image " + file, e);
            throw new IOException(e);
        }
    }

    /**
     * Creates a thumbnail for the image file using plain old java
     * @param file the image file
     * @param thumbFile the resulting thumbnail file
     * @param size the size of the thumbnail
     */
    private void createThumbnailUsingJava(Path file, Path thumbFile, IconSize size) throws IOException {

        try {
            BufferedImage image = ImageIO.read(file.toFile());

            int w = image.getWidth();
            int h = image.getHeight();

            // Never scale up
            if (w <= size.getSize() && h <= size.getSize()) {
                FileUtils.copyFile(file.toFile(), thumbFile.toFile());

            } else {
                // Compute the scale factor
                double dx = (double)size.getSize() / (double)w;
                double dy = (double)size.getSize() / (double)h;
                double d = Math.min(dx, dy);

                // Create the thumbnail
                BufferedImage thumbImage = new BufferedImage(
                        (int)Math.round(w * d),
                        (int)Math.round(h * d),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = thumbImage.createGraphics();
                AffineTransform at = AffineTransform.getScaleInstance(d, d);
                g2d.drawRenderedImage(image, at);
                g2d.dispose();

                // Save the thumbnail
                ImageIO.write(
                        thumbImage,
                        FilenameUtils.getExtension(thumbFile.getFileName().toString()),
                        thumbFile.toFile());

                // Releas resources
                image.flush();
                thumbImage.flush();
            }

        } catch (Exception e) {
            log.error("Error creating thumbnail for image " + file, e);
            throw new IOException(e);
        }
    }
}
