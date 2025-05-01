package ed.sanarenovo.services;

import ed.sanarenovo.entities.Equipment;
import ed.sanarenovo.entities.Historique;
import ed.sanarenovo.utils.MyConnection;
import ed.sanarenovo.utils.ReportGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueService {

    private final ReportGenerator reportGenerator = new ReportGenerator();

    // Insère les équipements avec des infos valides dans la table historique
    public void syncHistoriqueFromEquipment() {
        String sql = """
            INSERT INTO historique (equipment_id, date_reparation, rapport_detaille)
            SELECT id, date_reparation, rapport_detaille
            FROM equipment
            WHERE date_reparation IS NOT NULL
              AND rapport_detaille IS NOT NULL
              AND id NOT IN (SELECT equipment_id FROM historique)
        """;

        try (Connection conn = MyConnection.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int rows = stmt.executeUpdate();
            System.out.println(rows + " lignes insérées dans l'historique.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la synchronisation de l'historique : " + e.getMessage());
        }
    }

    // Récupère tous les historiques (avec info équipement)
    public List<Historique> getAllHistoriques() {
        List<Historique> historiques = new ArrayList<>();
        String sql = """
            SELECT h.*, e.name, e.model, e.status
            FROM historique h
            JOIN equipment e ON h.equipment_id = e.id
            ORDER BY h.date_reparation DESC
        """;

        try (Connection conn = MyConnection.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Equipment eq = new Equipment();
                eq.setId(rs.getInt("equipment_id"));
                eq.setName(rs.getString("name"));
                eq.setModel(rs.getString("model"));
                eq.setStatus(rs.getString("status"));

                Historique h = new Historique();
                h.setId(rs.getInt("id"));
                h.setDateReparation(rs.getDate("date_reparation"));
                h.setRapportDetaille(rs.getString("rapport_detaille"));
                h.setEquipment(eq);

                historiques.add(h);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération historique : " + e.getMessage());
        }

        return historiques;
    }

    public void generateReport(int historiqueId) {
        // Utilisation de la classe ReportGenerator pour générer un PDF
        reportGenerator.generateReport(historiqueId);
    }
}