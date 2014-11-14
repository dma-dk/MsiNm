package dk.dma.msinm.templates.vo;

import dk.dma.msinm.templates.model.CompositeParamType;

import java.util.ArrayList;
import java.util.List;

/**
 * Value object for the {@code CompositeParamType} model entity
 */
public class CompositeParamTypeVo extends ParamTypeVo<CompositeParamType> {

    List<TemplateParamVo> parameters = new ArrayList<>();

    /**
     * Constructor
     */
    public CompositeParamTypeVo() {
    }

    /**
     * Constructor
     *
     * @param paramType the entity
     */
    public CompositeParamTypeVo(CompositeParamType paramType) {
        super(paramType);

        paramType.getParameters().forEach(param -> checkCreateParameters().add(new TemplateParamVo(param)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompositeParamType toEntity() {
        CompositeParamType paramType = new CompositeParamType();
        paramType.setId(id);
        paramType.setName(name);
        if (parameters != null) {
            parameters.stream()
                    .filter(TemplateParamVo::isDefined)
                    .forEach(param -> paramType.getParameters().add(param.toEntity()));
        }

        return paramType;
    }

    /**
     * Returns or creates the list of parameters
     * @return the list of parameters
     */
    public List<TemplateParamVo> checkCreateParameters() {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        return parameters;
    }

    // ***********************************
    // Getters and setters
    // ***********************************

    public List<TemplateParamVo> getParameters() {
        return parameters;
    }

    public void setParameters(List<TemplateParamVo> parameters) {
        this.parameters = parameters;
    }
}
