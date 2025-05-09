package ed.sanarenovo.controllers.service;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import ed.sanarenovo.utils.MyConnection;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class Stat implements Initializable {

    @FXML
    private BarChart<String, Number> serviceChart;
    @FXML
    public Button rtr;
    @FXML
    private BarChart<String, Number> salleChart;

    @FXML
    private BarChart<String, Number> salleServiceChart;

    @FXML
    private BarChart<String, Number> salleEtatChart;

    @FXML
    private BarChart<String, Number> serviceEtatChart;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerStatistiquesServices();
        chargerStatistiquesSalles();
        chargerStatistiquesSallesParService();
        chargerStatistiquesParEtatSalle();
        chargerStatistiquesParEtatService();
    }

    private void chargerStatistiquesServices() {
        String sql = "SELECT nom, nbr_salle FROM service";

        try (Connection conn = MyConnection.getInstance().getCnx();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de salles par service");

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("nom"), rs.getInt("nbr_salle")));
            }

            serviceChart.getData().add(series);

        } catch (SQLException e) {
            System.out.println("❌ Erreur service : " + e.getMessage());
        }
    }

    private void chargerStatistiquesSalles() {
        String sql = "SELECT type, COUNT(*) AS total FROM salle GROUP BY type";

        try (Connection conn = MyConnection.getInstance().getCnx();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de salles par type");

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("type"), rs.getInt("total")));
            }

            salleChart.getData().add(series);

        } catch (SQLException e) {
            System.out.println("❌ Erreur salle : " + e.getMessage());
        }
    }

    private void chargerStatistiquesSallesParService() {
        String sql = "SELECT s.nom AS service, COUNT(*) AS total " +
                "FROM salle sa JOIN service s ON sa.service_id = s.id " +
                "GROUP BY s.nom";

        try (Connection conn = MyConnection.getInstance().getCnx();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de salles par service");

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("service"), rs.getInt("total")));
            }

            salleServiceChart.getData().add(series);

        } catch (SQLException e) {
            System.out.println("❌ Erreur salle/service : " + e.getMessage());
        }
    }

    private void chargerStatistiquesParEtatSalle() {
        String sql = "SELECT etat, COUNT(*) AS total FROM salle GROUP BY etat";

        try (Connection conn = MyConnection.getInstance().getCnx();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Salles par état");

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("etat"), rs.getInt("total")));
            }

            salleEtatChart.getData().add(series);

        } catch (SQLException e) {
            System.out.println("❌ Erreur salles/état : " + e.getMessage());
        }
    }

    private void chargerStatistiquesParEtatService() {
        String sql = "SELECT etat, COUNT(*) AS total FROM service GROUP BY etat";

        try (Connection conn = MyConnection.getInstance().getCnx();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Services par état");

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("etat"), rs.getInt("total")));
            }

            serviceEtatChart.getData().add(series);

        } catch (SQLException e) {
            System.out.println("❌ Erreur services/état : " + e.getMessage());
        }
    }

    @FXML
    private void retour(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/affichage.fxml"));
        Scene scene = new Scene(loader.load());

        // Récupération de la scène à partir du bouton qui a déclenché l'événement
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("ajouter salle");
        stage.show();
    }
}