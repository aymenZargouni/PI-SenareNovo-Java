package ed.sanarenovo.controllers.takoua;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    private Button btnCandidat;
    @FXML
    private Button btnRH;

    @FXML
    private void handleCandidat() {
        System.out.println("==> Bouton RH cliqué"); // <-- test

        try {
            // Charger la vue des offres
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/takoua_views/offres_view.fxml"));
            Parent root = loader.load();

            // Configurer la nouvelle scène
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/takoua_views/style.css").toExternalForm());

            // Configurer la fenêtre
            Stage stage = (Stage) btnCandidat.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Liste des Offres - Espace Candidat");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface candidat");
        }
    }

    @FXML
    private void handleRH() {
        System.out.println("==> Bouton RH cliqué"); // <-- test

        try {
            // Charger le dashboard RH
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/takoua_views/rh_dashboard.fxml"));
            Parent root = loader.load();

            // Configurer la nouvelle scène
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/takoua_views/style.css").toExternalForm());

            // Configurer la fenêtre
            Stage stage = (Stage) btnRH.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard RH");
            stage.setMaximized(true); // Maximiser pour le dashboard
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface RH");
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}