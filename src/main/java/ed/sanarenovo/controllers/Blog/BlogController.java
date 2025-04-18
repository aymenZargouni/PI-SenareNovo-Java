package ed.sanarenovo.controllers.Blog;
import ed.sanarenovo.entities.Category;
import ed.sanarenovo.services.CategoryServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.services.BlogServices;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java .util.*;

public class BlogController {
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TableView<Blog> tableBlog;
    @FXML private TableColumn<Blog, Integer> colId;
    @FXML private TableColumn<Blog, String> colTitle;
    @FXML private TableColumn<Blog, String> colContent;
    @FXML private TableColumn<Blog, String> colImage;

    @FXML private TextField txtTitle;
    @FXML private TextField txtContent;
    @FXML private TextField txtImage;
    @FXML private TextField txtSearch;
    @FXML private Label lblPage;
    @FXML private ListView<Category> listCategories;

    private final CategoryServices categoryService = new CategoryServices();

    private static final int ITEMS_PER_PAGE = 5;
    private int currentPage = 1;
    private List<Blog> allBlogs;

    private BlogServices blogService = new BlogServices();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colTitle.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        colContent.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));
        colImage.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getImage()));
        listCategories.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listCategories.setItems(FXCollections.observableArrayList(categoryService.getCategorys()));

        allBlogs = blogService.getBlogs(); // on charge tout au début
        showPage(currentPage);
    }

    @FXML
    private void addBlog() {
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();
        String image = txtImage.getText().trim();

        if (title.isEmpty() || content.isEmpty() || image.isEmpty() || image.isEmpty()){
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
        List<Category> selectedCategories = listCategories.getSelectionModel().getSelectedItems();

        Blog blog = new Blog(title, content, image);
        blog.setCategories(new ArrayList<>(selectedCategories));
        blogService.addBlog(blog);
        refreshTable();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        txtTitle.clear();
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
        String keyword = txtSearch.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshTable();
        } else {
            ObservableList<Blog> searchResults = FXCollections.observableArrayList(blogService.searchByTitle(keyword));
            tableBlog.setItems(searchResults);
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
    private void openCommentPage() {
        Blog selectedBlog = tableBlog.getSelectionModel().getSelectedItem();
        if (selectedBlog != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/Comment.fxml"));
                Parent root = loader.load();

                CommentController commentController = loader.getController();
                commentController.setBlog(selectedBlog);

                Stage stage = new Stage();
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


}
