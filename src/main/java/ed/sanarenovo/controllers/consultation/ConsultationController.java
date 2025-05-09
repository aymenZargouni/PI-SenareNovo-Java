package ed.sanarenovo.controllers.consultation;


import com.sun.speech.freetts.VoiceManager;
import ed.sanarenovo.entities.consultation;
import ed.sanarenovo.utils.MyConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import org.json.JSONObject;
import javafx.embed.swing.SwingNode;
import com.sun.speech.freetts.Voice;



public class ConsultationController implements Initializable {

    @FXML
    private TextField motifField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> typeConsultationComboBox;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private ComboBox<Integer> dossierComboBox;
    @FXML
    private ComboBox<String> patientComboBox;

    @FXML
    private Label dateError;
    @FXML
    private Label motifError;
    @FXML
    private Label typeError;
    @FXML
    private Label statusError;
    @FXML
    private Label dossierError;
    @FXML
    private Label patientError;

    @FXML
    private TableView<consultation> tableView;
    @FXML
    private TableColumn<consultation, Integer> colId;
    @FXML
    private TableColumn<consultation, String> colDate;
    @FXML
    private TableColumn<consultation, String> colMotif;
    @FXML
    private TableColumn<consultation, String> colTypeConsultation;
    @FXML
    private TableColumn<consultation, String> colStatus;
    @FXML
    private TableColumn<consultation, Integer> colDossierId;
    @FXML
    private TableColumn<consultation, String> colPatientName;
    @FXML
    private TableColumn<consultation, Void> colPlay;
    @FXML
    private TableColumn<consultation, Void> colMeeting;

    // Video conferencing components
    @FXML
    private ImageView meetingPreviewImage;
    @FXML
    private Label meetingStatusLabel;
    @FXML
    private Button startMeetingBtn;
    @FXML
    private Button endMeetingBtn;
    @FXML
    private Button copyUrlBtn;
    @FXML
    private StackPane meetingContainer;


    private SwingNode swingNode;

    private static final String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmFwcGVhci5pbiIsImF1ZCI6Imh0dHBzOi8vYXBpLmFwcGVhci5pbi92MSIsImV4cCI6OTAwNzE5OTI1NDc0MDk5MSwiaWF0IjoxNzQ1NDQzNTA1LCJvcmdhbml6YXRpb25JZCI6MzE0NzI3LCJqdGkiOiJjMGMwYjRmOC1hYzYzLTQxNWQtYmNjZS1lYTI2NTkwMDVkMGQifQ.N5aBKxCNxZr2OGMYTGUD7qMwO_Nxkk9NmoZ6zewT3cc";
    private String currentMeetingUrl;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ObservableList<consultation> data = FXCollections.observableArrayList();

