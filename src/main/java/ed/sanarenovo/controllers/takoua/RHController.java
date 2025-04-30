package ed.sanarenovo.controllers.takoua;

import ed.sanarenovo.utils.StatistiquesCandidatures;
import ed.sanarenovo.entities.Candidature;
import ed.sanarenovo.entities.Offre;
import ed.sanarenovo.services.CandidatureService;
import ed.sanarenovo.services.OffreService;
import ed.sanarenovo.utils.PDFPreview;
import ed.sanarenovo.utils.cvanalysis.CVAnalyzer;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import javafx.scene.control.Alert.AlertType;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class RHController {

    // Offres
    @FXML private TableView<Offre> tableOffres;
    @FXML private TableColumn<Offre, Integer> colOffreId;
    @FXML private TableColumn<Offre, String> colOffreTitre;
    @FXML private TableColumn<Offre, Date> colOffreExpiration;
    @FXML private TextField txtSearchOffres;
    @FXML private Button btnAddOffre, btnEditOffre, btnDeleteOffre;

    // Candidatures
    @FXML private TableView<Candidature> tableCandidatures;
    @FXML private TableColumn<Candidature, Integer> colCandId;
    @FXML private TableColumn<Candidature, String> colCandNom, colCandPrenom, colCandEmail, colCandStatut;
    @FXML private TableColumn<Candidature, Date> colCandDate;
    @FXML private TextField txtSearchCandidatures;
    @FXML private ComboBox<String> comboStatut;
    @FXML private TableColumn<Candidature, Integer> colCandScore;

    @FXML private Button btnUpdateStatut, btnPreviewCV, btnDownloadCV, btnRetour, btnShowStats;

    // Statistiques
    @FXML private VBox statistiquesPane;
    @FXML private ComboBox<String> comboTimeFilter;
    @FXML private ComboBox<String> comboOfferFilter;
    @FXML private BarChart<String, Number> candidaturesChart;
    @FXML private Label lblTotalCandidats, lblNouvellesCandidatures, lblCandidaturesAcceptees, lblCandidaturesRejetees;

    private final StatistiquesCandidatures statsGenerator = new StatistiquesCandidatures();
    private final OffreService offreService = new OffreService();
    private final CandidatureService candidatureService = new CandidatureService();
    private final ObservableList<Offre> offresList = FXCollections.observableArrayList();
    private final ObservableList<Candidature> candidaturesList = FXCollections.observableArrayList();
    private ObservableList<Candidature> allCandidatures;
    @FXML private Label lblAcceptanceRate, lblRejectionRate;

    // Animations
    private final DoubleProperty totalProgress = new SimpleDoubleProperty(0);
    private final DoubleProperty newProgress = new SimpleDoubleProperty(0);
    private final DoubleProperty acceptedProgress = new SimpleDoubleProperty(0);
    private final DoubleProperty rejectedProgress = new SimpleDoubleProperty(0);
    private final IntegerProperty totalCandidats = new SimpleIntegerProperty(0);
    private final IntegerProperty nouvellesCandidatures = new SimpleIntegerProperty(0);
    private final IntegerProperty candidaturesAcceptees = new SimpleIntegerProperty(0);
    private final IntegerProperty candidaturesRejetees = new SimpleIntegerProperty(0);
    private final ObservableList<String> statuts = FXCollections.observableArrayList(
            "En attente", "Acceptée", "Refusée", "En cours d'évaluation"
    );

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur RH");
        if (candidaturesChart == null) {
            System.err.println("Erreur: candidaturesChart n'est pas initialisé - vérifiez le fx:id dans le FXML");
        } else {
            System.out.println("Graphique initialisé correctement");
        }

        // Initialisation du graphique
        setupChart();
        initOffreTable();
        initCandidatureTable();
        comboStatut.setItems(statuts);

        loadOffres();
        loadCandidatures();
        setupRechercheOffres();
        setupRechercheCandidatures();

        setupStats();
        updateQuickStats();

        comboTimeFilter.setItems(FXCollections.observableArrayList("Toutes", "7 derniers jours", "30 derniers jours", "Cette année"));
        comboOfferFilter.setItems(FXCollections.observableArrayList("Toutes les offres"));
        // Set up the Status column with custom cell factory
        colCandStatut.setCellFactory(new StatusCellFactory());
        colCandScore.setCellValueFactory(cellData -> {
            Integer score = cellData.getValue().getAnalysisScore();
            return new SimpleObjectProperty<>(score);
        });



        colCandScore.setCellFactory(column -> new TableCell<Candidature, Integer>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);

                if (empty || score == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(score.toString());

                    // Style conditionnel basé sur le score
                    if (score >= 80) {
                        setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    } else if (score >= 50) {
                        setStyle("-fx-background-color: #FFC107;");
                    } else {
                        setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                    }
                }
            }
        });

        TableColumn<Candidature, Integer> colCandScore = new TableColumn<>("Score CV");
        colCandScore.setCellValueFactory(cell -> {
            Candidature cand = cell.getValue();
            if (cand.getAnalysisScore() != null) {
                return new SimpleIntegerProperty(cand.getAnalysisScore()).asObject();
            }
            Platform.runLater(() -> tableCandidatures.refresh());

            return null;

        });
        colCandScore.setCellFactory(column -> new TableCell<Candidature, Integer>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(score.toString());
                    // Colorer en fonction du score
                    if (score >= 80) setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    else if (score >= 50) setStyle("-fx-background-color: #FFC107;");
                    else setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                }
            }
        });
        tableCandidatures.getColumns().add(colCandScore);

    }
    // Classe pour fabriquer les cellules de statut
    private static class StatusCellFactory implements Callback<TableColumn<Candidature, String>, TableCell<Candidature, String>> {
        @Override
        public TableCell<Candidature, String> call(TableColumn<Candidature, String> param) {
            return new TableCell<Candidature, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        setText(item);
                        switch (item) {
                            case "En attente":
                                setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                                break;
                            case "Acceptée":
                                setStyle("-fx-background-color: green; -fx-text-fill: white;");
                                break;
                            case "Refusée":
                                setStyle("-fx-background-color: red; -fx-text-fill: white;");
                                break;
                            case "En cours d'évaluation":
                                setStyle("-fx-background-color: blue; -fx-text-fill: white;");
                                break;
                            default:
                                setStyle("");
                        }
                    } else {
                        setText(null);
                        setStyle("");
                    }
                }
            };
        }

}


    // Initialize Offre table
    private void initOffreTable() {
        colOffreId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOffreTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colOffreExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
    }

    // Initialize Candidature table
    private void initCandidatureTable() {
        colCandId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCandNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCandPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCandEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCandDate.setCellValueFactory(new PropertyValueFactory<>("dateCandidature"));
        colCandStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    // Load Offers from the database
    public void loadOffres() {
        offresList.setAll(offreService.getAllOffres());
        tableOffres.setItems(offresList);
    }
    private void loadCandidatures() {
        candidaturesList.setAll(candidatureService.getAllCandidatures());
        tableCandidatures.setItems(candidaturesList);
    }

    private void setupRechercheOffres() {
        FilteredList<Offre> filteredList = new FilteredList<>(offresList, p -> true);
        txtSearchOffres.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(offre -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return offre.getTitre().toLowerCase().contains(newVal.toLowerCase());
            });
        });
        SortedList<Offre> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableOffres.comparatorProperty());
        tableOffres.setItems(sortedList);
    }

    private void setupRechercheCandidatures() {
        FilteredList<Candidature> filteredList = new FilteredList<>(candidaturesList, p -> true);
        txtSearchCandidatures.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(cand -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }

                String lower = newVal.toLowerCase();

                // Vérification null-safe pour chaque champ
                boolean matches = (cand.getNom() != null && cand.getNom().toLowerCase().contains(lower)) ||
                        (cand.getPrenom() != null && cand.getPrenom().toLowerCase().contains(lower)) ||
                        (cand.getEmail() != null && cand.getEmail().toLowerCase().contains(lower)) ||
                        (cand.getTitreOffre() != null && cand.getTitreOffre().toLowerCase().contains(lower));

                return matches;
            });
        });

        SortedList<Candidature> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableCandidatures.comparatorProperty());
        tableCandidatures.setItems(sortedList);
    }
    private void filterCandidatures(String searchText) {
        ObservableList<Candidature> filtered = FXCollections.observableArrayList();
        for (Candidature cand : allCandidatures) {
            if (cand.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                    cand.getPrenom().toLowerCase().contains(searchText.toLowerCase()) ||
                    cand.getEmail().toLowerCase().contains(searchText.toLowerCase())) {
                filtered.add(cand);
            }
        }
        tableCandidatures.setItems(filtered);
    }

    @FXML
    private void handleSearchCandidatures() {
        filterCandidatures(txtSearchCandidatures.getText());
    }

    @FXML
    private void handleAddOffre() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/takoua_views/offre_form.fxml"));
        Parent root = loader.load();
        loader.<OffreFormController>getController().setRHController(this);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Ajouter une offre");
        stage.show();
    }

    @FXML
    private void handleEditOffre() throws IOException {
        Offre selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/takoua_views/offre_form.fxml"));
            Parent root = loader.load();
            OffreFormController controller = loader.getController();
            controller.setRHController(this);
            controller.setOffreForEdit(selected);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'offre");
            stage.show();
        } else {
            showAlert("Sélection requise", "Veuillez sélectionner une offre à modifier.");
        }
    }

    @FXML
    private void handleDeleteOffre() {
        Offre selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette offre ?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Confirmation");
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.YES) {
                    offreService.deleteOffre(selected.getId());
                    loadOffres();
                }
            });
        } else {
            showAlert("Sélection requise", "Veuillez sélectionner une offre à supprimer.");
        }
    }

    @FXML
    private void handleUpdateStatut() {
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        String newStatut = comboStatut.getValue();
        if (selected != null && newStatut != null) {
            candidatureService.updateStatut(selected.getId(), newStatut);
            loadCandidatures();
        } else {
            showAlert("Erreur", "Sélectionnez une candidature et un statut à modifier.");
        }
    }


    // Ajoutez cette méthode pour gérer le bouton d'analyse
    @FXML
    private void handleAnalyzeCV() {
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getCv() != null) {
            File cvFile = new File(selected.getCv());
            if (cvFile.exists()) {
                Alert loadingAlert = new Alert(AlertType.INFORMATION);
                loadingAlert.setTitle("Analyse en cours");
                loadingAlert.setHeaderText("Analyse du CV en cours...");
                loadingAlert.show();

                // Create a final copy of the selected candidature for use in the thread
                final Candidature finalSelected = selected;

                new Thread(() -> {
                    try {
                        CVAnalyzer analyzer = new CVAnalyzer();
                        Map<String, Object> analysis = analyzer.analyzeCV(cvFile);

                        // Handle potential null score
                        Integer score = (Integer) analysis.get("score");
                        if (score == null) {
                            score = 0; // Default value if analysis fails
                        }

                        Integer finalScore = score;
                        Platform.runLater(() -> {
                            loadingAlert.close();
                            finalSelected.setAnalysisScore(finalScore);
                            tableCandidatures.refresh();
                            showCVAnalysis(finalSelected, analysis);
                        });

                        // Update in database with null check
                        if (finalSelected.getAnalysisScore() != null) {
                            candidatureService.updateCandidature(finalSelected);
                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            loadingAlert.close();
                            showAlert("Erreur", "Erreur lors de l'analyse: " + e.getMessage());
                        });
                    }
                }).start();
            } else {
                showAlert("Erreur", "Fichier CV introuvable.");
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner une candidature avec un CV.");
        }
    }
    // Modifiez cette méthode pour gérer correctement les types
    private void showCVAnalysis(Candidature candidature, Map<String, Object> analysis) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Analyse du CV - " + candidature.getNom() + " " + candidature.getPrenom());
        alert.setHeaderText("Résultats de l'analyse automatique du CV");

        // Construction du message détaillé avec vérification des types
        StringBuilder content = new StringBuilder();
        content.append("Score global: ").append(analysis.getOrDefault("score", 0)).append("/100\n\n");

        content.append("Sections détectées:\n");
        content.append("- Formation: ").append(Boolean.TRUE.equals(analysis.get("has_education")) ? "Oui" : "Non").append("\n");
        content.append("- Expérience: ").append(Boolean.TRUE.equals(analysis.get("has_experience")) ? "Oui" : "Non").append("\n");
        content.append("- Compétences: ").append(Boolean.TRUE.equals(analysis.get("has_skills")) ? "Oui" : "Non").append("\n\n");

        content.append("Détails:\n");
        content.append("- Mots-clés techniques trouvés: ").append(analysis.getOrDefault("technical_keywords", 0)).append("\n");
        content.append("- Années d'expérience estimées: ").append(analysis.getOrDefault("experience_years", 0)).append("\n");
        content.append("- Diplômes détectés: ").append(analysis.getOrDefault("degrees", "Aucun")).append("\n\n");

        if (analysis.containsKey("ocr_text")) {
            content.append("Note: Ce CV a été analysé par OCR (document scanné)\n");
        }

        alert.setContentText(content.toString());

        // Ajouter un bouton pour voir le CV
        ButtonType viewCVButton = new ButtonType("Voir le CV");
        alert.getButtonTypes().add(viewCVButton);

        // Gérer la réponse
        alert.showAndWait().ifPresent(response -> {
            if (response == viewCVButton) {
                PDFPreview preview = new PDFPreview();
                preview.showPDFPreview(new File(candidature.getCv()));
            }
        });
    }
    @FXML
    private void handleDownloadCV() {
        System.out.println("Tentative d'ouverture du fichier: " );
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getCv() != null) {
            File cvFile = new File(selected.getCv());
            if (cvFile.exists()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName("CV_" + selected.getNom() + "_" + selected.getPrenom() + ".pdf");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
                File destination = fileChooser.showSaveDialog(null);
                if (destination != null) {
                    try (FileInputStream fis = new FileInputStream(cvFile);
                         FileOutputStream fos = new FileOutputStream(destination)) {
                        fis.transferTo(fos);

                        // Afficher l'aperçu automatiquement après l'enregistrement
                        PDFPreview preview = new PDFPreview();
                        preview.showPDFPreview(destination);

                        showAlert("Succès", "Le CV a été téléchargé avec succès.");
                    } catch (IOException e) {
                        showAlert("Erreur", "Erreur lors du téléchargement : " + e.getMessage());
                    }
                }
            } else {
                showAlert("Erreur", "Fichier CV introuvable.");
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner une candidature avec un CV.");
        }
    }
    @FXML
    private void handleRetour() throws IOException {
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/takoua_views/main_view.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("Accueil");
    }

    @FXML
    private void afficherStatistiquesOffresParCandidatures() {
        if (statistiquesPane == null) {
            System.err.println("Erreur: statistiquesPane n'est pas initialisé");
            return;
        }

        try {
            Map<String, Integer> stats = candidatureService.getNombreCandidaturesParOffre();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Candidatures par Offre");

            for (var entry : stats.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
            chart.setTitle("Statistiques des candidatures");
            chart.getData().add(series);

            statistiquesPane.getChildren().clear();
            statistiquesPane.getChildren().add(chart);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les statistiques");
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupChart() {
        if (candidaturesChart == null) {
            System.err.println("Erreur: candidaturesChart n'est pas initialisé");
            return;
        }

        // Utilisation du générateur de statistiques
        BarChart<String, Number> chart = statsGenerator.generateOffreStatsByCandidatures(offresList, candidaturesList);

        // Copie des données dans notre chart existant (pour conserver le style CSS)
        candidaturesChart.getData().clear();
        candidaturesChart.getData().addAll(chart.getData());

        // Ajouter des données de démonstration
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Jan", 45));
        series.getData().add(new XYChart.Data<>("Fév", 68));
        series.getData().add(new XYChart.Data<>("Mar", 72));
        series.getData().add(new XYChart.Data<>("Avr", 89));
        series.getData().add(new XYChart.Data<>("Mai", 56));
        series.getData().add(new XYChart.Data<>("Jun", 91));

        candidaturesChart.getData().clear();
        candidaturesChart.getData().add(series);

        // Style des barres
        for (XYChart.Data<String, Number> data : series.getData()) {
            // Create a final copy of the data for use in the lambda
            final XYChart.Data<String, Number> finalData = data;
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #700680;");
                }
            });
        }
    }

   private void setupStats() {
       // Example of stats setup
        updateStats(250, 45, 120, 85);
   }

    private void updateStats(int total, int nouvelles, int acceptees, int rejetees) {
       // Add stat display logic
   }
