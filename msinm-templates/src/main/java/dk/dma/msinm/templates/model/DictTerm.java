package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.BaseEntity;
import dk.dma.msinm.common.model.ILocalizable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class for the dictionary terms
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "DictTerm.findAll",
                query = "select dt from DictTerm dt order by lower(dt.key) asc")
})
public class DictTerm extends BaseEntity<Integer> implements ILocalizable<DictTermDesc> {

    @NotNull
    @Column(name = "termKey", unique = true)
    String key;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity", orphanRemoval = true)
    List<DictTermDesc> descs = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public DictTermDesc createDesc(String lang) {
        DictTermDesc desc = new DictTermDesc();
        desc.setLang(lang);
        desc.setEntity(this);
        getDescs().add(desc);
        return desc;
    }

    // ***********************************
    // Getters and setters
    // ***********************************

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public List<DictTermDesc> getDescs() {
        return descs;
    }

    @Override
    public void setDescs(List<DictTermDesc> descs) {
        this.descs = descs;
    }
}
