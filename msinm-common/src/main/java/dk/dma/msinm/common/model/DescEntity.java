package dk.dma.msinm.common.model;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * Base class for localizable description entities.
 */
@MappedSuperclass
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "lang", "entity_id" }))
public abstract class DescEntity<E extends ILocalizable> extends BaseEntity<Integer> implements ILocalizedDesc<E> {

    @NotNull
    String lang;

    @ManyToOne
    @NotNull
    E entity;

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public void setLang(String lang) {
        this.lang = lang;
    }

    public E getEntity() {
        return entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

}
