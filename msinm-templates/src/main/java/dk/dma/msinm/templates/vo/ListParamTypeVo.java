package dk.dma.msinm.templates.vo;

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.templates.model.ListParamType;

import java.util.ArrayList;
import java.util.List;

/**
 * Value object for the {@code ListParamType} model entity
 */
public class ListParamTypeVo extends ParamTypeVo {

    List<ListParamValueVo> values = new ArrayList<>();

    /**
     * Constructor
     */
    public ListParamTypeVo() {
        setKind(Kind.LIST);
    }

    /**
     * Constructor
     *
     * @param paramType the entity
     * @param dataFilter what type of data to include from the entity
     */
    public ListParamTypeVo(ListParamType paramType, DataFilter dataFilter) {
        this();

        DataFilter compFilter = dataFilter.forComponent(ListParamType.class);

        id = paramType.getId();
        name = paramType.getName();
        paramType.getValues().forEach(val -> checkCreateValues().add(new ListParamValueVo(val, compFilter)));
    }

    /**
     * Converts the VO to an entity
     * @return the entity
     */
    public ListParamType toEntity() {
        ListParamType listParamType = new ListParamType();
        listParamType.setId(id);
        listParamType.setName(name);
        if (values != null) {
            values.stream()
                    .filter(ListParamValueVo::isDefined)
                    .forEach(val -> listParamType.getValues().add(val.toEntity(listParamType)));
        }

        return listParamType;
    }

    /**
     * Returns or creates the list of values
     * @return the list of values
     */
    public List<ListParamValueVo> checkCreateValues() {
        if (values == null) {
            values = new ArrayList<>();
        }
        return values;
    }

    /**
     * Sorts all value descriptors by the given language
     * @param lang the language
     */
    public void sortValuesByLanguage(String lang) {
        if (values != null) {
            values.forEach(val -> val.sortDescs(lang));
        }
    }

    // ***********************************
    // Getters and setters
    // ***********************************

    public List<ListParamValueVo> getValues() {
        return values;
    }

    public void setValues(List<ListParamValueVo> values) {
        this.values = values;
    }
}
