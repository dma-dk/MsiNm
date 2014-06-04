package dk.dma.msinm.common.mail;

import java.io.Serializable;
import java.net.URL;

/**
 * Used to cache the data associated with a URL used for mail attachments
 */
public class CachedUrlData implements Serializable {

	private static final long serialVersionUID = 1L;
	URL url;
	String name;
	String contentType;
	byte[] content;
	
	public URL getUrl() { return url; }
	public void setUrl(URL url) { this.url = url; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getContentType() { return contentType; }
	public void setContentType(String contentType) { this.contentType = contentType; }
	
	public byte[] getContent() { return content; }
	public void setContent(byte[] content) { this.content = content; }
}
