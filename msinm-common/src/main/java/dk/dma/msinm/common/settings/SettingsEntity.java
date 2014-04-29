package dk.dma.msinm.common.settings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Used internally by the {@code BaseSettings} to persist settings
 */
@Entity
@Table(name="settings")
public class SettingsEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    String key;
    String value;

    public SettingsEntity() {
    }

    public SettingsEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public SettingsEntity(Setting setting) {
        this.key = setting.getSettingName();
        this.value = setting.defaultValue();
    }


    @Id
    @Column(name="settings_key",unique = true, nullable = false)
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    @Column(name="settings_value")
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}