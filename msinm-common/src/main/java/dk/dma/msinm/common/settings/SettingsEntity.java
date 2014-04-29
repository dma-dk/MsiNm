/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
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