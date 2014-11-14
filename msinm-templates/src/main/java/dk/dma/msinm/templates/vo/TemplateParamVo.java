package dk.dma.msinm.templates.vo;

import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.templates.model.TemplateParam;
import org.apache.commons.lang.StringUtils;

/**
 * Value object for the {@code TemplateParam} model entity
 */
public class TemplateParamVo extends BaseVo<TemplateParam> {

    String type;
    String name;
    boolean mandatory;
    boolean list;
    int sortKey;

    /**
     * Constructor
     */
    public TemplateParamVo() {
    }

    /**
     * Constructor
     *
     * @param param the entity
     */
    public TemplateParamVo(TemplateParam param) {
        super(param);

        type = param.getType();
        name = param.getName();
        mandatory = param.isMandatory();
        list = param.isList();
        sortKey = param.getSortKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateParam toEntity() {
        TemplateParam param = new TemplateParam();
        param.setType(type);
        param.setName(name);
        param.setMandatory(mandatory);
        param.setList(list);
        param.setSortKey(sortKey);
        return param;
    }

    /**
     * Checks that the template parameter is well defined, i.e. has a proper name and type
     * @return if the parameter is properly defined
     */
    public boolean isDefined() {
        return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(type);
    }

    // ***********************************
    // Getters and setters
    // ***********************************

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setSortKey(int sortKey) {
        this.sortKey = sortKey;
    }

}
