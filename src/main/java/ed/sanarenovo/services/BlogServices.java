package ed.sanarenovo.services;

import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.interfaces.IBlog;
import ed.sanarenovo.utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ed.sanarenovo.entities.Category;
public class BlogServices implements IBlog<Blog> {


    private void addBlogCategories(int blogId, List<Category> categories) throws SQLException {
        String sql = "INSERT INTO blog_category (blog_id, category_id) VALUES (?, ?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        for (Category category : categories) {
            pst.setInt(1, blogId);
            pst.setInt(2, category.getId());
            pst.addBatch();
        }
        pst.executeBatch();
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

            ResultSet rs = pst.getGeneratedKeys();
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
        List<Blog> data = new ArrayList<>();
        try {
            String requete = "SELECT * FROM blog";
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Blog b = new Blog();
                b.setId(rs.getInt("id"));
                b.setTitle(rs.getString("title"));
                b.setContent(rs.getString("content"));
                b.setImage(rs.getString("image"));
                data.add(b);
            }
        } catch (SQLException e) {
            System.out.println("\n" + "Erreur lors de la récupération des blogs : " + e.getMessage());
        }
        return data;
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
        List<Blog> results = new ArrayList<>();
        try {
            String requete = "SELECT * FROM blog WHERE title LIKE ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, "%" + title + "%");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
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
}
