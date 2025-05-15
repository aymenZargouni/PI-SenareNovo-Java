package ed.sanarenovo.controllers.Blog;

import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.entities.Category;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.utils.UserSession;
import ed.sanarenovo.services.TranslatorService;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import ed.sanarenovo.services.BlogServices;
import ed.sanarenovo.services.CategoryServices;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableCell;
import javafx.stage.FileChooser;
import javafx.scene.control.ListCell;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Element;

import java.awt.Desktop;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class BlogClientController {
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TableView<Blog> tableBlog;
    //@FXML private TableColumn<Blog, Integer> colId;
    @FXML private TableColumn<Blog, String> colTitle;
    @FXML private TableColumn<Blog, String> colContent;
    @FXML private TableColumn<Blog, String> colImage;
    @FXML private TableColumn<Blog, String> colCategory;
    @FXML private ComboBox<Category> cmbCategory;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnViewComments;
    @FXML private Button btnTranslate;
    @FXML private Button btnDownloadPDF;
    @FXML private Label blogTitle;
    @FXML private TextArea blogContent;

    @FXML private TextField txtTitle;
    @FXML private TextField txtContent;
    //@FXML private TextField txtImage;
    @FXML private TextField txtSearch;
    @FXML private Label lblPage;

    private static final int ITEMS_PER_PAGE = 5;
    private int currentPage = 1;
    private List<Blog> allBlogs;

    private BlogServices blogService = new BlogServices();
    private CategoryServices categoryService = new CategoryServices();
    private User currentUser;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        colContent.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));
        /*colImage.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImage()));*/
        
        // Configuration de la colonne image pour afficher l'image elle-même
        /*colImage.setCellFactory(column -> new TableCell<Blog, String>() {
            private final ImageView imageView = new ImageView();
            
            {
                imageView.setFitWidth(150);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
                setStyle("-fx-alignment: CENTER;");
            }
            
            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.trim().isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        Image image;
                        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                            image = new Image(imageUrl, true);
                        } else {
                            try {
                                // Essayer d'abord comme chemin absolu
                                File file = new File(imageUrl);
                                if (file.exists()) {
                                    image = new Image(file.toURI().toString());
                                } else {
                                    // Essayer comme chemin relatif dans le dossier resources
                                    String resourcePath = "/images/" + imageUrl;
                                    image = new Image(getClass().getResourceAsStream(resourcePath));
                                }
                            } catch (Exception e) {
                                // Si tout échoue, essayer une dernière fois avec le chemin direct
                                image = new Image("file:" + imageUrl);
                            }
                        }
                        
                        if (image != null && !image.isError()) {
                            imageView.setImage(image);
                            setGraphic(imageView);
                            setText(null);
                        } else {
                            setGraphic(null);
                            setText("Image non disponible");
                        }
                    } catch (Exception e) {
                        setGraphic(null);
                        setText("Image non disponible");
                        System.err.println("Erreur de chargement de l'image: " + imageUrl);
                        e.printStackTrace();
                    }
                }
            }
        });*/

        // Affichage les catégories
        colCategory.setCellValueFactory(cellData -> {
            Blog blog = cellData.getValue();
            if (blog.getCategories() != null && !blog.getCategories().isEmpty()) {
                return new SimpleStringProperty(blog.getCategories().get(0).getName());
            }
            return new SimpleStringProperty("");
        });

        List<Category> categories = categoryService.getCategorys();
        cmbCategory.setItems(FXCollections.observableArrayList(categories));
        
        // Personnaliser l'affichage des catégories dans le ComboBox
        cmbCategory.setCellFactory(lv -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });
        
        // Personnaliser l'affichage du bouton du ComboBox
        cmbCategory.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item == null ? "" : item.getName());
            }
        });
        
        cmbCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterBlogsByCategory(newVal);
            } else {
                refreshTable();
            }
        });

        UserSession session = UserSession.getInstance();
        if (session != null && session.isLoggedIn()) {
            currentUser = session.getUser();
            updateButtonVisibility();
        }

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                refreshTable();
            } else {
                ObservableList<Blog> searchResults = FXCollections.observableArrayList(
                        blogService.searchByTitle(newValue)
                );
                tableBlog.setItems(searchResults);
            }
        });

        allBlogs = blogService.getBlogs(); // on charge tout au début
        showPage(currentPage);
    }

    private void updateButtonVisibility() {
        if (currentUser == null) {
            btnAdd.setVisible(false);
            btnUpdate.setVisible(false);
            btnDelete.setVisible(false);
            return;
        }

        // Vérifier si l'utilisateur est admin
        boolean isAdmin = false;
        if (currentUser.getRoles() != null) {
            String roles = currentUser.getRoles();
            if (roles.contains("ROLE_ADMIN")) {
                isAdmin = true;
            }
        }

        /*if (isAdmin) {
            btnAdd.setVisible(true);
            btnUpdate.setVisible(true);
            btnDelete.setVisible(true);
        } else {
            btnAdd.setVisible(false);
            btnUpdate.setVisible(false);
            btnDelete.setVisible(false);
        }*/
    }

    private void filterBlogsByCategory(Category category) {
        if (category != null) {
            // Filtrer les blogs par catégorie
            List<Blog> filteredBlogs = new ArrayList<>();
            for (Blog blog : allBlogs) {
                if (blog.getCategories() != null) {
                    for (Category cat : blog.getCategories()) {
                        if (cat.getId() == category.getId()) {
                            filteredBlogs.add(blog);
                            break;
                        }
                    }
                }
            }
            ObservableList<Blog> blogs = FXCollections.observableArrayList(filteredBlogs);
            tableBlog.setItems(blogs);
        }
    }

    private void showPage(int page) {
        int fromIndex = (page - 1) * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allBlogs.size());

        if (fromIndex <= toIndex) {
            List<Blog> subList = allBlogs.subList(fromIndex, toIndex);
            ObservableList<Blog> blogs = FXCollections.observableArrayList(subList);
            tableBlog.setItems(blogs);
            lblPage.setText("Page " + page);
        }
    }

    private void refreshTable() {
        allBlogs = blogService.getBlogs();
        currentPage = 1;
        showPage(currentPage);
    }

    @FXML
    private void openCommentPage() {
        Blog selectedBlog = tableBlog.getSelectionModel().getSelectedItem();
        if (selectedBlog != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/CommentClient.fxml"));
                Parent root = loader.load();

                CommentClientController controller = loader.getController();
                controller.setBlog(selectedBlog); // passe le blog au controller

                Stage stage = new Stage();
                stage.setTitle("Ajouter un commentaire");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre des commentaires: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucun blog sélectionné", "Veuillez sélectionner un blog pour voir ses commentaires.");
        }
    }

    @FXML
    private void searchBlog() {
        String keyword = txtSearch.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshTable();
        } else {
            ObservableList<Blog> searchResults = FXCollections.observableArrayList(blogService.searchByTitle(keyword));
            tableBlog.setItems(searchResults);
        }
    }

    @FXML
    private void nextPage() {
        int maxPage = (int) Math.ceil((double) allBlogs.size() / ITEMS_PER_PAGE);
        if (currentPage < maxPage) {
            currentPage++;
            showPage(currentPage);
        }
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            showPage(currentPage);
        }
    }

    @FXML
    private void goToClientView(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/BlogClient.fxml"));
            Parent root = loader.load();
            tableBlog.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.printf("Error : ", e.getMessage());
        }
    }

    @FXML
    private void viewComments() {
        Blog selected = tableBlog.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/CommentClient.fxml"));
                Parent root = loader.load();
                CommentClientController controller = loader.getController();
                controller.setBlog(selected);

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setTitle("Commentaires - " + selected.getTitle());
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre des commentaires: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucun blog sélectionné", "Veuillez sélectionner un blog pour voir ses commentaires.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void setFullScreen() {
        // Implementation of setFullScreen method
    }

    @FXML
    private void translateBlog() {
        Blog selectedBlog = tableBlog.getSelectionModel().getSelectedItem();
        if (selectedBlog != null) {
            // Récupère le titre et le contenu
            String originalTitle = selectedBlog.getTitle();
            String originalContent = selectedBlog.getContent();

            // Appelle la classe TranslatorService
            String translatedTitle = TranslatorService.translate(originalTitle, "en");
            String translatedContent = TranslatorService.translate(originalContent, "en");

            // Affiche la traduction dans les composants
            blogTitle.setText(translatedTitle);
            blogContent.setText(translatedContent);
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un blog à traduire.");
        }
    }
    
    @FXML
    private void downloadPDF() {
        Blog selectedBlog = tableBlog.getSelectionModel().getSelectedItem();
        if (selectedBlog != null) {
            try {
                // Créer le chemin du dossier Downloads
                String downloadsPath = System.getProperty("user.home") + "/Downloads/";
                String fileName = selectedBlog.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
                File file = new File(downloadsPath + fileName);
                
                // Créer le document PDF
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                // Ajouter le titre
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph(selectedBlog.getTitle(), titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);
                
                // Ajouter la date
                Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                Paragraph date = new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), dateFont);
                date.setAlignment(Element.ALIGN_RIGHT);
                date.setSpacingAfter(20);
                document.add(date);
                
                // Ajouter l'image si elle existe
                if (selectedBlog.getImage() != null && !selectedBlog.getImage().isEmpty()) {
                    try {
                        com.itextpdf.text.Image image = null;
                        if (selectedBlog.getImage().startsWith("http://") || selectedBlog.getImage().startsWith("https://")) {
                            // Télécharger l'image depuis l'URL
                            URL imageUrl = new URL(selectedBlog.getImage());
                            image = com.itextpdf.text.Image.getInstance(imageUrl);
                        } else {
                            // Charger l'image depuis le système de fichiers
                            image = com.itextpdf.text.Image.getInstance(selectedBlog.getImage());
                        }
                        
                        if (image != null) {
                            // Redimensionner l'image pour qu'elle s'adapte à la page
                            float documentWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
                            image.scaleToFit(documentWidth, documentWidth * 0.75f);
                            image.setAlignment(Element.ALIGN_CENTER);
                            document.add(image);
                            document.add(new Paragraph("\n"));
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                    }
                }
                
                // Ajouter la catégorie
                if (selectedBlog.getCategories() != null && !selectedBlog.getCategories().isEmpty()) {
                    Font categoryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                    Paragraph category = new Paragraph("Catégorie: " + selectedBlog.getCategories().get(0).getName(), categoryFont);
                    category.setSpacingAfter(20);
                    document.add(category);
                }
                
                // Ajouter le contenu
                Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                Paragraph content = new Paragraph(selectedBlog.getContent(), contentFont);
                content.setSpacingAfter(20);
                document.add(content);
                
                // Fermer le document
                document.close();
                
                // Ouvrir le fichier PDF automatiquement
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                         "Le PDF a été généré avec succès et sauvegardé dans votre dossier Downloads.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Une erreur est survenue lors de la génération du PDF: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un blog à télécharger en PDF.");
        }
    }

    @FXML
    private void handleListen() {
        Blog selectedBlog = tableBlog.getSelectionModel().getSelectedItem();
        if (selectedBlog == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un blog à écouter.");
            return;
        }

        try {
            // Configuration critique
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            System.setProperty("freetts.voicespath", System.getProperty("user.dir") + "/lib");

            VoiceManager vm = VoiceManager.getInstance();
            Voice voice = vm.getVoice("kevin"); // Essayez différentes voix

            if (voice == null) {
                voice = vm.getVoice("cmu_us_kal16");
                if (voice == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune voix disponible.");
                    return;
                }
            }

            // Configuration de la voix
            voice.setRate(120f); // Vitesse normale
            voice.setPitch(100f); // Hauteur normale
            voice.setVolume(0.9f); // Volume légèrement réduit

            // Chargement manuel du lexique si nécessaire
            try {
                com.sun.speech.freetts.en.us.CMULexicon lexicon = new com.sun.speech.freetts.en.us.CMULexicon();
                voice.setLexicon(lexicon);
            } catch (Exception e) {
                System.err.println("Warning: Couldn't load custom lexicon");
            }

            voice.allocate();
            voice.speak(selectedBlog.getContent());
            voice.deallocate();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur TTS", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openStatCategory() {
        try {
            // Chargement de la page des catégories
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/BlogStatsCategory.fxml"));
            Parent root = loader.load();

            // Récupération de la scène actuelle
            Stage stage = (Stage) tableBlog.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des Statistiques catégories : " + e.getMessage());
        }
    }

    @FXML
    private void OpenChatSante() {
        try {
            // Chargement de la page des catégories
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/ChatbootSante.fxml"));
            Parent root = loader.load();

            // Récupération de la scène actuelle
            Stage stage = (Stage) tableBlog.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des Chat Sante : " + e.getMessage());
        }
    }

    @FXML
    private void OpenChatBootAI() {
        try {
            // Chargement de la page des catégories
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/chatbot_view.fxml"));
            Parent root = loader.load();

            // Récupération de la scène actuelle
            Stage stage = (Stage) tableBlog.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des Chat Boot AI : " + e.getMessage());
        }
    }

    @FXML
    private void OpenDashCovid() {
        try {
            // Chargement de la page des catégories
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/dashboard.fxml"));
            Parent root = loader.load();

            // Récupération de la scène actuelle
            Stage stage = (Stage) tableBlog.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des Dashboard Covid19 : " + e.getMessage());
        }
    }

    @FXML
    private void btnRetour() {
        try {
            // Chargement de la page des catégories
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AymenViews/ShowMedecin.fxml"));
            Parent root = loader.load();

            // Récupération de la scène actuelle
            Stage stage = (Stage) tableBlog.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la Show Medecin : " + e.getMessage());
        }
    }



}
