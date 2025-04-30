package ed.sanarenovo.controlles.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.*;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.MultiFormatWriter;

import ed.sanarenovo.entities.Salle;
import ed.sanarenovo.services.Salleserv;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class showsalle {

    @FXML
    private TableColumn<Salle, Integer> id;

    @FXML
    private TableColumn<Salle, Boolean> idetat;

    @FXML
    private TableColumn<Salle, Integer> idsser;

    @FXML
    private TableColumn<Salle, String> idtype;

    @FXML
    private TableColumn<Salle, Void> qrColumn;

    @FXML
    private TableView<Salle> tabl;

    @FXML
    public TextField etat;

    @FXML
    public TextField type;

    @FXML
    private TextField searchField;

    @FXML
    public Label message1;

    @FXML
    public Label message2;

    @FXML
    public Button mod;

    @FXML
    public Button supp;

    @FXML
    public Button add2;

    @FXML
    public Button servic;

    Salleserv salleService = new Salleserv();

    @FXML
    void initialize() {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        idetat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        idsser.setCellValueFactory(new PropertyValueFactory<>("service_id"));
        idtype.setCellValueFactory(new PropertyValueFactory<>("type"));

        ObservableList<Salle> list = FXCollections.observableArrayList(salleService.getSalles());
        tabl.setItems(list);

        idetat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "libre" : "réservé");
                }
            }
        });

        tabl.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String etatText = newSelection.isEtat() ? "libre" : "réservé";
                etat.setText(etatText);
                type.setText(newSelection.getType());
            }
        });

        addQrButtonToTable();
    }

    public void addQrButtonToTable() {
        qrColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("QR");

            {
                btn.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
                btn.setOnAction(e -> {
                    Salle salle = getTableView().getItems().get(getIndex());
                    if (salle != null) {
                        showQRCodePopup(salle);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void showQRCodePopup(Salle salle) {
        try {
            // Construire les données sous forme de texte
            String qrData = "Salle ID: " + salle.getId() +
                    "\nType: " + salle.getType() +
                    "\nEtat: " + (salle.isEtat() ? "libre" : "réservé");

            int size = 250;
            BitMatrix matrix = new MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, size, size);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            // Convertir BufferedImage en Image JavaFX
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            ImageView imageView = new ImageView(fxImage);
            imageView.setFitWidth(250);
            imageView.setFitHeight(250);

            // Lorsque tu cliques sur l'image du QR code
            imageView.setOnMouseClicked(event -> {
                // Quand l'utilisateur clique sur le QR -> Afficher les données dans un tableau décoré
                showSalleDataPopup(salle);
            });

            VBox box = new VBox(imageView);
            box.setStyle("-fx-padding: 20; -fx-alignment: center;");
            Scene scene = new Scene(box);

            Stage popup = new Stage();
            popup.setTitle("QR Code Salle #" + salle.getId());
            popup.setScene(scene);
            popup.show();

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    // Cette méthode ouvre un popup décoré pour afficher les détails de la salle
    private void showSalleDataPopup(Salle salle) {
        // Création d'un GridPane pour afficher les données en tableau
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #ffffff; -fx-border-color: #0078D7; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Ajouter les labels des titres
        Label keyTitle = new Label("Clé");
        keyTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        Label valueTitle = new Label("Valeur");
        valueTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        grid.add(keyTitle, 0, 0);
        grid.add(valueTitle, 1, 0);

        // Ajouter les données
        grid.add(new Label("Salle ID:"), 0, 1);
        grid.add(new Label(String.valueOf(salle.getId())), 1, 1);

        grid.add(new Label("Type:"), 0, 2);
        grid.add(new Label(salle.getType()), 1, 2);

        grid.add(new Label("Etat:"), 0, 3);
        grid.add(new Label(salle.isEtat() ? "libre" : "réservé"), 1, 3);

        VBox box = new VBox(grid);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Scene scene = new Scene(box);
        Stage popup = new Stage();
        popup.setTitle("Détails de la Salle");
        popup.setScene(scene);
        popup.show();
    }

    @FXML
    void receherchesalle(ActionEvent event) {
        String searchId = searchField.getText().trim();

        if (searchId.isEmpty()) {
            System.out.println("Le champ de recherche est vide.");
            return;
        }

        try {
            int idRecherche = Integer.parseInt(searchId);
            Salle salle = salleService.getSalleById(idRecherche);

            if (salle != null) {
                tabl.setItems(FXCollections.observableArrayList(salle));
            } else {
                tabl.setItems(FXCollections.observableArrayList());
            }
        } catch (NumberFormatException e) {
            System.out.println("ID invalide : ce n’est pas un nombre.");
        }
    }

    @FXML
    public void deleteSalle(ActionEvent event) {
        Salle selected = tabl.getSelectionModel().getSelectedItem();

        if (selected != null) {
            salleService.deleteSalle(selected);
            tabl.setItems(FXCollections.observableArrayList(salleService.getSalles()));

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

        message1.setText("");
        message2.setText("");

        if (selected != null) {
            try {
                String etatText = etat.getText().trim().toLowerCase();
                String typeText = type.getText().trim();

                if (!etatText.equals("libre") && !etatText.equals("réservé") && !etatText.equals("reserve")) {
                    message2.setText("⚠️ L'état doit être 'libre' ou 'réservé'.");
                    return;
                }

                boolean newEtat = etatText.equals("libre");

                if (typeText.length() <= 3) {
                    message1.setText("⚠️ Le type doit contenir plus de 3 caractères.");
                    return;
                }

                selected.setEtat(newEtat);
                selected.setType(typeText);
                salleService.updateSalle(selected);
                tabl.setItems(FXCollections.observableArrayList(salleService.getSalles()));
                message2.setText("✅ Salle modifiée avec succès !");

            } catch (Exception e) {
                message1.setText("❌ Erreur : " + e.getMessage());
            }
        } else {
            message2.setText("⚠️ Veuillez sélectionner une salle.");
        }
    }

    @FXML
    void navigations(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/addsalle.fxml"));
        Stage stage = (Stage) mod.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("ajouter salle");
        stage.show();
    }

    @FXML
    void servicshow(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mahdyviews/affichage.fxml"));
        Stage stage = (Stage) servic.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("list service");
        stage.show();
    }
}
