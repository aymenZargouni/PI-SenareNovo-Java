package ed.sanarenovo.services;

import ed.sanarenovo.entities.CommentReply;
import ed.sanarenovo.interfaces.ICommentReply;
import ed.sanarenovo.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentReplyService implements ICommentReply {
    private Connection connection;

    public CommentReplyService() {
        connection = MyConnection.getInstance().getCnx();
    }

    @Override
    public void addReply(CommentReply reply) {
        String sql = "INSERT INTO comment_reply (content, user_id, comment_id, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, reply.getContent()); // gerer le clé primaire
            pst.setInt(2, reply.getUserId());
            pst.setInt(3, reply.getCommentId());
            pst.setTimestamp(4, Timestamp.valueOf(reply.getCreatedAt()));
            
            int result = pst.executeUpdate();
            System.out.println("Résultat de l'ajout de la réponse: " + result);

            //Récupère l’ID généré par la base de données et l’attribue à l’objet reply.
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                reply.setId(rs.getInt(1));
                System.out.println("Réponse ajoutée avec l'ID: " + reply.getId());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la réponse: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deleteReply(int id) {
        String sql = "DELETE FROM comment_reply WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la réponse: " + e.getMessage());
        }
    }

    @Override
    public void updateReply(CommentReply reply, int id) {
        String sql = "UPDATE comment_reply SET content = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, reply.getContent());
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la réponse: " + e.getMessage());
        }
    }

    @Override
    public List<CommentReply> getAllReplies() {
        List<CommentReply> replies = new ArrayList<>();
        String sql = "SELECT cr.*, u.email as user_email FROM comment_reply cr " +
                    "LEFT JOIN user u ON cr.user_id = u.id " +
                    "ORDER BY cr.created_at DESC"; // Récupère toutes les réponses, avec l’email de l’utilisateur qui a répondu (jointure avec la table user).
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                replies.add(createReplyFromResultSet(rs)); // Convertit chaque ligne du résultat SQL en objet CommentReply.
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réponses: " + e.getMessage());
        }
        return replies;
    }

    @Override
    public List<CommentReply> getRepliesByCommentId(int commentId) {
        List<CommentReply> replies = new ArrayList<>();
        String sql = "SELECT cr.*, u.email as user_email FROM comment_reply cr " +
                    "LEFT JOIN user u ON cr.user_id = u.id " +
                    "WHERE cr.comment_id = ? " +
                    "ORDER BY cr.created_at DESC"; //  Récupérer les réponses d’un commentaire spécifique.
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, commentId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                replies.add(createReplyFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réponses: " + e.getMessage());
        }
        return replies;
    }

    @Override
    public List<CommentReply> getRepliesByUserId(int userId) {
        List<CommentReply> replies = new ArrayList<>();
        String sql = "SELECT cr.*, u.email as user_email FROM comment_reply cr " +
                    "LEFT JOIN user u ON cr.user_id = u.id " +
                    "WHERE cr.user_id = ? " +
                    "ORDER BY cr.created_at DESC"; // Lister toutes les réponses faites par un utilisateur.
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                replies.add(createReplyFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réponses: " + e.getMessage());
        }
        return replies;
    }

    @Override
    public CommentReply getReplyById(int id) { // Cherche une réponse précise par son identifiant.
        String sql = "SELECT cr.*, u.email as user_email FROM comment_reply cr " +
                    "LEFT JOIN user u ON cr.user_id = u.id " +
                    "WHERE cr.id = ?"; // Lire une réponse unique à partir de son ID.
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return createReplyFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la réponse: " + e.getMessage());
        }
        return null;
    }

    //Sert à transformer une ligne SQL (résultat d’une requête) en objet CommentReply
    private CommentReply createReplyFromResultSet(ResultSet rs) throws SQLException {
        CommentReply reply = new CommentReply();
        reply.setId(rs.getInt("id"));
        reply.setContent(rs.getString("content"));
        reply.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        reply.setUserId(rs.getInt("user_id"));
        reply.setCommentId(rs.getInt("comment_id"));
        reply.setUserEmail(rs.getString("user_email"));
        return reply;
    }
} 