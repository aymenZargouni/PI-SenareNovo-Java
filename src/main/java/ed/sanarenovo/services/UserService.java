package ed.sanarenovo.services;

import ed.sanarenovo.entities.User;
import ed.sanarenovo.interfaces.IUserService;
import ed.sanarenovo.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IUserService<User> {
    @Override
    public void add(User user) {
        String query = "INSERT INTO user (email, password, roles, is_blocked, reset_token, reset_token_expires_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query)) {
            pst.setString(1, user.getEmail());
            pst.setString(2, user.getPassword());
            pst.setString(3, user.getRoles());
            pst.setBoolean(4, user.isBlocked());
            pst.setString(5, user.getResetToken());
            pst.setTimestamp(6, user.getResetTokenExpiresAt() != null ? Timestamp.valueOf(user.getResetTokenExpiresAt()) : null);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(User user,int id) {
        String sql = "UPDATE user SET email = ?, password = ?, roles = ?, is_blocked = ?, reset_token = ?, reset_token_expires_at = ? WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setString(1, user.getEmail());
            pst.setString(2, user.getPassword());
            pst.setString(3, String.join(",", user.getRoles()));
            pst.setBoolean(4, user.isBlocked());
            pst.setString(5, user.getResetToken());
            pst.setObject(6, user.getResetTokenExpiresAt());
            pst.setInt(7,id);
            pst.executeUpdate();
            System.out.println("User mis à jour");
        } catch (SQLException e) {
            System.out.println("Erreur de mise à jour: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM user WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("User supprimé");
        } catch (SQLException e) {
            System.out.println("Erreur de suppression: " + e.getMessage());
        }
    }

    @Override
    public User getById(int id) {
        return null;
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRoles(rs.getString("roles"));
                u.setBlocked(rs.getBoolean("is_blocked"));
                u.setResetToken(rs.getString("reset_token"));
                Timestamp ts = rs.getTimestamp("reset_token_expires_at");
                if (ts != null) {
                    u.setResetTokenExpiresAt(String.valueOf(ts.toLocalDateTime()));
                }
                users.add(u);
            }
        } catch (SQLException e) {
            System.out.println("Erreur de récupération: " + e.getMessage());
        }
        return users;
    }

    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isBlocked(String email) {
        String query = "SELECT is_blocked FROM user WHERE email = ?";
        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_blocked");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

