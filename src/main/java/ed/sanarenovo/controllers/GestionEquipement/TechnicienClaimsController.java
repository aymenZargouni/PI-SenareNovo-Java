package ed.sanarenovo.controllers.GestionEquipement;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import ed.sanarenovo.entities.Claim;
import ed.sanarenovo.services.ClaimService;
import ed.sanarenovo.utils.CredentialService;
import ed.sanarenovo.utils.ReclamationIA;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;
import java.security.GeneralSecurityException;
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
    @FXML private TextArea rapportTextArea;
    @FXML private TextArea descriptionTextArea;

    private final ClaimService claimService = new ClaimService();
    private final int technicienId = 4; // Devrait être injecté
    private HostServices hostServices;

    // Setter pour l'injection de HostServices
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    public void initialize() {
        configureRadioButtons();
        configureTableColumns();
        configureStatusComboBox();
        loadClaims();
    }

    private void configureRadioButtons() {
        ToggleGroup reportTypeGroup = new ToggleGroup();
        radioAuto.setToggleGroup(reportTypeGroup);
        radioManual.setToggleGroup(reportTypeGroup);
        radioAuto.setSelected(true);
    }

    private void configureTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReclamation.setCellValueFactory(new PropertyValueFactory<>("reclamation"));
        colEquipment.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEquipment().getName()));
        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEquipment().getStatus()));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

    }


    private void loadClaims() {
        claimTable.getItems().setAll(
                claimService.getClaimsForTechnicien(technicienId, "panne", "maintenance"));
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de générer le rapport");
            return;
        }

        updateEquipmentStatus(selectedClaim, newStatus, repairDate, rapport);
    }

    private boolean validateInputs(Claim claim, LocalDate date, String status) {
        if (claim == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une réclamation");
            return false;
        }
        if (date == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une date de réparation");
            return false;
        }
        return true;
    }

    private String generateReport(String description) {
        try {
            if (radioAuto.isSelected()) {
                return ReclamationIA.genererRapport(description);
            } else {
                return showManualReportDialog();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur IA", "Problème de connexion : " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur inattendue", e.getMessage());
        }
        return "";
    }

    @FXML
    private void onGenerateRapport() {
        String description = descriptionTextArea.getText();

        if (description == null || description.isBlank()) {
            rapportTextArea.setText("Veuillez entrer une description.");
            return;
        }

        rapportTextArea.setText("Génération du rapport...");

        new Thread(() -> {
            try {
                String rapport = ReclamationIA.genererRapport(description);
                Platform.runLater(() -> rapportTextArea.setText(rapport));
            } catch (IOException e) {
                Platform.runLater(() -> rapportTextArea.setText("Erreur lors de la génération : " + e.getMessage()));
            }
        }).start();
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
        refreshInterface();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Mise à jour effectuée avec succès");
    }

    private void refreshInterface() {
        loadClaims();
        repairDatePicker.setValue(null);
        statusComboBox.getSelectionModel().selectFirst();
    }

//    @FXML
//    private void handleViewCalendar() {
//        try {
//            List<Event> events = getCalendarEvents();
//
//            Stage stage = new Stage();
//            TableView<Event> tableView = new TableView<>();
//
//            TableColumn<Event, String> titleCol = new TableColumn<>("Titre");
//            titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSummary()));
//
//            TableColumn<Event, String> dateCol = new TableColumn<>("Date");
//            dateCol.setCellValueFactory(data -> {
//                String startDate = data.getValue().getStart().getDateTime() != null ?
//                        data.getValue().getStart().getDateTime().toStringRfc3339() :
//                        data.getValue().getStart().getDate().toStringRfc3339();
//                return new SimpleStringProperty(startDate);
//            });
//
//            tableView.getColumns().addAll(titleCol, dateCol);
//            tableView.getItems().addAll(events);
//
//            stage.setScene(new Scene(tableView, 600, 400));
//            stage.setTitle("Calendrier des Réclamations");
//            stage.show();
//
//        } catch (GeneralSecurityException | IOException e) {
//            showAlert(Alert.AlertType.ERROR, "Erreur Calendrier",
//                    "Impossible de charger le calendrier: " + e.getMessage());
//        }
//    }
@FXML
private void handleViewCalendar() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Sabrineviews/CalendarView.fxml"));
        Parent root = loader.load();

        // Vous pouvez passer des paramètres au contrôleur si besoin
        CalendarViewController controller = loader.getController();

        Stage stage = new Stage();
        stage.setTitle("Calendrier des Réclamations");
        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    } catch (IOException e) {
        showAlert(Alert.AlertType.ERROR, "Erreur Calendrier",
                  "Impossible de charger le calendrier: " + e.getMessage());   }
}
    private List<Event> getCalendarEvents() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(
                HTTP_TRANSPORT,
                GsonFactory.getDefaultInstance(),
                CredentialService.getCredentials(HTTP_TRANSPORT))
                .setApplicationName("SanareNovo Claims")
                .build();

        // Récupère les événements des 30 prochains jours
        String now = new com.google.api.client.util.DateTime(System.currentTimeMillis()).toStringRfc3339();
        String future = new com.google.api.client.util.DateTime(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000).toStringRfc3339();

        Events events = service.events().list("primary")
                .setTimeMin(new com.google.api.client.util.DateTime(now))
                .setTimeMax(new com.google.api.client.util.DateTime(future))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setQ("Réclamation") // Filtre pour ne voir que les réclamations
                .execute();

        return events.getItems();
    }

    private void showEventInCalendar(int claimId) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(
                    HTTP_TRANSPORT,
                    GsonFactory.getDefaultInstance(),
                    CredentialService.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName("SanareNovo Claims")
                    .build();

            Events events = service.events().list("primary")
                    .setQ("Réclamation #" + claimId)
                    .execute();

            if (!events.getItems().isEmpty()) {
                String eventUrl = events.getItems().get(0).getHtmlLink();
                if (hostServices != null) {
                    hostServices.showDocument(eventUrl);
                } else {
                    // Fallback si hostServices n'est pas disponible
                    openInBrowser(eventUrl);
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Information",
                        "Aucun événement trouvé pour cette réclamation");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'afficher l'événement: " + e.getMessage());
        }
    }

    // Méthode de secours pour ouvrir le navigateur
    private void openInBrowser(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                // Fallback pour les systèmes sans Desktop
                Runtime runtime = Runtime.getRuntime();
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    runtime.exec("open " + url);
                } else {
                    runtime.exec("xdg-open " + url);
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Avertissement",
                    "Impossible d'ouvrir le navigateur. Lien disponible : " + url);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}