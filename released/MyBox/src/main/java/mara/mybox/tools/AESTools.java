package mara.mybox.tools;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import mara.mybox.dev.MyBoxLog;

/**
 * Reference: https://www.cnblogs.com/zhupig3028/p/16259271.html
 *
 * @Author Mara
 * @CreateDate 2023-3-29
 * @License Apache License Version 2.0
 */
// Refer To
public class AESTools {

    private final static String ENCODING = "utf-8";
    private final static String ALGORITHM = "AES";
    private final static String PATTERN = "AES/ECB/pkcs5padding";
    public static final String ALLCHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String DefaultKey = "8SaT6V9w5xpUr7qs";

    public static String encrypt(String plainText) {
        return encrypt(plainText, DefaultKey);
    }

    public static String decrypt(String plainText) {
        return decrypt(plainText, DefaultKey);
    }

    public static String generateAESKey() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(ALLCHAR.length())));
        }
        return sb.toString();
    }

    public static String generate3DESKey() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 24; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(ALLCHAR.length())));
        }
        return sb.toString();
    }

    public static Map<String, String> genKeyPair() {
        try {
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(1024, new SecureRandom());
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String privateKeyString = Base64.getEncoder().encodeToString((privateKey.getEncoded()));
            stringStringHashMap.put("0", publicKeyString);
            stringStringHashMap.put("1", privateKeyString);
            return stringStringHashMap;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String encrypt(String plainText, String key) {
        try {
            if (plainText == null || key == null || key.length() != 16) {
                return null;
            }
            SecretKey secretKey = new SecretKeySpec(key.getBytes(ENCODING), ALGORITHM);
            Cipher cipher = Cipher.getInstance(PATTERN);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptData = cipher.doFinal(plainText.getBytes(ENCODING));
            return Base64.getEncoder().encodeToString(encryptData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String decrypt(String plainText, String key) {
        try {
            if (plainText == null || key == null) {
                return null;
            }
            SecretKey secretKey = new SecretKeySpec(key.getBytes(ENCODING), ALGORITHM);
            Cipher cipher = Cipher.getInstance(PATTERN);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptData = cipher.doFinal(Base64.getDecoder().decode(plainText));
            return new String(encryptData, ENCODING);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
