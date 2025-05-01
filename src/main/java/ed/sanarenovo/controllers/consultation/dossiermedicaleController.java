package ed.sanarenovo.controllers.consultation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ed.sanarenovo.entities.dossiermedicale;
import ed.sanarenovo.entities.consultation;

import ed.sanarenovo.utils.MyConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class dossiermedicaleController implements Initializable {

    @FXML private TextField imcField;
    @FXML private DatePicker datePicker;
    @FXML private TextField observationsField;
    @FXML private TextField ordonnanceField;
    @FXML private Label imcMessage;
    @FXML private Label dateMessage;
    @FXML private Label observationsMessage;
    @FXML private Label ordonnanceMessage;

    @FXML private TableView<dossiermedicale> tableView;
    @FXML private TableColumn<dossiermedicale, Integer> colId;
    @FXML private TableColumn<dossiermedicale, Float> colImc;
    @FXML private TableColumn<dossiermedicale, String> colDate;
    @FXML private TableColumn<dossiermedicale, String> colObservations;
    @FXML private TableColumn<dossiermedicale, String> colOrdonnance;
    @FXML private TableColumn<dossiermedicale, String> colConsultations;
    @FXML private TextArea aiAnalysisArea;

    private final ObservableList<dossiermedicale> data = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colImc.setCellValueFactory(new PropertyValueFactory<>("imc"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colObservations.setCellValueFactory(new PropertyValueFactory<>("observations"));
        colOrdonnance.setCellValueFactory(new PropertyValueFactory<>("ordonnance"));

        // ⚠️ Important : mappe le résumé des consultations
        colConsultations.setCellValueFactory(cellData -> {
            dossiermedicale d = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(d.getConsultationSummary());
        });

        loadDossiers();
        tableView.setOnMouseClicked(this::handleTableClick);
    }

    private void loadDossiers() {
        data.clear();
        String query = "SELECT * FROM dossiermedicale";
        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ArrayList<dossiermedicale> tempList = new ArrayList<>();

            while (rs.next()) {
                dossiermedicale d = new dossiermedicale();
                d.setId(rs.getInt("id"));
                d.setImc(rs.getFloat("imc"));
                d.setDate(rs.getString("date"));
                d.setObservations(rs.getString("observations"));
                d.setOrdonnance(rs.getString("ordonnance"));

                tempList.add(d);
            }

            // Maintenant qu'on a fini avec le ResultSet, on peut charger les consultations
            for (dossiermedicale d : tempList) {
                dossiermedicale dossierWithConsultations = getDossierWithConsultations(d.getId());
                if (dossierWithConsultations != null) {
                    data.add(dossierWithConsultations);
                } else {
                    data.add(d); // fallback au dossier sans consultations si erreur
                }
            }

            tableView.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void handleTableClick(MouseEvent event) {
        dossiermedicale selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            imcField.setText(String.valueOf(selected.getImc()));
            datePicker.setValue(LocalDate.parse(selected.getDate()));
            observationsField.setText(selected.getObservations());
            ordonnanceField.setText(selected.getOrdonnance());

            // Call the method that takes an int parameter
            String analysis = analyzeDossier(selected.getId());
            aiAnalysisArea.setText(analysis);
        }
    }

    @FXML
    void ajouterDossier(javafx.event.ActionEvent event) {
        if (!validateInputs()) return;

        String query = "INSERT INTO dossiermedicale (imc, date, observations, ordonnance) VALUES (?, ?, ?, ?)";
        try (Connection cnx = MyConnection.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setFloat(1, Float.parseFloat(imcField.getText()));
            pst.setString(2, datePicker.getValue().toString());
            pst.setString(3, observationsField.getText());
            pst.setString(4, ordonnanceField.getText());

            pst.executeUpdate();
            clearFields(null);
            loadDossiers();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void updateDossier(javafx.event.ActionEvent event) {
        if (!validateInputs()) return;

        dossiermedicale selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "UPDATE dossiermedicale SET imc = ?, date = ?, observations = ?, ordonnance = ? WHERE id = ?";
            try (Connection cnx = MyConnection.getInstance().getCnx();
                 PreparedStatement pst = cnx.prepareStatement(query)) {

                pst.setFloat(1, Float.parseFloat(imcField.getText()));
                pst.setString(2, datePicker.getValue().toString());
                pst.setString(3, observationsField.getText());
                pst.setString(4, ordonnanceField.getText());
                pst.setInt(5, selected.getId());

                pst.executeUpdate();
                clearFields(null);
                loadDossiers();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    void supprimerDossier(javafx.event.ActionEvent event) {
        dossiermedicale selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "DELETE FROM dossiermedicale WHERE id = ?";
            try (Connection cnx = MyConnection.getInstance().getCnx();
                 PreparedStatement pst = cnx.prepareStatement(query)) {

                pst.setInt(1, selected.getId());
                pst.executeUpdate();
                loadDossiers();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void clearFields(javafx.event.ActionEvent event) {
        imcField.clear();
        datePicker.setValue(null);
        observationsField.clear();
        ordonnanceField.clear();
    }

    public dossiermedicale getDossierWithConsultations(int dossierId) {
        dossiermedicale dossier = null;

        String sql = """
            SELECT 
                d.id AS dossier_id, 
                d.imc, 
                d.date AS dossier_date, 
                d.observations, 
                d.ordonnance,
                c.id AS consultation_id, 
                c.date AS consultation_date, 
                c.motif, 
                c.typeconsultation, 
                c.status
            FROM dossiermedicale d
            LEFT JOIN consultation c ON d.id = c.dossiermedicale_id
            WHERE d.id = ?
        """;

        try (Connection cnx = MyConnection.getInstance().getCnx();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {

            stmt.setInt(1, dossierId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                if (dossier == null) {
                    dossier = new dossiermedicale();
                    dossier.setId(rs.getInt("dossier_id"));
                    dossier.setImc((float) rs.getDouble("imc"));
                    dossier.setDate(rs.getString("dossier_date"));
                    dossier.setObservations(rs.getString("observations"));
                    dossier.setOrdonnance(rs.getString("ordonnance"));
                    dossier.setConsultations(new ArrayList<>());
                }

                int consultationId = rs.getInt("consultation_id");
                if (consultationId > 0) {
                    consultation c = new consultation();
                    c.setId(consultationId);
                    c.setDate(rs.getString("consultation_date"));
                    c.setMotif(rs.getString("motif"));
                    c.setTypeConsultation(rs.getString("typeconsultation"));
                    c.setStatus(rs.getString("status"));
                    c.setDossiermedicaleId(dossier.getId());

                    dossier.getConsultations().add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dossier;
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Réinitialiser les styles et les messages
        imcField.getStyleClass().remove("input-error");
        datePicker.getStyleClass().remove("input-error");
        observationsField.getStyleClass().remove("input-error");
        ordonnanceField.getStyleClass().remove("input-error");

        imcMessage.setText("");
        dateMessage.setText("");
        observationsMessage.setText("");
        ordonnanceMessage.setText("");

        // IMC : doit être un float
        try {
            Float.parseFloat(imcField.getText());
        } catch (NumberFormatException e) {
            imcField.getStyleClass().add("input-error");
            imcMessage.setText("IMC invalide. Veuillez entrer un nombre.");
            isValid = false;
        }

        // Date : obligatoire
        if (datePicker.getValue() == null) {
            datePicker.getStyleClass().add("input-error");
            dateMessage.setText("La date est obligatoire.");
            isValid = false;
        }

        // Observations : obligatoire
        if (observationsField.getText().trim().isEmpty()) {
            observationsField.getStyleClass().add("input-error");
            observationsMessage.setText("L'observation est requise.");
            isValid = false;
        }

        // Ordonnance : obligatoire
        if (ordonnanceField.getText().trim().isEmpty()) {
            ordonnanceField.getStyleClass().add("input-error");
            ordonnanceMessage.setText("L'ordonnance est requise.");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void redirectTocons() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Youssef_views/cons.fxml"));
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

    private Map<String, Object> getDossierDataForAnalysis(int dossierId) {
        Map<String, Object> data = new HashMap<>();

        String sql = "SELECT imc, observations, ordonnance FROM dossiermedicale WHERE id = ?";
        try (Connection cnx = MyConnection.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setInt(1, dossierId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                data.put("imc", rs.getFloat("imc"));
                data.put("ordonnance", rs.getString("ordonnance"));

                // Récupérer les dates de consultation
                List<String> consultDates = new ArrayList<>();
                String datesSql = "SELECT date FROM consultation WHERE dossiermedicale_id = ?";
                try (PreparedStatement datesStmt = cnx.prepareStatement(datesSql)) {
                    datesStmt.setInt(1, dossierId);
                    ResultSet datesRs = datesStmt.executeQuery();
                    while (datesRs.next()) {
                        consultDates.add(datesRs.getString("date"));
                    }
                }
                data.put("consult_dates", consultDates);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    private String formatAnalysisResult(String jsonResult) {
        try {
            JsonNode result = new ObjectMapper().readTree(jsonResult);
            StringBuilder sb = new StringBuilder("Analyse IA:\n");

            if (result.get("imc_anomaly").asBoolean()) {
                sb.append("⚠ Anomalie IMC détectée\n");
            }

            sb.append("Type consultation: ").append(result.get("consult_type").asText()).append("\n");

            if (result.get("prescription_anomaly").asBoolean()) {
                sb.append("⚠ Ordonnance anormale\n");
            }

            if (result.get("freq_anomaly").asBoolean()) {
                sb.append("⚠ Fréquence de consultations élevée\n");
            }

            return sb.toString();
        } catch (IOException e) {
            return "Résultat d'analyse invalide";
        }
    }

    public String analyzeD(int dossierId) {
        try {
            // Récupérer les données nécessaires
            Map<String, Object> dossierData = getDossierDataForAnalysis(dossierId);

            // Ensure all required fields are present
            if (!dossierData.containsKey("imc")) dossierData.put("imc", 0f);
            if (!dossierData.containsKey("ordonnance")) dossierData.put("ordonnance", "");
            if (!dossierData.containsKey("observations")) dossierData.put("observations", "");
            if (!dossierData.containsKey("consult_dates")) dossierData.put("consult_dates", new ArrayList<>());

            // Add debug output
            System.out.println("Sending to Python: " + new ObjectMapper().writeValueAsString(dossierData));

            // Path to your Python script - adjust as needed
            String pythonScriptPath = "C:\\Users\\msi\\OneDrive\\Bureau\\projetJAVA\\PI-SenareNovo-Java\\src\\main\\resources\\Youssef_views\\analyze_dossier.py";

            Process p = new ProcessBuilder("python",
                    pythonScriptPath,
                    new ObjectMapper().writeValueAsString(dossierData))
                    .start();

            // Read output
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output = outputReader.readLine();

            // Read errors
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String error = errorReader.readLine();

            if (error != null) {
                System.err.println("Python error: " + error);
                return "Erreur d'analyse: " + error;
            }

            if (output == null || output.trim().isEmpty()) {
                return "Erreur: Le script Python n'a retourné aucun résultat";
            }

            // Format the analysis result
            JsonNode result = new ObjectMapper().readTree(output);
            StringBuilder analysis = new StringBuilder("=== Analyse du Dossier Médical ===\n\n");

            // IMC Analysis
            analysis.append("IMC: ").append(dossierData.get("imc")).append(" (")
                    .append(result.get("imc_category").asText()).append(")\n");
            if (result.get("imc_anomaly").asBoolean()) {
                analysis.append("⚠️ Anomalie: ").append(result.get("imc_category").asText()).append("\n");
            }

            // Medications
            analysis.append("\nMédicaments prescrits:\n");
            JsonNode meds = result.get("medications");
            if (meds.size() == 0) {
                analysis.append("Aucun médicament identifié\n");
            } else {
                for (JsonNode med : meds) {
                    analysis.append("- ").append(med.get("name").asText())
                            .append(" (").append(med.get("dosage").asText()).append(")\n");
                }
            }

            // Consultation frequency
            analysis.append("\nFréquence de consultation:\n");
            analysis.append("Moyenne: ").append(String.format("%.1f", result.get("avg_consult_gap").asDouble()))
                    .append(" jours entre les visites\n");
            if (result.get("freq_anomaly").asBoolean()) {
                analysis.append("⚠️ Anomalie de fréquence détectée\n");
            }

            // Text anomalies
            if (result.get("text_anomaly").asBoolean()) {
                analysis.append("\n⚠️ Anomalie détectée dans les observations\n");
            }

            // Detailed messages
            JsonNode messages = result.get("messages");
            if (messages.size() > 0) {
                analysis.append("\nDétails des anomalies:\n");
                for (JsonNode msg : messages) {
                    analysis.append("- ").append(msg.asText()).append("\n");
                }
            }

            return analysis.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur d'analyse: " + e.getMessage();
        }
    }

    @FXML
    private void analyzeDossier(ActionEvent actionEvent) {
        // Get the selected dossier from the table
        dossiermedicale selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // Call the analysis logic with the selected dossier's ID
            String analysisResult = analyzeD(selected.getId());

            // Display the result in the text area
            aiAnalysisArea.setText(analysisResult);
        } else {
            // Show error message if no dossier is selected
            aiAnalysisArea.setText("Veuillez sélectionner un dossier médical à analyser.");
        }
    }

    // This method is called from handleTableClick
    private String analyzeDossier(int dossierId) {
        return analyzeD(dossierId); // Just delegate to your existing analyzeD method
    }
}

