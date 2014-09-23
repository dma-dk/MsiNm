package dk.dma.msinm.vo;

import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.model.MailListTemplate;

/**
 * Value object for the {@code MailListTemplate} model entity
 */
public class MailListTemplateVo extends BaseVo<MailListTemplate> {

    Integer id;
    String name;
    String type;
    boolean collated;

    /**
     * No-argument constructor
     */
    public MailListTemplateVo() {
    }

    /**
     * Constructor
     * @param mailListTemplate the mail list template
     */
    public MailListTemplateVo(MailListTemplate mailListTemplate) {
        super(mailListTemplate);
        id = mailListTemplate.getId();
        name = mailListTemplate.getName();
        type = mailListTemplate.getType();
        collated = mailListTemplate.isCollated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MailListTemplate toEntity() {
        MailListTemplate mailListTemplate = new MailListTemplate();
        mailListTemplate.setId(id);
        mailListTemplate.setName(name);
        mailListTemplate.setType(type);
        mailListTemplate.setCollated(collated);
        return mailListTemplate;
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

    public boolean isCollated() {
        return collated;
    }

    public void setCollated(boolean collated) {
        this.collated = collated;
    }
}
