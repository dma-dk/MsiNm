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

import dk.dma.msinm.common.model.VersionedEntity;
import dk.dma.msinm.user.security.oath.OAuthLogin;
import org.apache.commons.lang.StringUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a user entity
 */
@Entity
@NamedQueries({
        @NamedQuery(name="User.findByEmail",
                query="SELECT u FROM User u left join fetch u.roles where u.email = :email"),
        @NamedQuery(name="User.findById",
                query="SELECT u FROM User u left join fetch u.roles where u.id = :id")
})
public class User extends VersionedEntity<Integer> implements Principal {

    @Column(name="email", unique=true)
    private String email;

    @Column(name="first_name")
    String firstName;

    @Column(name="last_name")
    String lastName;

    @Embedded
    SaltedPasswordHash password;

    @NotNull
    @ManyToMany(fetch= FetchType.LAZY, cascade = {CascadeType.PERSIST})
    List<Role> roles = new ArrayList<>();

    @OneToMany(fetch= FetchType.LAZY, mappedBy="user", cascade = {CascadeType.PERSIST})
    List<OAuthLogin> oauthLogins = new ArrayList<>();

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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<OAuthLogin> getOauthLogins() {
        return oauthLogins;
    }

    public void setOauthLogins(List<OAuthLogin> oauthLogins) {
        this.oauthLogins = oauthLogins;
    }

    /**
     * Returns if the customer has an associated password
     * @return if the customer has an associated password
     */
    public boolean hasPassword() {
        return (password != null && password.isDefined());
    }

    /**
     * Creates a Json representation of this entity
     * @return the Json representation
     */
    public JsonObjectBuilder toJson() {
        JsonArrayBuilder rolesJson = Json.createArrayBuilder();
        getRoles().forEach(role -> rolesJson.add(role.getName()));

        return Json.createObjectBuilder()
                .add("id", getId())
                .add("email", email)
                .add("firstName", firstName)
                .add("lastName", lastName)
                .add("roles", rolesJson);
    }

}
