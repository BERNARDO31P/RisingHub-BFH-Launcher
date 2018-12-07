package fm.bernardo.risinghub.classes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


final class SHA512
{

    static String sha512 (final String passwordToHash, final String salt)
    {
        String generatedPassword = null;
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            final byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }
}
