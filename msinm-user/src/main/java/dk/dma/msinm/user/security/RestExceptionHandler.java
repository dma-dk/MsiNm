package dk.dma.msinm.user.security;

import javax.servlet.http.HttpServletRequest;
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
        if (request.getAttribute(SecurityServletFilter.AUTH_ERROR_ATTR) != null) {
            int status = (Integer)request.getAttribute(SecurityServletFilter.AUTH_ERROR_ATTR);
            return Response.status(status).entity(exception.getMessage()).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity(exception.getMessage()).build();
    }
}

