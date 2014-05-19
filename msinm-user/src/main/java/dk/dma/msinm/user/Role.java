package dk.dma.msinm.user;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.Entity;

/**
 * Defines a user role
 */
@Entity
public class Role extends BaseEntity<Integer> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
