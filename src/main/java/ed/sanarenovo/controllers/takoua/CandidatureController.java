package ed.sanarenovo.controllers.takoua;

import ed.sanarenovo.entities.Candidature;
import ed.sanarenovo.services.CandidatureService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Date;
import java.util.Properties;

public class CandidatureController {
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCVPath;
    @FXML private TextArea txtLettreMotivation;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;
    @FXML private Button btnUploadCV;
    @FXML private Button btnGenerateMotivation;

    @FXML private TableView<Candidature> tableCandidatures;
    @FXML private TableColumn<Candidature, Integer> colCandId;
    @FXML private TableColumn<Candidature, String> colCandNom;
    @FXML private TableColumn<Candidature, String> colCandPrenom;
    @FXML private TableColumn<Candidature, String> colCandEmail;
    @FXML private TableColumn<Candidature, Date> colCandDate;
    @FXML private TableColumn<Candidature, String> colCandStatut;
    @FXML private ComboBox<String> comboStatut;

    private int offreId;
    private File selectedCVFile;
    private final CandidatureService candidatureService = new CandidatureService();

    @FXML
    private void initialize() {
        if (comboStatut != null) {
            comboStatut.getItems().addAll("En attente", "Accepté", "Refusé");
        }
    }

    public void setOffreId(int offreId) {
        this.offreId = offreId;
    }

    @FXML
    private void handleGenerateMotivation(ActionEvent event) {
        String nom = txtNom.getText();
        String prenom = txtPrenom.getText();
        String email = txtEmail.getText();

        if (nom.isEmpty() || prenom.isEmpty()) {
            showError("Veuillez renseigner votre nom et prénom pour générer la lettre.");
            return;
        }

        try {
            String prompt = "Rédige une lettre de motivation professionnelle pour un candidat nommé " + prenom + " " + nom +
                    " postulant pour une offre d'emploi. Inclure son adresse email : " + email;

            String generatedText = generateLettreMotivationAvecOpenRouter(prompt);
            txtLettreMotivation.setText(generatedText);
        } catch (Exception e) {
            showError("Erreur lors de la génération de la lettre : " + e.getMessage());
        }
    }

    public static String generateLettreMotivationAvecOpenRouter(String prompt) throws IOException {
        String apiKey = "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"; // ← Mets ici ta vraie clé API OpenRouter
        String model = "openai/gpt-3.5-turbo";

        URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-Title", "CandidatureApp");
        conn.setDoOutput(true);

        String inputJson = String.format("""
        {
          "model": "%s",
          "messages": [
            {"role": "user", "content": "%s"}
          ]
        }
        """, model, prompt.replace("\"", "\\\""));

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = inputJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
        }

        JSONObject json = new JSONObject(response.toString());
        JSONArray choices = json.getJSONArray("choices");
        return choices.getJSONObject(0).getJSONObject("message").getString("content").trim();
    }

    @FXML
    private void handleUpdateStatut(ActionEvent event) {
        Candidature selectedCandidature = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selectedCandidature == null) {
            showError("Veuillez sélectionner une candidature.");
            return;
        }

        String newStatut = comboStatut.getValue();
        if (newStatut == null || newStatut.isEmpty()) {
            showError("Veuillez sélectionner un nouveau statut.");
            return;
        }

        candidatureService.updateStatut(selectedCandidature.getId(), newStatut);
        selectedCandidature.setStatut(newStatut);
        tableCandidatures.refresh();

        String email = selectedCandidature.getEmail();
        String subject = "Mise à jour de votre candidature";
        String body = "Bonjour " + selectedCandidature.getNom() + ",\n\n";
        body += switch (newStatut.toLowerCase()) {
            case "accepté" -> "Félicitations ! Votre candidature a été acceptée.";
            case "refusé" -> "Nous sommes désolés, votre candidature a été refusée.";
            default -> "Le statut de votre candidature a été mis à jour à : " + newStatut + ".";
        };

        // MailSender.send(email, subject, body); // si tu utilises un service d'envoi
    }

    @FXML
    private void handleUploadCV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un CV (PDF)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedCVFile = file;
            txtCVPath.setText(file.getName());
        }
    }

    @FXML
    private void handleSubmit() {
        if (validateInputs()) {
            String destinationPath = "cv_uploads/" + System.currentTimeMillis() + "_" + selectedCVFile.getName();
            try {
                Files.createDirectories(Paths.get("cv_uploads"));
                Files.copy(selectedCVFile.toPath(), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                showError("Erreur lors du téléchargement du CV : " + e.getMessage());
                return;
            }

            Candidature candidature = new Candidature();
            candidature.setNom(txtNom.getText());
            candidature.setPrenom(txtPrenom.getText());
            candidature.setEmail(txtEmail.getText());
            candidature.setCv(destinationPath);
            candidature.setLettreMotivation(txtLettreMotivation.getText());
            candidature.setDateCandidature(new Date());
            candidature.setStatut("En attente");
            candidature.setOffreId(offreId);

            candidatureService.addCandidature(candidature);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Votre candidature a été soumise avec succès !");
            alert.showAndWait();

            closeWindow();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (txtNom.getText().isEmpty())
            errorMessage.append("Nom est obligatoire !\n");

        if (txtPrenom.getText().isEmpty())
            errorMessage.append("Prénom est obligatoire !\n");

        if (txtEmail.getText().isEmpty() || !txtEmail.getText().contains("@"))
            errorMessage.append("Email valide est obligatoire !\n");

        if (selectedCVFile == null)
            errorMessage.append("Veuillez sélectionner un fichier PDF pour votre CV !\n");

        if (txtLettreMotivation.getText().isEmpty())
            errorMessage.append("Lettre de motivation est obligatoire !\n");

        if (!errorMessage.isEmpty()) {
            showError(errorMessage.toString());
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Problème détecté");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
