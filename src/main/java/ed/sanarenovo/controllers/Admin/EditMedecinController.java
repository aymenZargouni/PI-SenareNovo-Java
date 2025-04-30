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

public class EditMedecinController {

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

    private Medecin medecinToEdit;

    private ShowMedecinController controllerRef;
    private String originalPassword;
    public void setControllerRef(ShowMedecinController controllerRef) {
        this.controllerRef = controllerRef;
    }
    public void setMedecinToEdit(Medecin medecin) {
        this.medecinToEdit = medecin;

        this.originalPassword = medecin.getUser().getPassword();

        tffullname.setText(medecin.getFullname());
        tfemail.setText(medecin.getUser().getEmail());
        tfmdp.setText(medecin.getUser().getPassword());
        tfspecialite.setValue(medecin.getSpecilite());
        tfdateembauche.setValue(Date.valueOf(String.valueOf(medecin.getDateEmbauche())).toLocalDate());

    }

    @FXML
    void addMed(ActionEvent event) {
        String fullname = tffullname.getText().trim();
        String email = tfemail.getText().trim();
        String password = tfmdp.getText().trim();
        String specialite = tfspecialite.getValue().trim();
        LocalDate localDate = tfdateembauche.getValue();

        // === Validation ===
        if (fullname.isEmpty() || email.isEmpty() || password.isEmpty() || specialite.isEmpty() || localDate == null) {
            showAlert(Alert.AlertType.ERROR, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        if (!fullname.matches("^[\\p{L} ]{1,15}$")) {
            showAlert(Alert.AlertType.ERROR, "Nom invalide", "Le nom complet doit contenir uniquement des lettres et être de 15 caractères maximum.");
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

        if (localDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Date invalide", "La date d'embauche doit être aujourd'hui ou dans le futur.");
            return;
        }

        String oldEmail = medecinToEdit.getUser().getEmail();
        UserService userService = new UserService();
        if (!email.equalsIgnoreCase(oldEmail) && userService.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Cet email est déjà utilisé !");
            return;
        }



        medecinToEdit.setFullname(fullname);
        medecinToEdit.setSpecilite(specialite);
        medecinToEdit.setDateEmbauche(Date.valueOf(localDate));

        User user = medecinToEdit.getUser();
        user.setEmail(email);


        String passwordInputHashed = PasswordHasher.hashPassword(password);
        if (!PasswordHasher.checkPassword(originalPassword, passwordInputHashed)) {
            user.setPassword(passwordInputHashed);
        } else {
            user.setPassword(originalPassword);
        }

        MedecinService medecinService = new MedecinService();
        userService.update(user, user.getId());
        medecinService.update(medecinToEdit, medecinToEdit.getId());


        showAlert(Alert.AlertType.INFORMATION, "Succès", "Médecin modifié avec succès !");

        if (controllerRef != null) {
            controllerRef.loadMedecins();
        }

        Stage stage = (Stage) addbtn.getScene().getWindow();
        stage.close();

    }

    @FXML
    void cancelAdd(ActionEvent event) {
        Stage stage = (Stage) addbtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    void initialize() {
        assert addbtn != null : "fx:id=\"addbtn\" was not injected: check your FXML file 'EditMedecin.fxml'.";
        assert cancelbtn != null : "fx:id=\"cancelbtn\" was not injected: check your FXML file 'EditMedecin.fxml'.";
        assert tfdateembauche != null : "fx:id=\"tfdateembauche\" was not injected: check your FXML file 'EditMedecin.fxml'.";
        assert tfemail != null : "fx:id=\"tfemail\" was not injected: check your FXML file 'EditMedecin.fxml'.";
        assert tffullname != null : "fx:id=\"tffullname\" was not injected: check your FXML file 'EditMedecin.fxml'.";
        assert tfmdp != null : "fx:id=\"tfmdp\" was not injected: check your FXML file 'EditMedecin.fxml'.";
        assert tfspecialite != null : "fx:id=\"tfspecialite\" was not injected: check your FXML file 'EditMedecin.fxml'.";


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

    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

