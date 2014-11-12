package dk.dma.msinm.templates.service;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.templates.model.ListParamType;
import dk.dma.msinm.templates.vo.ListParamTypeVo;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business interface for accessing list parameter types
 */
@Stateless
public class ListParamService extends BaseService {

    @Inject
    private Logger log;

    @Inject
    private MsiNmApp app;

    /**
     * Returns the list parameter types
     * NB: Returns all language variants, but sorted by the given language
     *
     * @param lang the language to sort by
     * @return the list parameter types
     */
    public List<ListParamTypeVo> getListParamTypes(String lang) {

        List<ListParamType> types = getAll(ListParamType.class);
        DataFilter dataFilter = DataFilter.get(DataFilter.ALL);

        List<ListParamTypeVo> result = types.stream()
                .map(type -> new ListParamTypeVo(type, dataFilter))
                .collect(Collectors.toList());

        // Sort the type values by the given language
        result.stream().forEach(type -> type.sortValuesByLanguage(lang));

        return result;
    }

    /**
     * Creates a new list parameter type from the given value object
     * @param typeVo the list parameter type value object
     * @return the new entity
     */
    public ListParamType createParamType(ListParamTypeVo typeVo) {

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
    public ListParamType updateParamType(ListParamTypeVo typeVo) {

        ListParamType original = getByPrimaryKey(ListParamType.class, typeVo.getId());
        ListParamType type = typeVo.toEntity();

        original.setName(typeVo.getName());
        original.getValues().clear();
        original.getValues().addAll(type.getValues());

        original = saveEntity(original);
        log.info("Updated list parameter type " + original);

        return original;
    }

    /**
     * Deletes the list parameter type
     * @param id the id of the list parameter type to delete
     */
    public boolean deleteParamType(Integer id) {

        ListParamType type = getByPrimaryKey(ListParamType.class, id);
        if (type != null) {
            remove(type);
            log.info("Deleted list parameter type " + id);
            return true;
        }
        return false;
    }

}
