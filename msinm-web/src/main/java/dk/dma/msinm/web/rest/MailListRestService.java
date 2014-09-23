package dk.dma.msinm.web.rest;

import dk.dma.msinm.common.util.JsonUtils;
import dk.dma.msinm.model.MailList;
import dk.dma.msinm.model.MailListTemplate;
import dk.dma.msinm.service.MailListService;
import dk.dma.msinm.service.MailPublisher;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import dk.dma.msinm.vo.MailListTemplateVo;
import dk.dma.msinm.vo.MailListVo;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST interface for accessing MSI-NM mailing lists.
 */
@Singleton
@Startup
@Path("/mailing-lists")
@SecurityDomain("msinm-policy")
@PermitAll
public class MailListRestService {

    @Inject
    Logger log;

    @Inject
    MailListService mailListService;

    @Inject
    UserService userService;

    /**
     * Returns the available mail lists templates for the current user
     * @return the available mail lists templates for the current user
     */
    @GET
    @Path("/user-mailing-list-templates")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @RolesAllowed({"user"})
    @Lock(LockType.READ)
    public List<MailListTemplateVo> findAvailableMailListTemplatesForUser() {
        User caller = userService.getCurrentUser();
        List<MailListTemplate> templates = caller.hasRole("admin")
                ? mailListService.findAllTemplates()
                : mailListService.findTemplatesByType(MailPublisher.MAIL_PUBLISHER_TYPE);

        return templates.stream()
                .map(MailListTemplateVo::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns the available public mail lists
     * @return the available public mail lists
     */
    @GET
    @Path("/public-mailing-lists")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @Lock(LockType.READ)
    public List<MailListVo> findPublicMailLists() {
        return mailListService.findPublicMailLists().stream()
                .map(MailListVo::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns the available mail lists for the current user
     * @return the available mail lists for the current user
     */
    @GET
    @Path("/user-mailing-lists")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @RolesAllowed({"user"})
    @Lock(LockType.READ)
    public List<MailListVo> findAvailableMailListsForUser() {
        User caller = userService.getCurrentUser();
        return mailListService.findAvailableMailListsForUser(caller).stream()
                .map(mailList -> new MailListVo(mailList, caller))
                .collect(Collectors.toList());
    }

    /**
     * Updates the subscriptions for the current user
     * @return the status
     */
    @PUT
    @Path("/update-user-subscription")
    @Consumes("application/json")
    @Produces("application/json")
    @GZIP
    @NoCache
    @RolesAllowed({"user"})
    @Lock(LockType.READ)
    public String updateUserSubscription(int[] mailListIds) throws Exception {
        User caller = userService.getCurrentUser();
        log.info("Updating mailing list subscription for " + caller.getEmail());
        mailListService.updateSubscription(caller, mailListIds);
        return "OK";
    }


    /**
     * Creates the mailing list
     * @return the created mailing list
     */
    @POST
    @Path("/mailing-list")
    @Consumes("application/json")
    @Produces("application/json")
    @GZIP
    @NoCache
    @RolesAllowed({"user"})
    @Lock(LockType.READ)
    public MailListVo createMailingList(MailListVo mailList) throws Exception {
        log.info("Creating mailing list " + mailList.getName());
        return new MailListVo(mailListService.createMailingList(mailList));
    }


    /**
     * Updates the mailing list
     * @return the updated mailing list
     */
    @PUT
    @Path("/mailing-list")
    @Consumes("application/json")
    @Produces("application/json")
    @GZIP
    @NoCache
    @RolesAllowed({"user"})
    @Lock(LockType.READ)
    public MailListVo updateMailingList(MailListVo mailList) throws Exception {
        log.info("Updating mailing list " + mailList.getName());
        return new MailListVo(mailListService.updateMailingList(mailList));
    }


    @DELETE
    @Path("/mailing-list/{mailListId}")
    @Produces("application/json")
    @RolesAllowed({"user"})
    public String deleteMailingList(@PathParam("mailListId") Integer mailListId) throws Exception {

        log.info("Deleting mailing list " + mailListId);
        mailListService.deleteMailingList(mailListId);
        return "OK";
    }

    /**
     * Generates a new mailing list template from search parameters
     */
    @GET
    @Path("/new-mailing-list-template")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public MailListVo newMailingListTemplate(
            @QueryParam("q") String query,
            @QueryParam("status") @DefaultValue("PUBLISHED") String status,
            @QueryParam("type") String type,
            @QueryParam("loc") String loc,
            @QueryParam("areas") String areas,
            @QueryParam("categories") String categories,
            @QueryParam("charts") String charts,
            @QueryParam("sortBy") @DefaultValue("DATE") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder
    ) throws Exception {
        User caller = userService.getCurrentUser();

        MailListVo mailList = new MailListVo();
        mailList.setUser(caller.getEmail());
        mailList.setRecipients(new ArrayList<>());
        mailList.getRecipients().add(caller.getEmail());
        mailList.setSchedule(MailList.Schedule.CONTINUOUS);
        mailList.setChangedMessages(true);
        mailList.setSendIfEmpty(false);
        mailList.setPublicMailingList(false);

        // Define the filter and filter description
        MessageSearchParams params = MessageSearchParams.readParams(query, status, type, loc, areas, categories, charts, sortBy, sortOrder);
        mailList.setFilter(JsonUtils.toJson(params));
        mailList.setFilterDescription(mailListService.toFilterDescription(params, caller.getLanguage()));

        return mailList;
    }


    /*********** TEST *********/

    @Inject
    MailPublisher mailPublisher;

    @GET
    @Path("/run-mail-lists")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @Lock(LockType.READ)
    public String runMailLists() {
        mailPublisher.processPendingMailLists();
        return "OK";
    }

}
