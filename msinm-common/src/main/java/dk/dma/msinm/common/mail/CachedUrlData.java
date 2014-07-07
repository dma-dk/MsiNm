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