//    private void applyRowAnimations() {
//        for (Node row : tableCandidatures.lookupAll("TableRow")) {
//            TranslateTransition
//            slideIn = new TranslateTransition(Duration.millis(500), row);
//            slideIn.setFromX(-500); // Start from the left
//            slideIn.setToX(0); // End at normal position
//            slideIn.play();
//        }
//    }
    @FXML
    private void handleRefreshStats() {
        // Recharger les données
        loadOffres();
        loadCandidatures();

        // Mettre à jour le graphique
        setupChart();

        // Mettre à jour les stats rapides
        updateQuickStats();

        // Animation de rafraîchissement
        FadeTransition ft = new FadeTransition(Duration.millis(300), statistiquesPane);
        ft.setFromValue(0.5);
        ft.setToValue(1);
        ft.play();
    }


    private void applyAnimations() {
        // Fade in animation for statistiquesPane
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), statistiquesPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    @FXML
    private void handleApplyFilter() {
        String timeFilter = comboTimeFilter.getValue();
        String offerFilter = comboOfferFilter.getValue();

        // Apply the filter logic here
        FilteredList<Candidature> filteredList = new FilteredList<>(candidaturesList, p -> true);

        if (timeFilter != null && !timeFilter.isEmpty()) {
            // Apply time filter logic (e.g., 7 days, 30 days, etc.)
        }

        if (offerFilter != null && !offerFilter.isEmpty()) {
            // Apply offer filter logic
        }

        // Update the displayed candidatures
        SortedList<Candidature> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableCandidatures.comparatorProperty());
        tableCandidatures.setItems(sortedList);
    }
    @FXML
    private void handleShowStatisticsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/takoua_views/StatistiquesPage.fxml"));
            Parent root = loader.load();

            // On réutilise le même contrôleur pour conserver les données
            RHController controller = loader.getController();
            controller.setupChart();
            controller.updateQuickStats();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques des Candidatures");

            // Animation d'ouverture
            root.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setToValue(1);
            ft.play();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page des statistiques");
        }
    }

    @FXML
    private void handleReturnFromStats() {
        // Ferme la fenêtre des statistiques
        ((Stage) lblTotalCandidats.getScene().getWindow()).close();
    }

    private void updateQuickStats() {
        // Calcul des statistiques rapides
        int total = candidaturesList.size();
        int nouvelles = (int) candidaturesList.stream()
                .filter(c -> "En attente".equals(c.getStatut()))
                .count();
        int acceptees = (int) candidaturesList.stream()
                .filter(c -> "Acceptée".equals(c.getStatut()))
                .count();
        int rejetees = (int) candidaturesList.stream()
                .filter(c -> "Refusée".equals(c.getStatut()))
                .count();

        // Mise à jour des labels
        if (lblTotalCandidats != null) lblTotalCandidats.setText(String.valueOf(total));
        if (lblNouvellesCandidatures != null) lblNouvellesCandidatures.setText(String.valueOf(nouvelles));
        if (lblCandidaturesAcceptees != null) lblCandidaturesAcceptees.setText(String.valueOf(acceptees));
        if (lblCandidaturesRejetees != null) lblCandidaturesRejetees.setText(String.valueOf(rejetees));
    }




}
