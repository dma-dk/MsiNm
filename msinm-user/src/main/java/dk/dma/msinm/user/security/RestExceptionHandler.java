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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * This {@code ExceptionMapper} will send a status 401 response when a JAX-RS call has failed
 * because of EJB-access restrictions.
 */
@Provider
public class RestExceptionHandler implements ExceptionMapper<javax.ejb.EJBAccessException> {

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(javax.ejb.EJBAccessException exception) {
        return Response
                .status(SecurityServletFilter.getErrorStatusCode(request, HttpServletResponse.SC_UNAUTHORIZED))
                .entity(exception.getMessage())
                .build();
    }
}

