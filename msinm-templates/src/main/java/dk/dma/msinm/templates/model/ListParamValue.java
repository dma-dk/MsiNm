package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.BaseEntity;
import dk.dma.msinm.common.model.ILocalizable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class for the list parameter type values
 */
@Entity
public class ListParamValue extends BaseEntity<Integer>  implements ILocalizable<ListParamValueDesc> {

    @ManyToOne
    @NotNull
    ListParamType listParamType;

    @NotNull
    private int sortKey;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity", orphanRemoval = true)
    List<ListParamValueDesc> descs = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public ListParamValueDesc createDesc(String lang) {
        ListParamValueDesc desc = new ListParamValueDesc();
        desc.setLang(lang);
        desc.setEntity(this);
        getDescs().add(desc);
        return desc;
    }

    // ***********************************
    // Getters and setters
    // ***********************************

    public ListParamType getListParamType() {
        return listParamType;
    }

    public void setListParamType(ListParamType listParamType) {
        this.listParamType = listParamType;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setSortKey(int sortKey) {
        this.sortKey = sortKey;
    }

    @Override
    public List<ListParamValueDesc> getDescs() {
        return descs;
    }

    @Override
    public void setDescs(List<ListParamValueDesc> descs) {
        this.descs = descs;
    }

}
