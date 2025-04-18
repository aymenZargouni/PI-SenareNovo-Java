package ed.sanarenovo.controllers.Blog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.services.BlogServices;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java .util.*;

public class BlogClientController {
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

        allBlogs = blogService.getBlogs(); // on charge tout au début
        showPage(currentPage);
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
    private void goToClientView(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/BlogClient.fxml"));
            Parent root = loader.load();
            tableBlog.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.printf("Error : ", e.getMessage());
        }
    }


}
