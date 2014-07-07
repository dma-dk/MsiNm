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
