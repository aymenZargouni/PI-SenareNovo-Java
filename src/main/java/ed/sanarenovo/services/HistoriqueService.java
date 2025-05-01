package ed.sanarenovo.services;

import ed.sanarenovo.entities.Equipment;
import ed.sanarenovo.entities.Historique;
import ed.sanarenovo.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;

public class HistoriqueService {

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
            SELECT h.*, e.name, e.model
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
        String sql = """
            SELECT h.*, e.name, e.model
            FROM historique h
            JOIN equipment e ON h.equipment_id = e.id
            WHERE h.id = ?
        """;

        try (Connection conn = MyConnection.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, historiqueId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String rapportDetaille = rs.getString("rapport_detaille");
                String equipmentName = rs.getString("name");
                String equipmentModel = rs.getString("model");
                Date dateReparation = rs.getDate("date_reparation");

                // Générer le contenu du rapport
                String content = "Rapport de Réparation\n";
                content += "----------------------------\n";
                content += "Équipement: " + equipmentName + "\n";
                content += "Modèle: " + equipmentModel + "\n";
                content += "Date de Réparation: " + dateReparation + "\n";
                content += "Rapport Détaille: \n" + rapportDetaille + "\n";

                // Sauvegarder dans un fichier texte
                String fileName = "rapport_reparation_" + historiqueId + ".txt";
                File reportFile = new File(fileName);
                try (FileWriter writer = new FileWriter(reportFile)) {
                    writer.write(content);
                    System.out.println("Rapport généré avec succès dans le fichier : " + fileName);
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'écriture du fichier rapport : " + e.getMessage());
                }
            } else {
                System.out.println("Aucun rapport trouvé pour l'historique ID: " + historiqueId);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du rapport : " + e.getMessage());
        }
    }
}
