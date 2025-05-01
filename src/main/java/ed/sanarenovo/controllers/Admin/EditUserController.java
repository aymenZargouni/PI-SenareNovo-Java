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

public class EditUserController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button cancelModifbtn;

    @FXML
    private Button modifUserbtn;

    @FXML
    private TextField tfemail;

    @FXML
    private ChoiceBox<Boolean> tfisblocked;

    @FXML
    private PasswordField tfpwd;

    @FXML
    private ChoiceBox<String> tfrole;

    private ShowUsers controllerRef;
    private String originalPassword;

    private User selectedUser;

    public void setControllerRef(ShowUsers controllerRef) {
        this.controllerRef = controllerRef;
    }
    public void setUser(User user) {
        this.selectedUser = user;
        tfemail.setText(user.getEmail());
        tfpwd.setText(user.getPassword());
        tfrole.setValue(user.getRoles());
        tfisblocked.setValue(user.isBlocked());
    }
    @FXML
    void initialize() {
        assert cancelModifbtn != null : "fx:id=\"cancelModifbtn\" was not injected: check your FXML file 'EditUser.fxml'.";
        assert modifUserbtn != null : "fx:id=\"modifUserbtn\" was not injected: check your FXML file 'EditUser.fxml'.";
        assert tfemail != null : "fx:id=\"tfemail\" was not injected: check your FXML file 'EditUser.fxml'.";
        assert tfisblocked != null : "fx:id=\"tfisblocked\" was not injected: check your FXML file 'EditUser.fxml'.";
        assert tfpwd != null : "fx:id=\"tfpwd\" was not injected: check your FXML file 'EditUser.fxml'.";
        assert tfrole != null : "fx:id=\"tfrole\" was not injected: check your FXML file 'EditUser.fxml'.";

        tfrole.getItems().addAll("[\"ROLE_ADMIN\"]", "[\"ROLE_PATIENT\"]", "[\"ROLE_MEDECIN\"]", "[\"ROLE_TECHNICIEN\"]", "[\"ROLE_COORDINATEUR\"]", "[\"ROLE_RH\"]");
        tfisblocked.getItems().addAll(true, false);
    }

    @FXML
    void cancelModif(ActionEvent event) {
        ((Stage) cancelModifbtn.getScene().getWindow()).close();
    }

    @FXML
    void modifUser(ActionEvent event) {
        String email = tfemail.getText().trim();
        String password = tfpwd.getText().trim();
        String role = tfrole.getValue();
        Boolean isBlocked = tfisblocked.getValue();


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

        String oldEmail = selectedUser.getEmail();
        UserService userService = new UserService();
        if (!email.equalsIgnoreCase(oldEmail) && userService.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Cet email est déjà utilisé !");
            return;
        }



        selectedUser.setEmail(email);
        selectedUser.setRoles(role);
        selectedUser.setBlocked(isBlocked);

        String passwordInputHashed = PasswordHasher.hashPassword(password);
        if (!PasswordHasher.checkPassword(selectedUser.getPassword(), passwordInputHashed)) {
            selectedUser.setPassword(passwordInputHashed);
        }

        userService.update(selectedUser, selectedUser.getId());

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur modifié avec succès !");

        if (controllerRef != null) {
            controllerRef.loadUsers();
        }

        ((Stage) modifUserbtn.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
