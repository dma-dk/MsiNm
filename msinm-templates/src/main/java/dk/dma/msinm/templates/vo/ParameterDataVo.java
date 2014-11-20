package dk.dma.msinm.templates.vo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import dk.dma.msinm.common.vo.JsonSerializable;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Adds a Freemarker-friendly version of the parameter data
     * @param fmData the Freemarker data
     * @param lang the language
     */
    public abstract void toFmParameterData(Map<String, Object> fmData, String lang);

    /**
     * Adds a Freemarker-friendly version of the parameter data
     * @param fmData the Freemarker data
     * @param lang the language
     * @param params the parameter data
     */
    public static void toFmParameterData(Map<String, Object> fmData, String lang, List<ParameterDataVo> params) {
        params.stream()
                .filter(param -> param.getValues() != null && param.getValues().size() > 0)
                .forEach(param -> param.toFmParameterData(fmData, lang));
    }

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

    /**********************************************/
    /** VO sub-classes                           **/
    /**********************************************/

    /**
     * Value object for base template parameter data, such as "text", "number", "boolean", etc
     */
    @JsonTypeName("BASE")
    public static class BaseParameterDataVo extends ParameterDataVo<Object> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void toFmParameterData(Map<String, Object> fmData, String lang) {
            fmData.put(getName(), new FmBaseParamValue(getValues()));
        }
    }

    /**
     * Value object for list template parameter data
     */
    @JsonTypeName("LIST")
    public static class ListParameterDataVo extends ParameterDataVo<ListParamValueVo> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void toFmParameterData(Map<String, Object> fmData, String lang) {
            fmData.put(getName(), new FmListParamValue(getValues(), lang));
        }
    }

    /**
     * Value object for composite template parameter data
     */
    @JsonTypeName("COMPOSITE")
    public static class CompositeParameterDataVo extends ParameterDataVo<List<ParameterDataVo>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void toFmParameterData(Map<String, Object> fmData, String lang) {
            fmData.put(getName(), new FmCompositeParamValue(getValues(), lang));
        }

    }


    /**********************************************/
    /** Freemarker parameter data classes        **/
    /**********************************************/

    /**
     * Used for encapsulating base parameter data for easier Freemarker template processing
     */
    public static class FmBaseParamValue {
        Object value;
        List<Object> values;

        public FmBaseParamValue(List<Object> paramValues) {
            this.values = paramValues;
            this.value = values.get(0);
        }

        @Override
        public String toString() {
            return "FmBaseParamValue{" +
                    "values=" + values +
                    '}';
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public List<Object> getValues() {
            return values;
        }

        public void setValues(List<Object> values) {
            this.values = values;
        }
    }

    /**
     * Used for encapsulating list parameter data for easier Freemarker template processing
     */
    public static class FmListParamValue {
        String longValue;
        String shortValue;
        List<String> longValues = new ArrayList<>();
        List<String> shortValues = new ArrayList<>();

        public FmListParamValue(List<ListParamValueVo> paramValues, String lang) {
            paramValues.stream()
                .filter(ListParamValueVo::isDefined)
                .forEach(val -> {
                    val.sortDescs(lang);
                    ListParamValueVo.ListParamValueDescVo desc = val.getDescs().get(0);
                    longValues.add(StringUtils.isNotBlank(desc.getLongValue()) ? desc.getLongValue() : desc.getShortValue());
                    shortValues.add(StringUtils.isNotBlank(desc.getShortValue()) ? desc.getShortValue() : desc.getLongValue());
                });
            if (longValues.size() > 0) {
                longValue = longValues.get(0);
                shortValue = shortValues.get(0);
            }
        }

        @Override
        public String toString() {
            return "FmListParamValue{" +
                    "longValues=" + longValues +
                    ", shortValues=" + shortValues +
                    '}';
        }

        public String getLongValue() {
            return longValue;
        }

        public void setLongValue(String longValue) {
            this.longValue = longValue;
        }

        public String getShortValue() {
            return shortValue;
        }

        public void setShortValue(String shortValue) {
            this.shortValue = shortValue;
        }

        public List<String> getLongValues() {
            return longValues;
        }

        public void setLongValues(List<String> longValues) {
            this.longValues = longValues;
        }

        public List<String> getShortValues() {
            return shortValues;
        }

        public void setShortValues(List<String> shortValues) {
            this.shortValues = shortValues;
        }
    }

    /**
     * Used for encapsulating composite parameter data for easier Freemarker template processing
     */
    public static class FmCompositeParamValue {

        Map<String, Object> value;
        List<Map<String, Object>> values = new ArrayList<>();

        public FmCompositeParamValue(List<List<ParameterDataVo>> paramValues, String lang) {
            paramValues.forEach(param -> {
                Map<String, Object> val = new HashMap<>();
                ParameterDataVo.toFmParameterData(val, lang, param);
                values.add(val);
            });
            value = values.get(0);
        }

        @Override
        public String toString() {
            return "FmCompositeParamValue{" +
                    "values=" + values +
                    '}';
        }

        public Map<String, Object> getValue() {
            return value;
        }

        public void setValue(Map<String, Object> value) {
            this.value = value;
        }

        public List<Map<String, Object>> getValues() {
            return values;
        }

        public void setValues(List<Map<String, Object>> values) {
            this.values = values;
        }
    }
}
