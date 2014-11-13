package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.model.IPreloadable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class for the list-based template parameter type
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "ListParamType.findAll",
                query = "select t from ListParamType t order by lower(t.name) asc")
})
public class ListParamType extends ParamType implements IPreloadable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "listParamType", orphanRemoval = true)
    @OrderBy("sortKey")
    List<ListParamValue> values = new ArrayList<>();

    public List<ListParamValue> getValues() {
        return values;
    }

    public void setValues(List<ListParamValue> values) {
        this.values = values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preload(DataFilter dataFilter) {
        values.forEach(v -> {});
    }
}
