package encryption;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
unsecure hashing algorithm
 */
public class MD5 {

    public static String getDigest(String input){
        try {
            // Message digest for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Update message digest
            md.update(input.getBytes(StandardCharsets.UTF_8));

            // Get hashbytes
            byte[] hashbytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b: hashbytes){
                sb.append(String.format("%02x",b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            return e.getMessage();
        }

    }
}
