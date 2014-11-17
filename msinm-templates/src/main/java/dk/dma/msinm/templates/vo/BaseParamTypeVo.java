package dk.dma.msinm.templates.vo;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Arrays;
import java.util.List;

/**
 * Value object for base template parameter types, such as "text", "number", "boolean", etc
 */
@JsonTypeName("BASE")
public class BaseParamTypeVo extends ParamTypeVo {

    /**
     * Constructor
     */
    public BaseParamTypeVo() {
    }

    /**
     * Constructor
     * @param name the name of the parameter
     */
    public BaseParamTypeVo(String name) {
        this();
        setName(name);
    }

    /**
     * Returns the list of base parameter types
     * @return the list of base parameter types
     */
    public static List<BaseParamTypeVo> getBaseParameterTypes() {
        return Arrays.asList(
                new BaseParamTypeVo("text"),
                new BaseParamTypeVo("number"),
                new BaseParamTypeVo("boolean")
        );
    }
}
