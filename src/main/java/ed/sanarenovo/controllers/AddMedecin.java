package ed.sanarenovo.controllers;

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
    private TextField tfspecialite;

    private ShowMedecinController controllerRef;

    public void setControllerRef(ShowMedecinController controllerRef) {
        this.controllerRef = controllerRef;
    }

    @FXML
    void addMed(ActionEvent event) {
        String fullname = tffullname.getText().trim();
        String email = tfemail.getText().trim();
        String password = tfmdp.getText().trim();
        String specialite = tfspecialite.getText().trim();
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

        Date dateEmbauche = Date.valueOf(localDate);

        // 1. Create User
        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordHasher.hashPassword(password)); // ⚠️ Add hashing later if needed
        user.setRoles("[\"ROLE_MEDECIN\"]");
        user.setBlocked(false);

        // 2. Create Medecin
        Medecin medecin = new Medecin();
        medecin.setFullname(fullname);
        medecin.setSpecilite(specialite);
        medecin.setDateEmbauche(dateEmbauche);
        medecin.setUser(user);

        // 3. Save Medecin
        MedecinService medecinService = new MedecinService();
        medecinService.add(medecin);

        // 4. Success alert
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Succès");
        success.setHeaderText(null);
        success.setContentText("Médecin ajouté avec succès !");
        success.showAndWait();

        // 5. Refresh the table on the previous screen
        if (controllerRef != null) {
            controllerRef.loadMedecins();
        }

        // 6. Close the add window
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

    }

}

