package ed.sanarenovo.controllers.Blog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;

import java.io.IOException;
import java.util.List;
import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.services.BlogServices;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class BlogStatsCategoryController {
    @FXML
    private PieChart categoryPieChart;

    @FXML
    private Button btnRetour;
    private final BlogServices blogServices = new BlogServices(); // ✅ utilise ton vrai service

    @FXML
    public void initialize() {
        List<Blog> blogs = blogServices.getBlogs(); // Utilisation de getBlogs()

        for (Blog blog : blogs) {
            categoryPieChart.getData().add(
                    new PieChart.Data(blog.getTitle(), blog.getCategories().size()) // ⚡ taille des catégories
            );
        }
    }

    @FXML
    private void RetourBlog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/BlogClient.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnRetour.getScene().getWindow(); // ✅ Correct
        stage.setScene(new Scene(root));
        stage.show();
    }


}