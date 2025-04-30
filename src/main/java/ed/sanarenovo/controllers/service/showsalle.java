package ed.sanarenovo.controllers.service;

import ed.sanarenovo.entities.Salle;
import ed.sanarenovo.services.Salleserv;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;



public class showsalle {

    @FXML
    private TableColumn<Salle, Integer> id;
    @FXML
    public Button add2;

    @FXML
    private TableColumn<Salle, Boolean> idetat;

    @FXML
    private TableColumn<Salle, Integer> idsser;

    @FXML
    private TableColumn<Salle, String> idtype;

    @FXML
    private TableView<Salle> tabl;
    @FXML
    public Button supp;
    @FXML
    public TextField etat;
    @FXML
    public Label message1;

    @FXML
    public Label message2;
    @FXML
    private Button mod;
    @FXML
    public TextField type;

    @FXML
    public void initialize() {
        Salleserv salleManager = new Salleserv();

        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        idetat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        idsser.setCellValueFactory(new PropertyValueFactory<>("service_id"));
        idtype.setCellValueFactory(new PropertyValueFactory<>("type"));

        ObservableList<Salle> list = FXCollections.observableArrayList(salleManager.getSalles());
        tabl.setItems(list);

        tabl.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                etat.setText(String.valueOf(newSelection.isEtat())); // true/false
                type.setText(newSelection.getType());

            }
        });
    }

    @FXML
    public void deleteSalle(ActionEvent event) {
        Salle selected = tabl.getSelectionModel().getSelectedItem();

        if (selected != null) {
            Salleserv service = new Salleserv();
            service.deleteSalle(selected);

            // Rafraîchir la table après suppression
            ObservableList<Salle> updatedList = FXCollections.observableArrayList(service.getSalles());
            tabl.setItems(updatedList);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Suppression");
            alert.setHeaderText(null);
            alert.setContentText("Salle supprimée avec succès !");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une salle à supprimer.");
            alert.showAndWait();
        }
    }



    @FXML
    public void updateSalle(ActionEvent event) {
        Salle selected = tabl.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // Clear any previous message


            try {
                String etatText = etat.getText().trim();
                String typeText = type.getText().trim();

                // Validate etat input
                if (!etatText.equalsIgnoreCase("true") && !etatText.equalsIgnoreCase("false")) {
                    message2.setText("L'état doit être 'true' ou 'false'.");

                    return;
                }

                boolean newEtat = Boolean.parseBoolean(etatText);

                // Validate type input
                if (typeText.isEmpty()) {
                    message1.setText("Le type ne peut pas être vide.");

                    return;
                }

                // Update the selected salle object
                selected.setEtat(newEtat);
                selected.setType(typeText);

                // Create a service and update the salle
                Salleserv service = new Salleserv();
                service.updateSalle(selected);

                // Refresh the table with updated salles
                ObservableList<Salle> updatedList = FXCollections.observableArrayList(service.getSalles());
                tabl.setItems(updatedList);

                // Show success message
                message2.setText("Salle modifiée avec succès !");

            } catch (NumberFormatException e) {
                message1.setText("Erreur de saisie, vérifiez les champs.");

            }
         ;
        }
    }
    @FXML
    void navigations (ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/addsalle.fxml"));

        // Créer une nouvelle scène
        Scene scene = new Scene(loader.load());

        // Obtenir la fenêtre (Stage) actuelle et changer la scène
        Stage stage = (Stage) mod.getScene().getWindow(); // Changez 'mod' si nécessaire pour le bouton que vous utilisez pour naviguer
        stage.setScene(scene);
        stage.show();
    }

}
