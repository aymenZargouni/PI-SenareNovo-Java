package ed.sanarenovo.controllers.takoua;

import ed.sanarenovo.entities.Offre;
import ed.sanarenovo.services.OffreService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;

public class OffreFormController {
    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtDescription;
    @FXML
    private DatePicker dateExpiration;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private final OffreService offreService = new OffreService();
    private RHController rhController;
    private Offre offreToEdit;

    public void setRHController(RHController rhController) {
        this.rhController = rhController;
    }

    public void setOffreForEdit(Offre offre) {
        this.offreToEdit = offre;
        txtTitre.setText(offre.getTitre());
        txtDescription.setText(offre.getDescription());

        if (offre.getDateExpiration() instanceof java.sql.Date) {
            java.sql.Date sqlDate = (java.sql.Date) offre.getDateExpiration();
            dateExpiration.setValue(sqlDate.toLocalDate());
        } else {
            dateExpiration.setValue(offre.getDateExpiration()
                    .toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate());
        }
    }

    @FXML
    private void initialize() {
        // Bloque les dates passées dans le date picker
        dateExpiration.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)));
            }
        });
    }

    @FXML
    private void handleSave() {
        if (validateInputs()) {
            if (offreToEdit == null) {
                Offre nouvelleOffre = new Offre();
                nouvelleOffre.setTitre(txtTitre.getText());
                nouvelleOffre.setDescription(txtDescription.getText());
                nouvelleOffre.setDatePublication(new java.util.Date());
                nouvelleOffre.setDateExpiration(Date.valueOf(dateExpiration.getValue()));

                offreService.addOffre(nouvelleOffre);
            } else {
                offreToEdit.setTitre(txtTitre.getText());
                offreToEdit.setDescription(txtDescription.getText());
                offreToEdit.setDateExpiration(Date.valueOf(dateExpiration.getValue()));

                offreService.updateOffre(offreToEdit);
            }

            if (rhController != null) {
              
                rhController.loadOffres();  // Remplacer refreshOffres() par loadOffres()

            }

            closeWindow();
        }
    }


    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        String errorMessage = "";

        if (txtTitre.getText() == null || txtTitre.getText().trim().isEmpty()) {
            errorMessage += "Titre est obligatoire!\n";
        }

        if (txtDescription.getText() == null || txtDescription.getText().trim().isEmpty()) {
            errorMessage += "Description est obligatoire!\n";
        }

        if (dateExpiration.getValue() == null) {
            errorMessage += "Date d'expiration est obligatoire!\n";
        } else if (!dateExpiration.getValue().isAfter(LocalDate.now())) {
            errorMessage += "La date d'expiration doit être postérieure à aujourd'hui!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
