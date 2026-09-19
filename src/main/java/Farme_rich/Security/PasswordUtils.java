package Farme_rich.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class PasswordUtils {
    public static final String ALGORITHM = "AES";

    public static SecretKeySpec getKey(String SECRET_KEY) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        key = sha.digest(key);
        return new SecretKeySpec(key, ALGORITHM);
    }

    public static String encrypt(String data, String SECRET_KEY) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getKey(SECRET_KEY));
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String encryptedData, String SECRET_KEY) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getKey(SECRET_KEY));
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)), StandardCharsets.UTF_8);
    }
}


