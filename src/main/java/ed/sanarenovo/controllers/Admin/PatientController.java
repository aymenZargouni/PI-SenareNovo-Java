package ed.sanarenovo.controllers.Admin;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import ed.sanarenovo.entities.Patient;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.PatientService;
import ed.sanarenovo.services.UserService;
import ed.sanarenovo.utils.CaptchaVerifier;
import ed.sanarenovo.utils.JavaConnector;
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

        if (strength.equals("Weak")) {
            showAlert(Alert.AlertType.ERROR, "Mot de passe faible", "Veuillez choisir un mot de passe plus fort !");
            return;
        }

        UserService userService = new UserService();
        if (userService.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Cet email est déjà utilisé !");
            return;
        }

        String recaptchaToken = fetchRecaptchaToken();

        if (!CaptchaVerifier.verify(recaptchaToken)) {
            showAlert(Alert.AlertType.ERROR, "Erreur CAPTCHA", "La vérification reCAPTCHA a échoué. Veuillez réessayer !");
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

        setupCaptcha();


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
            case "Weak":
                strengthBar.setProgress(0.2);
                strengthBar.setStyle("-fx-accent: red;");
                strengthLabel.setText("Weak");
                break;
            case "Okay":
                strengthBar.setProgress(0.6);
                strengthBar.setStyle("-fx-accent: orange;");
                strengthLabel.setText("Okay");
                break;
            case "Strong":
                strengthBar.setProgress(1.0);
                strengthBar.setStyle("-fx-accent: green;");
                strengthLabel.setText("Strong");
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

        if (score < 0.4) return "Weak";
        else if (score < 0.8) return "Okay";
        else return "Strong";
    }

    private String fetchRecaptchaToken() {
        final String[] tokenHolder = new String[1];

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/AymenViews/recaptcha.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector(tokenHolder));
            }
        });

        // Wait for token
        long start = System.currentTimeMillis();
        while (tokenHolder[0] == null && System.currentTimeMillis() - start < 5000) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return tokenHolder[0];
    }

    public void receiveToken(String token) {
        this.captchaToken = token;
        System.out.println("Received reCAPTCHA token: " + token);
    }
    
    private void setupCaptcha() {
        WebEngine webEngine = captchaWebView.getEngine();

        // Expose a Java object to JavaScript
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript(""
                        + "grecaptcha.ready(function() {"
                        + "  grecaptcha.execute('6LceouYqAAAAAOHZU_T84yeLIjQ3wEhLQZcAFRVS', {action: 'register'}).then(function(token) {"
                        + "    window.java.receiveToken(token);"
                        + "  });"
                        + "});"
                );
            }
        });

        // Set the Java connector
        captchaWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                captchaWebView.getEngine().executeScript("window.java = { receiveToken: function(token) { javafxConnector.receiveToken(token); } };");
            }
        });

        // Load a simple page with reCAPTCHA library
        webEngine.loadContent("<html><head><script src='https://www.google.com/recaptcha/api.js?render=YOUR_SITE_KEY'></script></head><body></body></html>");

        // Connect Java method
        captchaWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) captchaWebView.getEngine().executeScript("window");
                window.setMember("javafxConnector", this);
            }
        });
    }


}

