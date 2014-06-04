package dk.dma.msinm.common.mail;

import org.infinispan.Cache;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An inline (related) mail part for a resource associated with a HTML message body
 * 
 * @author peder
 */
public class InlineMailPart implements Serializable {

	private static final long serialVersionUID = 1L;
	String url;
	String contentId;
	
	/**
	 * Constructor
	 * @param contentId the id of the related inline body part
	 * @param url the url of the part
	 */
	public InlineMailPart(String contentId, String url) {
		this.contentId = contentId;
		this.url = url;
	}
	
	/**
	 * Returns a data-handler for this part
	 * @return a data-handler for this part
	 */
	public DataHandler getDataHandler(Cache<URL, CachedUrlData> cache) throws MessagingException {
		try {
			return new DataHandler(new CachedUrlDataSource(new URL(url), cache));
		} catch (MalformedURLException ex) {
			throw new MessagingException("Invalid url " + url);
		}
	}

	public String getUrl() { return url; }

	public String getContentId() { return contentId; }

	/**
	 * Returns a string representation of this part
	 * @return a string representation of this part
	 */
	public String toString() {
		return "[contentId=" + contentId + ", url=" + url + "]";
	}
}
