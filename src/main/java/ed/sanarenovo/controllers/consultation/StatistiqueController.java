package ed.sanarenovo.controllers.consultation;
import ed.sanarenovo.services.StatistiqueService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class StatistiqueController implements Initializable {

    @FXML
    private Label labelTotalDossiers;

    @FXML
    private Label labelTotalConsultations;

    @FXML
    private Label labelMoyenneConsultations;

    @FXML
    private PieChart pieChartConsultations;

    private final StatistiqueService statistiqueService = new StatistiqueService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStatistics();
    }

    private void loadStatistics() {
        Map<String, Object> stats = statistiqueService.getMedicalStatistics();

        labelTotalDossiers.setText("Total Dossiers: " + stats.get("total_dossiers"));
        labelTotalConsultations.setText("Total Consultations: " + stats.get("total_consultations"));
        labelMoyenneConsultations.setText("Moyenne Consultations/Dossier: " + String.format("%.2f", stats.get("moyenne_consultations_par_dossier")));

        Map<String, Integer> consultationsParType = (Map<String, Integer>) stats.get("consultations_par_type");

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : consultationsParType.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        pieChartConsultations.setData(pieChartData);
    }
}
