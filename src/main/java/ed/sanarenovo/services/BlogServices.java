package ed.sanarenovo.services;

import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.entities.Comment;
import ed.sanarenovo.interfaces.IBlog;
import ed.sanarenovo.utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ed.sanarenovo.entities.Category;
public class BlogServices implements IBlog<Blog> {


    private void addBlogCategories(int blogId, List<Category> categories) throws SQLException {
        String sql = "INSERT INTO blog_category (blog_id, category_id) VALUES (?, ?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql); //peut ajouter des variables et c’est plus sécurisé.
        for (Category category : categories) {
            pst.setInt(1, blogId);
            pst.setInt(2, category.getId());
            pst.addBatch();
        }
        // Exécute toutes les insertions en une fois
        pst.executeBatch(); // améliore les performances en envoyant plusieurs requêtes d'un coup.
    }
    @Override
    public void addBlog(Blog blog) {
        try {
            String requete = "INSERT INTO blog (title, content, image) VALUES (?, ?, ?)";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, blog.getTitle());
            pst.setString(2, blog.getContent());
            pst.setString(3, blog.getImage());
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys(); //Récupère l'ID du blog généré par la base (clé primaire)
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                addBlogCategories(generatedId, blog.getCategories());
            }

            System.out.println("Blog ajouté avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du blog : " + e.getMessage());
        }
    }


    @Override
    public void deleteBlog(Blog blog) {
        try {
            String requete = "DELETE FROM blog WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, blog.getId());
            pst.executeUpdate();
            System.out.println("Blog supprimé avec succès.");
        } catch (SQLException e) {
            System.out.println("\n" + "Erreur lors de la suppression du blog :" + e.getMessage());
        }
    }

    @Override
    public void updateBlog(Blog blog, int id) {
        try {
            String requete = "UPDATE blog SET title = ?, content = ?, image = ? WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, blog.getTitle());
            pst.setString(2, blog.getContent());
            pst.setString(3, blog.getImage());
            pst.setInt(4, id);
            pst.executeUpdate();
            System.out.println("Blog mis à jour avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du blog : " + e.getMessage());
        }
    }

    @Override
    public List<Blog> getBlogs() {
        List<Blog> blogs = new ArrayList<>();
        String requete = "SELECT * FROM blog";

        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);

            while (rs.next()) {
                int blogId = rs.getInt("id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String image = rs.getString("image");

                // Appel pour récupérer les catégories liées à ce blog
                List<Category> categories = getCategoriesForBlog(blogId);

                Blog blog = new Blog(blogId, title, content, image, categories);
                blogs.add(blog);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des blogs : " + e.getMessage());
        }

        return blogs;
    }

    private List<Category> getCategoriesForBlog(int blogId) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT c.id, c.name FROM category c " +
                "JOIN blog_category bc ON c.id = bc.category_id " +
                "WHERE bc.blog_id = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, blogId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name")); // ou .setNom() selon ta classe
                categories.add(category);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des catégories : " + e.getMessage());
        }

        return categories;
    }

    @Override
    public Blog getBlogById(int id) {
        Blog b = null;
        try {
            String requete = "SELECT * FROM blog WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                b = new Blog(rs.getInt("id"), rs.getString("title"), rs.getString("content"), rs.getString("image"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return b;
    }

    public List<Blog> searchByTitle(String title) {
        List<Blog> results = new ArrayList<>(); // lst : stocker rslt
        try {
            String requete = "SELECT * FROM blog WHERE title LIKE ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, "%" + title + "%"); //remplir le parametre avec une recherche floue
            ResultSet rs = pst.executeQuery();

            while (rs.next()) { // parcours de rslt
                Blog b = new Blog(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("image")
                );
                results.add(b);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche : " + e.getMessage());
        }
        return results;
    }

    public List<Comment> getCommentsForBlog(int blogId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE blog_id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, blogId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setContent(rs.getString("content"));
                comment.setId(blogId); // facultatif selon ta classe Comment
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commentaires : " + e.getMessage());
        }
        return comments;
    }

}
