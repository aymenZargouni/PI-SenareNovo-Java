package ed.sanarenovo.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import ed.sanarenovo.entities.Service;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PDFGenerator {

    public static void generateUserListPDF(List<Service> users, File file) {
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            Paragraph title = new Paragraph("📋 Service List", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(4); // 4 colonnes
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 3f, 1.5f, 2f}); // ajuster largeur des colonnes

            addTableHeader(table, headerFont);
            for (Service user : users) {
                addUserRow(table, user, bodyFont);
            }

            document.add(table);
            document.close();

            // Ouvrir automatiquement le fichier PDF généré
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addTableHeader(PdfPTable table, Font font) {
        String[] headers = {"Nom", "Chef Service", "Nombre Salle", "Capacité"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
    }

    private static void addUserRow(PdfPTable table, Service user, Font font) {
        table.addCell(new Phrase(user.getNom(), font));
        table.addCell(new Phrase(user.getChef_service(), font));
        table.addCell(new Phrase(String.valueOf(user.getNbr_salle()), font));
        table.addCell(new Phrase(String.valueOf(user.getCapacite()), font));
    }
}
