package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.VersionedEntity;
import dk.dma.msinm.model.Category;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class for the message templates
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "Template.findAll",
                query = "select t from Template t order by lower(t.name) asc")
})
public class Template extends VersionedEntity<Integer> {

    @Column(unique=true)
    String name;

    @ManyToMany(cascade = CascadeType.ALL)
    List<Category> categories = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortKey")
    List<TemplateParam> parameters = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<TemplateParam> getParameters() {
        return parameters;
    }

    public void setParameters(List<TemplateParam> parameters) {
        this.parameters = parameters;
    }
}
