package dk.dma.msinm.templates.vo;

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.model.ILocalizedDesc;
import dk.dma.msinm.common.vo.LocalizableVo;
import dk.dma.msinm.common.vo.LocalizedDescVo;
import dk.dma.msinm.templates.model.ListParamValue;
import dk.dma.msinm.templates.model.ListParamValueDesc;
import org.apache.commons.lang.StringUtils;

/**
 * Value object for the {@code ListParamValue} model entity
 */
public class ListParamValueVo extends LocalizableVo<ListParamValue, ListParamValueVo.ListParamValueDescVo> {

    Integer id;
    private int sortKey;

    /**
     * Constructor
     */
    public ListParamValueVo() {
    }

    /**
     * Constructor
     *
     * @param value the value
     * @param dataFilter what type of data to include from the entity
     */
    public ListParamValueVo(ListParamValue value, DataFilter dataFilter) {
        super(value);

        DataFilter compFilter = dataFilter.forComponent(ListParamValue.class);

        id = value.getId();
        sortKey = value.getSortKey();
        value.getDescs(compFilter).stream()
                .forEach(desc -> checkCreateDescs().add(new ListParamValueDescVo(desc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListParamValue toEntity() {
        ListParamValue value = new ListParamValue();
        value.setId(id);
        value.setSortKey(sortKey);
        if (getDescs() != null) {
            getDescs().stream().forEach(desc -> value.getDescs().add(desc.toEntity(value)));
            value.getDescs().removeIf(desc -> !desc.descDefined());
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListParamValueDescVo createDesc(String lang) {
        ListParamValueDescVo desc = new ListParamValueDescVo();
        desc.setLang(lang);
        checkCreateDescs().add(desc);
        return desc;
    }

    public boolean isDefined() {
        return getDescs() != null && getDescs().stream().anyMatch(ListParamValueDescVo::descDefined);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setSortKey(int sortKey) {
        this.sortKey = sortKey;
    }

    /**
     * The entity description VO
     */
    public static class ListParamValueDescVo extends LocalizedDescVo<ListParamValueDesc, ListParamValueVo> {

        private String shortValue;
        private String longValue;

        /**
         * Constructor
         */
        public ListParamValueDescVo() {
            super();
        }

        /**
         * Constructor
         * @param desc the entity
         */
        public ListParamValueDescVo(ListParamValueDesc desc) {
            super(desc);
            shortValue = desc.getShortValue();
            longValue = desc.getLongValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ListParamValueDesc toEntity() {
            ListParamValueDesc desc = new ListParamValueDesc();
            desc.setLang(getLang());
            desc.setShortValue(StringUtils.trim(shortValue));
            desc.setLongValue(StringUtils.trim(longValue));
            return desc;
        }

        public ListParamValueDesc toEntity(ListParamValue value) {
            ListParamValueDesc desc = toEntity();
            desc.setEntity(value);
            return desc;
        }

        public String getShortValue() {
            return shortValue;
        }

        public void setShortValue(String shortValue) {
            this.shortValue = shortValue;
        }

        public String getLongValue() {
            return longValue;
        }

        public void setLongValue(String longValue) {
            this.longValue = longValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean descDefined() {
            return ILocalizedDesc.fieldsDefined(shortValue, longValue);
        }
    }

}
