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
package dk.dma.msinm.user.security;

import org.apache.commons.lang.StringUtils;
import org.infinispan.util.concurrent.ConcurrentHashSet;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Configuration for the {@code SecurityServletFilter}.
 * <p></p>
 * Defines the URL's for which the security servlet kicks in.
 *
 * <pre>
 * # Auth types can be any combination of basic (Basic Authentication) and jwt (JWT token authentication)
 * authTypes=basic, jwt
 *
 * # JWT specific configuration
 * jwtAuthEndpoint=/auth
 *
 * # Define resource endpoints, optionally with required roles.
 * # Mostly, roles should not be checked by the security servlet, but via
 * # checks/annotations on the individual resource.
 * checkedResource=/auth
 * checkedResource=/rest/*
 * checkedResource=/rest/admin/* roles=admin
 *
 * </pre>
 */
public class SecurityConf implements Serializable {

    public static final String DEFAULT_JWT_AUTH_ENDPOINT = "/auth";
    public static final String KEY_AUTH_TYPES           = "authTypes";
    public static final String KEY_JWT_AUTH_ENDPOINT    = "jwtAuthEndpoint";
    public static final String KEY_CHECKED_RESOURCE     = "checkedResource";

    public static final Pattern CHECKED_RESOURCE_PATTERN_1 = Pattern.compile("(\\S+)");
    public static final Pattern CHECKED_RESOURCE_PATTERN_2 = Pattern.compile("(\\S+)\\s+roles=(\\S+)");
    public static final Pattern CHECKED_RESOURCE_PATTERN_3 = Pattern.compile("(\\S+)\\s+roles=(\\S+)\\s+redirect=(\\S+)");

    enum AuthType {BASIC, JWT }

    private Set<AuthType> authTypes = new ConcurrentHashSet<>();
    private List<CheckedResource> checkedResources = new CopyOnWriteArrayList<>();
    private String jwtAuthEndpoint;

    /**
     * Constructor.
     * Uses default configuration
     */
    public SecurityConf() {
        authTypes.add(AuthType.BASIC);
        authTypes.add(AuthType.JWT);
        jwtAuthEndpoint = DEFAULT_JWT_AUTH_ENDPOINT;
        checkedResources.add(new CheckedResource("/rest/*"));
        checkedResources.add(new CheckedResource("/rest/admin/*", "admin"));
        checkedResources.add(new CheckedResource("/auth"));
    }

    /**
     * Constructor.
     * Uses the configuration of the given file
     * @param file the configuration file
     */
    public SecurityConf(InputStream file) throws Exception {
        loadConfFile(file);
    }

    /**
     * Returns if JWT authentication is supported
     * @return if JWT authentication is supported
     */
    public boolean supportsJwtAuth() {
        return authTypes.contains(AuthType.JWT);
    }

    /**
     * Returns the request specifies the JWT authentication endpoint
     * @return the request specifies the JWT authentication endpoint
     */
    public boolean isJwtAuthEndpoint(HttpServletRequest request) {
        String uri = request.getServletPath() + StringUtils.defaultString(request.getPathInfo());
        return supportsJwtAuth() && jwtAuthEndpoint != null && jwtAuthEndpoint.equals(uri);
    }

    /**
     * Returns if Basic authentication is supported
     * @return if Basic authentication is supported
     */
    public boolean supportsBasicAuth() {
        return authTypes.contains(AuthType.BASIC);
    }

    /**
     * Returns if the resource specified by the request should be checked
     * @param request the request
     * @return if the resource specified by the request should be checked
     */
    public boolean checkResource(HttpServletRequest request) {
        return getMatchingResources(request).size() > 0;
    }

    /**
     * If the request does not have the required role for one of the matching
     * checked resources, then this checked resource is returned.
     * <p></p>
     * If the user does have the required role for all matching checked resources
     * then {@code null} is returned.
     *
     * @param request the request
     * @return the failing checked resource, or {@code null} if the user has the required role
     */
    public CheckedResource lacksRequiredRole(HttpServletRequest request) {
        for (CheckedResource resource : getMatchingResources(request)) {
            for (String role : resource.requiredRoles) {
                if (!request.isUserInRole(role)) {
                    return resource;
                }
            }
        }
        return null;
    }

