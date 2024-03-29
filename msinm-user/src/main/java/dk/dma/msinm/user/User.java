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

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.model.IPreloadable;
import dk.dma.msinm.common.model.VersionedEntity;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a user entity
 */
@Entity
@Cacheable
@NamedQueries({
        @NamedQuery(name="User.findByEmail",
                query="SELECT u FROM User u left join fetch u.roles where lower(u.email) = lower(:email)",
                hints=@QueryHint(name="org.hibernate.cacheable",value="true")),
        @NamedQuery(name="User.findById",
                query="SELECT u FROM User u left join fetch u.roles where u.id = :id",
                hints=@QueryHint(name="org.hibernate.cacheable",value="true")),
        @NamedQuery(name  = "User.searchUsers",
                query = "select distinct u from User u where lower(u.email) like lower(:term) "
                        + "or lower(concat(coalesce(u.firstName,''), ' ', coalesce(u.lastName,''))) like lower(:term) "
                        + "order by "
                        + "case when LOCATE(lower(:sort), lower(u.email)) = 0 then LOCATE(lower(:sort), lower(concat(coalesce(u.firstName,''), ' ', coalesce(u.lastName,'')))) "
                        + "else LOCATE(lower(:sort), lower(u.email)) end"),
})
public class User extends VersionedEntity<Integer> implements Principal, IPreloadable {

    @Column(name="email", unique=true)
    private String email;

    @Column(name="first_name")
    String firstName;

    @Column(name="last_name")
    String lastName;

    @Embedded
    SaltedPasswordHash password;

    String language;

    String resetPasswordToken;

    String mmsi;

    String vesselName;

    @NotNull
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @ManyToMany(fetch= FetchType.LAZY, cascade = {CascadeType.PERSIST})
    List<Role> roles = new ArrayList<>();

    public User() {
    }

    public User(String name) {
        int spacePosition = name.indexOf(' ');
        if (spacePosition == -1) {
            firstName = name;
        } else {
            firstName = name.substring(0, spacePosition);
            lastName = name.substring(spacePosition + 1);
        }
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

    @Override
    @Transient
    public String getName() {
        StringBuilder name = new StringBuilder();
        if (StringUtils.isNotBlank(firstName)) {
            name.append(firstName);
        }
        if (StringUtils.isNotBlank(lastName)) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(lastName);
        }
        if (name.length() == 0) {
            name.append(email);
        }
        return name.toString();
    }

    @Transient
    public void setName(String name) {
        int position = name.lastIndexOf(' ');
        if (position == -1) {
            lastName = name;
        } else {
            firstName = name.substring(0, position);
            lastName = name.substring(position + 1);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public SaltedPasswordHash getPassword() {
        return password;
    }

    public void setPassword(SaltedPasswordHash password) {
        this.password = password;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    /**
     * Returns if the customer has an associated password
     * @return if the customer has an associated password
     */
    public boolean hasPassword() {
        return (password != null && password.isDefined());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preload(DataFilter dataFilter) {
        getRoles().forEach(r -> {});
    }

    /**
     * Returns if the user has the given role
     * @param roleName the role name to check
     * @return if the user has the given role
     */
    public boolean hasRole(String roleName) {
        return getRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
}
