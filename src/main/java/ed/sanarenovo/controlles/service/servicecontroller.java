package ed.sanarenovo.controlles.service;

import ed.sanarenovo.entities.Service;
import ed.sanarenovo.services.Serviceserv;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class servicecontroller {

        @FXML
        private TextField cap;

        @FXML
        private Button enregist;
        @FXML
        private Button retour;

        @FXML
        private TextField etat;

        @FXML
        private Label nbr_salle;

        @FXML
        private TextField nbr_salles;

        @FXML
        private Label messafe4;

        @FXML
        private Label message1;

        @FXML
        private Label message2;

        @FXML
        private Label message3;

        @FXML
        private TextField nom;

        @FXML
        private TextField nomchef;

        @FXML
        void addService(ActionEvent event) {
                String nomService = nom.getText().trim();
                String nomChef = nomchef.getText().trim();
                String etatText = etat.getText().trim();
                String capText = cap.getText().trim();

                // Vérification que tous les champs sont remplis
                if (nomService.isEmpty() || nomChef.isEmpty() || etatText.isEmpty() || capText.isEmpty()) {
                        System.out.println(" Tous les champs doivent être remplis.");
                        return;
                }

                // Vérification que le nom du service est > 5 caractères
                if (nomService.length() <= 5) {
                        message1.setText(" Le nom du service doit contenir plus de 5 caractères.");
                        return;
                }

                // Vérification que le nom du chef est > 5 caractères
                if (nomChef.length() <= 5) {
                        message2.setText(" Le nom du chef de service doit contenir plus de 5 caractères.");
                        return;
                }

                // Vérification que etat est "true" ou "false"
                if (!etatText.equalsIgnoreCase("complet") && !etatText.equalsIgnoreCase("vide")) {
                        messafe4.setText(" Le champ 'état' doit être 'true' ou 'false'.");
                        return;
                }

                // Vérification que capacité est un entier > 0
                int capacite;
                try {
                        capacite = Integer.parseInt(capText);
                        if (capacite <= 0) {
                                System.out.println(" La capacité doit être un entier strictement positif.");
                                return;
                        }
                } catch (NumberFormatException e) {
                        message3.setText(" La capacité doit être un nombre entier valide.");
                        return;
                }

                boolean etatService = Boolean.parseBoolean(etatText);
                int nombreSalles = 0; // Par défaut à 0

                // Création et ajout du service
                Service serv = new Service(nomService, nomChef, nombreSalles, etatService, capacite);
                Serviceserv serviceManager = new Serviceserv();
                serviceManager.addService(serv);

                messafe4.setText("Service ajouté avec succès !");
        }


        @FXML
        public void initialize() {
                nbr_salles.setText("0");
                nbr_salles.setEditable(false); // Empêche l'utilisateur d'éditer
                nbr_salles.setFocusTraversable(false); // Pour éviter que le curseur s'y place
        }

        @FXML
        void retour(ActionEvent event) throws IOException {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/affichage.fxml"));

                // Créer une nouvelle scène
                Scene scene = new Scene(loader.load());

                // Obtenir la fenêtre (Stage) actuelle et changer la scène
                Stage stage = (Stage) retour.getScene().getWindow(); // Changez 'mod' si nécessaire pour le bouton que vous utilisez pour naviguer
                stage.setScene(scene);
                stage.show();

        }
}
