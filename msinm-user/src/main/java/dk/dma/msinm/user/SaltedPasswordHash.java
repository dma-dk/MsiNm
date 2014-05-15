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

import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Models a hashed password incl. salt.
 */
@Embeddable
public class SaltedPasswordHash implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name="password")
    String passwordHash;

    @Column(name="password_salt")
    String passwordSalt;

    /**
     * No-arg constructor
     */
    public SaltedPasswordHash() {
    }

    /**
     * Constructor
     * @param passwordHash the hashed password
     * @param passwordSalt the password salt
     */
    public SaltedPasswordHash(String passwordHash, String passwordSalt) {
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    /**
     * Returns if the password is defined, i.e. not empty
     * @return if the password is defined, i.e. not empty
     */
    @Transient
    public boolean isDefined() {
        return (StringUtils.isNotBlank(passwordHash) &&
                StringUtils.isNotBlank(passwordSalt));
    }
}
