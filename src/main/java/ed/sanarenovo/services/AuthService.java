package ed.sanarenovo.services;

import ed.sanarenovo.entities.User;
import ed.sanarenovo.utils.MyConnection;
import ed.sanarenovo.utils.PasswordHasher;
import ed.sanarenovo.utils.UserSession;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {


    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }

        String req = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(req)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                if (rs.getBoolean("is_blocked")) {
                    System.out.println("blocked");
                    return null;
                }

                String storedPass = rs.getString("password");
                if (PasswordHasher.checkPassword(password, storedPass)) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(storedPass);
                    u.setRoles(rs.getString("roles"));
                    UserSession.startSession(u);
                    return u;
                }
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }
}
