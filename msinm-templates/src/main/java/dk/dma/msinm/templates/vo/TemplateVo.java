package dk.dma.msinm.templates.vo;

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.templates.model.Template;
import dk.dma.msinm.vo.CategoryVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Value object for the {@code Template} model entity
 */
public class TemplateVo extends BaseVo<Template> {

    Integer id;
    String name;
    List<CategoryVo> categories;
    List<TemplateParamVo> parameters;
    List<FieldTemplateVo> fieldTemplates;

    /**
     * Constructor
     */
    public TemplateVo() {
    }

    /**
     * Constructor
     *
     * @param template the entity
     * @param dataFilter what type of data to include from the entity
     */
    public TemplateVo(Template template, DataFilter dataFilter) {
        super(template);

        DataFilter compFilter = dataFilter.forComponent(Template.class);

        id = template.getId();
        name = template.getName();
        template.getCategories().forEach(cat -> checkCreateCategories().add(new CategoryVo(cat, compFilter)));
        template.getParameters().forEach(param -> checkCreateParameters().add(new TemplateParamVo(param)));
        template.getFieldTemplates().forEach(ft -> checkCreateFieldTemplates().add(new FieldTemplateVo(ft)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Template toEntity() {
        Template template = new Template();
        template.setId(id);
        template.setName(name);
        if (categories != null) {
            categories.stream()
                    .forEach(cat -> template.getCategories().add(cat.toEntity()));
        }
        if (parameters != null) {
            parameters.stream()
                    .filter(TemplateParamVo::isDefined)
                    .forEach(param -> template.getParameters().add(param.toEntity()));
        }
        if (fieldTemplates != null) {
            fieldTemplates.stream()
                    .filter(FieldTemplateVo::isDefined)
                    .forEach(ft -> template.getFieldTemplates().add(ft.toEntity(template)));
        }
        return template;
    }

    /**
     * Returns or creates the list of categories
     * @return the list of categories
     */
    public List<CategoryVo> checkCreateCategories() {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        return categories;
    }

    /**
     * Returns or creates the list of parameters
     * @return the list of parameters
     */
    public List<TemplateParamVo> checkCreateParameters() {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        return parameters;
    }

    /**
     * Returns or creates the list of field templates
     * @return the list of field templates
     */
    public List<FieldTemplateVo> checkCreateFieldTemplates() {
        if (fieldTemplates == null) {
            fieldTemplates = new ArrayList<>();
        }
        return fieldTemplates;
    }
    /**
     * Sorts all category descriptors by the given language
     * @param lang the language
     */
    public void sortCategoriesByLanguage(String lang) {
        if (categories != null) {
            categories.forEach(cat -> cat.sortDescs(lang));
        }
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

    public List<CategoryVo> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryVo> categories) {
        this.categories = categories;
    }

    public List<TemplateParamVo> getParameters() {
        return parameters;
    }

    public void setParameters(List<TemplateParamVo> parameters) {
        this.parameters = parameters;
    }

    public List<FieldTemplateVo> getFieldTemplates() {
        return fieldTemplates;
    }

    public void setFieldTemplates(List<FieldTemplateVo> fieldTemplates) {
        this.fieldTemplates = fieldTemplates;
    }
}
