// Coordonne l'affichage des données dans l'interface utilisateur.
package ed.sanarenovo.controllers.Blog;

import ed.sanarenovo.entities.CovidData;
import ed.sanarenovo.services.DataFetcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController {
    @FXML private BarChart<String, Number> casesChart;
    @FXML private PieChart countryPieChart;
    @FXML private VBox mapContainer;
    @FXML
    private Button btnRetour;
    @FXML
    public void initialize() { // Point d'entrée, charge les données et initialise les graphiques
        System.out.println("Initialisation du tableau de bord ...");

        try {
            // Charge les données via DataFetcher
            DataFetcher fetcher = new DataFetcher();
            List<CovidData> data = fetcher.fetchGlobalData();
            System.out.println("Données chargées : " + data.size() + " records");

            if (data.isEmpty()) {
                System.err.println("Aucune donnée disponible !");
                return;
            }

            updateCharts(data);
            updateMap(data);

        } catch (Exception e) {
            System.err.println("Échec de l'initialisation du tableau de bord :");
            e.printStackTrace();
        }
    }
    // Met à jour les graphiques avec les nouvelles données
    private void updateCharts(List<CovidData> data) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.FRENCH);

        Map<String, Integer> byCountry = data.stream()
                .filter(d -> d.getCases() > 0)
                .collect(Collectors.groupingBy(
                        CovidData::getCountry,
                        Collectors.summingInt(CovidData::getCases)
                ));

        casesChart.getData().clear();
        countryPieChart.getData().clear();

        // BarChart - Top 10
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cas confirmés");

        byCountry.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .forEach(entry -> {
                    XYChart.Data<String, Number> chartData = new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    );
                    series.getData().add(chartData);

                    // Tooltip
                    Tooltip tooltip = new Tooltip(String.format("%s\n%,d cas",
                            entry.getKey(),
                            entry.getValue()));
                    Tooltip.install(chartData.getNode(), tooltip);
                });

        casesChart.getData().add(series);

        // PieChart - Top 10
        byCountry.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .forEach(entry -> {
                    PieChart.Data pieData = new PieChart.Data(
                            String.format("%s (%s)",
                                    entry.getKey(),
                                    numberFormat.format(entry.getValue())),
                            entry.getValue()
                    );
                    countryPieChart.getData().add(pieData);
                });
    }

    private void setupChartTooltips() {
        // Tooltips pour le BarChart
        for (XYChart.Series<String, Number> series : casesChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(String.format("%s\n%,d cas",
                        data.getXValue(),
                        data.getYValue().intValue()));
                Tooltip.install(data.getNode(), tooltip);

                // Animation au survol
                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setEffect(new Glow(0.3));
                });
                data.getNode().setOnMouseExited(e -> {
                    data.getNode().setEffect(null);
                });
            }
        }

        // Tooltips pour le PieChart
        for (PieChart.Data data : countryPieChart.getData()) {
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setEffect(new Glow(0.3));
            });
            data.getNode().setOnMouseExited(e -> {
                data.getNode().setEffect(null);
            });
        }
    }

    //Génère et affiche la carte interactive
    private void updateMap(List<CovidData> data) {
        WebView webView = new WebView();

        // Filtrer les données avec des coordonnées valides
        List<CovidData> validData = data.stream()
                .filter(d -> d.getLat() != 0 && d.getLon() != 0 && d.getCases() > 0)
                .collect(Collectors.toList());

        webView.getEngine().loadContent(generateMapHtml(validData));
        mapContainer.getChildren().setAll(webView);
    }

    // Crée le code HTML/JS pour la carte Leaflet
    private String generateMapHtml(List<CovidData> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css"/>
                <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
                <style>#map { height: 500px; }</style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([20, 0], 2);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
                    var data = [
            """);

        data.forEach(d -> sb.append(String.format(
                "{lat:%f, lng:%f, cases:%d, country:'%s'},\n",
                d.getLat(), d.getLon(), d.getCases(), d.getCountry().replace("'", "\\'")
        )));

        sb.append("""
                    ];
                    data.forEach(function(item) {
                        L.circle([item.lat, item.lng], {
                            radius: Math.sqrt(item.cases) * 1000,
                            color: '#e74c3c',
                            fillOpacity: 0.5
                        }).addTo(map)
                        .bindPopup('<b>' + item.country + '</b><br>Cas: ' + item.cases);
                    });
                </script>
            </body>
            </html>
            """);

        return sb.toString();
    }

    private String getMapHtml(List<CovidData> data) {
        // Filtrer les données sans coordonnées
        List<CovidData> validData = data.stream()
                .filter(d -> d.getLat() != 0 && d.getLon() != 0)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
            <style>
                #map { height: 500px; }
                .info { padding: 6px 8px; background: white; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map').setView([20, 0], 2);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 18,
                    attribution: '© OpenStreetMap'
                }).addTo(map);
                
                var heatData = [
        """);

        validData.forEach(d ->
                sb.append(String.format("[%.4f, %.4f, %d],", d.getLat(), d.getLon(), d.getCases()))
        );

        sb.append("""
                ];
                
                // Création des cercles proportionnels
                heatData.forEach(function(item) {
                    var radius = Math.sqrt(item[2]) * 150;
                    L.circle([item[0], item[1]], {
                        radius: radius,
                        fillColor: "#ff0000",
                        fillOpacity: 0.5,
                        stroke: false
                    }).addTo(map)
                    .bindPopup("<b>" + item[2].toLocaleString() + " cas</b>");
                });
            </script>
        </body>
        </html>
        """);

        return sb.toString();
    }
    @FXML
    private void retourBlog(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Blog/BlogClient.fxml"));
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}