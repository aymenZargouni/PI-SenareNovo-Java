package ed.sanarenovo.controlles.service;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class medicament {

    @FXML
    private TextField drugTextField;

    // Chaque Label séparé
    @FXML
    private Label genericNameLabel;

    @FXML
    private Label usageLabel;

    @FXML
    private Label dosageLabel;

    @FXML
    private Label warningsLabel;

    @FXML
    private void onSearchClicked() {
        String drugName = drugTextField.getText().trim();

        if (!drugName.isEmpty()) {
            fetchDrugInfo(drugName);
        } else {
            Platform.runLater(() -> {
                genericNameLabel.setText("❗ Veuillez entrer un nom de médicament.");
                usageLabel.setText("");
                dosageLabel.setText("");
                warningsLabel.setText("");
            });
        }
    }

    private void fetchDrugInfo(String drugName) {
        String url = "https://api.fda.gov/drug/label.json?search=openfda.generic_name:" + drugName + "&limit=1";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONArray results = json.getJSONArray("results");
                        JSONObject drug = results.getJSONObject(0);

                        JSONObject openfda = drug.optJSONObject("openfda");

                        String genericName = (openfda != null && openfda.has("generic_name"))
                                ? openfda.getJSONArray("generic_name").optString(0, "Non spécifié")
                                : "Non spécifié";

                        String usage = drug.optJSONArray("purpose") != null
                                ? drug.optJSONArray("purpose").optString(0, "Non spécifié")
                                : "Non spécifié";

                        String dosage = drug.optJSONArray("dosage_and_administration") != null
                                ? drug.optJSONArray("dosage_and_administration").optString(0, "Non spécifié")
                                : "Non spécifié";

                        String warnings = drug.optJSONArray("warnings") != null
                                ? drug.optJSONArray("warnings").optString(0, "Non spécifié")
                                : "Non spécifié";

                        Platform.runLater(() -> {
                            genericNameLabel.setText("🧬 Nom générique : " + genericName);
                            usageLabel.setText("🔍 Utilisation : " + usage);
                            dosageLabel.setText("💉 Posologie : " + dosage);
                            warningsLabel.setText("⚠️ Avertissements : " + warnings);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            genericNameLabel.setText("❌ Aucune information trouvée pour \"" + drugName + "\".");
                            usageLabel.setText("");
                            dosageLabel.setText("");
                            warningsLabel.setText("");
                        });
                    }
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        genericNameLabel.setText("🚫 Erreur lors de la connexion à l'API.");
                        usageLabel.setText("");
                        dosageLabel.setText("");
                        warningsLabel.setText("");
                    });
                    return null;
                });
    }
}
