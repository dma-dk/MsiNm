package dk.dma.msinm.vo;

import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.model.MailList;
import dk.dma.msinm.user.User;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Value object for the {@code MailList} model entity
 */
public class MailListVo extends BaseVo<MailList> {

    Integer id;
    MailListTemplateVo template;
    List<String> recipients;
    String user;
    String name;
    String filter;
    String filterDescription;
    boolean changedMessages;
    boolean sendIfEmpty;
    boolean publicMailingList;
    MailList.Schedule schedule;
    Integer scheduleTime;
    Date created;
    Date lastExecution;

    boolean selected;

    /**
     * No-argument constructor
     */
    public MailListVo() {
    }

    /**
     * Constructor
     * @param mailList the mail list
     */
    public MailListVo(MailList mailList) {
        super(mailList);
        id = mailList.getId();
        template = new MailListTemplateVo(mailList.getTemplate());
        if (mailList.getRecipients().size() > 0) {
            recipients = mailList.getRecipients().stream()
                    .map(User::getEmail)
                    .collect(Collectors.toList());
        }
        if (mailList.getUser() != null) {
            user = mailList.getUser().getEmail();
        }
        name = mailList.getName();
        filter = mailList.getFilter();
        filterDescription = mailList.getFilterDescription();
        changedMessages = mailList.isChangedMessages();
        sendIfEmpty = mailList.isSendIfEmpty();
        publicMailingList = mailList.isPublicMailingList();
        schedule = mailList.getSchedule();
        scheduleTime = mailList.getScheduleTime();
        created = mailList.getCreated();
        lastExecution = mailList.getLastExecution();
    }

    /**
     * Constructor
     * @param mailList the mail list
     * @param user the current user
     */
    public MailListVo(MailList mailList, User user) {
        this(mailList);
        selected = mailList.getRecipients().stream()
                    .anyMatch(u -> u.getId().equals(user.getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MailList toEntity() {
        // NB related entities such as template and users are not updated
        // Neither are read-only properties such as created and lastExecution
        MailList mailList = new MailList();
        mailList.setId(id);
        mailList.setName(name);
        mailList.setFilter(filter);
        mailList.setFilterDescription(filterDescription);
        mailList.setChangedMessages(changedMessages);
        mailList.setSendIfEmpty(sendIfEmpty);
        mailList.setPublicMailingList(publicMailingList);
        mailList.setSchedule(schedule);
        mailList.setScheduleTime(scheduleTime);
        return mailList;
    }

    // *************************************
    // ******** Getters and setters ********
    // *************************************

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MailListTemplateVo getTemplate() {
        return template;
    }

    public void setTemplate(MailListTemplateVo template) {
        this.template = template;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilterDescription() {
        return filterDescription;
    }

    public void setFilterDescription(String filterDescription) {
        this.filterDescription = filterDescription;
    }

    public boolean isChangedMessages() {
        return changedMessages;
    }

    public void setChangedMessages(boolean changedMessages) {
        this.changedMessages = changedMessages;
    }

    public boolean isSendIfEmpty() {
        return sendIfEmpty;
    }

    public void setSendIfEmpty(boolean sendIfEmpty) {
        this.sendIfEmpty = sendIfEmpty;
    }

    public boolean isPublicMailingList() {
        return publicMailingList;
    }

    public void setPublicMailingList(boolean publicMailingList) {
        this.publicMailingList = publicMailingList;
    }

    public MailList.Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(MailList.Schedule schedule) {
        this.schedule = schedule;
    }

    public Integer getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Integer scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
