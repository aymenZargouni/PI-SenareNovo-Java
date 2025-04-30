package ed.sanarenovo.controllers.Blog;

import ed.sanarenovo.entities.Comment;
import ed.sanarenovo.entities.CommentReply;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.CommentReplyService;
import ed.sanarenovo.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CommentReplyController {
    @FXML private Label lblCommentContent;
    @FXML private Label lblCommentAuthor;
    @FXML private TextArea txtReply;
    
    private final CommentReplyService replyService = new CommentReplyService();
    private Comment selectedComment;
    private User currentUser;
    
    @FXML
    // cette méthode récupère l’utilisateur actuellement connecté via une session singleton (UserSession) pour l’utiliser lors de l’envoi de réponse.
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session != null && session.isLoggedIn()) {
            currentUser = session.getUser();
        }
    }
    // Met à jour l’interface avec les détails du commentaire sélectionné :
    public void setComment(Comment comment) {
        this.selectedComment = comment;
        if (comment != null) {
            lblCommentContent.setText(comment.getContent());
            lblCommentAuthor.setText("Par: " + comment.getUser().getEmail());
        }
    }
    
    @FXML
    private void sendReply() {
        if (selectedComment == null || currentUser == null) { // si l'utilisateur ne pas connecte || si le contenue est vide
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'envoyer la réponse.");
            return;
        }
        
        String replyContent = txtReply.getText().trim();
        if (replyContent.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le contenu de la réponse ne peut pas être vide.");
            return;
        }
        //Appelle le service pour ajouter la réponse en base.
        CommentReply reply = new CommentReply(replyContent, currentUser, selectedComment);
        replyService.addReply(reply);
        
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre réponse a été envoyée avec succès.");
        closeWindow();
    }
    
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) txtReply.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 