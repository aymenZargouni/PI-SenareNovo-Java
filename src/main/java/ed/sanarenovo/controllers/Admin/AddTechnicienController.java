package ed.sanarenovo.controllers.Admin;

import java.net.URL;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.Technicien;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.TechnicienService;
import ed.sanarenovo.utils.PasswordHasher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddTechnicienController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button adduserbtn;

    @FXML
    private Button canceladdbtn;

    @FXML
    private TextField tfemail;

    @FXML
    private TextField tfnom;

    @FXML
    private TextField tfnumtel;

    @FXML
    private PasswordField tfpwd;

    private ShowTechnicienController controllerRef;

    public void setControllerRef(ShowTechnicienController controllerRef) {
        this.controllerRef = controllerRef;
    }

    @FXML
    void addTech(ActionEvent event) {
        String email = tfemail.getText().trim();
        String password = tfpwd.getText().trim();
        String nom = tfnom.getText().trim();
        String phone = tfnumtel.getText().trim();

        // === Validation ===
        if (email.isEmpty() || password.isEmpty() || nom.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            showAlert(Alert.AlertType.ERROR, "Email invalide", "Veuillez entrer une adresse e-mail valide.");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Mot de passe trop court", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }


        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordHasher.hashPassword(password));
        user.setRoles("[\"ROLE_TECHNICIEN\"]");
        user.setBlocked(false);

        Technicien tech = new Technicien();
        tech.setNom(nom);
        tech.setPhoneNumber(phone);
        tech.setUser(user);

        TechnicienService technicienService = new TechnicienService();
        technicienService.add(tech);

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Technicien ajouté avec succès !");

        if (controllerRef != null) {
            controllerRef.loadTechniciens(); // Refresh the table if the method exists
        }

        // Close the window
        Stage stage = (Stage) adduserbtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    void cancelAdd(ActionEvent event) {
        ((Stage) canceladdbtn.getScene().getWindow()).close();
    }

    @FXML
    void initialize() {
        assert adduserbtn != null : "fx:id=\"adduserbtn\" was not injected: check your FXML file 'AddTechnicien.fxml'.";
        assert canceladdbtn != null : "fx:id=\"canceladdbtn\" was not injected: check your FXML file 'AddTechnicien.fxml'.";
        assert tfemail != null : "fx:id=\"tfemail\" was not injected: check your FXML file 'AddTechnicien.fxml'.";
        assert tfnom != null : "fx:id=\"tfnom\" was not injected: check your FXML file 'AddTechnicien.fxml'.";
        assert tfnumtel != null : "fx:id=\"tfnumtel\" was not injected: check your FXML file 'AddTechnicien.fxml'.";
        assert tfpwd != null : "fx:id=\"tfpwd\" was not injected: check your FXML file 'AddTechnicien.fxml'.";

    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}

