public class AuthService {
    public static User login(String username, String password) {
        User foundUser = DataStore.findUserByUsername(username);
        if (foundUser != null && foundUser.checkPassword(password)) {
            return foundUser;
        }
        return null;
    }
}
