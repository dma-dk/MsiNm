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

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a user entity
 */
@Entity
public class User extends VersionedEntity<Integer> {

    @Column(name="email", unique=true)
    private String email;

    @Column(name="first_name")
    String firstName;

    @Column(name="last_name")
    String lastName;

    @Embedded
    SaltedPasswordHash password;

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

    public List<OAuthLogin> getOauthLogins() {
        return oauthLogins;
    }

    public void setOauthLogins(List<OAuthLogin> oauthLogins) {
        this.oauthLogins = oauthLogins;
    }

}
