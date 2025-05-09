package ed.sanarenovo.controllers.consultation;

import ed.sanarenovo.entities.Medecin;
import ed.sanarenovo.entities.Patient;
import ed.sanarenovo.entities.rv;
import ed.sanarenovo.utils.MyConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.function.Function;

public class Rendez_vousController implements Initializable {

    @FXML private TableView<rv> tableView;
    @FXML private TableColumn<rv, Integer> colId;
    @FXML private TableColumn<rv, String> colPatient;
    @FXML private TableColumn<rv, String> colMedecin;
    @FXML private TableColumn<rv, String> colDate;
    @FXML private TableColumn<rv, String> colMotif;
    @FXML private TableColumn<rv, String> colStatut;

    @FXML private ComboBox<Patient> patientComboBox;
    @FXML private ComboBox<Medecin> medecinComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField motifField;

    @FXML private Label patientMessage;
    @FXML private Label medecinMessage;
    @FXML private Label dateMessage;
    @FXML private Label motifMessage;

    private final ObservableList<rv> data = FXCollections.observableArrayList();

    private <T> void setupComboBoxDisplay(ComboBox<T> comboBox, Function<T, String> toStringFunction) {
        comboBox.setCellFactory(lv -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : toStringFunction.apply(item));
            }
        });

        comboBox.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : toStringFunction.apply(item));
            }
        });
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPatient().getFullname()));
        colMedecin.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMedecin().getFullname()));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateR"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Configuration des ComboBox
        setupComboBoxDisplay(patientComboBox, Patient::getFullname);
        setupComboBoxDisplay(medecinComboBox, Medecin::getFullname);

        // Chargement des données
        loadRendezVous();
        loadPatients();
        loadMedecins();

        // Écouteur de sélection
        tableView.setOnMouseClicked(this::handleTableClick);
    }

    private void loadRendezVous() {
        data.clear();
        String query = """
            SELECT rv.*, p.fullname as patient_name, m.fullname as medecin_name 
            FROM rendez_vous rv
            JOIN patient p ON rv.patient_id = p.id
            JOIN medecin m ON rv.medecin_id = m.id
            """;

        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                rv rv = new rv();
                rv.setId(rs.getInt("id"));

                Patient patient = new Patient();
                patient.setId(rs.getInt("patient_id"));
                patient.setFullname(rs.getString("patient_name"));
                rv.setPatient(patient);

                Medecin medecin = new Medecin();
                medecin.setId(rs.getInt("medecin_id"));
                medecin.setFullname(rs.getString("medecin_name"));
                rv.setMedecin(medecin);

                rv.setDateR(rs.getDate("date_r"));
                rv.setMotif(rs.getString("motif"));
                rv.setStatut(rs.getString("statut"));

                data.add(rv);
            }

            tableView.setItems(data);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement", "Erreur lors du chargement des rendez-vous: " + e.getMessage());
        }
    }

    private void loadPatients() {
        patientComboBox.getItems().clear();
        String query = "SELECT * FROM senarenovo.patient";

        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("id"));
                patient.setFullname(rs.getString("fullname"));
                patient.setGender(rs.getString("gender"));
                patient.setAdress(rs.getString("adress"));

                patientComboBox.getItems().add(patient);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement", "Erreur lors du chargement des patients: " + e.getMessage());
        }
    }

    private void loadMedecins() {
        medecinComboBox.getItems().clear();
        String query = "SELECT * FROM medecin";

        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Medecin medecin = new Medecin();
                medecin.setId(rs.getInt("id"));
                medecin.setFullname(rs.getString("fullname"));
                medecin.setDateEmbauche(rs.getDate("date_embauche"));
                medecin.setSpecilite(rs.getString("specilite"));

                medecinComboBox.getItems().add(medecin);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement", "Erreur lors du chargement des médecins: " + e.getMessage());
        }
    }

    private void handleTableClick(MouseEvent event) {
        rv selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            patientComboBox.setValue(selected.getPatient());
            medecinComboBox.setValue(selected.getMedecin());
            datePicker.setValue(selected.getDateR().toLocalDate());
            motifField.setText(selected.getMotif());
        }
    }

    @FXML
    private void ajouterRendezVous() {
        if (!validateInputs()) return;

        String query = "INSERT INTO rendez_vous (patient_id, medecin_id, date_r, motif, statut) VALUES (?, ?, ?, ?, ?)";

        try (Connection cnx = MyConnection.getInstance().getCnx();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setInt(1, patientComboBox.getValue().getId());
            pst.setInt(2, medecinComboBox.getValue().getId());
            pst.setDate(3, Date.valueOf(datePicker.getValue()));
            pst.setString(4, motifField.getText());
            pst.setString(5, "En attente"); // Statut par défaut

            pst.executeUpdate();
            clearFields();
            loadRendezVous();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur d'ajout", "Erreur lors de l'ajout du rendez-vous: " + e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        patientComboBox.setValue(null);
        medecinComboBox.setValue(null);
        datePicker.setValue(null);
        motifField.clear();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Réinitialiser les styles et messages
        patientComboBox.getStyleClass().remove("input-error");
        medecinComboBox.getStyleClass().remove("input-error");
        datePicker.getStyleClass().remove("input-error");
        motifField.getStyleClass().remove("input-error");

        patientMessage.setText("");
        medecinMessage.setText("");
        dateMessage.setText("");
        motifMessage.setText("");

        // Validation patient
        if (patientComboBox.getValue() == null) {
            patientComboBox.getStyleClass().add("input-error");
            patientMessage.setText("Patient requis");
            isValid = false;
        }

        // Validation médecin
        if (medecinComboBox.getValue() == null) {
            medecinComboBox.getStyleClass().add("input-error");
            medecinMessage.setText("Médecin requis");
            isValid = false;
        }

        // Validation date
        if (datePicker.getValue() == null) {
            datePicker.getStyleClass().add("input-error");
            dateMessage.setText("Date requise");
            isValid = false;
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            datePicker.getStyleClass().add("input-error");
            dateMessage.setText("La date doit être dans le futur");
            isValid = false;
        }

        // Validation motif
        if (motifField.getText() == null || motifField.getText().trim().isEmpty()) {
            motifField.getStyleClass().add("input-error");
            motifMessage.setText("Motif requis");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void changerStatutTermine(ActionEvent event) {
        rv selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "UPDATE rendez_vous SET statut = ? WHERE id = ?";

            try (Connection cnx = MyConnection.getInstance().getCnx();
                 PreparedStatement pst = cnx.prepareStatement(query)) {

                pst.setString(1, "Terminé");
                pst.setInt(2, selected.getId());
                pst.executeUpdate();

                // Update the displayed status
                selected.setStatut("Terminé");
                tableView.refresh();

            } catch (SQLException e) {
                showAlert("Erreur", "Erreur de mise à jour", "Erreur lors du changement de statut: " + e.getMessage());
            }
        } else {
            showAlert("Aucune sélection", "Aucun rendez-vous sélectionné", "Veuillez sélectionner un rendez-vous");
        }
    }

    @FXML
    private void changerStatutAnnule(ActionEvent event) {
        rv selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "UPDATE rendez_vous SET statut = ? WHERE id = ?";

            try (Connection cnx = MyConnection.getInstance().getCnx();
                 PreparedStatement pst = cnx.prepareStatement(query)) {

                pst.setString(1, "Annulé");
                pst.setInt(2, selected.getId());
                pst.executeUpdate();

                selected.setStatut("Annulé");
                tableView.refresh();

            } catch (SQLException e) {
                showAlert("Erreur", "Erreur de mise à jour", "Erreur lors du changement de statut: " + e.getMessage());
            }
        } else {
            showAlert("Aucune sélection", "Aucun rendez-vous sélectionné", "Veuillez sélectionner un rendez-vous");
        }
    }

    @FXML
    private void changerStatutEnCours(ActionEvent event) {
        rv selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "UPDATE rendez_vous SET statut = ? WHERE id = ?";

            try (Connection cnx = MyConnection.getInstance().getCnx();
                 PreparedStatement pst = cnx.prepareStatement(query)) {

                pst.setString(1, "En Cours");
                pst.setInt(2, selected.getId());
                pst.executeUpdate();

                selected.setStatut("En Cours");
                tableView.refresh();

            } catch (SQLException e) {
                showAlert("Erreur", "Erreur de mise à jour", "Erreur lors du changement de statut: " + e.getMessage());
            }
        } else {
            showAlert("Aucune sélection", "Aucun rendez-vous sélectionné", "Veuillez sélectionner un rendez-vous");
        }
    }

    private void changerStatutRendezVous(String nouveauStatut) {
        rv selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "UPDATE rendez_vous SET statut = ? WHERE id = ?";

            try (Connection cnx = MyConnection.getInstance().getCnx();
                 PreparedStatement pst = cnx.prepareStatement(query)) {

                pst.setString(1, nouveauStatut);
                pst.setInt(2, selected.getId());
                pst.executeUpdate();

                // Mettre à jour l'affichage
                selected.setStatut(nouveauStatut);
                tableView.refresh();

            } catch (SQLException e) {
                showAlert("Erreur", "Erreur de mise à jour", "Erreur lors du changement de statut: " + e.getMessage());
            }
        } else {
            showAlert("Aucune sélection", "Aucun rendez-vous sélectionné", "Veuillez sélectionner un rendez-vous");
        }
    }

    @FXML
    private void redirectToDossierMedicale() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Youssef_views/dm.fxml"));
            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/Youssef_views/design.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠️ CSS file not found: /design.css");
            }

            Stage currentStage = (Stage) tableView.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void redirectTostat() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Youssef_views/Statistiques.fxml"));
            Scene scene = new Scene(root);

            Stage currentStage = (Stage) tableView.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void redirectToCalendar(javafx.event.ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Youssef_views/ConsultationCalendar.fxml"));
            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/Youssef_views/design.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠️ CSS file not found: /design.css");
            }

            Stage currentStage = (Stage) tableView.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}