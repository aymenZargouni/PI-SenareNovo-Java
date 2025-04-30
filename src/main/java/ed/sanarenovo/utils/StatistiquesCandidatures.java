package ed.sanarenovo.utils;

import ed.sanarenovo.entities.Candidature;
import ed.sanarenovo.entities.Offre;
import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.util.Duration;

import java.util.Map;
import java.util.stream.Collectors;

public class StatistiquesCandidatures {

    public BarChart<String, Number> generateOffreStatsByCandidatures(ObservableList<Offre> offresList, ObservableList<Candidature> candidaturesList) {
        Map<Integer, Long> candidaturesParOffreId = candidaturesList.stream()
                .collect(Collectors.groupingBy(Candidature::getOffreId, Collectors.counting()));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Offres");
        yAxis.setLabel("Nombre de Candidatures");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Candidatures par Offre");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        offresList.forEach(offre -> {
            long count = candidaturesParOffreId.getOrDefault(offre.getId(), 0L);
            XYChart.Data<String, Number> data = new XYChart.Data<>(offre.getTitre(), count);
            series.getData().add(data);
        });

        barChart.getData().add(series);

        // Animation des barres
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    FadeTransition ft = new FadeTransition(Duration.millis(800), newNode);
                    ft.setFromValue(0);
                    ft.setToValue(1);
                    ft.play();
                    newNode.setStyle("-fx-bar-fill: #4CAF50;");
                }
            });
        }

        return barChart;
}
}