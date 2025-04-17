package ed.sanarenovo.services;

import ed.sanarenovo.entities.Technicien;
import ed.sanarenovo.utils.MyConnection;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TechnicienService {

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
}