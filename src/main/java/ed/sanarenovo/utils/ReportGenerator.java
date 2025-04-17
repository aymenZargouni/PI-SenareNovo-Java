package ed.sanarenovo.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import ed.sanarenovo.entities.Equipment;



import java.io.FileOutputStream;
import java.sql.*;


public class ReportGenerator  {

    public void generateReport(int historiqueId) {
        // Requête SQL pour récupérer les informations de l'équipement à partir de l'historique
        String query = "SELECT e.id, e.name, e.model, e.prix, e.date_achat, e.date_reparation, e.status, e.rapport_detaille " +
                "FROM equipment e " +
                "JOIN historique h ON e.id = h.equipment_id " +
                "WHERE h.id = ?";

        Equipment equipment = null;

        try (Connection connection = MyConnection.getInstance().getCnx();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set l'ID historique dans la requête
            statement.setInt(1, historiqueId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                // Création de l'objet Equipment avec les résultats de la requête
                equipment = new Equipment();
                equipment.setId(rs.getInt("id"));
                equipment.setName(rs.getString("name"));
                equipment.setModel(rs.getString("model"));
                equipment.setPrix(rs.getDouble("prix"));
                equipment.setDateAchat(rs.getDate("date_achat"));
                equipment.setDateReparation(rs.getDate("date_reparation"));
                equipment.setStatus(rs.getString("status"));
                equipment.setRapportDetaille(rs.getString("rapport_detaille"));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'équipement : " + e.getMessage());
        }

        if (equipment == null) {
            System.out.println("Équipement non trouvé pour l'historique ID : " + historiqueId);
            return;
        }

        // Créer le fichier PDF
        String fileName = "rapport_reparation_" + historiqueId + ".pdf";
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Ajouter un titre au document
            document.add(new Paragraph("Rapport de Réparation de l'Équipement"));
            document.add(new Paragraph("------------------------------------------------------"));

            // Ajouter les détails de l'équipement
            document.add(new Paragraph("Nom de l'équipement : " + equipment.getName()));
            document.add(new Paragraph("Modèle : " + equipment.getModel()));
            document.add(new Paragraph("Prix : " + equipment.getPrix()));
            document.add(new Paragraph("Date d'achat : " + equipment.getDateAchat()));
            document.add(new Paragraph("Date de réparation : " + equipment.getDateReparation()));
            document.add(new Paragraph("Statut : " + equipment.getStatus()));
            document.add(new Paragraph("------------------------------------------------------"));

            // Ajouter le rapport détaillé
            document.add(new Paragraph("Rapport détaillé :"));
            document.add(new Paragraph(equipment.getRapportDetaille()));
            document.add(new Paragraph("------------------------------------------------------"));

            // Fermer le document PDF
            document.close();

            System.out.println("Rapport généré avec succès à : " + fileName);
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du rapport PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }}