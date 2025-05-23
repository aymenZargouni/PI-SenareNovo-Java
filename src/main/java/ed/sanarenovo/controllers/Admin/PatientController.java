package ed.sanarenovo.controllers.Admin;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import ed.sanarenovo.entities.Patient;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.PatientService;
import ed.sanarenovo.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class PatientController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button cancelbtn;

    @FXML
    private Button registerbtn;

    @FXML
    private TextField tfaddress;

    @FXML
    private TextField tfemail;

    @FXML
    private TextField tffullname;

    @FXML
    private PasswordField tfpwd;

    @FXML
    private ChoiceBox<String> tfsexe;

    @FXML
    private ProgressBar strengthBar;

    @FXML
    private Label strengthLabel;

    @FXML
    private WebView captchaWebView;

    private String captchaToken;

    @FXML
    void AddPatient(ActionEvent event) {
        String email = tfemail.getText().trim();
        String password = tfpwd.getText();
        String fullname = tffullname.getText().trim();
        String gender = tfsexe.getValue();
        String address = tfaddress.getText().trim();
        String strength = evaluatePasswordStrength(password);

        if (fullname.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty() || gender == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Email invalide", "Veuillez entrer une adresse email valide !");
            return;
        }

        if (strength.equals("Faible")) {
            showAlert(Alert.AlertType.ERROR, "Mot de passe faible", "Veuillez choisir un mot de passe plus fort !");
            return;
        }

        UserService userService = new UserService();
        if (userService.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Cet email est déjà utilisé !");
            return;
        }


        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRoles("[\"ROLE_PATIENT\"]");
        user.setBlocked(false);

        Patient patient = new Patient();
        patient.setFullname(fullname);
        patient.setGender(gender);
        patient.setAdress(address);
        patient.setUser(user);

        PatientService patientService = new PatientService();
        patientService.register(patient);

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte patient créé avec succès !");
        redirectToLogin(event);
    }

    @FXML
    void cancelAdd(ActionEvent event) {
        clearFields();
    }

    @FXML
    void initialize() {
        assert cancelbtn != null : "fx:id=\"cancelbtn\" was not injected: check your FXML file 'RegisterPatient.fxml'.";
        assert registerbtn != null : "fx:id=\"registerbtn\" was not injected: check your FXML file 'RegisterPatient.fxml'.";
        assert tfaddress != null : "fx:id=\"tfaddress\" was not injected: check your FXML file 'RegisterPatient.fxml'.";
        assert tfemail != null : "fx:id=\"tfemail\" was not injected: check your FXML file 'RegisterPatient.fxml'.";
        assert tffullname != null : "fx:id=\"tffullname\" was not injected: check your FXML file 'RegisterPatient.fxml'.";
        assert tfpwd != null : "fx:id=\"tfpwd\" was not injected: check your FXML file 'RegisterPatient.fxml'.";
        assert tfsexe != null : "fx:id=\"tfsexe\" was not injected: check your FXML file 'RegisterPatient.fxml'.";

        tfsexe.getItems().addAll("Homme","Femme");
        tfsexe.setValue("Homme");

    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private void clearFields() {
        tfemail.clear();
        tfpwd.clear();
        tffullname.clear();
        tfaddress.clear();
        tfsexe.setValue("Homme");
    }

    private void redirectToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AymenViews/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void checkPasswordStrength() {
        String password = tfpwd.getText();
        String result = evaluatePasswordStrength(password);

        switch (result) {
            case "Faible":
                strengthBar.setProgress(0.2);
                strengthBar.setStyle("-fx-accent: red;");
                strengthLabel.setText("Faible");
                break;
            case "Okay":
                strengthBar.setProgress(0.6);
                strengthBar.setStyle("-fx-accent: orange;");
                strengthLabel.setText("Okay");
                break;
            case "Fort":
                strengthBar.setProgress(1.0);
                strengthBar.setStyle("-fx-accent: green;");
                strengthLabel.setText("Fort");
                break;
        }
    }

    private String evaluatePasswordStrength(String password) {
        int strength = 0;

        if (password.length() >= 8) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()-_+=<>?].*")) strength++;

        double score = strength / 5.0;

        if (score < 0.4) return "Faible";
        else if (score < 0.8) return "Okay";
        else return "Fort";
    }


}

