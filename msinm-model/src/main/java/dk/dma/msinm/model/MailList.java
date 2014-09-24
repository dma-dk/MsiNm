package dk.dma.msinm.model;

import dk.dma.msinm.common.model.VersionedEntity;
import dk.dma.msinm.common.util.TimeUtils;
import dk.dma.msinm.user.User;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Defines a mail list
 */
@Entity
@NamedQueries({
    @NamedQuery(name  = "MailList.findAvailableMailListsForUser",
                query = "select m from MailList m where m.user = :user or m.publicMailingList = true"),
    @NamedQuery(name  = "MailList.findMailListsForUser",
                query = "select distinct m from MailList m join m.recipients r where r = :user"),
    @NamedQuery(name  = "MailList.findPublicMailLists",
                query = "select m from MailList m where m.publicMailingList = true"),
    @NamedQuery(name  = "MailList.findPublicMailListByTemplateAndName",
                query = "select m from MailList m where m.publicMailingList = true and m.template = :template and m.name = :name"),
    @NamedQuery(name  = "MailList.findMailListByTemplateAndName",
                query = "select m from MailList m where m.template = :template and m.name = :name"),
    @NamedQuery(name  = "MailList.findPendingMailListsOfType",
                query = "select m from MailList m join m.template t where t.type = :type and m.nextExecution is not null and CURRENT_TIMESTAMP > m.nextExecution"),
})
public class MailList extends VersionedEntity<Integer> {

    public static final int DEFAULT_TIME_OF_DAY = 16;

    public enum Schedule {
        CONTINUOUS, // Runs every 5 minutes
        DAILY,      // Runs daily at hour denoted by scheduleTime
        WEEKLY,     // Runs weekly at day-index denoted by scheduleTime
        NONE        // Inactive
    }

    @ManyToOne
    @NotNull
    MailListTemplate template;

    @ManyToMany
    List<User> recipients = new ArrayList<>();

    /**
     * The user that created the mail list
     */
    @ManyToOne
    User user;

    @NotNull
    String name;

    /**
     * Contains the JSON-encoded MessageSearchParams
     * used as a message search filter.
     */
    String filter;

    /**
     * Contains a verbal description of the message search filter
     */
    String filterDescription;

    /**
     * Whether to fetch all messages matches the filter criteria
     * or just the changed messages matching the criteria
     */
    boolean changedMessages = true;

    /**
     * Used for WEEKLY and DAILY schedules.
     * Defines whether to send mails for empty message lists or not
     */
    boolean sendIfEmpty;

    /**
     * Whether this is a public mailing list or not.
     * For public mailing lists the combination of template and name must be unique
     */
    boolean publicMailingList;

    @NotNull
    @Enumerated(EnumType.STRING)
    Schedule schedule;

    /**
     * Used for WEEKLY and DAILY schedules.
     * Designates the day-of-week, respectively the hour-of-day
     */
    Integer scheduleTime;

    /**
     * Used for WEEKLY and DAILY schedules.
     * The computed next execution time
     */
    @Temporal(TemporalType.TIMESTAMP)
    Date nextExecution;

    /**
     * The last execution time
     */
    @Temporal(TemporalType.TIMESTAMP)
    Date lastExecution;


    /**
     * Computes the next execution time
     * based on the schedule and last execution
     */
    public void computeNextExecution() {
        Date now = new Date();
        long random = (long)(Math.random() * 120.0 - 60.0) * 1000; // +- 1 minute

        // Branch on schedule time
        if (getSchedule() == Schedule.NONE) {
            nextExecution = null;

        } else if (getSchedule() == Schedule.CONTINUOUS) {
            // Just add 5 minutes to now
            Calendar date = Calendar.getInstance();
            date.add(Calendar.MINUTE, 5);
            nextExecution = new Date(date.getTime().getTime() + random);

        } else if (getSchedule() == Schedule.DAILY) {
            // Set it to be at scheduleTime o'clock today ... or tomorrow
            Calendar date = TimeUtils.resetTime(Calendar.getInstance());
            date.set(Calendar.HOUR_OF_DAY, (scheduleTime == null) ? DEFAULT_TIME_OF_DAY : scheduleTime);
            if (date.getTime().before(now)) {
                date.add(Calendar.DATE, 1);
            }
            nextExecution = new Date(date.getTime().getTime() + random);

        } else if (getSchedule() == Schedule.WEEKLY) {
            // Set it to be 16 o'clock on the scheduleTime day of this week ... or next week
            Calendar date = TimeUtils.resetTime(Calendar.getInstance());
            date.set(Calendar.HOUR_OF_DAY, DEFAULT_TIME_OF_DAY);
            date.setFirstDayOfWeek(Calendar.MONDAY);
            date.set(Calendar.DAY_OF_WEEK, (scheduleTime == null) ? Calendar.FRIDAY : scheduleTime);
            if (date.getTime().before(now)) {
                date.add(Calendar.WEEK_OF_YEAR, 1);
            }
            nextExecution = new Date(date.getTime().getTime() + random);
        }

    }

    // *************************************
    // ******** Getters and setters ********
    // *************************************

    public MailListTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MailListTemplate template) {
        this.template = template;
    }

    public List<User> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<User> recipients) {
        this.recipients = recipients;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Integer getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Integer scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Date getNextExecution() {
        return nextExecution;
    }

    public void setNextExecution(Date nextExecution) {
        this.nextExecution = nextExecution;
    }

    public Date getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution;
    }
}
