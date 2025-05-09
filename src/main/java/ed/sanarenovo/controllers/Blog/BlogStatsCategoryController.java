package ed.sanarenovo.controllers.Blog;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.services.BlogServices;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BlogStatsCategoryController {
    @FXML
    private PieChart categoryPieChart;

    @FXML
    private VBox mainContainer;

    public void initialize(URL url, ResourceBundle rb) {
        mainContainer.getScene().getStylesheets().add(getClass().getResource("/css/CategoryStatStyles.css").toExternalForm());
    }

    private final BlogServices blogServices = new BlogServices(); // utiliser service

    @FXML
    public void initialize() {
        List<Blog> blogs = blogServices.getBlogs(); // Utilisation de getBlogs()

        for (Blog blog : blogs) {
            categoryPieChart.getData().add(
                    new PieChart.Data(blog.getTitle(), blog.getCategories().size()) // taille des catégories
            );
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/Blog.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

}