package ed.sanarenovo.controlles.service;

import ed.sanarenovo.entities.Service;
import ed.sanarenovo.services.Serviceserv;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.Optional;


public class showctrl {


    @FXML
    private TableView<Service> tabview;
    @FXML
    private TableColumn<Service, Integer> colID;
    @FXML
    private TableColumn<Service, String> colNom;

    @FXML
    private TableColumn<Service, String> colChef;

    @FXML
    private TableColumn<Service, Integer> colSalles;

    @FXML
    private TableColumn<Service, Integer> colCapacite;

    @FXML
    private TableColumn<Service, Boolean> colEtat;
    @FXML
    private Button supp;
    @FXML
    private Button updat;
    @FXML
    private Label message1;
    @FXML
    public Button addsall;
    @FXML

    private Label message2;
    @FXML
    public Button add3;
    @FXML
    private Label message3;

    @FXML
    private Label message4;

    @FXML private TextField nomField;
    @FXML private TextField chefField;
    @FXML private TextField nbrSallesField;
    @FXML private TextField capaciteField;
    @FXML private TextField etatField;

    @FXML
    void deleteService(ActionEvent event) {
        // Récupérer le service sélectionné
        Service selected = tabview.getSelectionModel().getSelectedItem();

        // Vérifier si un élément est sélectionné
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner un service à supprimer", Alert.AlertType.WARNING);
            return;
        }

        // Demander confirmation
        if (!showConfirmation("Confirmer la suppression",
                "Êtes-vous sûr de vouloir supprimer le service '" + selected.getNom() + "' ?")) {
            return;
        }

        try {
            // Appeler la méthode de suppression
            Serviceserv serviceServ = new Serviceserv();
            serviceServ.deleteService(selected);


            // Rafraîchir la table
            refreshTable();

            // Afficher un message de succès
            showAlert("Succès", "Service supprimé avec succès", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            // Gérer les erreurs
            showAlert("Erreur", "Échec de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Méthode pour rafraîchir la table
    private void refreshTable() {
        Serviceserv serviceServ = new Serviceserv();
        ObservableList<Service> services = FXCollections.observableArrayList(serviceServ.getServices());
        tabview.setItems(services);
        tabview.refresh();
    }

    // Méthode utilitaire pour afficher une alerte
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode utilitaire pour confirmation
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void initialize() {
        Serviceserv serviceManager = new Serviceserv();
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colChef.setCellValueFactory(new PropertyValueFactory<>("chef_service"));
        colSalles.setCellValueFactory(new PropertyValueFactory<>("nbr_salle"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        ObservableList<Service> list = FXCollections.observableArrayList(serviceManager.getServices());
        tabview.setItems(list);
    }
    @FXML
    public void updateService(ActionEvent event) {


        Service selected = tabview.getSelectionModel().getSelectedItem();
        message1.setText("");

        boolean valid = true;

        // Nom > 5 caractères
        if (!nomField.getText().isEmpty()) {
            if (nomField.getText().length() < 5) {
                message1.setText("Le nom du service doit contenir au moins 5 caractères.");
                valid = false;
            } else {
                selected.setNom(nomField.getText());
            }
        }

        // Chef > 5 caractères
        if (!chefField.getText().isEmpty()) {
            if (chefField.getText().length() < 5) {
                message2.setText("Le nom du chef de service doit contenir au moins 5 caractères.");
                valid = false;
            } else {
                selected.setChef_service(chefField.getText());
            }
        }

        // Capacité > 0
        if (!capaciteField.getText().isEmpty()) {
            try {
                int capacite = Integer.parseInt(capaciteField.getText());
                if (capacite <= 0) {
                    message3.setText("La capacité doit être un entier strictement positif.");
                    valid = false;
                } else {
                    selected.setCapacite(capacite);
                }
            } catch (NumberFormatException e) {
                message3.setText("La capacité doit être un nombre entier.");
                valid = false;
            }
        }

        // État = true/false
        if (!etatField.getText().isEmpty()) {
            String etat = etatField.getText().toLowerCase();
            if (!(etat.equals("true") || etat.equals("false"))) {
                message4.setText("L'état doit être 'true' ou 'false'.");
                valid = false;
            } else {
                selected.setEtat(Boolean.parseBoolean(etat));
            }
        }

        if (!valid) return;

        // Mise à jour via le service
        Serviceserv serviceServ = new Serviceserv();
        serviceServ.updateService(selected, selected.getId());

        // Rafraîchir la table
        refreshTable();

        // Message succès dans l'interface
        message4.setText("Service modifié avec succès !");
        message4.setStyle("-fx-text-fill: green;");

        // Vider les champs
        nomField.clear();
        chefField.clear();
        capaciteField.clear();
        etatField.clear();
    }
    @FXML
    void servicenav(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/serv.fxml"));

        // Créer une nouvelle scène
        Scene scene = new Scene(loader.load());

        // Obtenir la fenêtre (Stage) actuelle et changer la scène
        Stage stage = (Stage) add3.getScene().getWindow(); // Changez 'mod' si nécessaire pour le bouton que vous utilisez pour naviguer
        stage.setScene(scene);
        stage.show();
}
    @FXML
    void addsale (ActionEvent event)  throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/addsalle.fxml"));

        // Créer une nouvelle scène
        Scene scene = new Scene(loader.load());

        // Obtenir la fenêtre (Stage) actuelle et changer la scène
        Stage stage = (Stage) addsall.getScene().getWindow(); // Changez 'mod' si nécessaire pour le bouton que vous utilisez pour naviguer
        stage.setScene(scene);
        stage.show();
    }

}
