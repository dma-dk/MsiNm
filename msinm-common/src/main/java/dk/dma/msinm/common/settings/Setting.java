package dk.dma.msinm.common.settings;

/**
 * Interface that must be implemented by settings
 */
public interface Setting {

    /**
     * Returns the name of the setting
     * @return the name of the setting
     */
    String getSettingName();

    /**
     * Returns the default setting value
     * @return the default setting value
     */
    public default String defaultValue() {
        return "";
    }

    /**
     * Returns the source
     * @return the source
     */
    public default Source getSource() {
        return Source.DATABASE;
    }
}
