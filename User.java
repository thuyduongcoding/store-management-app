

public class User {
    private int userId;
    private String username;
    private String password;
    private String role; // e.g., "customer", "cashier", "manager"

    public User(int userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
}
