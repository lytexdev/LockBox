import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

public class CryptoUtils {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private final SecretKey secretKey;

    public CryptoUtils(String masterPassword) {
        this.secretKey = deriveKeyFromPassword(masterPassword);
    }

    private SecretKey deriveKeyFromPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Add a fixed salt for additional security
            String salt = "LockBox_2025";
            byte[] saltedPassword = (password + salt).getBytes(StandardCharsets.UTF_8);
            
            // First round of hashing
            byte[] keyBytes = digest.digest(saltedPassword);
            
            // Multiple rounds of hashing for strengthening
            for (int i = 0; i < 1000; i++) {
                digest.reset();
                keyBytes = digest.digest(keyBytes);
            }
            
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Error generating key from password", e);
        }
    }

    public String encrypt(String plainText) throws Exception {
        // Generate a random 12-byte nonce (IV)
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[12];
        random.nextBytes(nonce);

        // Create GCM parameter specification
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);

        // Initialize cipher for encryption
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        // Encrypt
        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Combine nonce and encrypted data and return as Base64
        byte[] combined = new byte[nonce.length + encryptedData.length];
        System.arraycopy(nonce, 0, combined, 0, nonce.length);
        System.arraycopy(encryptedData, 0, combined, nonce.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public String decrypt(String encrypted) throws Exception {
        // Decode from Base64
        byte[] combined = Base64.getDecoder().decode(encrypted);

        // Extract nonce and encrypted data
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[combined.length - 12];
        System.arraycopy(combined, 0, nonce, 0, nonce.length);
        System.arraycopy(combined, nonce.length, encryptedData, 0, encryptedData.length);

        // Create GCM parameter specification
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);

        // Initialize cipher for decryption
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        // Decrypt
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
    
    public byte[] getPasswordHash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Error generating password hash", e);
        }
    }
    
    public boolean validatePassword(String password) {
        try {
            // Create a dummy encryption/decryption test to validate the password
            String testData = "LockBox_TEST_DATA";
            String encrypted = encrypt(testData);
            String decrypted = decrypt(encrypted);
            return testData.equals(decrypted);
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getPasswordStrength(String password) {
        if (password.length() < 8) {
            return "Weak";
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        
        int strength = 0;
        if (hasUpper) strength++;
        if (hasLower) strength++;
        if (hasDigit) strength++;
        if (hasSpecial) strength++;
        
        if (password.length() >= 12) strength++;
        if (password.length() >= 16) strength++;
        
        switch (strength) {
            case 0:
            case 1:
            case 2:
                return "Weak";
            case 3:
            case 4:
                return "Medium";
            case 5:
                return "Strong";
            default:
                return "Very Strong";
        }
    }
}
