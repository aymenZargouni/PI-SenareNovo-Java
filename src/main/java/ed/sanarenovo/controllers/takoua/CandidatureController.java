package ed.sanarenovo.controllers.takoua;

import ed.sanarenovo.entities.Candidature;
import ed.sanarenovo.services.CandidatureService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Date;

public class CandidatureController {
    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextArea txtCV;
    @FXML
    private TextArea txtLettreMotivation;
    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;

    private int offreId;
    private CandidatureService candidatureService = new CandidatureService();

    public void setOffreId(int offreId) {
        this.offreId = offreId;
    }

    @FXML
    private void handleSubmit() {
        if (validateInputs()) {
            Candidature candidature = new Candidature();
            candidature.setNom(txtNom.getText());
            candidature.setPrenom(txtPrenom.getText());
            candidature.setEmail(txtEmail.getText());
            candidature.setCv(txtCV.getText());
            candidature.setLettreMotivation(txtLettreMotivation.getText());
            candidature.setDateCandidature(new Date());
            candidature.setStatut("En attente");
            candidature.setOffreId(offreId);

            candidatureService.addCandidature(candidature);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Votre candidature a été soumise avec succès!");
            alert.showAndWait();

            closeWindow();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        String errorMessage = "";

        if (txtNom.getText() == null || txtNom.getText().isEmpty()) {
            errorMessage += "Nom est obligatoire!\n";
        }

        if (txtPrenom.getText() == null || txtPrenom.getText().isEmpty()) {
            errorMessage += "Prénom est obligatoire!\n";
        }

        if (txtEmail.getText() == null || txtEmail.getText().isEmpty() || !txtEmail.getText().contains("@")) {
            errorMessage += "Email valide est obligatoire!\n";
        }

        if (txtCV.getText() == null || txtCV.getText().isEmpty()) {
            errorMessage += "CV est obligatoire!\n";
        }

        if (txtLettreMotivation.getText() == null || txtLettreMotivation.getText().isEmpty()) {
            errorMessage += "Lettre de motivation est obligatoire!\n";
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