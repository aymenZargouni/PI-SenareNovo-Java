package ed.sanarenovo.controllers;

import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.Medecin;
import ed.sanarenovo.entities.Technicien;
import ed.sanarenovo.services.TechnicienService;
import ed.sanarenovo.utils.PasswordHasher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditTechnicienController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button canceladdbtn;

    @FXML
    private Button modiftechbtn;

    @FXML
    private TextField tfemail;

    @FXML
    private TextField tfnom;

    @FXML
    private TextField tfnumtel;

    @FXML
    private PasswordField tfpwd;

    private Technicien selectedTechnicien;

    private ShowTechnicienController controllerRef;

    public void setControllerRef(ShowTechnicienController controllerRef) {
        this.controllerRef = controllerRef;
    }
    public void setTechnicien(Technicien technicien) {
        this.selectedTechnicien = technicien;


        tfnom.setText(technicien.getNom());
        tfemail.setText(technicien.getUser().getEmail());
        tfpwd.setText(technicien.getUser().getPassword());
        tfnumtel.setText(technicien.getPhoneNumber());
    }

    @FXML
    void cancelModif(ActionEvent event) {
        Stage stage = (Stage) canceladdbtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    void modifTech(ActionEvent event) {
        if (selectedTechnicien == null) return;

        String newNom = tfnom.getText().trim();
        String newEmail = tfemail.getText().trim();
        String newPwd = tfpwd.getText().trim();
        String newPhone = tfnumtel.getText().trim();

        // === Validation ===
        if (newNom.isEmpty() || newEmail.isEmpty() || newPwd.isEmpty() || newPhone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        if (!newEmail.matches("^\\S+@\\S+\\.\\S+$")) {
            showAlert(Alert.AlertType.ERROR, "Email invalide", "Veuillez entrer une adresse e-mail valide.");
            return;
        }

        if (newPwd.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Mot de passe trop court", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        // === Update Entities ===
        selectedTechnicien.setNom(newNom);
        selectedTechnicien.setPhoneNumber(newPhone);
        selectedTechnicien.getUser().setEmail(newEmail);

        String passwordInputHashed = PasswordHasher.hashPassword(newPwd);
        if (!PasswordHasher.checkPassword(selectedTechnicien.getUser().getPassword(), passwordInputHashed)) {
            selectedTechnicien.getUser().setPassword(passwordInputHashed);
        }
        TechnicienService technicienService = new TechnicienService();
        technicienService.update(selectedTechnicien, selectedTechnicien.getId());

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Technicien modifié avec succès !");

        if (controllerRef != null) {
            controllerRef.loadTechniciens();
        }

        // Close the window
        ((Stage) modiftechbtn.getScene().getWindow()).close();
    }

    @FXML
    void initialize() {
        assert canceladdbtn != null : "fx:id=\"canceladdbtn\" was not injected: check your FXML file 'EditTechnicien.fxml'.";
        assert modiftechbtn != null : "fx:id=\"modiftechbtn\" was not injected: check your FXML file 'EditTechnicien.fxml'.";
        assert tfemail != null : "fx:id=\"tfemail\" was not injected: check your FXML file 'EditTechnicien.fxml'.";
        assert tfnom != null : "fx:id=\"tfnom\" was not injected: check your FXML file 'EditTechnicien.fxml'.";
        assert tfnumtel != null : "fx:id=\"tfnumtel\" was not injected: check your FXML file 'EditTechnicien.fxml'.";
        assert tfpwd != null : "fx:id=\"tfpwd\" was not injected: check your FXML file 'EditTechnicien.fxml'.";

    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
