package ed.sanarenovo.controllers.service;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.util.ResourceBundle;

public class CovidController implements Initializable {

    @FXML
    private GridPane covidGrid;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fetchCovidInfo(); // Appelé automatiquement au chargement
    }

    private void fetchCovidInfo() {
        String url = "https://disease.sh/v3/covid-19/countries/tunisia";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        String country = json.getString("country");
                        int confirmed = json.getInt("cases");
                        int deaths = json.getInt("deaths");
                        int recovered = json.getInt("recovered");
                        long updated = json.getLong("updated");

                        // Conversion timestamp -> date lisible
                        String date = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                .withZone(java.time.ZoneId.systemDefault())
                                .format(java.time.Instant.ofEpochMilli(updated));

                        Platform.runLater(() -> {
                            covidGrid.getChildren().clear();
                            covidGrid.addRow(0, new Label("📍 Pays :"), new Label(country));
                            covidGrid.addRow(1, new Label("📅 Dernière mise à jour :"), new Label(date));
                            covidGrid.addRow(2, new Label("✅ Cas Confirmés :"), new Label(String.valueOf(confirmed)));
                            covidGrid.addRow(3, new Label("☠️ Décès :"), new Label(String.valueOf(deaths)));
                            covidGrid.addRow(4, new Label("💪 Rétablis :"), new Label(String.valueOf(recovered)));
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            covidGrid.getChildren().clear();
                            covidGrid.add(new Label("🚫 Erreur lors de l'analyse des données."), 0, 0);
                        });
                    }
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        covidGrid.getChildren().clear();
                        covidGrid.add(new Label("🚫 Erreur de connexion à l'API."), 0, 0);
                    });
                    return null;
                });
    }

}
