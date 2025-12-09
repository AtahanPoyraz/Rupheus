package ai.rupheus.application.component;

import ai.rupheus.application.config.logger.ApplicationLogger;
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

@Component
public class CryptoUtil {
    @Value("${security.crypto.master_key}")
    private String masterKey;

    private SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private final ApplicationLogger applicationLogger;

    @Autowired
    public CryptoUtil(
            ApplicationLogger applicationLogger
    ) {
        this.applicationLogger = applicationLogger;
    }

    @PostConstruct
    private void init() {
        byte[] keyBytes = Base64.getDecoder().decode(this.masterKey);

        if (keyBytes.length != 32) {
            this.applicationLogger.warn(CryptoUtil.class, "An error occurred while initializing crypto util");
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
            this.applicationLogger.warn(CryptoUtil.class, "An error occurred while encrypting plain text: " + e.getMessage());
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
            this.applicationLogger.warn(CryptoUtil.class, "An error occurred while decrypting encrypted: " + e.getMessage());
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}
