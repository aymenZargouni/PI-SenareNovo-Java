package ed.sanarenovo.controllers.takoua;

import ed.sanarenovo.entities.Candidature;
import ed.sanarenovo.services.AIService;
import ed.sanarenovo.services.CandidatureService;
import ed.sanarenovo.utils.MailSender;
import javafx.application.Platform;
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
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

public class CandidatureController {
    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtCVPath;
    @FXML
    private TextArea txtLettreMotivation;
    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnUploadCV;
    @FXML
    private TableView<Candidature> tableCandidatures;
    @FXML private TableColumn<Candidature, Integer> colCandId;
    @FXML private TableColumn<Candidature, String> colCandNom;
    @FXML private TableColumn<Candidature, String> colCandPrenom;
    @FXML private TableColumn<Candidature, String> colCandEmail;
    @FXML private TableColumn<Candidature, Date> colCandDate;
    @FXML private TableColumn<Candidature, String> colCandStatut;
    @FXML
    private ComboBox<String> comboStatut;
    private int offreId;
    private File selectedCVFile;
    private CandidatureService candidatureService = new CandidatureService();
    @FXML
    private void initialize() {
        if (comboStatut != null) {
            comboStatut.getItems().addAll("En attente", "Accepté", "Refusé");}}

    public void setOffreId(int offreId) {
        this.offreId = offreId;

    }
    @FXML
    private Button btnGenerateMotivation;


    @FXML
    private void handleGenerateMotivation(ActionEvent event)
        // Your code to handle the button click
    {
        String nom = txtNom.getText();
        String prenom = txtPrenom.getText();
        String email = txtEmail.getText();

        if (nom.isEmpty() || prenom.isEmpty()) {
            showError("Veuillez renseigner votre nom et prénom pour générer la lettre.");
            return;
        }

        // Appel à l'API d'IA (OpenAI par exemple)
        try {
            String prompt = "Rédige une lettre de motivation professionnelle pour un candidat nommé " + prenom + " " + nom + " postulant pour une offre d'emploi. Inclure son adresse email : " + email;

            String generatedText = generateLettreMotivation(prompt);
            txtLettreMotivation.setText(generatedText);
        } catch (Exception e) {
            showError("Erreur lors de la génération de la lettre : " + e.getMessage());
        }
    }
    private static final String API_KEY ="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"; // ⚠️ À remplacer

    public static String generateLettreMotivation(String prompt) throws Exception {
        URL url = new URL("https://api.openai.com/v1/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String inputJson = "{\n" +
                "  \"model\": \"text-davinci-003\",\n" +
                "  \"prompt\": \"" + prompt.replace("\"", "\\\"") + "\",\n" +
                "  \"max_tokens\": 300\n" +
                "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(inputJson.getBytes());
        }

        Scanner scanner = new Scanner(conn.getInputStream());
        String response = scanner.useDelimiter("\\A").next();
        scanner.close();

        // Extraire le texte généré depuis la réponse JSON
        int start = response.indexOf("\"text\":\"") + 8;
        int end = response.indexOf("\",", start);
        return response.substring(start, end).replace("\\n", "\n").trim();
    }


    public String generateMotivationLetter(String prompt) throws IOException {
        String apiKey = "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"; // remplace par ta vraie clé
        URL url = new URL("https://api.openai.com/v1/chat/completions");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInput = """
        {
          "model": "gpt-3.5-turbo",
          "messages": [{"role": "user", "content": "%s"}]
        }
        """.formatted(prompt);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return response.toString(); // tu peux parser le JSON ici
    }


    @FXML
    private void handleUpdateStatut(ActionEvent event) {
        // 1. Récupérer la candidature sélectionnée
        Candidature selectedCandidature = tableCandidatures.getSelectionModel().getSelectedItem();

        if (selectedCandidature == null) {
            System.out.println("Veuillez sélectionner une candidature.");
            return;
        }

        // 2. Récupérer le nouveau statut
        String newStatut = comboStatut.getValue();

        if (newStatut == null || newStatut.isEmpty()) {
            System.out.println("Veuillez sélectionner un nouveau statut.");
            return;
        }

        // 3. Mise à jour en base
        CandidatureService service = new CandidatureService(); // ou ton DAO
        service.updateStatut(selectedCandidature.getId(), newStatut);

        // 4. Mettre à jour l'objet affiché
        selectedCandidature.setStatut(newStatut);
        tableCandidatures.refresh();

        // 5. Préparer les informations pour l'e-mail
        String email = selectedCandidature.getEmail();
        String subject = "Mise à jour de votre candidature";
        String body = "Bonjour " + selectedCandidature.getNom() + ",\n\n";

        if (newStatut.equalsIgnoreCase("accepté")) {
            body += "Félicitations ! Votre candidature a été acceptée.";
        } else if (newStatut.equalsIgnoreCase("refusé")) {
            body += "Nous sommes désolés, votre candidature a été refusée.";
        } else {
            body += "Le statut de votre candidature a été mis à jour à : " + newStatut + ".";
        }

       }

    @FXML
    private void handleUploadCV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un CV (PDF)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedCVFile = file;
            txtCVPath.setText(file.getName());
        }
    }

    @FXML
    private void handleSubmit() {
        if (validateInputs()) {
            // Copier le fichier PDF vers un dossier local
            String destinationPath = "cv_uploads/" + System.currentTimeMillis() + "_" + selectedCVFile.getName();
            try {
                Files.createDirectories(Paths.get("cv_uploads")); // crée le dossier si non existant
                Files.copy(selectedCVFile.toPath(), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                showError("Erreur lors du téléchargement du CV : " + e.getMessage());
                return;
            }

            Candidature candidature = new Candidature();
            candidature.setNom(txtNom.getText());
            candidature.setPrenom(txtPrenom.getText());
            candidature.setEmail(txtEmail.getText());
            candidature.setCv(destinationPath); // on enregistre le chemin du fichier
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
        String errorMessage = "";

        if (txtNom.getText() == null || txtNom.getText().isEmpty())
            errorMessage += "Nom est obligatoire !\n";

        if (txtPrenom.getText() == null || txtPrenom.getText().isEmpty())
            errorMessage += "Prénom est obligatoire !\n";

        if (txtEmail.getText() == null || txtEmail.getText().isEmpty() || !txtEmail.getText().contains("@"))
            errorMessage += "Email valide est obligatoire !\n";

        if (selectedCVFile == null)
            errorMessage += "Veuillez sélectionner un fichier PDF pour votre CV !\n";

        if (txtLettreMotivation.getText() == null || txtLettreMotivation.getText().isEmpty())
            errorMessage += "Lettre de motivation est obligatoire !\n";

        if (!errorMessage.isEmpty()) {
            showError(errorMessage);
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
