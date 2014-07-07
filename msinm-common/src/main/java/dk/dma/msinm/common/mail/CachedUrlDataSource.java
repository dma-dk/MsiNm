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

import javax.activation.DataSource;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * If you compose a mime message with attachments by
 * setting "MimeBodyPart.setDataHandler(new DataHandler(url))",
 * then the url will actually be fetched 2-3 times during
 * the process of sending the mail by javamail!!
 * <p></p>
 * To alleviate this sickening behavior, this DataSource can 
 * be used to wrap the original data source.
 * The content is only loaded once and then cached. 
 */
public class CachedUrlDataSource implements DataSource {
	
	final static String DEFAULT_CONTENT_TYPE 	= "application/octet-stream";
	final static String DEFAULT_NAME 			= "unknown";
	
	private Cache<URL, CachedUrlData> cache;
    private URL url;
    private CachedUrlData data;
    
    /**
     * Constructor
     * 
     * @param url the url of the attachment
     * @param cache the cache
     */
    public CachedUrlDataSource(URL url, Cache<URL, CachedUrlData> cache) {
    	this.url = url;
        this.cache = cache;
    }

    /**
     * Checks if the data is cached. Otherwise the URL data is loaded and cached
     * @return the URL data
     */
    protected synchronized CachedUrlData loadData() {
    	// Check if the attachment has been loaded already
    	if (data != null) {
    		return data;
    	}
    	
    	// Check if the attachment is stored in the global attachment cache
    	if (cache != null && cache.containsKey(url)) {
    		data = cache.get(url);
    		return data;
    	}

    	data = new CachedUrlData();
    	data.setUrl(url);
    	try {
    		// Resolve the name from the URL path
			String name = url.getPath();
			if (name.contains("/")) {
				name = name.substring(name.lastIndexOf("/") + 1);
			}
			data.setName(name);
    		    		
    		URLConnection urlc = url.openConnection();

    		// give it 15 seconds to respond
   	      	urlc.setReadTimeout(15*1000);

    		// Fetch the content type from the header
    		data.setContentType(urlc.getHeaderField("Content-Type"));
    		
    		// Load the content
    		InputStream input = urlc.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ( (n = input.read(buffer)) != -1) {
            	if (n > 0) {
            		output.write(buffer, 0, n);
            	}
            }
            data.setContent(output.toByteArray());
    	} catch (Exception ex) {
    		// We don't really want to fail the mail if an attachment fails...
    	}
    	
    	// Cache the result
    	if (cache != null) {
    		cache.put(url, data);
    	}
    	
    	return data;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(loadData().getContent());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getContentType() {
    	String contentType = loadData().getContentType();
        return (contentType != null && contentType.length() > 0) ? contentType : DEFAULT_CONTENT_TYPE;
    }

    @Override
    public String getName() {
    	String name = loadData().getName();
        return (name != null && name.length() > 0) ? name : DEFAULT_NAME;
    }
}