package ed.sanarenovo.controllers.GestionEquipement;

import ed.sanarenovo.entities.Equipment;
import ed.sanarenovo.services.EquipmentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class EquipmentController {
    private final EquipmentService equipmentService = new EquipmentService();

    @FXML
    private TableView<Equipment> equipmentTable;
    @FXML
    private TextField nameField;
    @FXML
    private TextField modelField;
    @FXML
    private TextField priceField;
    @FXML
    private DatePicker dateAchatPicker;  // Utiliser pour la saisie de la date d'achat

    @FXML
    public void initialize() {
        refreshTable();
        dateAchatPicker.setValue(LocalDate.now());
        equipmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                modelField.setText(newSelection.getModel());
                priceField.setText(String.valueOf(newSelection.getPrix()));
                if (newSelection.getDateAchat() != null) {
                    dateAchatPicker.setValue(newSelection.getDateAchat().toLocalDate());
                }
            }
        });
    }

    @FXML
    private void handleAddEquipment() {
        if (!isInputValid()) return;
        Date dateAchat = Date.valueOf(dateAchatPicker.getValue());
        Equipment equipment = new Equipment(
                nameField.getText().trim(),
                modelField.getText().trim(),
                dateAchat,
                Double.parseDouble(priceField.getText())
        );
        equipmentService.addEntity(equipment);
        refreshTable();
        clearFields();
        showAlert("Succès", "Équipement ajouté avec succès", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleUpdateEquipment() {
        Equipment selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Veuillez sélectionner un équipement", Alert.AlertType.WARNING);
            return;
        }
        if (!isInputValid()) return;

        selected.setName(nameField.getText().trim());
        selected.setModel(modelField.getText().trim());
        selected.setPrix(Double.parseDouble(priceField.getText()));
        selected.setDateAchat(Date.valueOf(dateAchatPicker.getValue()));

        equipmentService.updateEntity(selected, selected.getId());
        refreshTable();
        clearFields();
        showAlert("Succès", "Équipement modifié avec succès", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleDeleteEquipment() {
        Equipment selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Veuillez sélectionner un équipement", Alert.AlertType.WARNING);
            return;
        }
        equipmentService.deleteEntity(selected.getId());
        refreshTable();
        clearFields();
        showAlert("Succès", "Équipement supprimé avec succès", Alert.AlertType.INFORMATION);
    }


    private void refreshTable() {
        List<Equipment> equipmentList = equipmentService.getAll();
        ObservableList<Equipment> observableList = FXCollections.observableArrayList(equipmentList);
        equipmentTable.setItems(observableList);
    }

    private void clearFields() {
        nameField.clear();
        modelField.clear();
        priceField.clear();
        dateAchatPicker.setValue(null);
        equipmentTable.getSelectionModel().clearSelection();
    }

    private boolean isInputValid() {
        String name = nameField.getText();
        String model = modelField.getText();
        String prix = priceField.getText();
        LocalDate dateAchat = dateAchatPicker.getValue();

        if (name == null || name.trim().isEmpty() ||
                model == null || model.trim().isEmpty() ||
                prix == null || prix.trim().isEmpty() ||
                dateAchat == null) {
            showAlert("Erreur", "Tous les champs sont obligatoires", Alert.AlertType.ERROR);
            return false;
        }
        try {
            double parsedPrice = Double.parseDouble(prix);
            if (parsedPrice < 0) {
                showAlert("Erreur", "Le prix doit être positif", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Prix invalide", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    @FXML
    private void handleClaimEquipment() {
        Equipment selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Sabrineviews/claim.fxml"));
                Parent root = loader.load();

                ClaimController claimController = loader.getController();
                claimController.initWithEquipment(selected);

                Stage stage = new Stage();
                stage.setTitle("Réclamation pour " + selected.getName());
                stage.setScene(new Scene(root, 600, 400));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'ouvrir l'interface de réclamation: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un équipement", Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
