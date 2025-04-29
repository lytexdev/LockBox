import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PasswordManager {
    private List<Account> accounts;
    private String masterPassword;
    private CryptoUtils cryptoUtils;

    public PasswordManager(String masterPassword) {
        this.masterPassword = masterPassword;
        this.accounts = new ArrayList<>();
        this.cryptoUtils = new CryptoUtils(masterPassword);
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public String generatePassword(int length) {
        if (length < 8) {
            length = 8;
        }

        String uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercaseChars = "abcdefghijklmnopqrstuvwxyz";
        String numberChars = "0123456789";
        String specialChars = "!@#$%^&*()_-+=<>?";
        String allChars = uppercaseChars + lowercaseChars + numberChars + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each group
        password.append(uppercaseChars.charAt(random.nextInt(uppercaseChars.length())));
        password.append(lowercaseChars.charAt(random.nextInt(lowercaseChars.length())));
        password.append(numberChars.charAt(random.nextInt(numberChars.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Fill the rest with random characters
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password characters
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = passwordArray[index];
            passwordArray[index] = passwordArray[i];
            passwordArray[i] = temp;
        }

        return new String(passwordArray);
    }

    public void saveToFile(String filename) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Account account : accounts) {
                String plainText = account.toDataString();
                String encrypted = cryptoUtils.encrypt(plainText);
                writer.write(encrypted);
                writer.newLine();
            }
        }
    }

    public void loadFromFile(String filename) throws Exception {
        accounts.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String decrypted = cryptoUtils.decrypt(line);
                    Account account = Account.fromString(decrypted);
                    accounts.add(account);
                } catch (Exception e) {
                    throw new Exception("Failed to decrypt data. Incorrect master password or corrupted file.");
                }
            }
            
            // Sort accounts by website name for better organization
            sortAccountsByWebsite();
        } catch (IOException e) {
            throw new Exception("Error reading file: " + e.getMessage());
        }
    }
    
    public void sortAccountsByWebsite() {
        Collections.sort(accounts, Comparator.comparing(Account::getWebsite, String.CASE_INSENSITIVE_ORDER));
    }
    
    public List<Account> searchAccounts(String searchTerm) {
        List<Account> results = new ArrayList<>();
        String lowerSearchTerm = searchTerm.toLowerCase();
        
        for (Account account : accounts) {
            if (account.getWebsite().toLowerCase().contains(lowerSearchTerm) ||
                account.getUsername().toLowerCase().contains(lowerSearchTerm)) {
                results.add(account);
            }
        }
        
        return results;
    }
}