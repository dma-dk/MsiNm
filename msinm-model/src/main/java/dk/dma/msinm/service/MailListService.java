package dk.dma.msinm.service;

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.common.util.JsonUtils;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.model.MailList;
import dk.dma.msinm.model.MailListTemplate;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import dk.dma.msinm.vo.MailListVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Business interface for processing mailing lists
 */
public class MailListService extends BaseService {

    @Inject
    private Logger log;

    @Inject
    UserService userService;

    @Inject
    AreaService areaService;

    @Inject
    CategoryService categoryService;

    @Inject
    ChartService chartService;


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

        // Update the next execution time
        mailList.computeNextExecution();

        // Persist the mail list
        mailList = saveEntity(mailList);
        log.info("Created new mail list " + mailList.getName());

        return mailList;
    }


    /**
     * Updates the last execution time and computes the next execution time
     * @param mailList the mail list
     * @param executionTime the last execution time
     * @return the updated mail list
     */
    public MailList updateExecutionTime(MailList mailList, Date executionTime) {
        mailList.setLastExecution(executionTime);
        mailList.computeNextExecution();
        return saveEntity(mailList);
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
     * Returns the mail list templates with the given type
     * @param type the type of the template
     * @return the mail list template with the given type
     */
    public List<MailListTemplate> findTemplatesByType(String type) {
        return em.createNamedQuery("MailListTemplate.findByType", MailListTemplate.class)
                .setParameter("type", type)
                .getResultList();
    }

    /**
     * Returns all mail list templates
     * @return all mail list template
     */
    public List<MailListTemplate> findAllTemplates() {
        return getAll(MailListTemplate.class);
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
     * Returns all mailing lists of the given type that are due for execution
     * @param type the type
     * @return all mailing lists of the given type that are due for execution
     */
    public List<MailList> findPendingMailListsOfType(String type) {
        return em.createNamedQuery("MailList.findPendingMailListsOfType", MailList.class)
                .setParameter("type", type)
                .getResultList();
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
     * Creates a new mailing list from a MailListVo
     * @param mailListVo the mail list VO
     * @return the created mailing list
     */
    public MailList createMailingList(MailListVo mailListVo) throws Exception {

        if (mailListVo.getTemplate() == null || StringUtils.isBlank(mailListVo.getName())) {
            throw new IllegalArgumentException("Invalid mailing list " + mailListVo);
        }

        User caller = userService.getCurrentUser();
        if (caller == null) {
            throw new Exception("Unauthorized access");
        }

        MailList mailList = mailListVo.toEntity();
        mailList.setTemplate(getByPrimaryKey(MailListTemplate.class, mailListVo.getTemplate().getId()));
        mailList.setUser(caller);

        return createMailList(mailList);
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
        original.setTemplate(findTemplateByName(mailList.getTemplate().getName()));
        original.getRecipients().clear();
        mailList.getRecipients().forEach(email -> original.getRecipients().add(userService.findByEmail(email)));

        // Update the next execution time
        original.computeNextExecution();

        return saveEntity(original);
    }


    /**
     * Returns a verbal description of the chart filter
     * @param params the search filter params
     * @param lang the language
     * @return the verbal description of the filter
     */
    public String toFilterDescription(MessageSearchParams params, String lang) {
        DataFilter langFilter = DataFilter.lang(lang);

        List<String> desc = new ArrayList<>();
        if (isNotBlank(params.getQuery())) {
            desc.add(String.format("Query: '%s'", params.getQuery()));
        }
        if (params.getLocations().size() > 0) {
            desc.add(String.format("Locations: %d locations", params.getLocations().size()));
        }
        if (params.getStatus() != null) {
            desc.add(String.format("Status: %s", params.getStatus()));
        }
        if (params.getTypes().size() > 0) {
            desc.add(String.format("Types: %s", params.getTypes().stream().map(Enum::name).collect(Collectors.joining(", "))));
        }
        if (params.getMainTypes().size() > 0) {
            desc.add(String.format("Main types: %s", params.getMainTypes().stream().map(Enum::name).collect(Collectors.joining(", "))));
        }
        if (params.getAreaIds().size() > 0) {
            String areas = params.getAreaIds().stream()
                    .map(id -> areaService.getByPrimaryKey(Area.class, id).getDescs(langFilter).get(0).getName())
                    .collect(Collectors.joining(", "));
            desc.add(String.format("Areas: %s", areas));
        }
        if (params.getCategoryIds().size() > 0) {
            String categories = params.getCategoryIds().stream()
                    .map(id -> categoryService.getByPrimaryKey(Category.class, id).getDescs(langFilter).get(0).getName())
                    .collect(Collectors.joining(", "));
            desc.add(String.format("Categories: %s", categories));
        }
        if (params.getChartIds().size() > 0) {
            String charts = params.getChartIds().stream()
                    .map(id -> chartService.getByPrimaryKey(Chart.class, id).toFullChartNumber())
                    .collect(Collectors.joining(", "));
            desc.add(String.format("Charts: %s", charts));
        }
        if (params.getSortBy() != null) {
            MessageSearchParams.SortOrder order = (params.getSortOrder() == null) ? MessageSearchParams.SortOrder.DESC : params.getSortOrder();
            desc.add(String.format("Sort by: %s (%s)", params.getSortBy(), order));
        }

        return desc.stream().collect(Collectors.joining("\n"));
    }

    /**
     * Updates the mail list filter
     * @param mailList the mail list
     * @param filter the filter
     * @param lang the language
     */
    public void updateFilter(MailList mailList, MessageSearchParams filter, String lang) throws IOException {
        mailList.setFilter(JsonUtils.toJson(filter));
        mailList.setFilterDescription(toFilterDescription(filter, lang));
    }
}

