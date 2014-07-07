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
package dk.dma.msinm.common.mail;

import org.apache.commons.lang.StringUtils;
import org.infinispan.Cache;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Encapsulates an attachment mail part
 */
public class AttachmentMailPart implements Serializable {

    private static final long serialVersionUID = 1L;

    String name;

    // Url attachment
    URL url;

    // File attachment
    String file;

    // Byte array attachment
    byte[] content;
    String contentType;

    /**
     * No-call constructor
     */
    private AttachmentMailPart(String name) {
        this.name = name;
    }

    /**
     * Instantiates an attachment mail part from a file
     * @param file the attachment file
     */
    public static AttachmentMailPart fromFile(String file) throws MessagingException {
        return fromFile(file, null);
    }

    /**
     * Instantiates an attachment mail part from a file
     * @param file the attachment file
     * @param name the name of the attachment.
     */
    public static AttachmentMailPart fromFile(String file, String name)  throws MessagingException {
        // Check that the file is valid
        Path path = Paths.get(file);
        if (!Files.isRegularFile(path)) {
            throw new MessagingException("Invalid file attachment: " + file);
        }

        // If name is not specified, compute it from the file
        if (StringUtils.isBlank(name)) {
            name = path.getFileName().toString();
        }

        // Instantiate a mail attachment
        AttachmentMailPart a = new AttachmentMailPart(name);
        a.file = file;
        return a;
    }

    /**
     * Instantiates an attachment mail part from a URL
     * @param url the attachment URL
     */
    public static AttachmentMailPart fromUrl(String url) throws MessagingException {
        return fromUrl(url, null);
    }

    /**
     * Instantiates an attachment mail part from a URL
     * @param urlStr the attachment URL
     * @param name the name of the attachment.
     */
    public static AttachmentMailPart fromUrl(String urlStr, String name)  throws MessagingException {
        // Check that the file is valid
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException ex) {
            throw new MessagingException("Invalid url attachment: " + urlStr);
        }

        // If name is not specified, compute it from the file
        if (StringUtils.isBlank(name)) {
            name = url.getPath();
            if (name.contains("/")) {
                name = name.substring(name.lastIndexOf("/") + 1);
            }
            if (StringUtils.isBlank(name)) {
                name = "unknown";
            }
        }

        // Instantiate a mail attachment
        AttachmentMailPart a = new AttachmentMailPart(name);
        a.url = url;
        return a;
    }

    /**
     * Instantiates an attachment mail part from a content byte array
     * @param content the content byte array
     * @param name the name of the attachment.
     */
    public static AttachmentMailPart fromContent(byte[] content, String name) throws MessagingException {
        return fromContent(content, null, name);
    }

    /**
     * Instantiates an attachment mail part from a content byte array
     * @param content the content byte array
     * @param contentType the content type
     * @param name the name of the attachment.
     */
    public static AttachmentMailPart fromContent(byte[] content, String contentType, String name)  throws MessagingException {
        // Check that the content is a valid byte array
        if (content == null || content.length == 0) {
            throw new MessagingException("Invalid empty content byte array");
        }
        if (contentType == null || contentType.length() == 0) {
            contentType = "application/octet-stream";
        }
        if (StringUtils.isBlank(name)) {
            name = "unknown";
        }

        // Instantiate a mail attachment
        AttachmentMailPart a = new AttachmentMailPart(name);
        a.content = content;
        a.contentType = contentType;
        return a;
    }

    /**
     * Returns a data-handler for this part
     * @return a data-handler for this part
     */
    public DataHandler getDataHandler(Cache<URL, CachedUrlData> cache) throws MessagingException {
        if (file != null) {
            return new DataHandler(new FileDataSource(file));
        } else if (url != null) {
            return new DataHandler(new CachedUrlDataSource(url, cache));
        } else {
            return new DataHandler(new ByteArrayDataSource(content, contentType));
        }
    }

    public String getName() { return name; }
}
