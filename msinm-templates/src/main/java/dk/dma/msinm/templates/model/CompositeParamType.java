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
 * Entity class for the composite template parameter type
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "CompositeParamType.findAll",
                query = "select t from CompositeParamType t order by lower(t.name) asc")
})
public class CompositeParamType extends ParamType implements IPreloadable {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortKey")
    List<TemplateParam> parameters = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void preload(DataFilter dataFilter) {
        parameters.forEach(p -> {});
    }

    public List<TemplateParam> getParameters() {
        return parameters;
    }

    public void setParameters(List<TemplateParam> parameters) {
        this.parameters = parameters;
    }
}
