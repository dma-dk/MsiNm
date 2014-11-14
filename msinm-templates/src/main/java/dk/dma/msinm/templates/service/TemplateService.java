package dk.dma.msinm.templates.service;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.templates.model.CompositeParamType;
import dk.dma.msinm.templates.model.ListParamType;
import dk.dma.msinm.templates.model.ListParamValue;
import dk.dma.msinm.templates.model.ParamType;
import dk.dma.msinm.templates.model.Template;
import dk.dma.msinm.templates.vo.CompositeParamTypeVo;
import dk.dma.msinm.templates.vo.FieldTemplateVo;
import dk.dma.msinm.templates.vo.ListParamTypeVo;
import dk.dma.msinm.templates.vo.TemplateVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business interface for managing templates plus related types
 */
@Stateless
public class TemplateService extends BaseService {

    @Inject
    private Logger log;

    @Inject
    private MsiNmApp app;

    // *******************************************
    // ** Template functionality
    // *******************************************

    /**
     * Returns the templates
     * NB: Returns all language variants, but sorted by the given language
     *
     * @param lang the language to sort by
     * @return the templates
     */
    public List<TemplateVo> getTemplates(String lang) {
        DataFilter dataFilter = DataFilter.get("Category.parent").setLang(lang);

        List<TemplateVo> templates = em.createNamedQuery("Template.findAll", Template.class)
                .getResultList()
                .stream()
                .map(type -> new TemplateVo(type, dataFilter))
                .collect(Collectors.toList());

        // Sort the template categories by the given language
        templates.stream().forEach(t -> t.sortCategoriesByLanguage(lang));

        return templates;
    }

    /**
     * Creates a new template from the given value object
     * @param templateVo the template value object
     * @return the new entity
     */
    public Template createTemplate(TemplateVo templateVo) {

        // Ensure validity of the template name
        if (StringUtils.isBlank(templateVo.getName())) {
            throw new IllegalArgumentException("Invalid parameter type name: " + templateVo.getName());
        }

        Template template = templateVo.toEntity();

        // Substitute the Categories with the persisted ones
        if (template.getCategories().size() > 0) {
            List<Category> categories = new ArrayList<>();
            template.getCategories().forEach(cat -> categories.add(getByPrimaryKey(Category.class, cat.getId())));
            template.setCategories(categories);
        }

        template = saveEntity(template);
        log.info("Created template " + template);

        return template;
    }

    /**
     * Updates an existing template from the given value object
     * @param templateVo the template value object
     * @return the updated entity
     */
    public Template updateTemplate(TemplateVo templateVo) {

        Template original = getByPrimaryKey(Template.class, templateVo.getId());
        Template template = templateVo.toEntity();

        original.setName(templateVo.getName());
        original.getParameters().clear();
        original.getParameters().addAll(template.getParameters());

        // Copy the Categories
        original.getCategories().clear();
        template.getCategories().forEach(cat -> original.getCategories().add(getByPrimaryKey(Category.class, cat.getId())));

        original.getFieldTemplates().clear();
        original.getFieldTemplates().addAll(template.getFieldTemplates());

        template = saveEntity(original);
        log.info("Updated template " + template);

        return template;
    }

    /**
     * Deletes the template with the given id
     * @param id the id of the template to delete
     */
    public boolean deleteTemplate(Integer id) {

        Template template = getByPrimaryKey(Template.class, id);
        if (template != null) {
            remove(template);
            log.info("Deleted template " + id);
            return true;
        }
        return false;
    }


    /**
     * Returns the list of available field templates
     * @return the list of available field templates
     */
    public List<FieldTemplateVo> getFieldTemplates() {
        List<FieldTemplateVo> fieldTemplates = new ArrayList<>();

        // Add "title" fields
        int sortKey = 10;
        for (String lang : app.getLanguages()) {
            fieldTemplates.add(new FieldTemplateVo("title", lang, sortKey++, true));
        }

        // Add "description" fields
        sortKey = 20;
        for (String lang : app.getLanguages()) {
            fieldTemplates.add(new FieldTemplateVo("description", lang, sortKey++, true));
        }

        // TODO: Publication fields

        return fieldTemplates;
    }

