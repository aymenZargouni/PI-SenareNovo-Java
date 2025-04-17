package ed.sanarenovo.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.Patient;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.PatientService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

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
    private TextField tfpwd;

    @FXML
    private ChoiceBox<String> tfsexe;

    @FXML
    void AddPatient(ActionEvent event) {
        String email = tfemail.getText();
        String password = tfpwd.getText();
        String fullname = tffullname.getText();
        String gender = tfsexe.getValue();
        String address = tfaddress.getText();

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
    }

    @FXML
    void cancelAdd(ActionEvent event) {
        tfemail.clear();
        tfpwd.clear();
        tffullname.clear();
        tfaddress.clear();
        tfsexe.setValue(null);
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
        tfsexe.setValue("Homme"); // default

    }

}

