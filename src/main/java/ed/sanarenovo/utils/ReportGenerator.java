package ed.sanarenovo.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import ed.sanarenovo.entities.Equipment;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;



import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class ReportGenerator {

    // Couleurs personnalisées
    private static final BaseColor PRIMARY_COLOR = new BaseColor(44, 62, 80);
    private static final BaseColor SECONDARY_COLOR = new BaseColor(52, 152, 219);
    private static final BaseColor SUCCESS_COLOR = new BaseColor(46, 204, 113);
    private static final BaseColor WARNING_COLOR = new BaseColor(241, 196, 15);
    private static final BaseColor DANGER_COLOR = new BaseColor(231, 76, 60);

    public void generateReport(int historiqueId) {
        Equipment equipment = fetchEquipmentData(historiqueId);

        if (equipment == null) {
            JOptionPane.showMessageDialog(null,
                    "Équipement non trouvé pour l'ID : " + historiqueId,
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Choisir emplacement de sauvegarde
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le rapport PDF");
        fileChooser.setSelectedFile(new File("rapport_reparation_" + historiqueId + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("Opération annulée.");
            return;
        }

        File file = fileChooser.getSelectedFile();

        try {
            // Génération du PDF
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            addLogo(document);
            addTitle(document, "Rapport de Réparation");
            addSubTitle(document, "Détails de l'Équipement");

            PdfPTable table = createInfoTable(equipment);
            document.add(table);

            addSubTitle(document, "Rapport détaillé de la réparation");
            addDetailedReport(document, equipment.getRapportDetaille());

            addFooter(document);

            document.close();
            System.out.println("Rapport généré avec succès : " + file.getAbsolutePath());

            // Afficher l'aperçu dans une fenêtre Swing
            showPDFPreview(file);

            JOptionPane.showMessageDialog(null,
                    "Rapport généré avec succès !\nEmplacement : " + file.getAbsolutePath(),
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du rapport PDF : " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Erreur lors de la génération du rapport : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showPDFPreview(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);

            // Rendre la première page avec un zoom de 100%
            BufferedImage image = renderer.renderImage(0, 1.0f);

            // Créer une fenêtre Swing pour l'aperçu
            JFrame previewFrame = new JFrame("Aperçu du Rapport - " + pdfFile.getName());
            previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            previewFrame.setSize(800, 600);

            JLabel imageLabel = new JLabel(new ImageIcon(image));
            JScrollPane scrollPane = new JScrollPane(imageLabel);
            previewFrame.add(scrollPane);

            previewFrame.setLocationRelativeTo(null); // Centrer la fenêtre
            previewFrame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Impossible d'afficher l'aperçu du PDF : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Equipment fetchEquipmentData(int historiqueId) {
        String query = "SELECT e.id, e.name, e.model, e.prix, e.date_achat, e.date_reparation, e.status, e.rapport_detaille " +
                "FROM equipment e JOIN historique h ON e.id = h.equipment_id WHERE h.id = ?";

        try (Connection connection = MyConnection.getInstance().getCnx();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, historiqueId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Equipment equipment = new Equipment();
                equipment.setId(rs.getInt("id"));
                equipment.setName(rs.getString("name"));
                equipment.setModel(rs.getString("model"));
                equipment.setPrix(rs.getDouble("prix"));
                equipment.setDateAchat(rs.getDate("date_achat"));
                equipment.setDateReparation(rs.getDate("date_reparation"));
                equipment.setStatus(rs.getString("status"));
                equipment.setRapportDetaille(rs.getString("rapport_detaille"));
                return equipment;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'équipement : " + e.getMessage());
        }
        return null;
    }

    private void addLogo(Document document) {
        try {
            Image logo = Image.getInstance(getClass().getResource("/Sabrineviews/icons/report_logo.png"));
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception e) {
            System.out.println("Logo non trouvé, génération sans logo.");
        }
    }

    private void addTitle(Document document, String text) throws DocumentException {
        Paragraph title = new Paragraph(text, new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, PRIMARY_COLOR));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
    }

    private void addSubTitle(Document document, String text) throws DocumentException {
        Paragraph subTitle = new Paragraph(text, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, SECONDARY_COLOR));
        subTitle.setSpacingAfter(15);
        document.add(subTitle);
    }

    private PdfPTable createInfoTable(Equipment equipment) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        addTableHeader(table, "Champ", "Valeur");

        addTableRow(table, "ID", String.valueOf(equipment.getId()));
        addTableRow(table, "Nom", equipment.getName());
        addTableRow(table, "Modèle", equipment.getModel());
        addTableRow(table, "Prix", String.format("%.2f €", equipment.getPrix()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        addTableRow(table, "Date d'achat", equipment.getDateAchat().toLocalDate().format(formatter));

        if (equipment.getDateReparation() != null) {
            addTableRow(table, "Date de réparation", equipment.getDateReparation().toLocalDate().format(formatter));
        }

        PdfPCell statusCell = new PdfPCell(new Phrase(equipment.getStatus(),
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, getStatusColor(equipment.getStatus()))));
        table.addCell(new Phrase("Statut", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        table.addCell(statusCell);

        return table;
    }

    private void addTableHeader(PdfPTable table, String header1, String header2) {
        PdfPCell cell1 = new PdfPCell(new Phrase(header1, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
        cell1.setBackgroundColor(PRIMARY_COLOR);

        PdfPCell cell2 = new PdfPCell(new Phrase(header2, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
        cell2.setBackgroundColor(PRIMARY_COLOR);

        table.addCell(cell1);
        table.addCell(cell2);
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        table.addCell(new Phrase(label, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        table.addCell(new Phrase(value, new Font(Font.FontFamily.HELVETICA, 12)));
    }

    private void addDetailedReport(Document document, String detailsText) throws DocumentException {
        Paragraph details = new Paragraph(detailsText, new Font(Font.FontFamily.HELVETICA, 12));
        details.setSpacingAfter(20);
        document.add(details);
    }

    private void addFooter(Document document) throws DocumentException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Paragraph footer = new Paragraph("Généré le " + java.time.LocalDate.now().format(formatter),
                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
    }

    private BaseColor getStatusColor(String status) {
        if (status == null) return BaseColor.BLACK;

        switch (status.toLowerCase()) {
            case "reparé":
                return SUCCESS_COLOR;
            case "panne":
                return DANGER_COLOR;
            case "maintenance":
                return WARNING_COLOR;
            default:
                return BaseColor.BLACK;
        }
    }
}