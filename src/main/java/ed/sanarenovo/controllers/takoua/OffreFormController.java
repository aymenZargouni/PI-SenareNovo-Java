package ed.sanarenovo.controllers.takoua;

import ed.sanarenovo.entities.Offre;
import ed.sanarenovo.services.OffreService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;

public class OffreFormController {
    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtDescription;
    @FXML
    private DatePicker dateExpiration;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private OffreService offreService = new OffreService();
    private RHController rhController;
    private Offre offreToEdit;

    public void setRHController(RHController rhController) {
        this.rhController = rhController;
    }

    public void setOffreForEdit(Offre offre) {
        this.offreToEdit = offre;
        txtTitre.setText(offre.getTitre());
        txtDescription.setText(offre.getDescription());

        // ✅ Correction ici : cast vers java.sql.Date, puis conversion vers LocalDate
        if (offre.getDateExpiration() instanceof java.sql.Date) {
            java.sql.Date sqlDate = (java.sql.Date) offre.getDateExpiration();
            dateExpiration.setValue(sqlDate.toLocalDate());
        } else {
            // fallback au cas où c'est un java.util.Date (très peu probable dans ton cas)
            dateExpiration.setValue(offre.getDateExpiration().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
    }

    @FXML
    private void handleSave() {
        if (txtTitre.getText() == null || txtTitre.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Titre manquant");
            alert.setContentText("Le titre de l'offre est obligatoire.");
            alert.showAndWait();
            return; // Ne pas continuer si le titre est vide
        }

        // Autres vérifications ici
        if (validateInputs()) {
            if (offreToEdit == null) {
                // Ajout d'une nouvelle offre
                Offre nouvelleOffre = new Offre(
                        txtTitre.getText(),
                        txtDescription.getText(),
                        java.sql.Date.valueOf(dateExpiration.getValue())
                );
                offreService.addOffre(nouvelleOffre);
            } else {
                // Modification d'une offre existante
                offreToEdit.setTitre(txtTitre.getText());
                offreToEdit.setDescription(txtDescription.getText());
                offreToEdit.setDateExpiration(java.sql.Date.valueOf(dateExpiration.getValue()));
                offreService.updateOffre(offreToEdit);
            }

            rhController.refreshOffres();
            closeWindow();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        String errorMessage = "";

        if (txtTitre.getText() == null || txtTitre.getText().isEmpty()) {
            errorMessage += "Titre est obligatoire!\n";
        }

        if (txtDescription.getText() == null || txtDescription.getText().isEmpty()) {
            errorMessage += "Description est obligatoire!\n";
        }

        if (dateExpiration.getValue() == null) {
            errorMessage += "Date d'expiration est obligatoire!\n";
        } else if (dateExpiration.getValue().isBefore(LocalDate.now())) {
            errorMessage += "Date d'expiration doit être dans le futur!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes:");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
