package ed.sanarenovo.services;

import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.entities.Comment;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.interfaces.IComment;
import ed.sanarenovo.utils.MyConnection;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentServices implements IComment<Comment> {

    private Connection cnx;

    public CommentServices() {
        cnx = MyConnection.getInstance().getCnx();
    }

    @Override
    public void addComment(Comment comment) {
        String sql = "INSERT INTO comment (content, blog_id, user_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getBlog().getId());
            stmt.setInt(3, comment.getUser().getId()); // 👈 ajout important
            stmt.executeUpdate();
            System.out.println("Commentaire ajouté avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void deleteComment(int id) {
        String query = "DELETE FROM comment WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\n" + "Commentaire supprimé avec succès.");
            } else {
                System.out.println("Aucun commentaire trouvé avec l'ID :" + id);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du commentaire :" + e.getMessage());
        }
    }

    @Override
    public void updateComment(Comment comment, int id) {
        String query = "UPDATE comment SET content = ? WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, comment.getContent());
            pst.setInt(2, id);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Commentaire supprimé avec succès.");
            } else {
                System.out.println("\n" + "Aucun commentaire trouvé avec l'ID : " + id);
            }
        } catch (SQLException e) {
            System.out.println("\n" + "Erreur lors de la mise à jour du commentaire :" + e.getMessage());
        }
    }

    @Override
    public List<Comment> getComments() {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comment";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Comment c = new Comment(rs.getInt("id"), rs.getString("content"));
                comments.add(c);
            }
        } catch (SQLException e) {
            System.out.println("\n" + "Erreur lors de la récupération des commentaires :" + e.getMessage());
        }
        return comments;
    }

    @Override
    public Comment getCommentById(int id) {
        String query = "SELECT * FROM comment WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Comment(rs.getInt("id"), rs.getString("content"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du commentaire par ID :" + e.getMessage());
        }
        return null;
    }

    public List<Comment> getCommentsByBlogId(int blogId) {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT c.*, u.id as user_id, u.email, u.password, u.roles, u.is_blocked, " +
                      "u.reset_token, u.reset_token_expires_at, u.blocked_until, u.infraction_count " +
                      "FROM comment c " +
                      "LEFT JOIN user u ON c.user_id = u.id " +
                      "WHERE c.blog_id = ? " +
                      "ORDER BY c.id DESC";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, blogId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                // Construire le blog lié à ce commentaire
                Blog blog = new Blog();
                blog.setId(blogId);
                
                // Construire l'utilisateur lié à ce commentaire
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRoles(rs.getString("roles"));
                user.setBlocked(rs.getBoolean("is_blocked"));
                user.setResetToken(rs.getString("reset_token"));
                user.setResetTokenExpiresAt(rs.getString("reset_token_expires_at"));
                
                Timestamp blockedUntil = rs.getTimestamp("blocked_until");
                if (blockedUntil != null) {
                    user.setBlockedUntil(blockedUntil.toLocalDateTime());
                }
                user.setInfractionCount(rs.getInt("infraction_count"));

                // Construire le commentaire
                Comment comment = new Comment(
                    rs.getInt("id"),
                    rs.getString("content"),
                    blog
                );
                comment.setUser(user);
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commentaires pour le blog ID " + blogId + ": " + e.getMessage());
        }

        return comments;
    }


}
