package ed.sanarenovo.utils;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.awt.image.BufferedImage;
import java.io.File;

public class PDFPreview {

    public void showPDFPreview(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);

            // Rendre la première page avec un zoom de 100%
            BufferedImage image = renderer.renderImage(0, 1.0f);

            // Créer une fenêtre JavaFX au lieu de Swing pour une meilleure intégration
            Stage previewStage = new Stage();
            previewStage.setTitle("Aperçu du CV - " + pdfFile.getName());

            ImageView imageView = new ImageView(SwingFXUtils.toFXImage(image, null));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(800);

            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);

            Scene scene = new Scene(scrollPane, 800, 600);
            previewStage.setScene(scene);
            previewStage.show();

        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Impossible d'afficher l'aperçu du PDF");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        }
    }

}
