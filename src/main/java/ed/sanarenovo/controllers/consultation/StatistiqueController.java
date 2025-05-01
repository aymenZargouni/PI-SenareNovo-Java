package ed.sanarenovo.controllers.consultation;

import ed.sanarenovo.entities.dossiermedicale;
import ed.sanarenovo.entities.consultation;
import ed.sanarenovo.services.StatistiqueService;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class StatistiqueController implements Initializable {

    // Déclaration des composants FXML
    @FXML private Label labelTotalDossiers;
    @FXML private Label labelTotalConsultations;
    @FXML private Label labelMoyenneConsultations;
    @FXML private Label labelDossiersSansConsultation;
    @FXML private Label labelTauxRemplissageIMC;
    @FXML private Label labelRepartitionIMC;
    @FXML private Label labelConsultationsParStatus;
    @FXML private VBox statsContainer;
    @FXML private AnchorPane chartContainer;
    @FXML private AnchorPane imcChartContainer;
    @FXML private AnchorPane statusChartContainer;

    private final StatistiqueService statistiqueService = new StatistiqueService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAllStatistics();
    }

    /**
     * Charge toutes les statistiques médicales
     */
    private void loadAllStatistics() {
        Map<String, Object> stats = statistiqueService.getMedicalStatistics();

        // Affichage des statistiques de base
        labelTotalDossiers.setText("Total Dossiers: " + stats.get("total_dossiers"));
        labelTotalConsultations.setText("Total Consultations: " + stats.get("total_consultations"));
        labelMoyenneConsultations.setText("Moyenne Consultations/Dossier: " +
                String.format("%.2f", stats.get("moyenne_consultations_par_dossier")));

        // Affichage des indicateurs supplémentaires
        labelDossiersSansConsultation.setText("Dossiers sans consultation: " +
                stats.get("dossiers_sans_consultation"));

        labelTauxRemplissageIMC.setText("Taux de remplissage IMC: " +
                String.format("%.1f%%", (double)stats.get("taux_remplissage_imc") * 100));

        // Création des graphiques
        createPieChart(
                chartContainer,
                "Répartition des consultations par type",
                (Map<String, Integer>) stats.get("consultations_par_type")
        );

        createBarChart(
                imcChartContainer,
                "Distribution des IMC",
                "Catégorie IMC",
                "Nombre de patients",
                (Map<String, Long>) stats.get("imc_distribution")
        );

        createPieChart(
                statusChartContainer,
                "Consultations par statut",
                (Map<String, Integer>) stats.get("consultations_par_status")
        );

        // Affichage des détails supplémentaires
        labelRepartitionIMC.setText("Répartition IMC: " +
                formatDistribution((Map<String, Long>) stats.get("imc_distribution")));

        labelConsultationsParStatus.setText("Statut consultations: " +
                formatDistribution((Map<String, Integer>) stats.get("consultations_par_status")));
    }

    /**
     * Crée un graphique camembert
     */
    private void createPieChart(AnchorPane container, String title, Map<String, ? extends Number> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        data.forEach((key, value) -> dataset.setValue(key, value));

        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true, true, false
        );

        // Personnalisation du graphique
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint(0, new Color(79, 129, 189));
        plot.setSectionPaint(1, new Color(192, 80, 77));
        plot.setSectionPaint(2, new Color(155, 187, 89));
        plot.setSimpleLabels(true);
        chart.setBackgroundPaint(Color.white);

        // Configuration du ChartPanel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(
                (int) (container.getWidth() > 0 ? container.getWidth() - 20 : 600),
                (int) (container.getHeight() > 0 ? container.getHeight() - 20 : 400)
        ));
        chartPanel.setMinimumDrawWidth(400);
        chartPanel.setMinimumDrawHeight(300);

        SwingNode swingNode = new SwingNode();
        swingNode.setContent(chartPanel);

        container.getChildren().clear();
        container.getChildren().add(swingNode);

        // Gestion du redimensionnement
        container.widthProperty().addListener((obs, oldVal, newVal) -> {
            chartPanel.setPreferredSize(new Dimension(
                    newVal.intValue() - 20,
                    chartPanel.getHeight()
            ));
        });

        container.heightProperty().addListener((obs, oldVal, newVal) -> {
            chartPanel.setPreferredSize(new Dimension(
                    chartPanel.getWidth(),
                    newVal.intValue() - 20
            ));
        });
    }

    /**
     * Crée un graphique en barres
     */
    private void createBarChart(AnchorPane container, String title,
                                String xAxisLabel, String yAxisLabel,
                                Map<String, Long> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        data.forEach((category, count) ->
                dataset.addValue(count, "Valeur", category));

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset
        );

        // Personnalisation du graphique
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRenderer().setSeriesPaint(0, new Color(79, 129, 189));
        chart.setBackgroundPaint(Color.white);
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.lightGray);

        // Configuration du ChartPanel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(
                (int) (container.getWidth() > 0 ? container.getWidth() - 20 : 600),
                (int) (container.getHeight() > 0 ? container.getHeight() - 20 : 400)
        ));
        chartPanel.setMinimumDrawWidth(400);
        chartPanel.setMinimumDrawHeight(300);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);

        SwingNode swingNode = new SwingNode();
        swingNode.setContent(chartPanel);

        container.getChildren().clear();
        container.getChildren().add(swingNode);

        // Gestion du redimensionnement
        container.widthProperty().addListener((obs, oldVal, newVal) -> {
            chartPanel.setPreferredSize(new Dimension(
                    newVal.intValue() - 20,
                    chartPanel.getHeight()
            ));
        });

        container.heightProperty().addListener((obs, oldVal, newVal) -> {
            chartPanel.setPreferredSize(new Dimension(
                    chartPanel.getWidth(),
                    newVal.intValue() - 20
            ));
        });
    }

    /**
     * Formate la distribution pour l'affichage
     */
    private String formatDistribution(Map<String, ? extends Number> distribution) {
        StringBuilder sb = new StringBuilder();
        distribution.forEach((k, v) ->
                sb.append(k).append(": ").append(v).append(" | "));
        return sb.length() > 0 ? sb.substring(0, sb.length() - 3) : "";
    }

    /**
     * Redirige vers la page des consultations
     */
    @FXML
    private void redirectToConsultation() {
        loadViewWithCSS("/Youssef_views/cons.fxml", "/Youssef_views/design.css", "Gestion des Consultations");
    }

    /**
     * Redirige vers la page des dossiers médicaux
     */
    @FXML
    private void redirectToDossierMedical() {
        loadViewWithCSS("/Youssef_views/dm.fxml", "/Youssef_views/design.css", "Gestion des Dossiers Médicaux");
    }

    /**
     * Méthode utilitaire pour charger une vue avec son CSS
     */
    private void loadViewWithCSS(String fxmlPath, String cssPath, String title) {
        try {
            // Chargement du FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Création de la scène
            Scene scene = new Scene(root);

            // Application du CSS
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());

            // Récupération de la fenêtre actuelle
            Stage currentStage = (Stage) labelTotalDossiers.getScene().getWindow();

            // Mise à jour de la scène
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            currentStage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue: " + fxmlPath);
            // Vous pourriez ajouter ici une alerte pour informer l'utilisateur
        }
    }
}