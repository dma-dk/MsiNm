package dk.dma.msinm.common.web;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * This {@code ExceptionMapper} will send a status 401 response when a JAX-RS call has failed
 * because of EJB-access restrictions.
 */
@Provider
public class RestExceptionHandler implements ExceptionMapper<javax.ejb.EJBAccessException> {

    @Override
    public Response toResponse(javax.ejb.EJBAccessException exception)
    {
        return Response.status(Response.Status.UNAUTHORIZED).entity(exception.getMessage()).build();
    }
}

