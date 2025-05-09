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
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
            StringBuilder sb = new StringBuilder("=== ANALYSE COMPLÈTE DU DOSSIER ===\n\n");

            // IMC Analysis
            if (result.has("imc")) {
                JsonNode imc = result.get("imc");
                sb.append("IMC: ").append(imc.get("value").asDouble()).append("\n");
                sb.append("Catégorie: ").append(imc.get("category").asText()).append("\n");
                sb.append("Évaluation: ").append(imc.get("details").asText()).append("\n");
                if (imc.get("anomaly").asBoolean()) {
                    sb.append("⚠ Anomalie détectée:\n");
                    for (JsonNode msg : imc.get("messages")) {
                        sb.append("  • ").append(msg.asText()).append("\n");
                    }
                }
                sb.append("\n");
            }

            // Prescription Analysis
            if (result.has("prescription")) {
                JsonNode prescription = result.get("prescription");
                sb.append("ORDONNANCE:\n");
                sb.append("Nombre de médicaments: ").append(prescription.get("count").asInt()).append("\n");
                sb.append("Évaluation: ").append(prescription.get("details").asText()).append("\n");
                if (prescription.get("anomaly").asBoolean()) {
                    sb.append("⚠ Anomalie détectée:\n");
                    for (JsonNode msg : prescription.get("messages")) {
                        sb.append("  • ").append(msg.asText()).append("\n");
                    }
                }

                if (prescription.has("medications")) {
                    sb.append("\nMédicaments identifiés:\n");
                    for (JsonNode med : prescription.get("medications")) {
                        sb.append("  • ").append(med.get("name").asText())
                                .append(" (").append(med.get("dosage").asText()).append(")\n");
                    }
                }
                sb.append("\n");
            }

            // Consultation Analysis
            if (result.has("consultations")) {
                JsonNode consultations = result.get("consultations");
                sb.append("CONSULTATIONS:\n");
                sb.append("Nombre: ").append(consultations.get("count").asInt()).append("\n");
                if (consultations.get("count").asInt() > 1) {
                    sb.append("Intervalle moyen: ").append(consultations.get("avg_gap_days").asDouble()).append(" jours\n");
                }
                sb.append("Évaluation: ").append(consultations.get("details").asText()).append("\n");
                if (consultations.get("anomaly").asBoolean()) {
                    sb.append("⚠ Anomalie détectée:\n");
                    for (JsonNode msg : consultations.get("messages")) {
                        sb.append("  • ").append(msg.asText()).append("\n");
                    }
                }
                sb.append("\n");
            }

            // Observations Analysis
            if (result.has("observations")) {
                JsonNode observations = result.get("observations");
                sb.append("OBSERVATIONS:\n");
                sb.append("Longueur: ").append(observations.get("length").asInt()).append(" caractères\n");
                sb.append("Évaluation: ").append(observations.get("details").asText()).append("\n");
                if (observations.get("anomaly").asBoolean()) {
                    sb.append("⚠ Anomalie détectée:\n");
                    for (JsonNode msg : observations.get("messages")) {
                        sb.append("  • ").append(msg.asText()).append("\n");
                    }
                }
                sb.append("\n");
            }

            // General Info
            sb.append("TYPE DE CONSULTATION: ").append(result.get("consult_type").asText()).append("\n\n");

            if (result.has("summary")) {
                sb.append("RÉSUMÉ: ").append(result.get("summary").asText()).append("\n");
            }

            return sb.toString();
        } catch (IOException e) {
            return "Erreur de formatage des résultats: " + e.getMessage();
        }
    }

    public String analyzeD(int dossierId) {
        try {
            // 1. Get dossier data
            Map<String, Object> dossierData = getDossierDataForAnalysis(dossierId);

            // Create ObjectMapper with proper configuration
            ObjectMapper mapper = new ObjectMapper();

            // Ensure all dates are properly formatted as strings
            if (dossierData.containsKey("consult_dates")) {
                List<String> dates = (List<String>) dossierData.get("consult_dates");
                dossierData.put("consult_dates", dates.stream().map(Object::toString).collect(Collectors.toList()));
            }

            // Convert to properly formatted JSON string
            String jsonInput = mapper.writeValueAsString(dossierData);

            // Debug output
            System.out.println("Raw JSON being sent: " + jsonInput);

            // 2. Prepare Python command
            String pythonExecutable = "python";
            URL scriptUrl = getClass().getResource("/Youssef_views/analyze_dossier.py");
            if (scriptUrl == null) {
                return "Erreur: Fichier Python introuvable";
            }
            String pythonScriptPath = new File(scriptUrl.toURI()).getAbsolutePath();

            // 3. Create process with proper argument handling
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    pythonScriptPath
            );
            pb.redirectErrorStream(true);


            // Set working directory to the script location
            pb.directory(new File(pythonScriptPath).getParentFile());

            // 4. Handle output and errors
            pb.redirectErrorStream(true);

            Process p = pb.start();
            try (OutputStreamWriter writer = new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(jsonInput);
                writer.flush();
            }

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                return "Python script error (code " + exitCode + "):\n" + output.toString();
            }

            return formatAnalysisResult(output.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "Analysis error: " + e.getMessage();
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

