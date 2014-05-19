package dk.dma.msinm.user.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Functionlity for encrypting passawords
 */
public class PasswordUtils {

    /**
     * Hashes and Hex'es the password
     * @param pwd the password
     * @param type the digest, e.g. "SHA-512"
     * @return the hashed and hex'ed password
     */
    public static String encrypt(String pwd, String type) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to create message digest", e);
        }

        byte[] val = md.digest(pwd.getBytes());

        StringBuilder sb = new StringBuilder(val.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : val) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }

}