    // *******************************************
    // ** List parameter type functionality
    // *******************************************
    /**
     * Returns the list parameter types
     * NB: Returns all language variants, but sorted by the given language
     *
     * @param lang the language to sort by
     * @return the list parameter types
     */
    public List<ListParamTypeVo> getListParamTypes(String lang) {
        DataFilter dataFilter = DataFilter.get(DataFilter.ALL);

        List<ListParamTypeVo> types = em.createNamedQuery("ListParamType.findAll", ListParamType.class)
                .getResultList()
                .stream()
                .map(type -> new ListParamTypeVo(type, dataFilter))
                .collect(Collectors.toList());

        // Sort the type values by the given language
        types.stream().forEach(type -> type.sortValuesByLanguage(lang));

        return types;
    }

    /**
     * Creates a new list parameter type from the given value object
     * @param typeVo the list parameter type value object
     * @return the new entity
     */
    public ListParamType createListParamType(ListParamTypeVo typeVo) {

        // Ensure validity of the type name
        if (StringUtils.isBlank(typeVo.getName()) || getParameterTypeNames().contains(typeVo.getName())) {
            throw new IllegalArgumentException("Invalid parameter type name: " + typeVo.getName());
        }

        ListParamType type = typeVo.toEntity();
        type = saveEntity(type);
        log.info("Created list parameter type " + type);

        return type;
    }

    /**
     * Updates an existing list parameter type from the given value object
     * @param typeVo the list parameter type value object
     * @return the updated entity
     */
    public ListParamType updateListParamType(ListParamTypeVo typeVo) {

        ListParamType original = getByPrimaryKey(ListParamType.class, typeVo.getId());
        ListParamType type = typeVo.toEntity();

        original.setName(typeVo.getName());
        original.getValues().clear();
        for (ListParamValue val : type.getValues()) {
            original.getValues().add(val);
            val.setListParamType(original);
        }

        original = saveEntity(original);
        log.info("Updated list parameter type " + original);

        return original;
    }

    // *******************************************
    // ** Composite parameter type functionality
    // *******************************************

    /**
     * Returns the composite parameter types
     * @return the composite parameter types
     */
    public List<CompositeParamTypeVo> getCompositeParamTypes() {

        return em.createNamedQuery("CompositeParamType.findAll", CompositeParamType.class)
                .getResultList()
                .stream()
                .map(CompositeParamTypeVo::new)
                .collect(Collectors.toList());
    }


    /**
     * Creates a new composite parameter type from the given value object
     * @param typeVo the composite parameter type value object
     * @return the new entity
     */
    public CompositeParamType createCompositeParamType(CompositeParamTypeVo typeVo) {

        // Ensure validity of the type name
        if (StringUtils.isBlank(typeVo.getName()) || getParameterTypeNames().contains(typeVo.getName())) {
            throw new IllegalArgumentException("Invalid parameter type name: " + typeVo.getName());
        }

        CompositeParamType type = typeVo.toEntity();
        type = saveEntity(type);
        log.info("Created composite parameter type " + type);

        return type;
    }

    /**
     * Updates an existing composite parameter type from the given value object
     * @param typeVo the composite parameter type value object
     * @return the updated entity
     */
    public CompositeParamType updateCompositeParamType(CompositeParamTypeVo typeVo) {

        CompositeParamType original = getByPrimaryKey(CompositeParamType.class, typeVo.getId());
        CompositeParamType type = typeVo.toEntity();

        original.setName(typeVo.getName());
        original.getParameters().clear();
        original.getParameters().addAll(type.getParameters());

        original = saveEntity(original);
        log.info("Updated composite parameter type " + original);

        return original;
    }

    // *******************************************
    // ** Common parameter type functionality
    // *******************************************

    /**
     * Returns the list of parameter names
     * @return Returns the list of parameter names
     */
    public List<String> getParameterTypeNames() {

        List<String> names = new ArrayList<>();

        // Add the base types
        names.add("text");
        names.add("number");
        names.add("boolean");

        // Add the list parameter types
        names.addAll(
                em.createNamedQuery("ListParamType.findAll", ListParamType.class)
                        .getResultList()
                        .stream()
                        .map(ParamType::getName)
                        .collect(Collectors.toList()));

        // Add the composite parameter types
        names.addAll(
                em.createNamedQuery("CompositeParamType.findAll", CompositeParamType.class)
                        .getResultList()
                        .stream()
                        .map(ParamType::getName)
                        .collect(Collectors.toList()));

        return names;
    }

    /**
     * Deletes the parameter type with the given id
     * @param id the id of the parameter type to delete
     */
    public boolean deleteParamType(Integer id) {

        ParamType type = getByPrimaryKey(ParamType.class, id);
        if (type != null) {
            remove(type);
            log.info("Deleted list parameter type " + id);
            return true;
        }
        return false;
    }

}