    private void updateMeetingUrlInDatabase(int consultationId, String meetingUrl) {
        String query = "UPDATE consultation SET meeting_url = ? WHERE id = ?";
        try (Connection cnx = MyConnection.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setString(1, meetingUrl);
            pst.setInt(2, consultationId);
            pst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        Platform.runLater(() -> {
            tableView.refresh();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colTypeConsultation.setCellValueFactory(new PropertyValueFactory<>("typeConsultation"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDossierId.setCellValueFactory(new PropertyValueFactory<>("dossiermedicaleId"));

        loadConsultations();
        loadDossiers();
        loadPatients();

        tableView.setOnMouseClicked(this::handleTableClick);

        typeConsultationComboBox.setItems(FXCollections.observableArrayList("En ligne", "Presentiel"));
        statusComboBox.setItems(FXCollections.observableArrayList("En attente", "En cours", "Terminée"));

        statusComboBox.setDisable(true);

        // Add play button column
        colPlay.setCellFactory(param -> new TableCell<>() {
            private final Button playButton = new Button("Play");

            {
                // Style the button
                playButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");

                // Add hover effects
                playButton.setOnMouseEntered(e -> playButton.setStyle(
                        "-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-size: 12px;"));
                playButton.setOnMouseExited(e -> playButton.setStyle(
                        "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;"));

                playButton.setOnAction(event -> {
                    consultation c = getTableView().getItems().get(getIndex());
                    speakConsultationDetails(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(playButton);
                }
            }
        });

        colMeeting.setCellFactory(param -> new TableCell<>() {
            private final Button meetingButton = new Button();

            {
                // Style de base du bouton
                meetingButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");

                meetingButton.setOnAction(event -> {
                    consultation c = getTableView().getItems().get(getIndex());
                    if (c.getMeetingUrl() != null && !c.getMeetingUrl().isEmpty()) {
                        openMeetingInDesktop(c.getMeetingUrl());
                    } else {
                        try {
                            String meetingUrl = createWherebyMeeting();
                            c.setMeetingUrl(meetingUrl);
                            refreshTable();
                            openMeetingInDesktop(meetingUrl);
                        } catch (Exception e) {
                            showErrorDialog("Erreur lors de la création du meeting: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    consultation c = getTableView().getItems().get(getIndex());
                    if (c.getMeetingUrl() != null && !c.getMeetingUrl().isEmpty()) {
                        meetingButton.setText("Join");
                        meetingButton.setStyle("-fx-background-color: #3498db;");
                    } else {
                        meetingButton.setText("Create");
                        meetingButton.setStyle("-fx-background-color: #2ecc71;");
                    }
                    setGraphic(meetingButton);
                }
            }
        });

    }


    // Video Conferencing Methods ==============================================

    @FXML
    private String createWherebyMeeting() throws Exception {
        try {
            String endDate = ZonedDateTime.now()
                    .plusHours(1)
                    .format(DateTimeFormatter.ISO_INSTANT);

            JSONObject requestBody = new JSONObject();
            requestBody.put("endDate", endDate);
            requestBody.put("fields", new String[]{"hostRoomUrl", "roomUrl"});
            requestBody.put("roomNamePrefix", "MedicalConsultation");
            requestBody.put("roomMode", "normal");

            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.whereby.dev/v1/meetings").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    currentMeetingUrl = jsonResponse.getString("roomUrl");
                    return currentMeetingUrl;
                }
            } else {
                throw new RuntimeException("API Error: " + responseCode + " - " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            throw new Exception("Failed to create meeting: " + e.getMessage(), e);
        }
    }

    @FXML
    private void copyMeetingUrl() {
        if (currentMeetingUrl != null && !currentMeetingUrl.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(currentMeetingUrl);
            clipboard.setContent(content);

            // Show confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Meeting URL copied to clipboard");
            alert.showAndWait();
        }
    }

    @FXML
    private void openMeetingInDesktop(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                Desktop.getDesktop().browse(new URI(url));
            } else {
                copyMeetingUrl(); // Fallback to copy if can't open browser
            }
        } catch (Exception e) {
            showErrorDialog("Failed to open browser: " + e.getMessage());
            copyMeetingUrl(); // Final fallback
        }
    }

    private void speakConsultationDetails(consultation c) {
        new Thread(() -> {
            try {
                // Configure FreeTTS
                System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");

                String text = String.format(
                        "Consultation. Patient %s. Date %s. Motif %s. Type %s. Statut %s.",
                        c.getFullname(), c.getDate(), c.getMotif(),
                        c.getTypeConsultation(), c.getStatus()
                );

                // Initialize FreeTTS
                VoiceManager voiceManager = VoiceManager.getInstance();
                Voice voice = voiceManager.getVoice("kevin16"); // Voix masculine anglaise

                if (voice == null) {
                    voice = voiceManager.getVoices()[0]; // Fallback à la première voix disponible
                }

                voice.allocate();
                voice.speak(text);
                voice.deallocate();

            } catch (Exception e) {
                Platform.runLater(() -> showErrorDialog(
                        "Erreur TTS: " + e.getMessage() +
                                "\nAssurez-vous que les JARs FreeTTS sont dans le classpath"));
            }
        }).start();
    }

    // Database and Consultation Management Methods ============================

    private void loadConsultations() {
        data.clear();
        String query = "SELECT c.*, p.fullname as patientName " +
                "FROM consultation c " +
                "LEFT JOIN patient p ON c.patient_id = p.id";

        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                consultation c = new consultation();
                c.setId(rs.getInt("id"));
                c.setDate(rs.getString("date"));
                c.setMotif(rs.getString("motif"));
                c.setTypeConsultation(rs.getString("typeconsultation"));
                c.setStatus(rs.getString("status"));
                c.setDossiermedicaleId(rs.getInt("dossiermedicale_id"));

                String patientName = rs.getString("patientName");
                c.setPatientName(patientName != null ? patientName : "N/A");

                data.add(c);
            }

            tableView.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDossiers() {
        ObservableList<Integer> dossierIds = FXCollections.observableArrayList();
        String query = "SELECT id FROM dossiermedicale";
        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                dossierIds.add(rs.getInt("id"));
            }

            dossierComboBox.setItems(dossierIds);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPatients() {
        ObservableList<String> patients = FXCollections.observableArrayList();
        String query = "SELECT fullname FROM patient";
        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                patients.add(rs.getString("fullname"));
            }

            patientComboBox.setItems(patients);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleTableClick(MouseEvent event) {
        consultation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            motifField.setText(selected.getMotif());
            datePicker.setValue(LocalDate.parse(selected.getDate()));
            typeConsultationComboBox.setValue(selected.getTypeConsultation());
            statusComboBox.setValue(selected.getStatus());
            dossierComboBox.setValue(selected.getDossiermedicaleId());
            patientComboBox.setValue(selected.getFullname());

            statusComboBox.setDisable(false);
        }
    }

    private Integer getDossierIdByPatientName(String patientName) {
        String query = "SELECT d.id FROM dossiermedicale d " +
                "JOIN patient p ON d.id = p.dossiermedical_id " +
                "WHERE p.fullname = ?";
        try (Connection cnx = MyConnection.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setString(1, patientName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    void ajouterConsultation(javafx.event.ActionEvent event) {
        if (!validateInputs(false)) return;

        Integer patientId = getPatientIdByName(patientComboBox.getValue());
        if (patientId == null) {
            showErrorDialog("Patient non trouvé.");
            return;
        }

        String query = "INSERT INTO consultation (date, motif, typeconsultation, status, dossiermedicale_id, patient_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = MyConnection.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, datePicker.getValue().toString());
            pst.setString(2, motifField.getText());
            pst.setString(3, typeConsultationComboBox.getValue());
            pst.setString(4, "En attente");
            pst.setInt(5, dossierComboBox.getValue());
            pst.setInt(6, patientId);

            pst.executeUpdate();

            // Créer un nouvel objet consultation pour l'ajouter à la table
            consultation newConsultation = new consultation();
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newConsultation.setId(generatedKeys.getInt(1));
                }
            }
            newConsultation.setDate(datePicker.getValue().toString());
            newConsultation.setMotif(motifField.getText());
            newConsultation.setTypeConsultation(typeConsultationComboBox.getValue());
            newConsultation.setStatus("En attente");
            newConsultation.setDossiermedicaleId(dossierComboBox.getValue());
            newConsultation.setPatientName(patientComboBox.getValue());

            // Si c'est une consultation en ligne, créer un meeting
            if ("En ligne".equals(typeConsultationComboBox.getValue())) {
                try {
                    String meetingUrl = createWherebyMeeting();
                    newConsultation.setMeetingUrl(meetingUrl);
                } catch (Exception e) {
                    showErrorDialog("Erreur lors de la création du meeting: " + e.getMessage());
                }
            }

            data.add(newConsultation);
            clearFields(null);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer getPatientIdByName(String patientName) {
        String query = "SELECT id FROM patient WHERE fullname = ?";
        try (Connection cnx = MyConnection.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setString(1, patientName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    void updateConsultation(javafx.event.ActionEvent event) {
        if (!validateInputs(true)) return;

        consultation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "UPDATE consultation SET date = ?, motif = ?, typeconsultation = ?, status = ?, dossiermedicale_id = ? WHERE id = ?";
            try (Connection cnx = MyConnection.getInstance().getCnx();
                 PreparedStatement pst = cnx.prepareStatement(query)) {

                pst.setString(1, datePicker.getValue().toString());
                pst.setString(2, motifField.getText());
                pst.setString(3, typeConsultationComboBox.getValue());
                pst.setString(4, statusComboBox.getValue());
                pst.setInt(5, dossierComboBox.getValue());
                pst.setInt(6, selected.getId());

                pst.executeUpdate();
                clearFields(null);
                loadConsultations();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void supprimerConsultation(javafx.event.ActionEvent event) {
        consultation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Suppression en base
            String query = "DELETE FROM consultation WHERE id = ?";
            try (Connection cnx = MyConnection.getInstance().getCnx();
                 PreparedStatement pst = cnx.prepareStatement(query)) {

                pst.setInt(1, selected.getId());
                pst.executeUpdate();

                // Suppression de la liste affichée
                data.remove(selected);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void clearFields(javafx.event.ActionEvent event) {
        motifField.clear();
        datePicker.setValue(null);
        typeConsultationComboBox.setValue(null);
        statusComboBox.setValue(null);
        dossierComboBox.setValue(null);
        patientComboBox.setValue(null);

        statusComboBox.setDisable(true);
    }

    private boolean validateInputs(boolean isUpdate) {
        boolean isValid = true;

        // Reset errors
        dateError.setText("");
        motifError.setText("");
        typeError.setText("");
        statusError.setText("");
        dossierError.setText("");
        patientError.setText("");

        datePicker.getStyleClass().remove("input-error");
        motifField.getStyleClass().remove("input-error");
        typeConsultationComboBox.getStyleClass().remove("input-error");
        statusComboBox.getStyleClass().remove("input-error");
        dossierComboBox.getStyleClass().remove("input-error");
        patientComboBox.getStyleClass().remove("input-error");

        // Date validation
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            dateError.setText("La date est obligatoire.");
            datePicker.getStyleClass().add("input-error");
            isValid = false;
        } else if (selectedDate.isBefore(LocalDate.now())) {
            dateError.setText("La date ne peut pas être dans le passé.");
            datePicker.getStyleClass().add("input-error");
            isValid = false;
        }

        // Motif validation
        if (motifField.getText().trim().isEmpty()) {
            motifError.setText("Le motif est obligatoire.");
            motifField.getStyleClass().add("input-error");
            isValid = false;
        }

        // Type consultation validation
        if (typeConsultationComboBox.getValue() == null) {
            typeError.setText("Veuillez sélectionner un type.");
            typeConsultationComboBox.getStyleClass().add("input-error");
            isValid = false;
        }

        // Status validation (only for update)
        if (isUpdate && statusComboBox.getValue() == null) {
            statusError.setText("Veuillez sélectionner un statut.");
            statusComboBox.getStyleClass().add("input-error");
            isValid = false;
        }

        // Dossier validation
        if (dossierComboBox.getValue() == null) {
            dossierError.setText("Veuillez sélectionner un dossier.");
            dossierComboBox.getStyleClass().add("input-error");
            isValid = false;
        }

        // Patient validation
        if (patientComboBox.getValue() == null) {
            patientError.setText("Veuillez sélectionner un nom de patient.");
            patientComboBox.getStyleClass().add("input-error");
            isValid = false;
        }

        return isValid;
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void redirectToDossierMedicale() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Youssef_views/dm.fxml"));
            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/Youssef_views/design.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠️ CSS file not found: /design.css");
            }

            Stage currentStage = (Stage) tableView.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void redirectTostat() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Youssef_views/Statistiques.fxml"));
            Scene scene = new Scene(root);

            Stage currentStage = (Stage) tableView.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void redirectTorv() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Youssef_views/rendez_vousMed.fxml"));
            Scene scene = new Scene(root);

            // Ajoute la feuille de style CSS seulement si elle existe
            URL cssUrl = getClass().getResource("/Youssef_views/design.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠️ CSS file not found: /cons.css");
            }

            // Récupère la fenêtre actuelle
            Stage currentStage = (Stage) tableView.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void redirectToCalendar(javafx.event.ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Youssef_views/ConsultationCalendar.fxml"));
            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/Youssef_views/design.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠️ CSS file not found: /design.css");
            }

            Stage currentStage = (Stage) tableView.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}