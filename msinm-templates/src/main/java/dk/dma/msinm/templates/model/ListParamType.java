package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.BaseEntity;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.model.IPreloadable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class for the list-based template parameter type
 */
@Entity
public class ListParamType extends BaseEntity<Integer> implements IPreloadable {

    @Column(unique=true)
    String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortKey")
    List<ListParamValue> values = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
