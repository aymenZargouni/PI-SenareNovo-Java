package ed.sanarenovo.controllers.takoua;

import ed.sanarenovo.entities.Offre;
import ed.sanarenovo.services.OffreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
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
    private Button btnRetour;
    @FXML
    private TextField searchFieldOffres;
    @FXML
    private ObservableList<Offre> offresList = FXCollections.observableArrayList();

    private OffreService offreService = new OffreService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPublication.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        OffreService offreService = new OffreService();
        offreService.supprimerOffresExpirees(); // ⏳ Supprimer les offres expirées au démarrage
        loadOffres();
        FilteredList<Offre> filteredData = new FilteredList<>(offresList, b -> true);

        searchFieldOffres.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(offre -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                return offre.getTitre().toLowerCase().contains(newValue.toLowerCase());
            });
        });

        SortedList<Offre> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableOffres.comparatorProperty());

        tableOffres.setItems(sortedData);
        addClearButtonToTextField(searchFieldOffres);
    }


    public void addClearButtonToTextField(TextField textField) {
        Button clearButton = new Button("✖");
        clearButton.setOnAction(e -> textField.clear());
        clearButton.getStyleClass().add("clear-button");

        clearButton.setVisible(false);
        textField.textProperty().addListener((obs, oldText, newText) -> {
            clearButton.setVisible(!newText.isEmpty());
        });

        StackPane stackPane = new StackPane(textField, clearButton);
        StackPane.setAlignment(clearButton, javafx.geometry.Pos.CENTER_RIGHT);
        AnchorPane.setTopAnchor(stackPane, AnchorPane.getTopAnchor(textField));
        AnchorPane.setLeftAnchor(stackPane, AnchorPane.getLeftAnchor(textField));
        AnchorPane.setRightAnchor(stackPane, AnchorPane.getRightAnchor(textField));
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