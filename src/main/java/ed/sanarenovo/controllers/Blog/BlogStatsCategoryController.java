package ed.sanarenovo.controllers.Blog;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import java.util.List;
import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.services.BlogServices;
public class BlogStatsCategoryController {
    @FXML
    private PieChart categoryPieChart;

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
}