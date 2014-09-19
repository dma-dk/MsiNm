package dk.dma.msinm.service;

import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.MailList;
import dk.dma.msinm.model.MailListTemplate;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import dk.dma.msinm.vo.MailListVo;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business interface for processing mailing lists
 */
public class MailListService extends BaseService {

    @Inject
    private Logger log;

    @Inject
    UserService userService;

    /**
     * Saves the given mail list template
     * @param template the mail list template to save
     * @return the saved template
     */
    public MailListTemplate saveMailListTemplate(MailListTemplate template) {
        template = saveEntity(template);
        log.info("Create mail list template " + template.getName());
        return template;
    }

    /**
     * Creates a new mail list, and add the user as a recipient
     * @param mailList the mail list
     * @return the persisted mail list
     */
    public MailList createMailList(MailList mailList) {

        // For public mail lists, check that the combination of template and name is unique
        if (mailList.isPublicMailingList() && findPublicMailListByTemplateAndName(mailList.getTemplate(), mailList.getName()) != null) {
            throw new IllegalArgumentException("Public mail lists must have a unique combination of template and name");
        }

        // Add the current user
        if (mailList.getUser() != null) {
            mailList.getRecipients().add(mailList.getUser());
        }

        // Persist the mail list
        mailList = saveEntity(mailList);
        log.info("Created new mail list " + mailList.getName());

        return mailList;
    }

    /**
     * Returns the mail list template with the given name, or null if not found
     * @param name the name of the template
     * @return the mail list template with the given name
     */
    public MailListTemplate findTemplateByName(String name) {
        try {
            return em.createNamedQuery("MailListTemplate.findByName", MailListTemplate.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the available mail lists for the given user
     * @param user the user
     * @return the available mail lists for the given user
     */
    public List<MailList> findAvailableMailListsForUser(User user) {
        return em.createNamedQuery("MailList.findAvailableMailListsForUser", MailList.class)
                .setParameter("user", user)
                .getResultList();
    }

    /**
     * Returns the mail lists which the given user subscribes to
     * @param user the user
     * @return the mail lists which the given user subscribes to
     */
    public List<MailList> findMailListsForUser(User user) {
        return em.createNamedQuery("MailList.findMailListsForUser", MailList.class)
                .setParameter("user", user)
                .getResultList();
    }

    /**
     * Returns the public mail lists
     * @return the public mail lists
     */
    public List<MailList> findPublicMailLists() {
        return em.createNamedQuery("MailList.findPublicMailLists", MailList.class)
                .getResultList();
    }

    /**
     * Finds the public mail list with the given template and name
     * @param template the template of the system mail list
     * @param name the name of the system mail list
     * @return the public mail list with the given name
     */
    public MailList findPublicMailListByTemplateAndName(MailListTemplate template, String name) {
        try {
            return em.createNamedQuery("MailList.findPublicMailListByTemplateAndName", MailList.class)
                    .setParameter("template", template)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Updates the mailing list subscription for the given user
     * @param user the user
     * @param mailListIds the mailing lists to subscribe to
     */
    public void updateSubscription(User user, int... mailListIds) {

        // Current subscriptions
        Set<MailList> currentMailLists = new HashSet<>(findMailListsForUser(user));
        Set<MailList> allowedMailLists = new HashSet<>(findAvailableMailListsForUser(user));

        // Add the new subscriptions
        for (int id : mailListIds) {
            MailList mailList = getByPrimaryKey(MailList.class, id);
            if (mailList != null && !currentMailLists.remove(mailList) && allowedMailLists.contains(mailList)) {
                mailList.getRecipients().add(user);
                saveEntity(mailList);
            }
        }

        // The remaining mailing lists in the mailLists list should be un-subscribed
        currentMailLists.forEach(mailList -> {
            mailList.getRecipients().remove(user);
            saveEntity(mailList);
        });
    }

    /**
     * Delete the given mailing list
     * @param mailListId the id of the mailing list
     */
    public void deleteMailingList(Integer mailListId) throws Exception {
        MailList mailList = getByPrimaryKey(MailList.class, mailListId);
        if (mailList == null) {
            throw new IllegalArgumentException("Invalid mailing list id " + mailListId);
        }

        // The current user must be the owner of the mailing list or a sysadmin
        User caller = userService.getCurrentUser();
        if (caller == null) {
            throw new Exception("Unauthorized access");
        }
        boolean owner = mailList.getUser() != null && mailList.getUser().getId().equals(caller.getId());
        if (!owner && !caller.hasRole("sysadmin")) {
            throw new Exception("Unauthorized access");
        }

        log.info(String.format("User %s deleting mailing list '%s' with id %d", caller.getEmail(), mailList.getName(), mailList.getId()));
        em.remove(mailList);
    }

    /**
     * Updates the given mailing list
     * @param mailList the mail list template
     * @return the updated mailing list
     */
    public MailList updateMailingList(MailListVo mailList) throws Exception {
        MailList original = getByPrimaryKey(MailList.class, mailList.getId());
        if (original == null) {
            throw new IllegalArgumentException("Invalid mailing list " + mailList.getId());
        }

        // The current user must be the owner of the mailing list or a sysadmin
        User caller = userService.getCurrentUser();
        if (caller == null) {
            throw new Exception("Unauthorized access");
        }
        boolean owner = original.getUser() != null && original.getUser().getId().equals(caller.getId());
        if (!owner && !caller.hasRole("sysadmin")) {
            throw new Exception("Unauthorized access");
        }

        // Update the original
        original.setName(mailList.getName());
        original.setSchedule(mailList.getSchedule());
        original.setScheduleTime(mailList.getScheduleTime());
        original.setSendIfEmpty(mailList.isSendIfEmpty());
        original.setChangedMessages(mailList.isChangedMessages());
        original.setPublicMailingList(mailList.isPublicMailingList());
        original.setFilter(mailList.getFilter());
        original.setFilterDescription(mailList.getFilterDescription());
        original.setTemplate(findTemplateByName(mailList.getTemplate()));
        original.getRecipients().clear();
        mailList.getRecipients().forEach(email -> {
            original.getRecipients().add(userService.findByEmail(email));
        });

        return saveEntity(original);
    }
}
