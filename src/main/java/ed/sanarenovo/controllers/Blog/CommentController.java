package ed.sanarenovo.controllers.Blog;

import ed.sanarenovo.entities.Blog;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import ed.sanarenovo.entities.Comment;
import ed.sanarenovo.services.CommentServices;

import java.util.*;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class CommentController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TableView<Comment> tableComment;
    @FXML private TableColumn<Comment, Integer> colId;
    @FXML private TableColumn<Comment, String> colContent;
    @FXML private TextField txtContent;
    @FXML private Label lblBlogTitle;
    @FXML private TableColumn<Comment, Integer> colBlogId;
    private Blog blog;
    private CommentServices commentService = new CommentServices();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colContent.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getContent()));
        colBlogId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getBlog().getId()).asObject());

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



    /*@FXML
    private void addComment() {
        String content = txtContent.getText();
        if (!content.isEmpty()) {
            Comment comment = new Comment(content);
            commentService.addComment(comment);
            refreshTable();
            txtContent.clear();
        }
    }*/

    @FXML
    private void addComment() {
        String content = txtContent.getText().trim();
        if (content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champ requis", "Le Content doit être rempli.");
            return;
        }
        if (content.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Content invalide", "Le Content doit contenir au moins 3 caractères.");
            return;
        }

        if (!content.isEmpty()) {
            // Liste de mots bannis (à personnaliser selon les besoins)
            List<String> bannedWords = List.of("bête", "idiot", "stupide", "nul"); // Exemple

            // Vérification si le commentaire contient un mot interdit
            boolean hasBannedWord = bannedWords.stream()
                    .anyMatch(word -> content.toLowerCase().contains(word.toLowerCase()));

            if (hasBannedWord) {
                // Message d'alerte à l'utilisateur
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Commentaire refusé");
                alert.setHeaderText(null);
                alert.setContentText("Votre commentaire contient des mots interdits et ne respecte pas les règles.");
                alert.showAndWait();


            // Récupérer l'utilisateur connecté (par exemple via une session)
            //    User currentUser = Session.getCurrentUser();

            // Incrémenter un compteur d'infractions (ex : currentUser.incrementInfractionCount())

            // Si l'utilisateur atteint 3 infractions :
            //    - Bloquer les commentaires pendant 24 heures
            //    - Sauvegarder la date/heure du blocage
            //    if (currentUser.getInfractionCount() >= 3) {
            //        currentUser.setBlockedUntil(LocalDateTime.now().plusHours(24));
            //    }

            // Lors de l'ajout d'un commentaire, vérifier si l'utilisateur est encore bloqué :
            //    if (currentUser.getBlockedUntil().isAfter(LocalDateTime.now())) {
            //        // Afficher une alerte que l'utilisateur est bloqué
            //        return;
            //    }

            } else {
                Comment comment = new Comment(content, blog);
                commentService.addComment(comment);
                refreshTable();
                txtContent.clear();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String content, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(content);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void updateComment() {
        Comment selected = tableComment.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setContent(txtContent.getText());
            commentService.updateComment(selected, selected.getId());
            refreshTable();
            txtContent.clear();
        }
    }

    @FXML
    private void deleteComment() {
        Comment selected = tableComment.getSelectionModel().getSelectedItem();
        if (selected != null) {
            commentService.deleteComment(selected.getId());
            refreshTable();
            txtContent.clear();
        }
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
        refreshTable();
    }
}
