package dk.dma.msinm.templates.vo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import dk.dma.msinm.common.vo.JsonSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract value object class for the template parameter data
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value=ParameterDataVo.BaseParameterDataVo.class, name="BASE"),
        @JsonSubTypes.Type(value=ParameterDataVo.CompositeParameterDataVo.class, name="COMPOSITE"),
        @JsonSubTypes.Type(value=ParameterDataVo.ListParameterDataVo.class, name="LIST")
})
public abstract class ParameterDataVo<V> implements JsonSerializable {

    String name;
    String type;
    boolean list;
    List<V> values = new ArrayList<>();

    // ***********************************
    // Getters and setters
    // ***********************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }

    public List<V> getValues() {
        return values;
    }

    public void setValues(List<V> values) {
        this.values = values;
    }

    /**
     * Value object for base template parameter data, such as "text", "number", "boolean", etc
     */
    @JsonTypeName("BASE")
    public static class BaseParameterDataVo extends ParameterDataVo<Object> {
    }

    /**
     * Value object for list template parameter data
     */
    @JsonTypeName("LIST")
    public static class ListParameterDataVo extends ParameterDataVo<ListParamValueVo> {
    }

    /**
     * Value object for composite template parameter data
     */
    @JsonTypeName("COMPOSITE")
    public static class CompositeParameterDataVo extends ParameterDataVo<List<ParameterDataVo>> {
    }

}