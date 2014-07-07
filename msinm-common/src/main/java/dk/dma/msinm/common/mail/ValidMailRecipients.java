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
package dk.dma.msinm.common.mail;


import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Instantiated with a comma-separated list of valid recipients, this class can
 * be used to filter an array of mail recipients into valid recipients.
 * <p>
 *     If the special token "ALL" is used as the {@code validMailRecipients}
 *     parameter, then allValid recipients are valid. Use this in production.
 * </p>
 * <p>
 *     Otherwise, if an invalid recipient is encountered, the first mail address
 *     in the {@code validMailRecipients} list is used instead.
 * </p>
 */
class ValidMailRecipients {
    public static final String ALL_RECIPIENTS_VALID_TOKEN = "ALL";

    private Set<String> validAddresses = new LinkedHashSet<>();
    private boolean allValid;

    /**
     * Constructor
     * @param validMailRecipients either "ALL" or comma-separated list of valid recipients
     */
    public ValidMailRecipients(String validMailRecipients) {
        if (ALL_RECIPIENTS_VALID_TOKEN.equalsIgnoreCase(validMailRecipients)) {
            allValid = true;
        } else {
            for (String r : validMailRecipients.split(",")) {
                validAddresses.add(r.trim().toLowerCase());
            }
        }
    }

    /**
     * Filters the address and ensures that it is a valid address
     * @param addr the address to filter
     * @return the valid address
     */
    public Address filter(String addr) throws AddressException {

        // Check that it can be parsed as a single email address
        InternetAddress addrs[] = InternetAddress.parse(addr);
        if (addrs.length != 1) {
            throw new AddressException("Invalid recipient: " + addr);
        }

        addrs[0].validate();

        // Production mode
        if (allValid) {
            return addrs[0];

        } else if (validAddresses.contains(addrs[0].getAddress().toLowerCase())) {
            return addrs[0];

        } else {
            // Return the first valid email address
            try {
                return new InternetAddress(validAddresses.iterator().next(), "(" + addr + ")");
            } catch (UnsupportedEncodingException e) {
                throw new AddressException("Invalid recipient with no substitute address: " + addr);
            }
        }
    }
}

