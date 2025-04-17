package ed.sanarenovo.controllers.Admin;

import java.net.URL;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.UserService;
import ed.sanarenovo.utils.PasswordHasher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddUserController {

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
    private ChoiceBox<Boolean> tfisblocked;

    @FXML
    private PasswordField tfpwd;

    @FXML
    private ChoiceBox<String> tfrole;

    private ShowUsers controllerRef;

    public void setControllerRef(ShowUsers controllerRef) {
        this.controllerRef = controllerRef;
    }
    @FXML
    void addUser(ActionEvent event) {
        String email = tfemail.getText().trim();
        String password = tfpwd.getText().trim();
        String role = tfrole.getValue();
        Boolean isBlocked = tfisblocked.getValue();

        // === Validations ===
        if (email.isEmpty() || password.isEmpty() || role == null || isBlocked == null) {
            showAlert(Alert.AlertType.ERROR, "Champs manquants", "Veuillez remplir tous les champs.");
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

        // === Create User ===
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(PasswordHasher.hashPassword(password));
        newUser.setRoles(role);
        newUser.setBlocked(isBlocked);

        UserService userService = new UserService();
        userService.add(newUser);

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur ajouté avec succès !");

        if (controllerRef != null) {
            controllerRef.loadUsers();
        }

        ((Stage) adduserbtn.getScene().getWindow()).close();
    }

    @FXML
    void cancelAdd(ActionEvent event) {
        ((Stage) canceladdbtn.getScene().getWindow()).close();
    }

    @FXML
    void initialize() {
        assert adduserbtn != null : "fx:id=\"adduserbtn\" was not injected: check your FXML file 'AddUser.fxml'.";
        assert canceladdbtn != null : "fx:id=\"canceladdbtn\" was not injected: check your FXML file 'AddUser.fxml'.";
        assert tfemail != null : "fx:id=\"tfemail\" was not injected: check your FXML file 'AddUser.fxml'.";
        assert tfisblocked != null : "fx:id=\"tfisblocked\" was not injected: check your FXML file 'AddUser.fxml'.";
        assert tfpwd != null : "fx:id=\"tfpwd\" was not injected: check your FXML file 'AddUser.fxml'.";
        assert tfrole != null : "fx:id=\"tfrole\" was not injected: check your FXML file 'AddUser.fxml'.";

        tfrole.getItems().addAll("[\"ROLE_ADMIN\"]", "[\"ROLE_PATIENT\"]", "[\"ROLE_MEDECIN\"]", "[\"ROLE_TECHNICIEN\"]", "[\"ROLE_COORDINATEUR\"]", "[\"ROLE_RH\"]");
        tfisblocked.getItems().addAll(true, false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

