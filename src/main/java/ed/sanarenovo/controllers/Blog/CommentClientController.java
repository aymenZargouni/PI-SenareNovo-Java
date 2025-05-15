package ed.sanarenovo.controllers.Blog;
import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.AvertissementSMSTwilio;
import ed.sanarenovo.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import ed.sanarenovo.entities.Comment;
import ed.sanarenovo.services.CommentServices;
import ed.sanarenovo.services.UserService;
import ed.sanarenovo.entities.CommentReply;
import ed.sanarenovo.services.CommentReplyService;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CommentClientController {
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TableView<Comment> tableComment;
    @FXML private TableColumn<Comment, Integer> colId;
    @FXML private TableColumn<Comment, String> colContent;
    @FXML private TableColumn<Comment, String> colUserEmail;
    @FXML private TextField txtContent;
    @FXML private Label lblBlogTitle;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnReply;
    @FXML private TableView<CommentReply> replyTable;
    @FXML private TableColumn<CommentReply, String> colReplyContent;
    @FXML private TableColumn<CommentReply, String> colReplyAuthor;
    @FXML private TableColumn<CommentReply, String> colReplyDate;
    @FXML
    private Button btnRetour;
    private Blog blog;
    private final CommentServices commentService = new CommentServices();
    private final UserService userService = new UserService();
    private User currentUser;
    private final CommentReplyService replyService = new CommentReplyService();

    @FXML
    public void initialize() {
        // Configuration des colonnes du tableau des commentaires
        colId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colContent.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getContent()));
        colUserEmail.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUser() != null
                        ? cellData.getValue().getUser().getEmail()
                        : "Anonyme"));

        // Configuration des colonnes de réponses
        if (replyTable != null && colReplyContent != null && colReplyAuthor != null && colReplyDate != null) {
            colReplyContent.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getContent()));
            colReplyAuthor.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getUserEmail()));
            colReplyDate.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        }

        // Récupérer l'utilisateur connecté
        UserSession session = UserSession.getInstance();
        if (session != null && session.isLoggedIn()) {
            currentUser = session.getUser();
            System.out.println("Utilisateur connecté: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
            updateButtonVisibility();
        } else {
            if (btnAdd != null) btnAdd.setVisible(false);
            if (btnUpdate != null) btnUpdate.setVisible(false);
            if (btnDelete != null) btnDelete.setVisible(false);
            if (btnReply != null) btnReply.setVisible(false);
        }

        // Gestion de la sélection d'un commentaire dans le tableau
        if (tableComment != null) {
            tableComment.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                // Mettre à jour la visibilité des boutons en fonction de la sélection
                updateButtonVisibility();

                // Si un commentaire est sélectionné
                if (newSelection != null) {
                    // Afficher le contenu du commentaire dans le TextField
                    txtContent.setText(newSelection.getContent());
                    // Activer le bouton de modification
                    btnUpdate.setDisable(false);
                } else {
                    // Si aucun commentaire n'est sélectionné
                    txtContent.clear();
                    // Désactiver le bouton de modification
                    btnUpdate.setDisable(true);
                }
            });
        }
    }

    private void updateButtonVisibility() {
        UserSession session = UserSession.getInstance();
        boolean isLoggedIn = session != null && session.isLoggedIn();

        if (btnAdd != null) btnAdd.setVisible(isLoggedIn);

        Comment selectedComment = tableComment != null ? tableComment.getSelectionModel().getSelectedItem() : null;
        if (selectedComment != null) {
            User currentUser = session != null ? session.getUser() : null;
            boolean isOwner = currentUser != null && selectedComment.getUser() != null &&
                    selectedComment.getUser().getId() == currentUser.getId();
            boolean isAdmin = currentUser != null && currentUser.getRoles() != null &&
                    currentUser.getRoles().contains("ROLE_ADMIN");

            if (btnUpdate != null) btnUpdate.setVisible(isOwner || isAdmin);
            if (btnDelete != null) btnDelete.setVisible(isOwner || isAdmin);
            if (btnReply != null) btnReply.setVisible(isLoggedIn);

            // Charger les réponses pour le commentaire sélectionné
            if (replyTable != null) {
                loadReplies(selectedComment.getId());
            }
        } else {
            if (btnUpdate != null) btnUpdate.setVisible(false);
            if (btnDelete != null) btnDelete.setVisible(false);
            if (btnReply != null) btnReply.setVisible(false);
            if (replyTable != null) {
                replyTable.setItems(FXCollections.observableArrayList());
            }
        }
    }

    private void loadReplies(int commentId) {
        if (replyTable != null) {
            List<CommentReply> replies = replyService.getRepliesByCommentId(commentId);
            replyTable.setItems(FXCollections.observableArrayList(replies));
        }
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
        lblBlogTitle.setText("Commentaires de : " + blog.getTitle());
        refreshTable();
    }

    private void refreshTable() {
        if (blog != null) {
            ObservableList<Comment> comments = FXCollections.observableArrayList(
                    commentService.getCommentsByBlogId(blog.getId())
            );
            tableComment.setItems(comments);
        }
    }

    @FXML
    private void addComment() {
        String content = txtContent.getText().trim();

        if (content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champ requis", "Le contenu du commentaire est vide.");
            return;
        }

        if (content.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Commentaire trop court", "Le commentaire doit contenir au moins 3 caractères.");
            return;
        }

        UserSession session = UserSession.getInstance();
        if (session == null || !session.isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Non connecté", "Vous devez être connecté pour commenter.");
            return;
        }

        User currentUser = session.getUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur utilisateur", "Utilisateur introuvable.");
            return;
        }

        // Vérification du blocage
        if (currentUser.getBlockedUntil() != null && currentUser.getBlockedUntil().isAfter(LocalDateTime.now())) {
            showAlert(Alert.AlertType.WARNING, "Accès bloqué", "Vous êtes bloqué jusqu'à " + currentUser.getBlockedUntil());
            // Déconnexion automatique de l'utilisateur bloqué
            session.logout();
            try {
                // Redirection vers la page de connexion
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AymenViews/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) txtContent.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        List<String> bannedWords = List.of("bête", "idiot", "stupide", "nul");

        boolean hasBadWord = bannedWords.stream().anyMatch(word ->
                content.toLowerCase().contains(word));

        if (hasBadWord) {
            currentUser.setInfractionCount(currentUser.getInfractionCount() + 1);

            if (currentUser.getInfractionCount() >= 3) {
                // Blocage de l'utilisateur pour 24 heures
                currentUser.setBlockedUntil(LocalDateTime.now().plusHours(24));
                currentUser.setInfractionCount(0);
                currentUser.setBlocked(true);
                userService.update(currentUser, currentUser.getId());

                // Envoi du SMS d'avertissement à l'admin
                try {
                    String adminPhoneNumber = "+21658888302"; // Le numéro admin au format E.164
                    String smsMessage = "Alerte: L'utilisateur " + currentUser.getEmail() + " a été bloqué pour 24h après 3 infractions.";
                    AvertissementSMSTwilio.sendSMS(adminPhoneNumber, smsMessage);
                } catch (Exception e) {
                    e.printStackTrace(); // En cas d'erreur SMS, on continue sans bloquer
                }

                showAlert(Alert.AlertType.ERROR, "Bloqué", "Vous avez été bloqué pendant 24h.");

                // Déconnexion automatique après blocage
                session.logout();
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/AymenViews/Login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) txtContent.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;

            } else {
                userService.update(currentUser, currentUser.getId());
                showAlert(Alert.AlertType.WARNING, "Mot interdit", "Votre commentaire contient des mots interdits. Tentative " + currentUser.getInfractionCount() + "/3");
                return;
            }
        }

        Comment comment = new Comment(content, blog);
        comment.setUser(currentUser); // Association du user
        commentService.addComment(comment);

        refreshTable();
        txtContent.clear();
    }

    @FXML
    private void updateComment() {
        Comment selectedComment = tableComment.getSelectionModel().getSelectedItem();
        if (selectedComment == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un commentaire à modifier.");
            return;
        }

        String content = txtContent.getText().trim();
        if (content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champ requis", "Le contenu du commentaire est vide.");
            return;
        }

        UserSession session = UserSession.getInstance();
        if (session == null || !session.isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Non connecté", "Vous devez être connecté pour modifier un commentaire.");
            return;
        }

        User currentUser = session.getUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur utilisateur", "Utilisateur introuvable.");
            return;
        }

        boolean isOwner = selectedComment.getUser() != null && selectedComment.getUser().getId() == currentUser.getId();
        boolean isAdmin = currentUser.getRoles() != null && currentUser.getRoles().contains("ROLE_ADMIN");

        if (!isOwner && !isAdmin) {
            showAlert(Alert.AlertType.ERROR, "Non autorisé", "Vous ne pouvez pas modifier ce commentaire.");
            return;
        }

        try {
            selectedComment.setContent(content);
            commentService.updateComment(selectedComment, selectedComment.getId());
            refreshTable();
            txtContent.clear();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Commentaire modifié avec succès.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la modification du commentaire : " + e.getMessage());
        }
    }

    @FXML
    private void deleteComment() {
        Comment selectedComment = tableComment.getSelectionModel().getSelectedItem();
        if (selectedComment == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un commentaire à supprimer.");
            return;
        }

        UserSession session = UserSession.getInstance();
        if (session == null || !session.isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Non connecté", "Vous devez être connecté pour supprimer un commentaire.");
            return;
        }

        User currentUser = session.getUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur utilisateur", "Utilisateur introuvable.");
            return;
        }

        boolean isOwner = selectedComment.getUser() != null && selectedComment.getUser().getId() == currentUser.getId();
        boolean isAdmin = currentUser.getRoles() != null && currentUser.getRoles().contains("ROLE_ADMIN");

        if (!isOwner && !isAdmin) {
            showAlert(Alert.AlertType.ERROR, "Non autorisé", "Vous ne pouvez pas supprimer ce commentaire.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                commentService.deleteComment(selectedComment.getId());
                refreshTable();
                txtContent.clear();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Commentaire supprimé avec succès.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la suppression du commentaire : " + e.getMessage());
            }
        }
    }

    @FXML
    private void openReplyWindow() {
        Comment selectedComment = tableComment.getSelectionModel().getSelectedItem();
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
            //scene.getStylesheets().add(getClass().getResource("/Blog/css/CommentStyle.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            // Recharger les réponses après la fermeture de la fenêtre
            loadReplies(selectedComment.getId());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de réponse : " + e.getMessage());
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
    private void retourBlog(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Blog/BlogClient.fxml"));
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}