package ed.sanarenovo.controllers.takoua;

import ed.sanarenovo.entities.Offre;
import ed.sanarenovo.services.OffreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Date;

public class OffresController {
    @FXML
    private TableView<Offre> tableOffres;
    @FXML
    private TableColumn<Offre, Integer> colId;
    @FXML
    private TableColumn<Offre, String> colTitre;
    @FXML
    private TableColumn<Offre, String> colDescription;
    @FXML
    private TableColumn<Offre, Date> colPublication;
    @FXML
    private TableColumn<Offre, Date> colExpiration;
    @FXML
    private Button btnPostuler;
    @FXML
    private Button btnRetour;

    private OffreService offreService = new OffreService();
    private ObservableList<Offre> offresList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPublication.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));

        loadOffres();
    }

    private void loadOffres() {
        offresList.clear();
        offresList.addAll(offreService.getAllOffres());
        tableOffres.setItems(offresList);
    }

    @FXML
    private void handlePostuler() throws IOException {
        Offre selectedOffre = tableOffres.getSelectionModel().getSelectedItem();
        if (selectedOffre != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/takoua_views/candidature_form.fxml"));
            Parent root = loader.load();
            CandidatureController controller = loader.getController();
            controller.setOffreId(selectedOffre.getId());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Postuler à l'offre");
            stage.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une offre à laquelle postuler");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRetour() throws IOException {
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/takoua_views/main_view.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("Accueil");
        stage.show();
    }
}