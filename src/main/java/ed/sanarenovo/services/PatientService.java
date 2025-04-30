package ed.sanarenovo.services;

import ed.sanarenovo.entities.Patient;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.interfaces.IPatientService;
import ed.sanarenovo.utils.MyConnection;
import ed.sanarenovo.utils.PasswordHasher;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PatientService implements IPatientService {
    @Override
    public void register(Patient patient) {
        String insertUserQuery = "INSERT INTO user (email, password, roles, is_blocked) VALUES (?, ?, ?, ?)";
        String insertPatientQuery = "INSERT INTO patient (fullname, gender, adress, user_id) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement pstUser = MyConnection.getInstance().getCnx().prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS);
            User user = patient.getUser();


            String hashedPassword = PasswordHasher.hashPassword(user.getPassword());

            pstUser.setString(1, user.getEmail());
            pstUser.setString(2, hashedPassword);
            pstUser.setString(3, user.getRoles());
            pstUser.setBoolean(4, user.isBlocked());

            pstUser.executeUpdate();
            ResultSet rs = pstUser.getGeneratedKeys();
            int generatedUserId = -1;
            if (rs.next()) {
                generatedUserId = rs.getInt(1);
            }

            PreparedStatement pstPatient = MyConnection.getInstance().getCnx().prepareStatement(insertPatientQuery);
            pstPatient.setString(1, patient.getFullname());
            pstPatient.setString(2, patient.getGender());
            pstPatient.setString(3, patient.getAdress());
            pstPatient.setInt(4, generatedUserId);

            pstPatient.executeUpdate();

            System.out.println("Patient ajouté avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Patient> getAll() {
        List<Patient> list = new ArrayList<>();
        String query = "SELECT p.*, u.id as uid, u.email, u.password, u.roles, u.is_blocked " +
                "FROM patient p JOIN user u ON p.user_id = u.id";

        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Patient p = new Patient();
                p.setId(rs.getInt("p.id"));
                p.setFullname(rs.getString("p.fullname"));
                p.setGender(rs.getString("p.gender"));
                p.setAdress(rs.getString("p.adress"));

                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRoles(rs.getString("roles"));
                u.setBlocked(rs.getBoolean("is_blocked"));

                p.setUser(u);

                list.add(p);
            }
        } catch (Exception e) {
            System.out.println("Error fetching patients: " + e.getMessage());
        }

        return list;
    }

    @Override
    public Patient getById(int id) {
        Patient p = null;
        String query = "SELECT p.*, u.id as uid, u.email, u.password, u.roles, u.isBlocked " +
                "FROM patient p JOIN user u ON p.user_id = u.id WHERE p.id = ?";

        try (PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                p = new Patient();
                p.setId(rs.getInt("id"));
                p.setFullname(rs.getString("fullname"));
                p.setGender(rs.getString("gender"));
                p.setAdress(rs.getString("adress"));

                User u = new User();
                u.setId(rs.getInt("uid"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRoles(rs.getString("roles"));
                u.setBlocked(rs.getBoolean("isBlocked"));

                p.setUser(u);
            }
        } catch (Exception e) {
            System.out.println("Error fetching patient by ID: " + e.getMessage());
        }

        return p;
    }

}