    /**
     * Returns the list of configured resource endpoints that matches the request
     * @param request the request
     * @return the list of configured resource endpoints that matches the request
     */
    private List<CheckedResource> getMatchingResources(HttpServletRequest request) {
        String uri = request.getServletPath() + StringUtils.defaultString(request.getPathInfo());
        return checkedResources.stream()
                .filter(r -> r.pattern.matcher(uri).matches())
                .collect(Collectors.toList());
    }

    /**
     * Loads the configuration file
     * @param file the configuration file
     */
    private void loadConfFile(InputStream file) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] keyValue = parseKeyValue(line);

                if (keyValue[0].equalsIgnoreCase(KEY_AUTH_TYPES)) {
                    Arrays.asList(keyValue[1].split(","))
                            .forEach(a -> authTypes.add(AuthType.valueOf(a.trim().toUpperCase())));

                } else if (keyValue[0].equalsIgnoreCase(KEY_JWT_AUTH_ENDPOINT)) {
                    jwtAuthEndpoint = keyValue[1].trim();

                } else if (keyValue[0].equalsIgnoreCase(KEY_CHECKED_RESOURCE)) {
                    parseCheckedResource(keyValue[1]);
                }
            }
        }
    }

    /**
     * Parses a checkedResource configuration value.
     * @param value the value to parse
     */
    private void parseCheckedResource(String value) throws Exception {
        // Check the supported formats

        // Format 1: uri
        Matcher m = CHECKED_RESOURCE_PATTERN_1.matcher(value.trim());

        // Format 2: uri roles=r1,r2,r3
        if (!m.matches()) {
            m = CHECKED_RESOURCE_PATTERN_2.matcher(value.trim());
        }

        // Format 3: uri roles=r1,r2,r3 redirect=uri
        if (!m.matches()) {
            m = CHECKED_RESOURCE_PATTERN_3.matcher(value.trim());
        }

        if (!m.matches()) {
            throw new Exception("Invalid format at checkedResource: " + value);
        }

        String resource = m.group(1);
        String roles = m.groupCount() > 1 ? m.group(2) : null;
        String redirect = m.groupCount() > 2 ? m.group(3) : null;

        checkedResources.add(new CheckedResource(resource, roles, redirect));
    }

    /**
     * Parses the line as a key-value pair
     * @param line the line to parse
     * @return the value
     */
    private String[] parseKeyValue(String line) {
        if (line == null || !line.contains("=")) {
            return new String[]{"", ""};
        }
        int index = line.indexOf("=");
        return new String[] {
          line.substring(0, index).trim(),
          line.substring(index + 1).trim(),
        };
    }

    /**
     * Returns a string representation of the security configuration
     * @return a string representation of the security configuration
     */
    @Override
    public String toString() {
        return "SecurityConf{" +
                "authTypes=" + authTypes +
                ", checkedResources=" + checkedResources +
                ", jwtAuthEndpoint='" + jwtAuthEndpoint + '\'' +
                '}';
    }

    /**
     * Used by the SecurityConf class to keep track of checked resources and required roles.
     */
    static class CheckedResource {
        private String uri;
        private Set<String> requiredRoles = new HashSet<>();
        private String redirect;
        private Pattern pattern;

        /**
         * Constructor
         * @param uri the URI
         * @param roles the comma-separated list of roles required for this resource
         * @param redirect optionally a redirect action
         */
        public CheckedResource(String uri, String roles, String redirect) {
            Objects.requireNonNull(uri);
            this.uri = uri;
            if (roles != null) {
                Arrays.asList(roles).forEach(requiredRoles::add);
            }
            this.redirect = redirect;
            pattern = Pattern.compile("^" + uri.replace("*", "\\S*") + "$");
        }

        /**
         * Constructor
         * @param uri the URI
         */
        public CheckedResource(String uri) {
            this(uri, null, null);
        }

        /**
         * Constructor
         * @param uri the URI
         * @param roles the comma-separated list of roles required for this resource
         */
        public CheckedResource(String uri, String roles) {
            this(uri, roles, null);
        }

        public String getUri() {
            return uri;
        }

        public Set<String> getRequiredRoles() {
            return requiredRoles;
        }

        public String getRedirect() {
            return redirect;
        }

        public Pattern getPattern() {
            return pattern;
        }

        @Override
        public String toString() {
            return "CheckedResource{" +
                    "uri='" + uri + '\'' +
                    ", requiredRoles=" + requiredRoles +
                    ", redirect='" + redirect + '\'' +
                    '}';
        }
    }
}
