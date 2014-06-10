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

