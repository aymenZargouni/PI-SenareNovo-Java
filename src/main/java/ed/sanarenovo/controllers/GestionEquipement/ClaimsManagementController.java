package ed.sanarenovo.controllers.GestionEquipement;

import ed.sanarenovo.entities.Claim;
import ed.sanarenovo.entities.Equipment;
import ed.sanarenovo.entities.Technicien;
import ed.sanarenovo.services.ClaimService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.util.List;

public class ClaimsManagementController {
    private final ClaimService claimService = new ClaimService();

    @FXML private TableView<Claim> claimTable;
    @FXML private TextArea descriptionArea;
    @FXML private TableColumn<Claim, Integer> idColumn;
    @FXML private TableColumn<Claim, String> equipmentColumn;
    @FXML private TableColumn<Claim, String> technicienColumn;
    @FXML private TableColumn<Claim, String> descriptionColumn;
    @FXML private TableColumn<Claim, Timestamp> dateColumn;
    @FXML private TableColumn<Claim, String> statusColumn;

    @FXML
    public void initialize() {
        // Configuration explicite des colonnes
        configureTableColumns();

        // Configurer la sélection
        claimTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                descriptionArea.setText(newSelection.getReclamation());
            }
        });

        // Charger les données
        refreshClaims();
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        equipmentColumn.setCellValueFactory(cellData -> {
            Equipment equipment = cellData.getValue().getEquipment();
            return equipment != null ? equipment.nameProperty() : new SimpleStringProperty("");
        });

        technicienColumn.setCellValueFactory(cellData -> {
            Technicien technicien = cellData.getValue().getTechnicien();
            return technicien != null ? technicien.nomProperty() : new SimpleStringProperty("");
        });

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("reclamation"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        statusColumn.setCellValueFactory(cellData -> {
            Equipment equipment = cellData.getValue().getEquipment();
            return equipment != null ? equipment.statusProperty() : new SimpleStringProperty("");
        });
    }
    @FXML
    private void handleUpdateClaim() {
        Claim selected = claimTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String newDescription = descriptionArea.getText();
            if (!newDescription.isEmpty()) {
                if (claimService.isClaimOlderThan24Hours(selected.getId())) {
                    showAlert("Erreur", "Impossible de modifier après 24 heures", Alert.AlertType.ERROR);
                    return;
                }

                selected.setReclamation(newDescription);
                claimService.updateEntity(selected, selected.getId());
                refreshClaims();
                showAlert("Succès", "Réclamation mise à jour", Alert.AlertType.INFORMATION);
            }
        } else {
            showAlert("Avertissement", "Veuillez sélectionner une réclamation", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleDeleteClaim() {
        Claim selected = claimTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (claimService.isClaimOlderThan24Hours(selected.getId())) {
                showAlert("Erreur", "Impossible de supprimer après 24 heures", Alert.AlertType.ERROR);
                return;
            }

            claimService.deleteEntity(selected.getId());
            refreshClaims();
            showAlert("Succès", "Réclamation supprimée", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Avertissement", "Veuillez sélectionner une réclamation", Alert.AlertType.WARNING);
        }
    }

    private void refreshClaims() {
        List<Claim> claims = claimService.getAll();
        claimTable.getItems().setAll(claims);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}