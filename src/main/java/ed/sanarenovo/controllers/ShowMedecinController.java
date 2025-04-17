package ed.sanarenovo.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.Medecin;
import ed.sanarenovo.services.MedecinService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ShowMedecinController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableView<Medecin> medTable;

    @FXML
    private TableColumn<Medecin, Date> dateembauche;

    @FXML
    private TableColumn<Medecin, String> email;

    @FXML
    private TableColumn<Medecin, String> fullname;

    @FXML
    private TableColumn<Medecin, String> specialite;

    @FXML
    private Button modifbtn;

    @FXML
    private Button deletebtn;

    @FXML
    private VBox rootContainer;

    @FXML
    void initialize() {
        assert dateembauche != null : "fx:id=\"dateembauche\" was not injected: check your FXML file 'ShowMedecin.fxml'.";
        assert email != null : "fx:id=\"email\" was not injected: check your FXML file 'ShowMedecin.fxml'.";
        assert fullname != null : "fx:id=\"fullname\" was not injected: check your FXML file 'ShowMedecin.fxml'.";
        assert specialite != null : "fx:id=\"specialite\" was not injected: check your FXML file 'ShowMedecin.fxml'.";

        MedecinService medecinService = new MedecinService();
        List<Medecin> medecinList = medecinService.getAll();

        medTable.setItems(FXCollections.observableArrayList(medecinList));

        fullname.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        dateembauche.setCellValueFactory(new PropertyValueFactory<>("dateEmbauche"));
        specialite.setCellValueFactory(new PropertyValueFactory<>("specilite"));
        email.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUser().getEmail())
        );
    }


    @FXML
    void addMed(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddMedecin.fxml"));
            Parent root = loader.load();
            AddMedecin addController = loader.getController();
            addController.setControllerRef(this);

            Stage stage = new Stage();
            stage.setTitle("Add New Medecin");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void deleteMed(ActionEvent event) {
        Medecin selected = medTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Suppression");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un médecin à supprimer !");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce médecin ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            MedecinService medecinService = new MedecinService();
            medecinService.delete(selected.getId());

            loadMedecins();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText(null);
            success.setContentText("Médecin supprimé avec succès !");
            success.showAndWait();
        }
    }

    public void loadMedecins() {
        MedecinService medecinService = new MedecinService();
        List<Medecin> medecins = medecinService.getAll();

        ObservableList<Medecin> data = FXCollections.observableArrayList(medecins);
        medTable.setItems(data);
    }
    @FXML
    void modifMed(ActionEvent event) {
        Medecin selectedMedecin = medTable.getSelectionModel().getSelectedItem();

        if (selectedMedecin != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditMedecin.fxml"));
                Parent root = loader.load();

                EditMedecinController controller = loader.getController();
                controller.setMedecinToEdit(selectedMedecin);
                controller.setControllerRef(this);
                Stage stage = new Stage();
                stage.setTitle("Modify Medecin");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a medecin to modify.");
            alert.showAndWait();
        }
    }


}

