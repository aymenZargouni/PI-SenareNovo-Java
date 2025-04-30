package ed.sanarenovo.controllers.GestionEquipement;

import ed.sanarenovo.entities.Claim;
import ed.sanarenovo.entities.Equipment;
import ed.sanarenovo.entities.Technicien;
import ed.sanarenovo.services.ClaimService;
import ed.sanarenovo.services.EquipmentService;
import ed.sanarenovo.services.TechnicienService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ClaimController {

    private final ClaimService claimService = new ClaimService();
    private final EquipmentService equipmentService = new EquipmentService();
    private final TechnicienService technicienService = new TechnicienService();

    @FXML private TableView<Claim> claimTable;
    @FXML private ComboBox<Equipment> equipmentCombo;
    @FXML private ComboBox<Technicien> technicienCombo;
    @FXML private TextArea descriptionArea;

    @FXML
    public void initialize() {
        loadEquipments();
        loadTechniciens();
        refreshClaims();
        configureComboBoxes();
        configureTable();
    }

    public void initWithEquipment(Equipment equipment) {
        equipmentCombo.getSelectionModel().select(equipment);
        equipmentCombo.setDisable(true);
    }

    private void configureComboBoxes() {
        equipmentCombo.setCellFactory(lv -> new ListCell<Equipment>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        technicienCombo.setCellFactory(lv -> new ListCell<Technicien>() {
            @Override
            protected void updateItem(Technicien item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " - " + item.getNom());
                }
            }
        });

        technicienCombo.setButtonCell(new ListCell<Technicien>() {
            @Override
            protected void updateItem(Technicien item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " - " + item.getNom());
                }
            }
        });
    }

    private void configureTable() {
        claimTable.setRowFactory(tv -> new TableRow<Claim>() {
            @Override
            protected void updateItem(Claim item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && claimService.isClaimOlderThan24Hours(item.getId())) {
                    setStyle("-fx-background-color: #fff3cd;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadEquipments() {
        List<Equipment> equipmentList = equipmentService.getAll();
        equipmentCombo.setItems(FXCollections.observableArrayList(equipmentList));
    }


    private void refreshClaims() {
        List<Claim> claims = claimService.getAll();
        claimTable.getItems().setAll(claims);
    }

    @FXML
    private void handleCreateClaim() {
        Equipment equipment = equipmentCombo.getSelectionModel().getSelectedItem();
        Technicien technicien = technicienCombo.getSelectionModel().getSelectedItem();
        String description = descriptionArea.getText().trim();

        // Validation
        StringBuilder errors = new StringBuilder();

        if (equipment == null) {
            errors.append("- Veuillez sélectionner un équipement\n");
        }

        if (technicien == null) {
            errors.append("- Veuillez sélectionner un technicien\n");
        }

        if (description.isEmpty()) {
            errors.append("- Veuillez saisir une description\n");
        }

        if (errors.length() > 0) {
            showAlert("Erreur de validation", errors.toString(), Alert.AlertType.ERROR);
            return;
        }

        // Vérification des mots inappropriés
        if (claimService.containsBadWords(description)) {
            showAlert("Langage inapproprié",
                    "Votre réclamation contient des termes inappropriés.\n"
                            + "Veuillez reformuler votre demande.",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            claimService.claimEquipment(equipment, technicien.getId(), description);

            descriptionArea.clear();
            refreshClaims();
            showAlert("Succès", "Réclamation créée avec succès", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la création: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }



    private void loadTechniciens() {
        List<Technicien> techniciens = technicienService.getAllTechniciens();

        technicienCombo.setItems(FXCollections.observableArrayList(techniciens));

        technicienCombo.setCellFactory(lv -> new ListCell<Technicien>() {
            @Override
            protected void updateItem(Technicien item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getId() == 0) {
                    setText("");
                } else {
                    setText(item.getId() + " - " + item.getNom());
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleOpenClaimsManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Sabrineviews/claims_management.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Réclamations");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des réclamations", Alert.AlertType.ERROR);
        }
    }
}