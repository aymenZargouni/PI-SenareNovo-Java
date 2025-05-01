package ed.sanarenovo.services;

import ed.sanarenovo.entities.Equipment;
import ed.sanarenovo.interfaces.IService;
import ed.sanarenovo.utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentService implements IService<Equipment> {

    @Override
    public void addEntity(Equipment equipment) {
        try {
            String requete = "INSERT INTO equipment (name, model, status, date_achat, prix) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = MyConnection.getInstance().getCnx()
                    .prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, equipment.getName());
            pst.setString(2, equipment.getModel());
            pst.setString(3, equipment.getStatus()); // "reparé" par défaut
            pst.setDate(4, equipment.getDateAchat());
            pst.setDouble(5, equipment.getPrix());

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    equipment.setId(generatedKeys.getInt(1));
                }
                System.out.println("Equipment added successfully with ID: " + equipment.getId());
            } else {
                System.out.println("Aucune ligne insérée.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding equipment: " + e.getMessage());
        }
    }

    @Override
    public void updateEntity(Equipment equipment, int id) {
        try {
            String requete = "UPDATE equipment SET " +
                    "id = ?, " +
                    "name = ?, " +
                    "model = ?, " +
                    "status = ?, " +
                    "date_achat = ?, " +
                    "prix = ? " +
                    "WHERE id = ?";

            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);

            pst.setInt(1, equipment.getId());  // Remplacer 'equipment_id' si nécessaire
            pst.setString(2, equipment.getName());
            pst.setString(3, equipment.getModel());
            pst.setString(4, equipment.getStatus());
            pst.setDate(5, equipment.getDateAchat());
            pst.setDouble(6, equipment.getPrix());
            pst.setInt(7, id);
            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Equipment updated successfully");
            } else {
                System.out.println("No equipment found with the specified ID");
            }

        } catch (SQLException e) {
            System.out.println("Error updating equipment: " + e.getMessage());
        }
    }

    public void deleteEntity(int id) {
        try {
            String requete = "DELETE FROM equipment WHERE id = ?";

            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);

            pst.setInt(1, id);

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Equipment deleted successfully");
            } else {
                System.out.println("No equipment found with the specified ID");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting equipment: " + e.getMessage());
        }
    }

    public List<Equipment> getAll() {
        List<Equipment> equipementsList = new ArrayList<>();

        try {
            String requete = "SELECT * FROM equipment";

            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Equipment equipment = new Equipment();
                equipment.setId(rs.getInt("id"));
                equipment.setName(rs.getString("name"));
                equipment.setModel(rs.getString("model"));
                equipment.setStatus(rs.getString("status"));
                equipment.setDateAchat(rs.getDate("date_achat"));
                equipment.setPrix(rs.getDouble("prix"));

                equipementsList.add(equipment);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving all equipment: " + e.getMessage());
        }

        return equipementsList;

    }

}