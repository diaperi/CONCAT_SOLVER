package concat.SolverWeb.user.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String encrypt(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}


