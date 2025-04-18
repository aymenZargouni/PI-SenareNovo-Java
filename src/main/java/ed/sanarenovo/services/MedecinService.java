package ed.sanarenovo.services;

import ed.sanarenovo.entities.Medecin;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.interfaces.IUserService;
import ed.sanarenovo.utils.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedecinService implements IUserService<Medecin> {

    private static UserService userService = new UserService();
    @Override
    public void add(Medecin medecin) {
        String insertUserQuery = "INSERT INTO user (email, password, roles, is_blocked) VALUES (?, ?, ?, ?)";
        String insertMedecinQuery = "INSERT INTO medecin (fullname, date_embauche, specilite, user_id) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement pstUser = MyConnection.getInstance().getCnx().prepareStatement(insertUserQuery,Statement.RETURN_GENERATED_KEYS);
            User user = medecin.getUser();

            pstUser.setString(1, user.getEmail());
            pstUser.setString(2, user.getPassword());
            pstUser.setString(3, user.getRoles());
            pstUser.setBoolean(4, user.isBlocked());

            pstUser.executeUpdate();
            ResultSet rs = pstUser.getGeneratedKeys();
            int generatedUserId = -1;
            if (rs.next()) {
                generatedUserId = rs.getInt(1);  // This is the new User ID
            }



            PreparedStatement pstMed = MyConnection.getInstance().getCnx().prepareStatement(insertMedecinQuery);
            pstMed.setString(1, medecin.getFullname());
            pstMed.setDate(2, new java.sql.Date(medecin.getDateEmbauche().getTime()));
            pstMed.setString(3, medecin.getSpecilite());
            pstMed.setInt(4, generatedUserId);

            pstMed.executeUpdate();

            System.out.println("Médecin ajouté avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Medecin medecin, int id) {
        String query = "UPDATE medecin SET fullname = ?, date_embauche = ?, specilite = ? WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, medecin.getFullname());
            pst.setDate(2, new java.sql.Date(medecin.getDateEmbauche().getTime()));
            pst.setString(3, medecin.getSpecilite());
            pst.setInt(4, medecin.getId());

            pst.executeUpdate();
            System.out.println("Médecin modifié avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        Medecin medecin = getById(id);
        String query = "DELETE FROM medecin WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, id);
            pst.executeUpdate();
            userService.delete(medecin.getUser().getId());
            System.out.println("Médecin supprimé avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Medecin getById(int id) {
        // SQL query to get Medecin based on id
        String sql = "SELECT m.id, m.fullname, m.date_embauche, m.specilite, u.id AS user_id, u.email, u.password, u.roles, u.is_blocked, u.reset_token, u.reset_token_expires_at " +
                "FROM Medecin m " +
                "JOIN User u ON m.user_id = u.id " +
                "WHERE m.id = ?";

        try (PreparedStatement stmt = MyConnection.getInstance().getCnx().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Create User object
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRoles(rs.getString("roles"));
                user.setBlocked(rs.getBoolean("is_blocked"));
                user.setResetToken(rs.getString("reset_token"));
                user.setResetTokenExpiresAt(rs.getString("reset_token_expires_at"));

                // Create Medecin object
                Medecin medecin = new Medecin();
                medecin.setId(rs.getInt("id"));
                medecin.setFullname(rs.getString("fullname"));
                medecin.setDateEmbauche(rs.getDate("date_embauche"));
                medecin.setSpecilite(rs.getString("specilite"));
                medecin.setUser(user); // Set the User object

                return medecin;
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exception appropriately
        }
        return null; // If no Medecin found with the given id
    }

    @Override
    public List<Medecin> getAll() {
        List<Medecin> list = new ArrayList<>();
        String query = "SELECT m.*, u.email, u.password, u.roles, u.is_blocked FROM medecin m JOIN user u ON m.user_id = u.id";

        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRoles(rs.getString("roles"));
                user.setBlocked(rs.getBoolean("is_blocked"));

                Medecin m = new Medecin();
                m.setId(rs.getInt("id"));
                m.setFullname(rs.getString("fullname"));
                m.setDateEmbauche(rs.getDate("date_embauche"));
                m.setSpecilite(rs.getString("specilite"));
                m.setUser(user);

                list.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
