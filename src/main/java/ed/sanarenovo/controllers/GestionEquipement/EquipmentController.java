package ed.sanarenovo.controllers.GestionEquipement;

import ed.sanarenovo.entities.Equipment;
import ed.sanarenovo.services.EquipmentService;
import ed.sanarenovo.utils.PriceTableCell;
import ed.sanarenovo.utils.StatusTableCell;
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
    private static final double EUR_TO_TND_RATE = 3.3;

    @FXML
    private TableView<Equipment> equipmentTable;
    @FXML
    private TextField nameField;
    @FXML
    private TextField modelField;
    @FXML
    private TextField priceField;
    @FXML
    private DatePicker dateAchatPicker;
    @FXML
    private ToggleGroup currencyToggleGroup;

    private String currentCurrency = "EUR";

    @FXML
    public void initialize() {
        refreshTable();
        dateAchatPicker.setValue(LocalDate.now());

        currencyToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                currentCurrency = ((RadioButton) newToggle).getUserData().toString();
                refreshTable();
            }
        });

        equipmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                modelField.setText(newSelection.getModel());

                double price = newSelection.getPrix();
                if (currentCurrency.equals("TND")) {
                    price = price * EUR_TO_TND_RATE;
                }
                TableColumn<Equipment, Double> priceColumn = (TableColumn<Equipment, Double>) equipmentTable.getColumns().get(4);
                priceColumn.setCellFactory(new PriceTableCell(this));
                if (newSelection.getDateAchat() != null) {
                    dateAchatPicker.setValue(newSelection.getDateAchat().toLocalDate());
                }
            }
        });
    }

    public String getCurrentCurrency() {
        return currentCurrency;
    }

    @FXML
    private void handleCurrencyChange() {
        RadioButton selectedRadioButton = (RadioButton) currencyToggleGroup.getSelectedToggle();
        String newCurrency = selectedRadioButton.getUserData().toString();

        if (!currentCurrency.equals(newCurrency)) {
            String priceText = priceField.getText();
            if (!priceText.isEmpty()) {
                try {
                    double price = Double.parseDouble(priceText);
                    if (currentCurrency.equals("EUR") && newCurrency.equals("TND")) {
                        priceField.setText(String.format("%.2f", price * EUR_TO_TND_RATE));
                    } else if (currentCurrency.equals("TND") && newCurrency.equals("EUR")) {
                        priceField.setText(String.format("%.2f", price / EUR_TO_TND_RATE));
                    }
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Prix invalide", Alert.AlertType.ERROR);
                }
            }
            currentCurrency = newCurrency;
            refreshTable();
        }
    }

    @FXML
    private void handleAddEquipment() {
        if (!isInputValid()) return;

        try {
            double price = Double.parseDouble(priceField.getText());
            if (currentCurrency.equals("TND")) {
                price = price / EUR_TO_TND_RATE; // Convertir en EUR pour le stockage
            }

            Date dateAchat = Date.valueOf(dateAchatPicker.getValue());
            Equipment equipment = new Equipment(
                    nameField.getText().trim(),
                    modelField.getText().trim(),
                    dateAchat,
                    price
            );

            equipmentService.addEntity(equipment);
            refreshTable();
            clearFields();
            showAlert("Succès", "Équipement ajouté avec succès", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Prix invalide", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdateEquipment() {
        Equipment selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Veuillez sélectionner un équipement", Alert.AlertType.WARNING);
            return;
        }
        if (!isInputValid()) return;

        try {
            double price = Double.parseDouble(priceField.getText());
            if (currentCurrency.equals("TND")) {
                price = price / EUR_TO_TND_RATE; // Convertir en EUR pour le stockage
            }

            selected.setName(nameField.getText().trim());
            selected.setModel(modelField.getText().trim());
            selected.setPrix(price);
            selected.setDateAchat(Date.valueOf(dateAchatPicker.getValue()));

            equipmentService.updateEntity(selected, selected.getId());
            refreshTable();
            clearFields();
            showAlert("Succès", "Équipement modifié avec succès", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Prix invalide", Alert.AlertType.ERROR);
        }
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

        // Configuration de la colonne prix
        TableColumn<Equipment, Double> priceColumn = (TableColumn<Equipment, Double>) equipmentTable.getColumns().get(4);
        priceColumn.setCellFactory(new PriceTableCell(this));

        // Configuration de la colonne statut
        TableColumn<Equipment, String> statusColumn = (TableColumn<Equipment, String>) equipmentTable.getColumns().get(3); // Statut est la 4e colonne (index 3)
        statusColumn.setCellFactory(col -> new StatusTableCell());
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

    @FXML
    private void handleOpenHistorique() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Sabrineviews/HistoriqueView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Historique des Réparations");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface Historique: " + e.getMessage(), Alert.AlertType.ERROR);
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