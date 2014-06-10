package dk.dma.msinm.user;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Defines a user role
 */
@Entity
@NamedQueries({
        @NamedQuery(name="Role.findByName",
                query="SELECT r FROM Role r where r.name = :name")
})
public class Role extends BaseEntity<Integer> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
