package ed.sanarenovo.controllers.Admin;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Login {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField tfemail;

    @FXML
    private PasswordField tfpwd;

    @FXML
    void login(ActionEvent event) {
        AuthService authService = new AuthService();
        String email = tfemail.getText();
        String password = tfpwd.getText();

        User user = authService.login(email, password);
        if (user != null) {
            redirectBasedOnRole(user.getRoles());
        }
    }

    private void redirectBasedOnRole(String role) {
        String cleanedRole = role.replaceAll("[\\[\\]\"]", "");

        switch (cleanedRole) {
            case "ROLE_ADMIN":
                loadFXML("/ShowMedecin.fxml");
                break;
            case "ROLE_MEDECIN":
                loadFXML("/AddMedecin.fxml");
                break;
            case "ROLE_PATIENT":
                loadFXML("/patient/PatientDashboard.fxml");
                break;
            case "ROLE_TECHNICIEN":
                loadFXML("/technicien/TechnicienDashboard.fxml");
                break;
        }
    }
    private void loadFXML(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) tfemail.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        assert tfemail != null : "fx:id=\"tfemail\" was not injected: check your FXML file 'Login.fxml'.";
        assert tfpwd != null : "fx:id=\"tfpwd\" was not injected: check your FXML file 'Login.fxml'.";

    }

}
