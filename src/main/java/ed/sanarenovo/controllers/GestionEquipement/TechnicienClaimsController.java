package ed.sanarenovo.controllers.GestionEquipement;

import ed.sanarenovo.entities.Claim;
import ed.sanarenovo.services.ClaimService;
import ed.sanarenovo.services.TechnicienService;
import ed.sanarenovo.utils.ReclamationIA;
import ed.sanarenovo.utils.UserSession;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TechnicienClaimsController {

    @FXML private TableView<Claim> claimTable;
    @FXML private TableColumn<Claim, Integer> colId;
    @FXML private TableColumn<Claim, String> colReclamation;
    @FXML private TableColumn<Claim, String> colEquipment;
    @FXML private TableColumn<Claim, String> colStatus;
    @FXML private TableColumn<Claim, Timestamp> colDate;
    @FXML private DatePicker repairDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private RadioButton radioAuto;
    @FXML private RadioButton radioManual;

    private ToggleGroup reportTypeGroup;
    private final ClaimService claimService = new ClaimService();
    private ObservableList<Claim> observableClaims;
    private int technicienId = 4;
    private TechnicienService technicienService = new TechnicienService();
    @FXML
    public void initialize() {

        // Get authenticated user's ID
        int userId = UserSession.getInstance().getUser().getId();

        // Find Technicien by user ID
        technicienId = technicienService.getTechnicienIdByUserId(userId);

        reportTypeGroup = new ToggleGroup();
        radioAuto.setToggleGroup(reportTypeGroup);
        radioManual.setToggleGroup(reportTypeGroup);
        radioAuto.setSelected(true);

        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colReclamation.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReclamation()));
        colEquipment.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEquipment().getName()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEquipment().getStatus()));
        colDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCreatedAt()));

        loadClaims();
        configureStatusComboBox();
    }

    private void loadClaims() {
        List<Claim> claims = claimService.getClaimsForTechnicien(technicienId, "panne", "maintenance");
        observableClaims = FXCollections.observableArrayList(claims);
        claimTable.setItems(observableClaims);
    }

    private void configureStatusComboBox() {
        statusComboBox.getItems().addAll("reparé", "maintenance", "panne");
        statusComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void onUpdateStatus() {
        Claim selectedClaim = claimTable.getSelectionModel().getSelectedItem();
        LocalDate repairDate = repairDatePicker.getValue();
        String newStatus = statusComboBox.getValue();

        if (!validateInputs(selectedClaim, repairDate, newStatus)) {
            return;
        }

        String rapport = generateReport(selectedClaim.getReclamation());
        if (rapport == null || rapport.isEmpty()) {
            showAlert("Erreur", "Impossible de générer le rapport");
            return;
        }

        updateEquipmentStatus(selectedClaim, newStatus, repairDate, rapport);
        refreshInterface();
    }

    private boolean validateInputs(Claim claim, LocalDate date, String status) {
        if (claim == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation");
            return false;
        }
        if (date == null) {
            showAlert("Erreur", "Veuillez sélectionner une date de réparation");
            return false;
        }
        if (status == null || status.isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner un statut");
            return false;
        }
        return true;
    }

    private String generateReport(String description) {
        if (radioAuto.isSelected()) {
            return generateAutoReport(description);
        } else {
            return showManualReportDialog();
        }
    }

    private String generateAutoReport(String description) {
        String rapport = ReclamationIA.genererRapport(description);
        if (rapport.startsWith("Erreur")) {
            showAlert("Erreur IA", rapport);
            return "";
        }
        return rapport;
    }

    private String showManualReportDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rapport Manuel");
        dialog.setHeaderText("Rédigez votre rapport technique");
        dialog.setContentText("Contenu du rapport:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }

    private void updateEquipmentStatus(Claim claim, String status, LocalDate date, String rapport) {
        claimService.updateEquipmentWithReport(
                claim.getEquipment().getId(),
                status,
                Date.valueOf(date),
                rapport
        );
    }

    private void refreshInterface() {
        loadClaims();
        repairDatePicker.setValue(null);
        statusComboBox.getSelectionModel().clearSelection();
        showAlert("Succès", "Mise à jour effectuée avec succès");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}