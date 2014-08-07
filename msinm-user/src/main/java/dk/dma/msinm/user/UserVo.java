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
package dk.dma.msinm.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class used for creating users
 */
public class UserVo extends BaseVo<User> {
    Integer id;
    String email;
    String firstName;
    String lastName;
    String language;
    String mmsi;
    String vesselName;
    List<String> roles = new ArrayList<>();

    /**
     * Constructor
     */
    public UserVo() {
    }

    /**
     * Constructor
     * @param entity the user entity
     */
    public UserVo(User entity) {
        super(entity);
        this.id = entity.getId();
        this.email = entity.getEmail();
        this.firstName = entity.getFirstName();
        this.lastName = entity.getLastName();
        this.language = entity.getLanguage();
        this.mmsi = entity.getMmsi();
        this.vesselName = entity.getVesselName();
        entity.getRoles().forEach(role -> roles.add(role.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User toEntity() {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setLanguage(language);
        user.setMmsi(mmsi);
        user.setVesselName(vesselName);
        return user;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMmsi() {
        return mmsi;
    }

    public void setMmsi(String mmsi) {
        this.mmsi = mmsi;
    }

    public String getVesselName() {
        return vesselName;
    }

    public void setVesselName(String vesselName) {
        this.vesselName = vesselName;
    }

    public List<String> getRoles() {
        return roles;
    }

    @JsonDeserialize(contentAs = String.class)
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
