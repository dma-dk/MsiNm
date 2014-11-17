package dk.dma.msinm.templates.vo;

import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.templates.model.FmInclude;
import org.apache.commons.lang.StringUtils;

/**
 * Value object for the {@code FmInclude} model entity
 */
public class FmIncludeVo extends BaseVo<FmInclude> {

    Integer id;
    String name;
    String fmTemplate;

    /**
     * Constructor
     */
    public FmIncludeVo() {
    }

    /**
     * Constructor
     *
     * @param fmInclude the entity
     */
    public FmIncludeVo(FmInclude fmInclude) {
        super(fmInclude);

        id = fmInclude.getId();
        name = fmInclude.getName();
        fmTemplate = fmInclude.getFmTemplate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FmInclude toEntity() {
        FmInclude fmInclude = new FmInclude();
        fmInclude.setId(id);
        fmInclude.setName(name);
        fmInclude.setFmTemplate(fmTemplate);
        return fmInclude;
    }

    /**
     * Returns if this Freemarker include defines a proper Freemarker template
     * @return if this Freemarker include defines a proper Freemarker template
     */
    public boolean isDefined() {
        return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(fmTemplate);
    }

    // ***********************************
    // Getters and setters
    // ***********************************

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

    public String getFmTemplate() {
        return fmTemplate;
    }

    public void setFmTemplate(String fmTemplate) {
        this.fmTemplate = fmTemplate;
    }
}
