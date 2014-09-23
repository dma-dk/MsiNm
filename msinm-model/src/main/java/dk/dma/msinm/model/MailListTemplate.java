package dk.dma.msinm.model;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a mailing list template, i.e. the Freemarker template
 * and whether messages are collated or not
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "MailListTemplate.findByName",
                query = "select mt from MailListTemplate mt where mt.name = :name"),
        @NamedQuery(name  = "MailListTemplate.findByType",
                query = "select mt from MailListTemplate mt where mt.type = :type")
})
public class MailListTemplate extends BaseEntity<Integer> {

    @NotNull
    @Column(unique = true)
    String name;

    /**
     * The type is associated with the publisher using the template
     */
    @NotNull
    String type;

    /**
     * The path to the Freemarker template
     */
    @NotNull
    String template;

    /**
     * A resource bundle
     */
    String bundle;

    /**
     * Whether the mail is used for individual messages or message lists
     */
    boolean collated;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "template")
    List<MailList> mailLists = new ArrayList<>();

    // *************************************
    // ******** Getters and setters ********
    // *************************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public boolean isCollated() {
        return collated;
    }

    public void setCollated(boolean collated) {
        this.collated = collated;
    }

    public List<MailList> getMailLists() {
        return mailLists;
    }

    public void setMailLists(List<MailList> mailLists) {
        this.mailLists = mailLists;
    }
}
