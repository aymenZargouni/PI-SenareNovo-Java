package ed.sanarenovo.tests;

import ed.sanarenovo.entities.CovidData;
import ed.sanarenovo.services.DataFetcher;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;

public class TestDashboard extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        testDataLoading();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/dashboard.fxml"));
        Parent root = loader.load();

        // Configurer la scène pour utiliser tout l'espace
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/Blog/css/dashboardStyle.css").toExternalForm());

        stage.setTitle("COVID-19 Dashboard - Statistiques mondiales");
        stage.setScene(scene);
        stage.setMaximized(true); // Ouvrir en plein écran
        stage.show();
    }

    private void testDataLoading() {
        System.out.println("\n=== TEST DE CHARGEMENT DES DONNÉES ===");
        System.out.println("Encodage du système: " + System.getProperty("file.encoding"));

        try {
            DataFetcher fetcher = new DataFetcher();
            List<CovidData> data = fetcher.fetchGlobalData();

            // Affiche avec un formatage propre
            System.out.println("\nDonnées valides (10 premiers) :");
            System.out.println("------------------------------------------------------------");
            System.out.println("Pays                | Province       | Coordonnées    | Cas      ");
            System.out.println("------------------------------------------------------------");

            data.stream()
                    .filter(d -> d.getCases() > 0)
                    .limit(10)
                    .forEach(d -> System.out.println(
                            String.format("%-20s | %-15s | %7.4f, %7.4f | %,8d",
                                    d.getCountry(),
                                    d.getProvince().isEmpty() ? "N/A" : d.getProvince(),
                                    d.getLat(),
                                    d.getLon(),
                                    d.getCases()
                            )
                    ));

        } catch (Exception e) {
            System.err.println("ERREUR: Impossible de charger les données");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Force l'encodage UTF-8 si nécessaire
        System.setProperty("file.encoding", "UTF-8");
        launch(args);
    }
}