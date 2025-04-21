package ed.sanarenovo.controllers.Admin;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.Medecin;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.MedecinService;
import ed.sanarenovo.services.UserService;
import ed.sanarenovo.utils.PasswordHasher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddMedecin {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button addbtn;

    @FXML
    private Button cancelbtn;

    @FXML
    private DatePicker tfdateembauche;

    @FXML
    private TextField tfemail;

    @FXML
    private TextField tffullname;

    @FXML
    private PasswordField tfmdp;

    @FXML
    private ChoiceBox<String> tfspecialite;

    private ShowMedecinController controllerRef;

    public void setControllerRef(ShowMedecinController controllerRef) {
        this.controllerRef = controllerRef;
    }

    @FXML
    void addMed(ActionEvent event) {
        String fullname = tffullname.getText().trim();
        String email = tfemail.getText().trim();
        String password = tfmdp.getText().trim();
        String specialite = tfspecialite.getValue().trim();
        LocalDate localDate = tfdateembauche.getValue();

        // Validation
        if (fullname.isEmpty() || email.isEmpty() || password.isEmpty() || specialite.isEmpty() || localDate == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Champs manquants");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs !");
            alert.showAndWait();
            return;
        }

        if (!fullname.matches("^[\\p{L} ]{1,15}$")) {
            showAlert(Alert.AlertType.ERROR, "Nom invalide", "Le nom complet doit contenir uniquement des lettres et être de 15 caractères maximum.");
            return;
        }


        if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Email invalide");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez entrer une adresse e-mail valide !");
            alert.showAndWait();
            return;
        }

        if (password.length() < 6) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Mot de passe faible");
            alert.setHeaderText(null);
            alert.setContentText("Le mot de passe doit contenir au moins 6 caractères.");
            alert.showAndWait();
            return;
        }

        if (localDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Date invalide", "La date d'embauche doit être aujourd'hui ou dans le futur.");
            return;
        }

        UserService userService = new UserService();
        if (userService.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Cet email est déjà utilisé !");
            return;
        }
        Date dateEmbauche = Date.valueOf(localDate);


        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordHasher.hashPassword(password));
        user.setRoles("[\"ROLE_MEDECIN\"]");
        user.setBlocked(false);


        Medecin medecin = new Medecin();
        medecin.setFullname(fullname);
        medecin.setSpecilite(specialite);
        medecin.setDateEmbauche(dateEmbauche);
        medecin.setUser(user);


        MedecinService medecinService = new MedecinService();
        medecinService.add(medecin);


        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Succès");
        success.setHeaderText(null);
        success.setContentText("Médecin ajouté avec succès !");
        success.showAndWait();


        if (controllerRef != null) {
            controllerRef.loadMedecins();
        }


        Stage stage = (Stage) addbtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    void cancelAdd(ActionEvent event) {
        ((Stage) cancelbtn.getScene().getWindow()).close();
    }

    @FXML
    void initialize() {
        assert addbtn != null : "fx:id=\"addbtn\" was not injected: check your FXML file 'AddMedecin.fxml'.";
        assert cancelbtn != null : "fx:id=\"cancelbtn\" was not injected: check your FXML file 'AddMedecin.fxml'.";
        assert tfdateembauche != null : "fx:id=\"tfdateembauche\" was not injected: check your FXML file 'AddMedecin.fxml'.";
        assert tfemail != null : "fx:id=\"tfemail\" was not injected: check your FXML file 'AddMedecin.fxml'.";
        assert tffullname != null : "fx:id=\"tffullname\" was not injected: check your FXML file 'AddMedecin.fxml'.";
        assert tfmdp != null : "fx:id=\"tfmdp\" was not injected: check your FXML file 'AddMedecin.fxml'.";
        assert tfspecialite != null : "fx:id=\"tfspecialite\" was not injected: check your FXML file 'AddMedecin.fxml'.";

        // ADD THIS PART 👇
        tfspecialite.getItems().addAll(
                "Cardiologue",
                "Dermatologie",
                "Gastronentérologie",
                "Neurologie",
                "Pédiatrie",
                "Psychiatrie",
                "Chirurgie",
                "Ophtalmologie",
                "Néphrologie"
        );

        tfspecialite.getSelectionModel().selectFirst();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

