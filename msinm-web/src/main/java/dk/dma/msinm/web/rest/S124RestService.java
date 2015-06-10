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
package dk.dma.msinm.web.rest;

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateService;
import dk.dma.msinm.common.templates.TemplateType;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.service.MessageService;
import dk.dma.msinm.vo.MessageVo;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Prototype REST interface for generating S-124 compliant GML data.
 */
@Path("/s124")
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class S124RestService {

    @Inject
    Logger log;

    @Inject
    MessageService messageService;

    @Inject
    private TemplateService templateService;

    /**
     * Returns the message with the given ID or series ID
     *
     * @return the message, or null if not found
     */
    @GET
    @Path("/message/{messageId}.gml")
    //@Produces("text/plain;charset=UTF-8")
    @GZIP
    @NoCache
    public Response generateGML(@PathParam("messageId") String messageId, @QueryParam("lang") String lang) throws Exception {

        long t0 = System.currentTimeMillis();

        try {
            // Get the message id
            Integer id = getMessageId(messageId);
            if (id == null) {
                throw new IllegalArgumentException("Invalid message id " + messageId);
            }

            // Look up the cached message with the computed id
            Message message = messageService.getCachedMessage(id);
            DataFilter filter = new DataFilter(MessageService.CACHED_MESSAGE_DATA).setLang(lang);
            MessageVo msgVo = new MessageVo(message, filter);

            String result = generateGML(msgVo, lang);
            log.info("Generated GML for message " + messageId + " in " + (System.currentTimeMillis() - t0) + " ms");
            return Response.ok(result)
                    .type("application/gml+xml;charset=UTF-8")
                    .build();
        } catch (Exception e) {
            log.error("Error generating S-124 GML for message " + messageId, e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_HTML_TYPE)
                    .entity("Error generating GML: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Translates the messageId, which may be an ID or a series identifier, into a message id
     *
     * @param messageId the mesage id
     * @return the message id
     */
    private Integer getMessageId(String messageId) {
        // Sanity check
        if (messageId == null) {
            throw new IllegalArgumentException("Must specify message id");
        }

        // The message id is either the ID of the message or the message series identifier
        Integer id = null;
        if (StringUtils.isNumeric(messageId)) {
            id = Integer.valueOf(messageId);
        } else {
            Message message = messageService.findBySeriesIdentifier(messageId);
            if (message != null) {
                id = message.getId();
            }
        }
        return id;
    }


    /**
     * Generates S-124 compliant GML for the message
     * @param msg the message
     * @param language the language
     * @return the generated GML
     */
    private String generateGML(MessageVo msg, String language) throws IOException, TemplateException {
        Map<String, Object> data = new HashMap<>();
        data.put("msg", msg);

        TemplateContext ctx = templateService.getTemplateContext(
                TemplateType.GML,
                "s124-template.ftl",
                data,
                language,
                null);

        return templateService.process(ctx);

    }
}
