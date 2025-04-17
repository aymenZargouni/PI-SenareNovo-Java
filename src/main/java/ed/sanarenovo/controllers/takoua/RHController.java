package ed.sanarenovo.controllers.takoua;

import ed.sanarenovo.entities.Candidature;
import ed.sanarenovo.entities.Offre;
import ed.sanarenovo.services.CandidatureService;
import ed.sanarenovo.services.MailService;
import ed.sanarenovo.services.OffreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Date;

public class RHController {
    @FXML
    private TabPane tabPane;

    // Onglet Offres
    @FXML
    private TableView<Offre> tableOffres;
    @FXML
    private TableColumn<Offre, Integer> colOffreId;
    @FXML
    private TableColumn<Offre, String> colOffreTitre;
    @FXML
    private TableColumn<Offre, Date> colOffreExpiration;
    @FXML
    private Button btnAddOffre;
    @FXML
    private Button btnEditOffre;
    @FXML
    private Button btnDeleteOffre;

    // Onglet Candidatures
    @FXML
    private TableView<Candidature> tableCandidatures;
    @FXML
    private TableColumn<Candidature, Integer> colCandId;
    @FXML
    private TableColumn<Candidature, String> colCandNom;
    @FXML
    private TableColumn<Candidature, String> colCandPrenom;
    @FXML
    private TableColumn<Candidature, String> colCandEmail;
    @FXML
    private TableColumn<Candidature, Date> colCandDate;
    @FXML
    private TableColumn<Candidature, String> colCandStatut;
    @FXML
    private ComboBox<String> comboStatut;
    @FXML
    private Button btnUpdateStatut;
    @FXML
    private Button btnRetour;

    private OffreService offreService = new OffreService();
    private CandidatureService candidatureService = new CandidatureService();
    private MailService mailService = new MailService();

    private ObservableList<Offre> offresList = FXCollections.observableArrayList();
    private ObservableList<Candidature> candidaturesList = FXCollections.observableArrayList();
    private ObservableList<String> statuts = FXCollections.observableArrayList(
            "En attente", "Acceptée", "Refusée", "En cours d'évaluation");

    @FXML
    public void initialize() {
        // Configuration de l'onglet Offres
        colOffreId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOffreTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colOffreExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));

        // Configuration de l'onglet Candidatures
        colCandId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCandNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCandPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCandEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCandDate.setCellValueFactory(new PropertyValueFactory<>("dateCandidature"));
        colCandStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        comboStatut.setItems(statuts);

        loadOffres();
        loadCandidatures();
    }

    private void loadOffres() {
        offresList.clear();
        offresList.addAll(offreService.getAllOffres());
        tableOffres.setItems(offresList);
    }

    private void loadCandidatures() {
        candidaturesList.clear();
        candidaturesList.addAll(candidatureService.getAllCandidatures());
        tableCandidatures.setItems(candidaturesList);
    }

    @FXML
    private void handleAddOffre() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/takoua_views/offre_form.fxml"));
        Parent root = loader.load();
        OffreFormController controller = loader.getController();
        controller.setRHController(this);

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Ajouter une nouvelle offre");
        stage.show();
    }

    @FXML
    private void handleEditOffre() {
        Offre selectedOffre = tableOffres.getSelectionModel().getSelectedItem();
        if (selectedOffre != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/takoua_views/offre_form.fxml"));
                Parent root = loader.load();
                OffreFormController controller = loader.getController();
                controller.setRHController(this);
                controller.setOffreForEdit(selectedOffre);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Modifier l'offre");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner une offre à modifier");
        }
    }

    @FXML
    private void handleDeleteOffre() {
        Offre selectedOffre = tableOffres.getSelectionModel().getSelectedItem();
        if (selectedOffre != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer l'offre");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette offre?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                offreService.deleteOffre(selectedOffre.getId());
                loadOffres();
            }
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner une offre à supprimer");
        }
    }

    @FXML
    private void handleUpdateStatut() {
        Candidature selectedCandidature = tableCandidatures.getSelectionModel().getSelectedItem();
        String newStatut = comboStatut.getValue();

        if (selectedCandidature != null && newStatut != null) {
            candidatureService.updateStatut(selectedCandidature.getId(), newStatut);
            mailService.sendMail(selectedCandidature, newStatut);
            loadCandidatures();
        } else {
            showAlert("Sélection requise", "Veuillez sélectionner une candidature et un statut");
        }
    }

    @FXML
    private void handleRetour() throws IOException {
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/takoua_views/main_view.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("Accueil");
        stage.show();
    }

    public void refreshOffres() {
        loadOffres();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}