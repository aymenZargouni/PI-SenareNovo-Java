package ed.sanarenovo.controllers.Admin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.AuthService;
import ed.sanarenovo.utils.MailSender;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

public class Login {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField tfemail;

    @FXML
    private PasswordField tfpwd;

    private int loginAttempts = 0;

    @FXML
    void login(ActionEvent event) {
        AuthService authService = new AuthService();
        String email = tfemail.getText().trim();
        String password = tfpwd.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un email valide !");
            return;
        }

        User user = authService.login(email, password);
        if (user != null) {
            redirectBasedOnRole(user.getRoles());
        } else {
            loginAttempts++;
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe incorrect, ou compte bloqué !");

            if (loginAttempts >= 3) {
                takePhotoWithWebcam();
            }
        }
    }

    private void redirectBasedOnRole(String role) {
        String cleanedRole = role.replaceAll("[\\[\\]\"]", "");

        switch (cleanedRole) {
            case "ROLE_ADMIN":
                loadFXML("/AymenViews/ShowMedecin.fxml");
                break;
            case "ROLE_MEDECIN":
                loadFXML("/Youssef_views/dm.fxml");
                break;
            case "ROLE_PATIENT":
                loadFXML("/Youssef_views/cons.fxml");
                break;
            case "ROLE_COORDINATEUR":
                loadFXML("/Sabrineviews/equipment.fxml");
                break;
            case "ROLE_TECHNICIEN":
                loadFXML("/Sabrineviews/TechClaims.fxml");
                break;
            case "ROLE_RH":
                loadFXML("/takoua_views/main_view.fxml");
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
    void redirectToRegister(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AymenViews/RegisterPatient.fxml"));
            Parent registerRoot = loader.load();


            tfemail.getScene().setRoot(registerRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void initialize() {
        assert tfemail != null : "fx:id=\"tfemail\" was not injected: check your FXML file 'Login.fxml'.";
        assert tfpwd != null : "fx:id=\"tfpwd\" was not injected: check your FXML file 'Login.fxml'.";

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

    private void takePhotoWithWebcam() {
        System.load("C:\\Cycle3A\\PI-Java\\opencv\\build\\java\\x64\\opencv_java4110.dll");

        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Error: Camera not accessible.");
            return;
        }

        Mat frame = new Mat();
        if (camera.read(frame)) {
            String filePath = "snapshot.png";
            Imgcodecs.imwrite(filePath, frame);
            System.out.println("Photo saved to " + filePath);


            MailSender.sendEmailWithAttachment(
                    "aymen.zargouni@gmail.com",
                    "Security Alert - 3 Failed Login Attempts",
                    "this user : "+ tfemail.getText()+" has failed to log in 3 times. See attached photo.",
                    new File(filePath)
            );
        }

        camera.release();
    }

}
