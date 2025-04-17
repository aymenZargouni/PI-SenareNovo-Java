package ed.sanarenovo.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.Patient;
import ed.sanarenovo.services.PatientService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class ShowPatients {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableColumn<Patient, String> address;

    @FXML
    private TableColumn<Patient, String> email;

    @FXML
    private TableColumn<Patient, String> fullname;

    @FXML
    private TableColumn<Patient, String> gender;

    @FXML
    private TableView<Patient> patientTable;

    @FXML
    private VBox rootContainer;

    @FXML
    void initialize() {
        assert address != null : "fx:id=\"address\" was not injected: check your FXML file 'ShowPatients.fxml'.";
        assert email != null : "fx:id=\"email\" was not injected: check your FXML file 'ShowPatients.fxml'.";
        assert fullname != null : "fx:id=\"fullname\" was not injected: check your FXML file 'ShowPatients.fxml'.";
        assert gender != null : "fx:id=\"gender\" was not injected: check your FXML file 'ShowPatients.fxml'.";
        assert patientTable != null : "fx:id=\"patientTable\" was not injected: check your FXML file 'ShowPatients.fxml'.";
        assert rootContainer != null : "fx:id=\"rootContainer\" was not injected: check your FXML file 'ShowPatients.fxml'.";
        // Set cell value factories
        fullname.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        gender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        address.setCellValueFactory(new PropertyValueFactory<>("adress"));
        email.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUser().getEmail()));

        PatientService patientService = new PatientService();
        List<Patient> patientList = patientService.getAll();
        patientTable.setItems(FXCollections.observableArrayList(patientList));
    }

}

