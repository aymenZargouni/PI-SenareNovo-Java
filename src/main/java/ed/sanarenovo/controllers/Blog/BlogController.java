package ed.sanarenovo.controllers.Blog;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import ed.sanarenovo.entities.Category;
import ed.sanarenovo.services.CategoryServices;
import ed.sanarenovo.services.TranslatorService;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.services.BlogServices;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.utils.UserSession;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import ed.sanarenovo.services.CommentServices;
import ed.sanarenovo.services.UserService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java .util.*;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.io.FileOutputStream;
import com.lowagie.text.pdf.PdfWriter;
import javafx.fxml.Initializable;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class BlogController implements Initializable {
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TableView<Blog> tableBlog;
    @FXML private TableColumn<Blog, Integer> colId;
    @FXML private TableColumn<Blog, String> colTitle;
    @FXML private TableColumn<Blog, String> colContent;
    @FXML private TableColumn<Blog, String> colImage;
    @FXML private TableColumn<Blog, String> colCategory;

    @FXML private TextField txtTitle;
    @FXML private TextArea  txtContent;
    @FXML private TextField txtImage;
    @FXML private TextField txtSearch;
    @FXML private Label lblPage;
    @FXML private ListView<Category> listCategories;
    @FXML private Label blogTitle;
    @FXML private TextArea blogContent;

    // Filter components
    @FXML private ComboBox<Category> filterCategory;

    private final CategoryServices categoryService = new CategoryServices();

    private static final int ITEMS_PER_PAGE = 5;
    private int currentPage = 1;
    private List<Blog> allBlogs;

    private BlogServices blogService = new BlogServices();

    @FXML private Text captchaText;
    @FXML private TextField captchaInput;
    private String currentCaptcha;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérification de l'accès administrateur
        //checkAdminAccess();
        generateCaptcha();
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colTitle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        colContent.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContent()));
        colImage.setCellValueFactory(new PropertyValueFactory<>("image")); // récupère juste le chemin ou URL de l'image

        // Configuration de la colonne image pour afficher l'image elle-même
        colImage.setCellFactory(column -> new TableCell<Blog, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.trim().isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        // Corriger ici : ajouter file: devant si ce n'est pas déjà une URL http
                        Image image;
                        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                            image = new Image(imageUrl);
                        } else {
                            File file = new File(imageUrl);
                            if (file.exists()) {
                                image = new Image(file.toURI().toString()); // ← c'est CORRECT comme ça pour un fichier local
                            } else {
                                throw new Exception("Fichier image non trouvé : " + imageUrl);
                            }
                        }
                        imageView.setImage(image);
                        setGraphic(imageView);
                        setText(null);
                    } catch (Exception e) {
                        setGraphic(null);
                        setText("Image non disponible");
                    }
                }
            }
        });

        // Configuration de la colonne catégorie
        colCategory.setCellValueFactory(cellData -> {
            Blog blog = cellData.getValue();
            if (blog.getCategories() != null && !blog.getCategories().isEmpty()) {
                return new SimpleStringProperty(blog.getCategories().get(0).getName());
            }
            return new SimpleStringProperty("");
        });
        // Selectioner multiple dans la liste des catégories
        listCategories.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listCategories.setItems(FXCollections.observableArrayList(categoryService.getCategorys()));         // Remplissage La Liste
        filterCategory.setItems(FXCollections.observableArrayList(categoryService.getCategorys())); // remplissae la comboBox

        // selectionne de blog et remplir avec les champs
        tableBlog.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtTitle.setText(newSelection.getTitle());
                txtContent.setText(newSelection.getContent());
                txtImage.setText(newSelection.getImage());

                // Remplir les catégories si tu veux aussi les charger
                listCategories.getSelectionModel().clearSelection();
                if (newSelection.getCategories() != null) {
                    for (Category category : newSelection.getCategories()) {
                        listCategories.getSelectionModel().select(category);
                    }
                }
            }
        });
        // filtrer selon le texte tapé
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

    // Ajoutez cette méthode pour générer le CAPTCHA
    private void generateCaptcha() {
        currentCaptcha = CaptchaGenerator.generateCaptcha(6);
        captchaText.setText(currentCaptcha);
        captchaInput.clear();
    }

    private void checkAdminAccess() {
        // Récupération de la session utilisateur
        UserSession session = UserSession.getInstance();

        // Vérification si l'utilisateur est connecté
        if (session == null || !session.isLoggedIn()) {
            showAccessDeniedAndRedirect();
            return;
        }

        // Récupération de l'utilisateur connecté
        User currentUser = session.getUser();

        // Vérification si l'utilisateur est administrateur
        if (currentUser == null || !currentUser.getRoles().contains("ROLE_ADMIN")) {
            showAccessDeniedAndRedirect();
        }
    }

    private void showAccessDeniedAndRedirect() {
        // Affichage d'une alerte d'accès refusé
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Accès refusé. Cette page est réservée aux administrateurs.",
                ButtonType.OK);
        alert.showAndWait();

        try {
            // Redirection vers la page d'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/BlogClient.fxml"));
            Parent root = loader.load();
            // Récupération de la scène actuelle à partir du tableau
            Stage stage = (Stage) tableBlog.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addBlog() {
        // Vérification du CAPTCHA d'abord
        if (!captchaInput.getText().equals(currentCaptcha)) {
            showAlert(Alert.AlertType.ERROR, "Erreur CAPTCHA", "Le CAPTCHA saisi est incorrect !");
            generateCaptcha();
            return;
        }
        // Récupère le texte des champs et supprime les espaces inutiles
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();
        String image = txtImage.getText().trim();
        List<Category> selectedCategories = new ArrayList<>(listCategories.getSelectionModel().getSelectedItems());

        if (title.isEmpty() || content.isEmpty() || image.isEmpty()){
            showAlert(Alert.AlertType.ERROR, "Champs requis", "Tous les champs doivent être remplis.");
            return;
        }

        if (title.length() < 3 || content.length() < 3 || image.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Champs invalide", "Les Champs doit contenir au moins 3 caractères.");
            return;
        }

        /*if (!image.matches("^(http|https)?://.*\\.(jpg|jpeg|png|gif|bmp)$")) {
            showAlert(Alert.AlertType.WARNING, "URL de l'image invalide", "Veuillez entrer une URL valide d'image (jpg, png, etc).");
            return;
        }*/

        Blog blog = new Blog(title, content, image, selectedCategories);
        blog.setCategories(new ArrayList<>(selectedCategories)); // definit les categories
        blogService.addBlog(blog);
        refreshTable();
        clearFields();
        generateCaptcha();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); // Crée une nouvelle alerte de type donné (erreur, information, etc.)
        alert.setTitle(title); // definit le titre
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        txtTitle.clear(); // vider le champ titre
        txtContent.clear();
        txtImage.clear();
        listCategories.getSelectionModel().clearSelection();

    }

    @FXML
    private void updateBlog() {
        Blog selected = tableBlog.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setTitle(txtTitle.getText());
            selected.setContent(txtContent.getText());
            selected.setImage(txtImage.getText());
            List<Category> selectedCategories = listCategories.getSelectionModel().getSelectedItems();
            selected.setCategories(new ArrayList<>(selectedCategories));
            blogService.updateBlog(selected, selected.getId());
            refreshTable();
            clearFields();

        }
    }

    @FXML
    private void deleteBlog() {
        Blog selected = tableBlog.getSelectionModel().getSelectedItem();
        if (selected != null) {
            blogService.deleteBlog(selected);

            refreshTable();
        }
    }

    @FXML
    private void searchBlog() {
        String keyword = txtSearch.getText(); // recupere le texte entre dans le champ de recherche
        if (keyword == null || keyword.trim().isEmpty()) { // Si le champ est vide, recharge tous les blogs
            refreshTable();
        } else {
            ObservableList<Blog> searchResults = FXCollections.observableArrayList(blogService.searchByTitle(keyword)); // effectue une recherche par titre
            tableBlog.setItems(searchResults);
        }
    }

    private void showPage(int page) {
        int fromIndex = (page - 1) * ITEMS_PER_PAGE; // //calcule l'index de début et de fin des elements pour cette page
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allBlogs.size());

        if (fromIndex <= toIndex) { // verifier que l'intervale est valide
            List<Blog> subList = allBlogs.subList(fromIndex, toIndex); // Récupère la sous-liste de blogs
            ObservableList<Blog> blogs = FXCollections.observableArrayList(subList);
            tableBlog.setItems(blogs);
            lblPage.setText("Page " + page);
        }
    }

    private void refreshTable() {
        allBlogs = blogService.getBlogs(); // depuis le service recharger les blogs
        currentPage = 1; //Réinitialise
        showPage(currentPage);
    }

    @FXML
    private void nextPage() {
        int maxPage = (int) Math.ceil((double) allBlogs.size() / ITEMS_PER_PAGE); // calcule nrb total de pages
        if (currentPage < maxPage) { // ne pas a la derniere page
            currentPage++;
            showPage(currentPage);
        }
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) { // a la premiere page
            currentPage--;
            showPage(currentPage);
        }
    }

    @FXML
    private void openCommentPage() {
        Blog selectedBlog = tableBlog.getSelectionModel().getSelectedItem();// Récupère le blog sélectionné dans le tableau
        if (selectedBlog != null) { // bien selectioné
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/Comment.fxml")); // charge l'interface commentaire
                Parent root = loader.load();
                CommentController commentController = loader.getController();
                commentController.setBlog(selectedBlog);
                Stage stage = new Stage();// nv fenetre
                stage.setTitle("Commentaires du blog : " + selectedBlog.getTitle());
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucun blog sélectionné", "Veuillez d'abord sélectionner un blog.");
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser(); // ouverture : pour choisir un image
        fileChooser.setTitle("Choisir une image");
        // choisir uniquemment le fichier de type ...
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers d'image", ".png", ".jpg", ".jpeg", ".gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(null); // Afficher le fichier et recupere le fichier selectionné

        if (selectedFile != null) {
            txtImage.setText(selectedFile.getAbsolutePath()); //affichier le chemin absolu du fich dans le champ txtImg
        }

    }

    @FXML
    protected void handleTranslate() {
        // Récupérer l'élément sélectionné dans le tableau
        Blog selectedBlog = tableBlog.getSelectionModel().getSelectedItem();

        if (selectedBlog == null) {
            System.out.println("Aucune ligne sélectionnée !");
            return;
        }
        // Récupère le titre et le contenu
        String originalTitle = selectedBlog.getTitle();
        String originalContent = selectedBlog.getContent();

        // Appelle la classe TranslatorService
        String translatedTitle = TranslatorService.translate(originalTitle, "en");
        String translatedContent = TranslatorService.translate(originalContent, "en");

        // Affiche la traduction dans les composants
        blogTitle.setText(translatedTitle);
        blogContent.setText(translatedContent);
    }

    @FXML
    protected void handleDownloadPdf() {
        Blog selectedBlog = tableBlog.getSelectionModel().getSelectedItem(); // recuperé blog selectionné
        if (selectedBlog == null) {
            System.out.println("Aucun blog sélectionné.");
            return;
        }
        try {
            Document document = new Document();
            // Définit le chemin du fichier PDF dans le dossier "Downloads" de l'utilisateur
            String userHome = System.getProperty("user.home");
            String downloadDir = userHome + File.separator + "Downloads";
            String fileName = downloadDir + File.separator + "Blog_" + selectedBlog.getId() + ".pdf";

            // Initialise l'écriture du fichier PDF
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            document.add(new Paragraph("Titre : " + selectedBlog.getTitle(), titleFont));
            document.add(new Paragraph(" ")); // espace
            document.add(new Paragraph("\nContenu :\n" + selectedBlog.getContent()));
            String imagePath = selectedBlog.getImage(); // ajout l'image si exist
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(imageFile.getAbsolutePath());
                    img.scaleToFit(300, 300);
                    document.add(img);
                } else {
                    System.out.println("Image introuvable : " + imageFile.getAbsolutePath());
                }
            }

            document.close();
            System.out.println("PDF généré avec succès : " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void applyFilters() {
        Category categoryFilter = filterCategory.getValue(); // recupere la categorie selectionné
        List<Blog> filteredBlogs = new ArrayList<>(blogService.getBlogs()); // Get all blogs

        // Filtre les blogs selon la catégorie si une catégorie est sélectionnée
        if (categoryFilter != null) {
            filteredBlogs = filteredBlogs.stream()
                    .filter(blog -> blog.getCategories() != null &&
                            blog.getCategories().stream()
                                    .anyMatch(cat -> cat.getId() == categoryFilter.getId()))
                    .collect(Collectors.toList());
        }
        // maj l'affichage
        allBlogs = filteredBlogs;
        currentPage = 1;
        showPage(currentPage);
    }

    @FXML
    private void resetFilters() {
        filterCategory.setValue(null); // Réinitialise le filtre de catégorie
        refreshTable();
    }

    @FXML
    public void setFullScreen() {
        Stage stage = (Stage) tableBlog.getScene().getWindow();
        stage.setFullScreen(true);
    }

    @FXML
    private void openCategoriesPage() {
        try {
            // Chargement de la page des catégories
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/Category.fxml"));
            Parent root = loader.load();

            // Récupération de la scène actuelle
            Stage stage = (Stage) tableBlog.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des catégories : " + e.getMessage());
        }
    }

    @FXML
    private void handleListen() {
        Blog selectedBlog = tableBlog.getSelectionModel().getSelectedItem();
        if (selectedBlog != null) {
            String textToRead = selectedBlog.getContent();

            // Utiliser FreeTTS pour lire le texte
            System.setProperty("freetts.voices",
                    "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            Voice voice;
            VoiceManager vm = VoiceManager.getInstance();
            voice = vm.getVoice("kevin16");

            if (voice != null) {
                voice.allocate();
                voice.speak(textToRead);
                voice.deallocate();
            } else {
                System.out.println("La voix n'a pas été trouvée !");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un blog à écouter.");
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


}