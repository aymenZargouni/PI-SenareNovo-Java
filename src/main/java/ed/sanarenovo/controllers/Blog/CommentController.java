package ed.sanarenovo.controllers.Blog;

import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.entities.Comment;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.entities.CommentReply;
import ed.sanarenovo.services.CommentServices;
import ed.sanarenovo.services.UserService;
import ed.sanarenovo.services.CommentReplyService;
import ed.sanarenovo.utils.BannedWordsManager;
import ed.sanarenovo.utils.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ed.sanarenovo.services.AvertissementSMSTwilio;

import java.time.LocalDateTime;
import java.util.List;
import java.io.IOException;

public class CommentController {
    @FXML private TableView<Comment> tableComments;
    @FXML private TableColumn<Comment, Integer> colId;
    @FXML private TableColumn<Comment, String> colContent;
    @FXML private TableColumn<Comment, String> colUsername;
    @FXML private TextField txtContent;
    @FXML private Button btnAdd;
    @FXML private Button btnDelete;
    @FXML private Button btnUpdate;
    @FXML private Button btnReply;
    @FXML private Button btnDeleteReply;
    @FXML private TableView<CommentReply> replyTable;
    @FXML private TableColumn<CommentReply, String> colReplyContent;
    @FXML private TableColumn<CommentReply, String> colReplyAuthor;
    @FXML private TableColumn<CommentReply, String> colReplyDate;

    private Blog blog;
    private CommentServices commentService = new CommentServices();
    private UserService userService = new UserService();
    private CommentReplyService replyService = new CommentReplyService();
    private BannedWordsManager bannedWordsManager = new BannedWordsManager();
    private User currentUser;

    private static final int MAX_INFRACTIONS = 3;
    private static final int BLOCK_DURATION_HOURS = 24;

