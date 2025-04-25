package ed.sanarenovo.controllers.consultation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ed.sanarenovo.entities.consultation;
import ed.sanarenovo.utils.MyConnection;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.layout.StackPane;
import org.json.JSONObject;

public class ConsultationController implements Initializable {

    @FXML private TextField motifField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> typeConsultationComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<Integer> dossierComboBox;

    @FXML private Label dateError;
    @FXML private Label motifError;
    @FXML private Label typeError;
    @FXML private Label statusError;
    @FXML private Label dossierError;

    @FXML private TableView<consultation> tableView;
    @FXML private TableColumn<consultation, Integer> colId;
    @FXML private TableColumn<consultation, String> colDate;
    @FXML private TableColumn<consultation, String> colMotif;
    @FXML private TableColumn<consultation, String> colTypeConsultation;
    @FXML private TableColumn<consultation, String> colStatus;
    @FXML private TableColumn<consultation, Integer> colDossierId;
    @FXML private WebView webView;

    private static final String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmFwcGVhci5pbiIsImF1ZCI6Imh0dHBzOi8vYXBpLmFwcGVhci5pbi92MSIsImV4cCI6OTAwNzE5OTI1NDc0MDk5MSwiaWF0IjoxNzQ1NDQzNTA1LCJvcmdhbml6YXRpb25JZCI6MzE0NzI3LCJqdGkiOiJjMGMwYjRmOC1hYzYzLTQxNWQtYmNjZS1lYTI2NTkwMDVkMGQifQ.N5aBKxCNxZr2OGMYTGUD7qMwO_Nxkk9NmoZ6zewT3cc"; // Ta clé API directe
    private final ObservableList<consultation> data = FXCollections.observableArrayList();
    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    public void sessionOnline() {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                showErrorDialog("Clé API introuvable. Veuillez la définir dans le code.");
                return;
            }

            var data = Map.of(
                    "endDate", "2026-04-18T14:23:00.000Z",
                    "fields", Collections.singletonList("hostRoomUrl")
            );

            var request = HttpRequest.newBuilder(URI.create("https://api.whereby.dev/v1/meetings"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(data)))
                    .build();

            var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode == 200 || statusCode == 201) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(responseBody);
                String meetingUrl = jsonNode.get("hostRoomUrl").asText();

                // Open in default system browser:
                java.awt.Desktop.getDesktop().browse(new URI(meetingUrl));
            } else if (statusCode == 403) {
                showErrorDialog("Erreur 403 : Clé API invalide ou sans permissions.");
            } else {
                showErrorDialog("Erreur " + statusCode + ": " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Une erreur est survenue : " + e.getMessage());
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colTypeConsultation.setCellValueFactory(new PropertyValueFactory<>("typeConsultation"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDossierId.setCellValueFactory(new PropertyValueFactory<>("dossiermedicaleId"));

        loadConsultations();
        loadDossiers();

        tableView.setOnMouseClicked(this::handleTableClick);

        typeConsultationComboBox.setItems(FXCollections.observableArrayList("En ligne", "Presentiel"));
        statusComboBox.setItems(FXCollections.observableArrayList("En attente", "En cours", "Terminée"));


        statusComboBox.setDisable(true);
    }

    private void loadConsultations() {
        data.clear();
        String query = "SELECT * FROM consultation";
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

    private void handleTableClick(MouseEvent event) {
        consultation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            motifField.setText(selected.getMotif());
            datePicker.setValue(LocalDate.parse(selected.getDate()));
            typeConsultationComboBox.setValue(selected.getTypeConsultation());
            statusComboBox.setValue(selected.getStatus());
            dossierComboBox.setValue(selected.getDossiermedicaleId());


            statusComboBox.setDisable(false);
        }
    }

    @FXML
    void ajouterConsultation(javafx.event.ActionEvent event) {
        if (!validateInputs(false)) return;

        String query = "INSERT INTO consultation (date, motif, typeconsultation, status, dossiermedicale_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection cnx = MyConnection.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setString(1, datePicker.getValue().toString());
            pst.setString(2, motifField.getText());
            pst.setString(3, typeConsultationComboBox.getValue());
            pst.setString(4, "En attente"); // Statut forcé
            pst.setInt(5, dossierComboBox.getValue());

            pst.executeUpdate();
            clearFields(null);
            loadConsultations();

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                pst.setString(4, statusComboBox.getValue()); // Médecin peut modifier
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
            String query = "DELETE FROM consultation WHERE id = ?";
            try (Connection cnx = MyConnection.getInstance().getCnx();
                 PreparedStatement pst = cnx.prepareStatement(query)) {

                pst.setInt(1, selected.getId());
                pst.executeUpdate();
                loadConsultations();

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

        // Re-désactiver statut (mode ajout)
        statusComboBox.setDisable(true);
    }

    private boolean validateInputs(boolean isUpdate) {
        boolean isValid = true;

        // Reset erreurs
        dateError.setText("");
        motifError.setText("");
        typeError.setText("");
        statusError.setText("");
        dossierError.setText("");

        datePicker.getStyleClass().remove("input-error");
        motifField.getStyleClass().remove("input-error");
        typeConsultationComboBox.getStyleClass().remove("input-error");
        statusComboBox.getStyleClass().remove("input-error");
        dossierComboBox.getStyleClass().remove("input-error");

        // Date
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

        // Motif
        if (motifField.getText().trim().isEmpty()) {
            motifError.setText("Le motif est obligatoire.");
            motifField.getStyleClass().add("input-error");
            isValid = false;
        }

        // Type consultation
        if (typeConsultationComboBox.getValue() == null) {
            typeError.setText("Veuillez sélectionner un type.");
            typeConsultationComboBox.getStyleClass().add("input-error");
            isValid = false;
        }

        // Statut (uniquement à l'update)
        if (isUpdate && statusComboBox.getValue() == null) {
            statusError.setText("Veuillez sélectionner un statut.");
            statusComboBox.getStyleClass().add("input-error");
            isValid = false;
        }

        // Dossier
        if (dossierComboBox.getValue() == null) {
            dossierError.setText("Veuillez sélectionner un dossier.");
            dossierComboBox.getStyleClass().add("input-error");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void redirectToDossierMedicale() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Youssef_views/dm.fxml"));
            Scene scene = new Scene(root);

            // Ajoute la feuille de style CSS seulement si elle existe
            URL cssUrl = getClass().getResource("/Youssef_views/design.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠️ CSS file not found: /design.css");
            }

            // Récupère la fenêtre actuelle
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

            // Récupère la fenêtre actuelle
            Stage currentStage = (Stage) tableView.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
