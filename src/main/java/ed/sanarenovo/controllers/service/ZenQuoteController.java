package ed.sanarenovo.controllers.service;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

public class ZenQuoteController {

    @FXML
    private TextFlow quoteTextFlow;

    @FXML
    private void initialize() {
        fetchQuote();
    }

    private void fetchQuote() {
        String url = "https://zenquotes.io/api/random";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    JSONArray json = new JSONArray(response);
                    String quote = json.getJSONObject(0).getString("q");
                    String author = json.getJSONObject(0).getString("a");

                    javafx.application.Platform.runLater(() -> displayQuote(quote, author));
                })
                .exceptionally(e -> {
                    javafx.application.Platform.runLater(() -> {
                        quoteTextFlow.getChildren().clear();
                        quoteTextFlow.getChildren().add(new Text("Erreur lors du chargement de la citation."));
                    });
                    return null;
                });
    }

    private void displayQuote(String quote, String author) {
        quoteTextFlow.getChildren().clear();

        Text quoteText = new Text("💬 \"" + quote + "\"\n");
        quoteText.setFont(Font.font("Georgia", 20)); // Police élégante pour la citation
        quoteText.setFill(Color.BLACK);
        quoteText.setStyle("-fx-font-weight: bold;");

        Text authorText = new Text("— " + author);
        authorText.setFont(Font.font("Verdana", 16)); // Une autre police pour l'auteur
        authorText.setFill(Color.DARKCYAN); // Couleur différente

        quoteTextFlow.getChildren().addAll(quoteText, authorText);
    }

    @FXML
    private void onMedicamentClicked(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/medicament.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Recherche Médicament");
        stage.show();
    }
    @FXML
    private void onRecommendClicked(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/recommande.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Service recommandé");
        stage.show();
    }
    @FXML
    private void onCovidClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/covid19.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Info Covid");
        stage.show();
    }

    @FXML
    public void onRendezVouClicked(javafx.event.ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Youssef_views/rendez_vous.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Rendez-Vous");
        stage.show();
    }
}