package ed.sanarenovo.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class AdminSideBarController {


    @FXML
    private Button btnUsers;
    @FXML
    private Button btnMedecins;
    @FXML
    private Button btnTechniciens;
    @FXML
    private Button btnPatients;

    @FXML
    void initialize() {
        btnUsers.setOnAction(e -> openView("/ShowUsers.fxml"));
        btnMedecins.setOnAction(e -> openView("/ShowMedecin.fxml"));
        btnTechniciens.setOnAction(e -> openView("/ShowTechnicien.fxml"));
        btnPatients.setOnAction(e -> openView("/ShowPatients.fxml"));
    }

    private void openView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) btnUsers.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
