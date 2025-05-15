package ed.sanarenovo.controllers.Blog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ed.sanarenovo.entities.Category;
import ed.sanarenovo.services.CategoryServices;
import ed.sanarenovo.utils.UserSession;
import ed.sanarenovo.entities.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;

public class CategoryController {
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TableView<Category> tableCategory;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TextField txtName;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnBackToBlogs;
    @FXML
    private Button btnRetour;

    private final CategoryServices categoryService = new CategoryServices();

    @FXML
    public void initialize() {
        // Vérification de l'accès administrateur
        //checkAdminAccess();

        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        colName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        refreshTable();

        tableCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtName.setText(newSelection.getName());
            }
        });
        
        // Set full screen mode
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) tableCategory.getScene().getWindow();
            if (stage != null) {
                stage.setMaximized(true);
            }
        });
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
            Stage stage = (Stage) tableCategory.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        ObservableList<Category> categories = FXCollections.observableArrayList(
                categoryService.getCategorys()
        );
        tableCategory.setItems(categories);
    }

    @FXML
    private void addCategory() {
        // Vérification de l'accès administrateur
        if (!isAdmin()) {
            showAccessDeniedAndRedirect();
            return;
        }

        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champ requis", "Le nom doit être rempli.");
            return;
        }
        if (name.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Nom invalide", "Le nom doit contenir au moins 3 caractères.");
            return;
        }

        Category category = new Category(0, name);
        categoryService.addCategory(category);
        refreshTable();
        clearFields();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        txtName.clear();
    }

    @FXML
    private void updateCategory() {
        // Vérification de l'accès administrateur
        if (!isAdmin()) {
            showAccessDeniedAndRedirect();
            return;
        }

        Category selected = tableCategory.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setName(txtName.getText());
            categoryService.updateCategory(selected, selected.getId());
            refreshTable();
            clearFields();
        }
    }

    @FXML
    private void deleteCategory() {
        // Vérification de l'accès administrateur
        if (!isAdmin()) {
            showAccessDeniedAndRedirect();
            return;
        }

        Category selected = tableCategory.getSelectionModel().getSelectedItem();
        if (selected != null) {
            categoryService.deleteCategory(selected);
            refreshTable();
            clearFields();
        }
    }

    @FXML
    private void openBlogsPage() {
        try {
            // Chargement de la page des blogs
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/Blog.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableCategory.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des blogs : " + e.getMessage());
        }
    }

    private boolean isAdmin() {
        UserSession session = UserSession.getInstance();
        if (session == null || !session.isLoggedIn()) {
            return false;
        }
        User currentUser = session.getUser();
        return currentUser != null && currentUser.getRoles().contains("ROLE_ADMIN");
    }

    @FXML
    public void setFullScreen() {
        Stage stage = (Stage) tableCategory.getScene().getWindow();
        stage.setFullScreen(true);
    }

    @FXML
    private void retourBlog(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Blog/Blog.fxml"));
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
