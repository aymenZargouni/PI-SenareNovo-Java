package ed.sanarenovo.controllers.service;

import ed.sanarenovo.entities.Service;
import ed.sanarenovo.services.Serviceserv;
import ed.sanarenovo.utils.PDFGenerator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;


public class showctrl {

    @FXML
    public Button stat1;
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
    private TextField searchField;
    @FXML
    private Button pdf;

    @FXML
    private Button speakButton;

    @FXML
    void speakSelectedService(ActionEvent event) {
        Service selected = tabview.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String message = "Service : " + selected.getNom() + ", Chef : " + selected.getChef_service() + ", nombre de salle  : " + selected.getNbr_salle() + ", Capacité  : " + selected.getCapacite();
            handleTextToSpeech(message);
        } else {
            handleTextToSpeech("Aucun service sélectionné.");
        }
    }

    Serviceserv salleService = new Serviceserv(); // Assure-toi d’avoir une instance de ton service

    @FXML
    void receherchesalle(ActionEvent event) {
        String searchId = searchField.getText().trim();

        if (searchId.isEmpty()) {
            System.out.println("Le champ de recherche est vide.");
            return;
        }

        try {
            int idRecherche = Integer.parseInt(searchId);
            Service salle = salleService.getServiceById(idRecherche);

            if (salle != null) {
                ObservableList<Service> resultats = FXCollections.observableArrayList();
                resultats.add(salle);
                tabview.setItems(resultats);
            } else {
                System.out.println("Aucune salle trouvée avec l’ID " + idRecherche);
                tabview.setItems(FXCollections.observableArrayList()); // Vide
            }

        } catch (NumberFormatException e) {
            System.out.println("ID invalide : ce n’est pas un nombre.");
        }
    }


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

        // ➕ Cellule personnalisée pour afficher "Libre" / "Réservé"
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        colEtat.setCellFactory(column -> new TableCell<Service, Boolean>() {
            @Override
            protected void updateItem(Boolean etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) {
                    setText(null);
                } else {
                    setText(etat ? "Libre" : "Réservé");
                }
            }
        });

        // Charger la liste des services
        ObservableList<Service> list = FXCollections.observableArrayList(serviceManager.getServices());
        tabview.setItems(list);

        // 👉 Ajouter le listener de sélection
        tabview.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                nomField.setText(newValue.getNom());
                chefField.setText(newValue.getChef_service());

                capaciteField.setText(String.valueOf(newValue.getCapacite()));
                etatField.setText(newValue.isEtat() ? "libre" : "reserver");
            } else {
                nomField.clear();
                chefField.clear();
                capaciteField.clear();
                etatField.clear();
            }
        });
    }

    @FXML
    public void updateService(ActionEvent event) {


        Service selected = tabview.getSelectionModel().getSelectedItem();
        message1.setText("");
        message2.setText("");
        message3.setText("");
        message4.setText("");
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
            if (!(etat.equals("libre") || etat.equals("reserver"))) {
                message4.setText("L'état doit être 'libre' ou 'reserver'.");
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
        stage.setTitle("ajouter service");
        stage.show();
}
    @FXML
    void stat(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/stat.fxml"));

        // Créer une nouvelle scène
        Scene scene = new Scene(loader.load());

        // Obtenir la fenêtre (Stage) actuelle et changer la scène
        Stage stage = (Stage) stat1.getScene().getWindow(); // Changez 'mod' si nécessaire pour le bouton que vous utilisez pour naviguer
        stage.setScene(scene);
        stage.setTitle("ajouter service");
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
        stage.setTitle("ajouter salle");
        stage.show();
    }
    @FXML
    void exportserviceToPDF()
    {
        List<Service> users = Serviceserv.getServices();


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (.pdf)", ".pdf"));
        fileChooser.setInitialFileName("user-list.pdf");

        File selectedFile = fileChooser.showSaveDialog(tabview.getScene().getWindow());

        if (selectedFile != null) {
            PDFGenerator.generateUserListPDF(users, selectedFile);
        }
    }
    @FXML
    private Button btnRecommande;

    @FXML
    void ouvrirRecommandation(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/recommande.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) btnRecommande.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Recommandation médicale");
        stage.show();
    }
    @FXML
    private void handleTextToSpeech(String text) {
        if (text == null || text.trim().isEmpty()) {
            System.out.println("❌ Aucun texte à lire !");
            return;
        }

        try {
            String pythonScriptPath = "src/main/python/text_to_speech.py"; // relative path
            ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath, text);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("🔄 Script terminé avec code : " + exitCode);

        } catch (IOException | InterruptedException e) {
            System.out.println("❌ Erreur lors de l'appel au script Python : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
