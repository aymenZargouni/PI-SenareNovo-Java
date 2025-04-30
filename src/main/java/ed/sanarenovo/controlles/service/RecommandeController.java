package ed.sanarenovo.controlles.service;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RecommandeController {

    @FXML
    private TextField etatField;

    @FXML
    private TextField ageField;

    @FXML
    private ComboBox<String> sexeComboBox;

    @FXML
    private Button recommanderBtn;

    @FXML
    private Label serviceLabel;

    @FXML
    private Label conseilLabel;

    @FXML
    void handleRecommendation(ActionEvent event) {
        String etat = etatField.getText().trim();
        String age = ageField.getText().trim();
        String sexe = (sexeComboBox.getValue() != null) ? sexeComboBox.getValue().trim() : "";

        if (etat.isEmpty() || age.isEmpty() || sexe.isEmpty()) {
            serviceLabel.setText("Veuillez remplir tous les champs : symptôme, âge, sexe.");
            conseilLabel.setText("");
            return;
        }

        try {
            String inputData = etat + "|" + age + "|" + sexe;

            String basePath = new File("").getAbsolutePath();
            String pythonScriptPath = basePath + "/src/main/python/recommande_service.py";
            ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath, inputData);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[PYTHON]: " + line);
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            System.out.println("[DEBUG] Script exited with code: " + exitCode);

            String[] lines = output.toString().split("\n");
            for (String l : lines) {
                if (l.contains("|")) {
                    String[] parts = l.split("\\|");
                    if (parts.length >= 3) {
                        serviceLabel.setText("Service : " + parts[0]);
                        conseilLabel.setText("Conseil : " + parts[1] + "\nMaladies possibles : " + parts[2]);
                        return;
                    }
                }
            }

            serviceLabel.setText("❌ Erreur de communication avec le modèle.");
            conseilLabel.setText("");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            serviceLabel.setText("Erreur lors de l'exécution du script Python.");
            conseilLabel.setText("");
        }
    }

    @FXML
    private void retour(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/affichage.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("ajouter salle");
        stage.show();
    }
}
