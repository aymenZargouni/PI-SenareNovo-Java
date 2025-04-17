package ed.sanarenovo.services;

import ed.sanarenovo.entities.Technicien;
import ed.sanarenovo.utils.MyConnection;

import ed.sanarenovo.entities.Medecin;
import ed.sanarenovo.entities.Technicien;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.interfaces.IUserService;
import ed.sanarenovo.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TechnicienService implements IUserService<Technicien> {
    UserService userService = new UserService();
    @Override
    public void add(Technicien technicien) {
        String queryUser = "INSERT INTO user (email, password, roles, is_blocked) VALUES (?, ?, ?, ?)";
        String queryTechnicien = "INSERT INTO technicien (nom, phone_number, user_id) VALUES (?, ?, ?)";

        try {
            // Insert user
            PreparedStatement pstUser = MyConnection.getInstance().getCnx().prepareStatement(queryUser, Statement.RETURN_GENERATED_KEYS);
            pstUser.setString(1, technicien.getUser().getEmail());
            pstUser.setString(2, technicien.getUser().getPassword());
            pstUser.setString(3, "[\"ROLE_TECHNICIEN\"]");
            pstUser.setBoolean(4, false);
            pstUser.executeUpdate();

            ResultSet rs = pstUser.getGeneratedKeys();
            int userId = -1;
            if (rs.next()) {
                userId = rs.getInt(1);
            }

            // Insert technicien
            PreparedStatement pstTech = MyConnection.getInstance().getCnx().prepareStatement(queryTechnicien);
            pstTech.setString(1, technicien.getNom());
            pstTech.setString(2, technicien.getPhoneNumber());
            pstTech.setInt(3, userId);
            pstTech.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du technicien : " + e.getMessage());
        }
    }

    @Override
    public void update(Technicien technicien, int id) {

        String queryUser = "UPDATE user SET email = ?, password = ? WHERE id = ?";
        String queryTech = "UPDATE technicien SET nom = ?, phone_number = ? WHERE id = ?";

        try {
            PreparedStatement pstUser = MyConnection.getInstance().getCnx().prepareStatement(queryUser);
            pstUser.setString(1, technicien.getUser().getEmail());
            pstUser.setString(2, technicien.getUser().getPassword());
            pstUser.setInt(3, technicien.getUser().getId());
            pstUser.executeUpdate();

            PreparedStatement pstTech = MyConnection.getInstance().getCnx().prepareStatement(queryTech);
            pstTech.setString(1, technicien.getNom());
            pstTech.setString(2, technicien.getPhoneNumber());
            pstTech.setInt(3, technicien.getId());
            pstTech.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du technicien : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        Technicien technicien = getById(id);
        String query = "DELETE FROM technicien WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, id);
            pst.executeUpdate();
            userService.delete(technicien.getUser().getId());
            System.out.println("Médecin supprimé avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du technicien : " + e.getMessage());
        }
    }

    @Override
    public Technicien getById(int id) {
        Technicien t = null;
        String query = "SELECT t.*, u.* FROM technicien t JOIN user u ON t.user_id = u.id WHERE t.id = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("u.id"),
                        rs.getString("u.email"),
                        rs.getString("u.password"),
                        rs.getString("u.roles"),
                        rs.getBoolean("u.is_blocked")
                );

                t = new Technicien(
                        rs.getInt("t.id"),
                        rs.getString("t.nom"),
                        rs.getString("t.phone_number"),
                        user
                );
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du technicien : " + e.getMessage());
        }

        return t;
    }


    @Override
    public List<Technicien> getAll() {
        List<Technicien> list = new ArrayList<>();
        String query = "SELECT t.*, u.* FROM technicien t JOIN user u ON t.user_id = u.id";

        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                User user = new User(
                        rs.getInt("u.id"),
                        rs.getString("u.email"),
                        rs.getString("u.password"),
                        rs.getString("u.roles"),
                        rs.getBoolean("u.is_blocked")
                );

                Technicien t = new Technicien(
                        rs.getInt("t.id"),
                        rs.getString("t.nom"),
                        rs.getString("t.phone_number"),
                        user
                );
                list.add(t);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des techniciens : " + e.getMessage());
        }

        return list;    }
  
      public List<Technicien> getAllTechniciens() {
        List<Technicien> techniciens = new ArrayList<>();
        String query = "SELECT id, nom, phone_number FROM technicien";
        try (Connection conn = MyConnection.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Technicien t = new Technicien();
                t.setId(rs.getInt("id"));
                t.setNom(rs.getString("nom"));
                t.setPhoneNumber(rs.getString("phone_number"));
                techniciens.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur DB: " + e.getMessage());
        }
        return techniciens;
    }

    public int getTechnicienIdByUserId(int userId) {
        int technicienId = -1;
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            String query = "SELECT id FROM technicien WHERE user_id = ?";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                technicienId = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return technicienId;
    }
}