    @FXML
    public void initialize() {
        // Configuration des colonnes de commentaires
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        colUsername.setCellValueFactory(cellData -> {
            User user = cellData.getValue().getUser();
            return new SimpleStringProperty(user != null ? user.getEmail() : "");
        });

        // Configuration des colonnes de réponses
        colReplyContent.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getContent()));
        colReplyAuthor.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUserEmail()));
        colReplyDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        UserSession session = UserSession.getInstance();
        if (session != null && session.isLoggedIn()) {
            currentUser = session.getUser();
            updateButtonVisibility();
        }

        // Ajouter un listener pour la sélection de commentaire
        tableComments.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadReplies(newSelection.getId());
            } else {
                replyTable.setItems(FXCollections.observableArrayList());
            }
        });

        refreshTable();

        // Set full screen mode
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) tableComments.getScene().getWindow();
            if (stage != null) {
                stage.setMaximized(true);
            }
        });
    }

    private void updateButtonVisibility() {
        if (currentUser == null) {
            btnAdd.setVisible(false);
            btnDelete.setVisible(false);
            btnUpdate.setVisible(false);
            btnReply.setVisible(false);
            btnDeleteReply.setVisible(false);
            return;
        }

        boolean isAdmin = currentUser.getRoles() != null && currentUser.getRoles().contains("ROLE_ADMIN");
        
        // Les admins peuvent tout faire
        if (isAdmin) {
            btnDelete.setVisible(true);
            btnUpdate.setVisible(true);
            btnDeleteReply.setVisible(true);
        } else {
            btnDelete.setVisible(false);
            btnUpdate.setVisible(false);
            btnDeleteReply.setVisible(false);
        }
        
        // Tout utilisateur connecté peut ajouter des commentaires et répondre
        btnAdd.setVisible(true);
        btnReply.setVisible(true);
    }

    private void loadReplies(int commentId) {
        List<CommentReply> replies = replyService.getRepliesByCommentId(commentId);
        replyTable.setItems(FXCollections.observableArrayList(replies));
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
        refreshTable();
    }

    private void refreshTable() {
        if (blog != null) {
            List<Comment> comments = commentService.getCommentsByBlogId(blog.getId());
            ObservableList<Comment> commentList = FXCollections.observableArrayList(comments);
            tableComments.setItems(commentList);
        }
    }

    @FXML
    private void openReplyWindow() {
        Comment selectedComment = tableComments.getSelectionModel().getSelectedItem();
        if (selectedComment == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un commentaire pour répondre.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/CommentReply.fxml"));
            Parent root = loader.load();
            CommentReplyController controller = loader.getController();
            controller.setComment(selectedComment);

            Stage stage = new Stage();
            stage.setTitle("Répondre au commentaire");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();

            // Recharger les réponses après la fermeture de la fenêtre
            loadReplies(selectedComment.getId());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de réponse : " + e.getMessage());
        }
    }

    @FXML
    private void deleteReply() {
        CommentReply selectedReply = replyTable.getSelectionModel().getSelectedItem();
        if (selectedReply == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une réponse à supprimer.");
            return;
        }

        if (currentUser != null && currentUser.getRoles() != null && 
            currentUser.getRoles().contains("ROLE_ADMIN")) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                replyService.deleteReply(selectedReply.getId());
                loadReplies(selectedReply.getCommentId());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous n'avez pas la permission de supprimer des réponses.");
        }
    }

    @FXML
    private void addComment() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour ajouter un commentaire.");
            return;
        }

        String content = txtContent.getText().trim();
        if (content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le contenu du commentaire ne peut pas être vide.");
            return;
        }

        // Vérifier si l'utilisateur est bloqué
        if (currentUser.isBlocked() && currentUser.getBlockedUntil() != null) {
            if (LocalDateTime.now().isBefore(currentUser.getBlockedUntil())) {
                showAlert(Alert.AlertType.ERROR, "Compte bloqué",
                        "Votre compte est bloqué jusqu'au " + currentUser.getBlockedUntil() +
                                " en raison de commentaires inappropriés.");
                return;
            } else {
                // Débloque l'utilisateur si la période est terminée
                currentUser.setBlocked(false);
                currentUser.setBlockedUntil(null);
                currentUser.setInfractionCount(0);
                userService.update(currentUser, currentUser.getId());
            }
        }

        // Vérifier les mots interdits
        if (bannedWordsManager.containsBannedWords(content)) {
            currentUser.setInfractionCount(currentUser.getInfractionCount() + 1);

            // Incrémente les infractions et bloque si nécessaire
            if (currentUser.getInfractionCount() >= MAX_INFRACTIONS) {
                currentUser.setBlocked(true);
                currentUser.setBlockedUntil(LocalDateTime.now().plusHours(BLOCK_DURATION_HOURS));

                // Envoi du SMS à l'admin
                String adminPhoneNumber = "+21658888302"; // Ton numéro admin
                String smsMessage = "ALERTE : L'utilisateur " + currentUser.getEmail() +
                        " a été bloqué pour 24h après avoir publié 3 commentaires inappropriés.";
                AvertissementSMSTwilio.sendSMS(adminPhoneNumber, smsMessage);

                showAlert(Alert.AlertType.ERROR, "Compte bloqué",
                        "Votre compte a été bloqué pour 24 heures en raison de commentaires inappropriés.");

            } else {
                showAlert(Alert.AlertType.WARNING, "Avertissement",
                        "Votre commentaire contient des mots interdits. " +
                                "Attention : " + (MAX_INFRACTIONS - currentUser.getInfractionCount()) +
                                " avertissement(s) restant(s).");
            }

            userService.update(currentUser, currentUser.getId());
            return;
        }

        // Si pas de problème, ajouter le commentaire
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setBlog(blog);
        comment.setUser(currentUser);

        commentService.addComment(comment);
        refreshTable();
        txtContent.clear();
    }


    @FXML
    private void deleteComment() {
        Comment selectedComment = tableComments.getSelectionModel().getSelectedItem(); //Récupération du commentaire sélectionné
        if (selectedComment != null) { // Seul un admin peut supprimer des commentaires.
            if (currentUser != null && currentUser.getRoles() != null &&
                currentUser.getRoles().contains("ROLE_ADMIN")) {
                commentService.deleteComment(selectedComment.getId());
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vous n'avez pas la permission de supprimer des commentaires.");
            }
        }


    }

    @FXML
    private void updateComment() {
        Comment selectedComment = tableComments.getSelectionModel().getSelectedItem();
        if (selectedComment != null) {
            if (currentUser != null && currentUser.getRoles() != null &&
                currentUser.getRoles().contains("ROLE_ADMIN")) {
                String newContent = txtContent.getText().trim();
                if (!newContent.isEmpty()) {
                    selectedComment.setContent(newContent);
                    commentService.updateComment(selectedComment, selectedComment.getId());
                    refreshTable();
                    txtContent.clear();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vous n'avez pas la permission de modifier des commentaires.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void setFullScreen() {
        Stage stage = (Stage) tableComments.getScene().getWindow();
        stage.setFullScreen(true);
    }
}
