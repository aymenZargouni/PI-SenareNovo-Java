package ed.sanarenovo.interfaces;

import ed.sanarenovo.entities.dossiermedicale;
import ed.sanarenovo.utils.MyConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public interface dossiermedicaleService {
    public void initialize(URL url, ResourceBundle resourceBundle);
    void loadDossiers();
    void handleTableClick(MouseEvent event);
    void ajouterDossier(javafx.event.ActionEvent event);
    void supprimerDossier(javafx.event.ActionEvent event);
    void updateDossier(javafx.event.ActionEvent event);
    void clearFields(javafx.event.ActionEvent event);
    void showAlert(String message);
}
