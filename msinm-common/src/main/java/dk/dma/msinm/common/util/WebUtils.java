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
package dk.dma.msinm.common.util;

import org.apache.commons.lang.ArrayUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Web-related utility functions
 */
public class WebUtils {

    private WebUtils() {
    }

    /**
     * Returns the base URL of the request
     * @param request the request
     * @return the base URL
     */
    public static String getWebAppUrl(HttpServletRequest request, String... appends) {
        String result = String.format(
                "%s://%s%s%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort(),
                request.getContextPath());
        for (String a : appends) {
            result = result + a;
        }
        return result;
    }

    /**
     * Returns the base URL of the request
     * @param request the request
     * @return the base URL
     */
    public static String getServletUrl(HttpServletRequest request, String... appends) {
        String[] args = (String[])ArrayUtils.addAll(new String[] { request.getServletPath() }, appends);
        return getWebAppUrl(request, args);
    }

    /**
     * Returns the cookie with the given name or null if not found
     * @param request the request
     * @param name the name
     * @return the cookie with the given name or null if not found
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals(name)) {
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Returns the value of the cookie with the given name or null if not found
     * @param request the request
     * @param name the name
     * @return the value of the cookie with the given name or null if not found
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie c = getCookie(request, name);
        return (c == null) ? null : c.getValue();
    }
}
