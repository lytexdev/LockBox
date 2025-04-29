public class Account {
    private String website;
    private String username;
    private String password;

    public Account(String website, String username, String password) {
        this.website = website;
        this.username = username;
        this.password = password;
    }

    public String getWebsite() {
        return website;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return website;
    }
    
    public String toDataString() {
        return website + "," + username + "," + password;
    }

    public static Account fromString(String data) {
        String[] parts = data.split(",", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid account data format");
        }
        return new Account(parts[0], parts[1], parts[2]);
    }
}
