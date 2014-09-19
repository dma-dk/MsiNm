package dk.dma.msinm.web.rest;

import dk.dma.msinm.service.MailListService;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import dk.dma.msinm.vo.MailListVo;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

    @Resource
    SessionContext ctx;

    @Inject
    MailListService mailListService;

    @Inject
    UserService userService;

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
        User caller = getCaller();
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
        User caller = getCaller();
        log.info("Updating mailing list subscription for " + caller.getEmail());
        mailListService.updateSubscription(caller, mailListIds);
        return "OK";
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
     * Returns the calling user
     * @return the calling user
     */
    private User getCaller() {
        if (ctx != null && ctx.getCallerPrincipal() != null) {
            return userService.findByPrincipal(ctx.getCallerPrincipal());
        }
        return null;
    }


}
