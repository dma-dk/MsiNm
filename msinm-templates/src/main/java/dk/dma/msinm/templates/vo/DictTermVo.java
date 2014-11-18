package dk.dma.msinm.templates.vo;

import dk.dma.msinm.common.model.ILocalizedDesc;
import dk.dma.msinm.common.vo.LocalizableVo;
import dk.dma.msinm.common.vo.LocalizedDescVo;
import dk.dma.msinm.templates.model.DictTerm;
import dk.dma.msinm.templates.model.DictTermDesc;
import org.apache.commons.lang.StringUtils;

/**
 * Value object for the {@code ListParamValue} model entity
 */
public class DictTermVo extends LocalizableVo<DictTerm, DictTermVo.DictTermDescVo> {

    Integer id;
    private String key;

    /**
     * Constructor
     */
    public DictTermVo() {
    }

    /**
     * Constructor
     *
     * @param term the value
     */
    public DictTermVo(DictTerm term) {
        super(term);

        id = term.getId();
        key = term.getKey();
        term.getDescs().stream()
                .forEach(desc -> checkCreateDescs().add(new DictTermDescVo(desc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DictTerm toEntity() {
        DictTerm term = new DictTerm();
        term.setId(id);
        term.setKey(key);
        if (getDescs() != null) {
            getDescs().stream().forEach(desc -> term.getDescs().add(desc.toEntity(term)));
            term.getDescs().removeIf(desc -> !desc.descDefined());
        }

        return term;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DictTermDescVo createDesc(String lang) {
        DictTermDescVo desc = new DictTermDescVo();
        desc.setLang(lang);
        checkCreateDescs().add(desc);
        return desc;
    }

    /**
     * Returns if this list parameter value defines any data
     * @return if this list parameter value defines any data
     */
    public boolean isDefined() {
        return getDescs() != null && getDescs().stream().anyMatch(DictTermDescVo::descDefined);
    }

    // ***********************************
    // Getters and setters
    // ***********************************

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The entity description VO
     */
    public static class DictTermDescVo extends LocalizedDescVo<DictTermDesc, DictTermVo> {

        private String value;

        /**
         * Constructor
         */
        public DictTermDescVo() {
            super();
        }

        /**
         * Constructor
         * @param desc the entity
         */
        public DictTermDescVo(DictTermDesc desc) {
            super(desc);
            value = desc.getValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DictTermDesc toEntity() {
            DictTermDesc desc = new DictTermDesc();
            desc.setLang(getLang());
            desc.setValue(StringUtils.trim(value));
            return desc;
        }

        public DictTermDesc toEntity(DictTerm value) {
            DictTermDesc desc = toEntity();
            desc.setEntity(value);
            return desc;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean descDefined() {
            return ILocalizedDesc.fieldsDefined(value);
        }

        // ***********************************
        // Getters and setters
        // ***********************************

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
