package security;

import model.User;

public interface Authenticator {
    User login(String username, String password);
}
