package dk.dma.msinm.common.settings;

import java.util.Objects;

/**
 * Default implementation of a setting
 */
public class DefaultSetting implements Setting {

    String settingName;
    String defaultValue;
    Source source;

    public DefaultSetting(String settingName, String defaultValue, Source source) {
        Objects.requireNonNull(settingName);
        Objects.requireNonNull(source);

        this.settingName = settingName;
        this.defaultValue = defaultValue;
        this.source = source;
    }

    public DefaultSetting(String settingName, String defaultValue) {
        this(settingName, defaultValue, Source.DATABASE);
    }

    public DefaultSetting(String settingName, Source source) {
        this(settingName, null, source);
    }

    public DefaultSetting(String settingName) {
        this(settingName, null, Source.DATABASE);
    }

    @Override
    public String getSettingName() {
        return settingName;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }

    @Override
    public Source getSource() {
        return source;
    }
}
