package ed.sanarenovo.controllers.GestionEquipement;

import ed.sanarenovo.entities.Historique;
import ed.sanarenovo.services.HistoriqueService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;

public class HistoriqueController {

    @FXML private TableView<Historique> historiqueTable;
    @FXML
    private TableColumn<Historique, Integer> colId;
    @FXML
    private TableColumn<Historique, String> colEquipement;
    @FXML
    private TableColumn<Historique, String> colModel;
    @FXML
    private TableColumn<Historique, LocalDate> colDate;
    @FXML private TableColumn<Historique, String> colDescription;
    private final HistoriqueService historiqueService = new HistoriqueService();
    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colEquipement.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEquipment() != null ?
                        cellData.getValue().getEquipment().getName() : ""));
        colModel.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEquipment() != null ?
                        cellData.getValue().getEquipment().getModel() : ""));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateReparation"));
        colDescription.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getRapportDetaille() != null ?
                        cellData.getValue().getRapportDetaille() : ""));
        loadHistoriqueData();
    }

    private void loadHistoriqueData() {
        ObservableList<Historique> data = FXCollections.observableArrayList(historiqueService.getAllHistoriques());
        historiqueTable.setItems(data);
    }

    @FXML
    private void onSyncHistorique() {
        historiqueService.syncHistoriqueFromEquipment();
        loadHistoriqueData();
    }

    @FXML
    private void onGenerateReport() {
        Historique selected = historiqueTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            historiqueService.generateReport(selected.getId());
            showAlert("Succès", "Rapport généré pour l'historique ID " + selected.getId());
        } else {
            showAlert("Erreur", "Veuillez sélectionner un historique dans la table.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}