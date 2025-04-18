package ed.sanarenovo.controlles.service;

import javafx.event.ActionEvent;
import ed.sanarenovo.entities.Salle;
import ed.sanarenovo.services.Salleserv;
import ed.sanarenovo.services.Serviceserv;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class addSalle implements Initializable {

    @FXML
    private Button energ;
    @FXML
    public Button salo;
    @FXML
    private TextField etat;

    @FXML
    private ComboBox<Integer> id_serv;

    @FXML
    private TextField type;
    @FXML
    private Label message1;
    @FXML
    private ComboBox<String> etatcombo;
    @FXML
    private Label message2;

    private final Salleserv salleService = new Salleserv();
    private final Serviceserv serviceService = new Serviceserv(); // classe qui récupère les ids services

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger les id des services dans la ComboBox
        List<Integer> ids = serviceService.getAllServiceIds(); // cette méthode retourne une liste des ids
        id_serv.getItems().addAll(ids);

        etatcombo.getItems().addAll("vide", "complet");
    }


    @FXML
    void addSalle(ActionEvent event) {
        message1.setText("");
        message2.setText("");
        message1.setStyle("-fx-text-fill: red;");
        message2.setStyle("-fx-text-fill: red;");

        try {
            Integer serviceId = id_serv.getValue();
            String typeText = type.getText().trim();
            String etatText = etatcombo.getValue();

            boolean isValid = true;

            if (typeText.length() < 3) {
                message1.setText("Le type doit contenir au moins 3 caractères.");
                isValid = false;
            }

            if (etatText == null) {
                message2.setText("Veuillez sélectionner un état (vide ou complet).");
                isValid = false;
            }

            if (serviceId == null) {
                message2.setText("Veuillez sélectionner un service.");
                isValid = false;
            }

            if (!isValid) return;

            // Traduire le choix "vide" => true, "complet" => false
            boolean etatBool = etatText.equals("vide");

            Salle s = new Salle();
            s.setService_id(serviceId);
            s.setType(typeText);
            s.setEtat(etatBool);

            salleService.addSalle(s);

            message1.setStyle("-fx-text-fill: green;");
            message1.setText("Salle ajoutée avec succès !");
            message2.setText("");

            type.clear();
            etatcombo.getSelectionModel().clearSelection();
            id_serv.getSelectionModel().clearSelection();

        } catch (Exception e) {
            message2.setText("Erreur : " + e.getMessage());
        }
    }
    @FXML
    void salshw(ActionEvent event)throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/showsalle.fxml"));

        // Créer une nouvelle scène
        Scene scene = new Scene(loader.load());

        // Obtenir la fenêtre (Stage) actuelle et changer la scène
        Stage stage = (Stage) salo.getScene().getWindow(); // Changez 'mod' si nécessaire pour le bouton que vous utilisez pour naviguer
        stage.setScene(scene);
        stage.show();
    }
    }



