package ai.rupheus.application.common.crypto;

import ai.rupheus.application.common.logger.ApplicationLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Component
public class CryptoManager {
    @Value("${security.crypto.master_key}")
    private String masterKey;

    private SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private final ApplicationLogger applicationLogger;

    @Autowired
    public CryptoManager(
        ApplicationLogger applicationLogger
    ) {
        this.applicationLogger = applicationLogger;
    }

    @PostConstruct
    private void init() {
        byte[] keyBytes = Base64.getDecoder().decode(this.masterKey);

        if (keyBytes.length != 32) {
            this.applicationLogger.warn(CryptoManager.class, "An error occurred while initializing crypto util");
            throw new IllegalArgumentException("Master key must be exactly 32 bytes for AES-256.");
        }

        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[16];
            this.secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];

            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            this.applicationLogger.warn(CryptoManager.class, "An error occurred while encrypting plain text: " + e.getMessage());
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combined.length - 16];

            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            this.applicationLogger.warn(CryptoManager.class, "An error occurred while decrypting encrypted: " + e.getMessage());
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    public void encryptField(Map<String, Object> map, String field) {
        if (map.containsKey(field) && map.get(field) != null) {
            map.put(field, this.encrypt(map.get(field).toString()));
        }
    }

    public void decryptField(Map<String, Object> map, String field) {
        if (!map.containsKey(field)) {
            return;
        }

        Object value = map.get(field);
        if (value == null) {
            return;
        }

        String stringValue = value.toString();
        if (stringValue.isBlank() || stringValue.contains("*")) {
            return;
        }

        if (!isBase64(stringValue)) {
            return;
        }

        try {
            map.put(field, this.decrypt(stringValue));
        } catch (Exception e) {
            this.applicationLogger.warn(CryptoManager.class, "An error occurred while decrypting field: " + e.getMessage());
        }
    }

    private boolean isBase64(String value) {
        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
