package ed.sanarenovo.controllers.Admin;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.Technicien;
import ed.sanarenovo.services.TechnicienService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ShowTechnicienController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button addMedbtn;

    @FXML
    private Button deletebtn;

    @FXML
    private TableColumn<Technicien, String> email;

    @FXML
    private TableColumn<Technicien, String> fullname;

    @FXML
    private Button modifbtn;

    @FXML
    private VBox rootContainer;

    @FXML
    private TableColumn<Technicien, Boolean> status;

    @FXML
    private TableView<Technicien> techTable;

    @FXML
    private TableColumn<Technicien, String> tel;

    private final TechnicienService technicienService = new TechnicienService();

    @FXML
    void addTech(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AymenViews/AddTechnicien.fxml"));
            Parent root = loader.load();
            AddTechnicienController controller = loader.getController();
            controller.setControllerRef(this);
            Stage stage = new Stage();
            stage.setTitle("Add New Technicien");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteTech(ActionEvent event) {
        Technicien selected = techTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Suppression");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un technicien à supprimer !");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce technicien ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            technicienService.delete(selected.getId());

            loadTechniciens(); // Refresh table if you have this method

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText(null);
            success.setContentText("Technicien supprimé avec succès !");
            success.showAndWait();
        }
    }

    @FXML
    void modifTech(ActionEvent event) {
        Technicien selectedTechnicien = techTable.getSelectionModel().getSelectedItem();

        if (selectedTechnicien != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AymenViews/EditTechnicien.fxml"));
                Parent root = loader.load();

                EditTechnicienController controller = loader.getController();
                controller.setTechnicien(selectedTechnicien);
                controller.setControllerRef(this);
                Stage stage = new Stage();
                stage.setTitle("Modify Technicien");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a Technicien to modify.");
            alert.showAndWait();
        }
    }

    @FXML
    void initialize() {
        assert addMedbtn != null : "fx:id=\"addMedbtn\" was not injected: check your FXML file 'ShowTechnicien.fxml'.";
        assert deletebtn != null : "fx:id=\"deletebtn\" was not injected: check your FXML file 'ShowTechnicien.fxml'.";
        assert email != null : "fx:id=\"email\" was not injected: check your FXML file 'ShowTechnicien.fxml'.";
        assert fullname != null : "fx:id=\"fullname\" was not injected: check your FXML file 'ShowTechnicien.fxml'.";
        assert modifbtn != null : "fx:id=\"modifbtn\" was not injected: check your FXML file 'ShowTechnicien.fxml'.";
        assert rootContainer != null : "fx:id=\"rootContainer\" was not injected: check your FXML file 'ShowTechnicien.fxml'.";
        assert status != null : "fx:id=\"status\" was not injected: check your FXML file 'ShowTechnicien.fxml'.";
        assert techTable != null : "fx:id=\"techTable\" was not injected: check your FXML file 'ShowTechnicien.fxml'.";
        assert tel != null : "fx:id=\"tel\" was not injected: check your FXML file 'ShowTechnicien.fxml'.";

        List<Technicien> techList = technicienService.getAll();
        techTable.setItems(FXCollections.observableArrayList(techList));

        fullname.setCellValueFactory(new PropertyValueFactory<>("nom"));
        tel.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        email.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUser().getEmail()));
        status.setCellValueFactory(cellData ->
                new SimpleBooleanProperty(cellData.getValue().getUser().isBlocked()));
    }

    public void loadTechniciens() {
        List<Technicien> techniciens = technicienService.getAll();

        ObservableList<Technicien> data = FXCollections.observableArrayList(techniciens);
        techTable.getItems().setAll(data);
    }
}
