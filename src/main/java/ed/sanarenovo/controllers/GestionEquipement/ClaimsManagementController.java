package ed.sanarenovo.controllers.GestionEquipement;

import ed.sanarenovo.entities.Claim;
import ed.sanarenovo.entities.Equipment;
import ed.sanarenovo.entities.Technicien;
import ed.sanarenovo.services.ClaimService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

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
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private HBox expirationWarning;

    private final Tooltip updateTooltip = new Tooltip("Cette réclamation a plus de 24h\nModification impossible");
    private final Tooltip deleteTooltip = new Tooltip("Cette réclamation a plus de 24h\nSuppression impossible");

    @FXML
    public void initialize() {
        configureTableColumns();
        refreshClaims();

        updateTooltip.setStyle("-fx-font-size: 12; -fx-text-fill: #ff4444;");
        deleteTooltip.setStyle("-fx-font-size: 12; -fx-text-fill: #ff4444;");

        claimTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                descriptionArea.setText(newSelection.getReclamation());

                boolean isOlderThan24h = false;
                if (newSelection.getId() != null) {
                    isOlderThan24h = claimService.isClaimOlderThan24Hours(newSelection.getId());
                }

                updateButton.setDisable(isOlderThan24h);
                deleteButton.setDisable(isOlderThan24h);

                // Gérer l'affichage du label d'avertissement
                expirationWarning.setVisible(isOlderThan24h);
                expirationWarning.setAccessibleText(isOlderThan24h ? "Cette réclamation a plus de 24h et ne peut être ni modifiée ni supprimée" : "");

                // Appliquer ou retirer les tooltips
                if (isOlderThan24h) {
                    Tooltip.install(updateButton, updateTooltip);
                    Tooltip.install(deleteButton, deleteTooltip);
                } else {
                    Tooltip.uninstall(updateButton, updateTooltip);
                    Tooltip.uninstall(deleteButton, deleteTooltip);
                }
            } else {
                // Si rien n'est sélectionné
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
                descriptionArea.clear();
                expirationWarning.setVisible(false);
                Tooltip.uninstall(updateButton, updateTooltip);
                Tooltip.uninstall(deleteButton, deleteTooltip);
            }
        });
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        equipmentColumn.setCellValueFactory(cellData -> {
            Equipment equipment = cellData.getValue().getEquipment();
            return equipment != null ? new SimpleStringProperty(equipment.getName()) : new SimpleStringProperty("");
        });

        technicienColumn.setCellValueFactory(cellData -> {
            Technicien technicien = cellData.getValue().getTechnicien();
            return technicien != null ? new SimpleStringProperty(technicien.getNom()) : new SimpleStringProperty("");
        });

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("reclamation"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        statusColumn.setCellValueFactory(cellData -> {
            Equipment equipment = cellData.getValue().getEquipment();
            return equipment != null ? new SimpleStringProperty(equipment.getStatus()) : new SimpleStringProperty("");
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

                if (!newDescription.equals(selected.getReclamation())) {
                    selected.setReclamation(newDescription);
                    claimService.updateEntity(selected, selected.getId());
                    refreshClaims();
                    showAlert("Succès", "Réclamation mise à jour", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Avertissement", "Aucune modification n'a été apportée à la réclamation", Alert.AlertType.WARNING);
                }
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
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        descriptionArea.clear();
        expirationWarning.setVisible(false); // Correction ici
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
