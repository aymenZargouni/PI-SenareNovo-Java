package ed.sanarenovo.controllers.Admin;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ShowUsers {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button addMedbtn;

    @FXML
    private Button deletebtn;

    @FXML
    private TableColumn<User, String> email;

    @FXML
    private TableColumn<User, Integer> id;

    @FXML
    private TableColumn<User, Boolean> isblocked;

    @FXML
    private TableView<User> userTable;

    @FXML
    private Button modifbtn;

    @FXML
    private TableColumn<User, String> password;

    @FXML
    private TableColumn<User, String> roles;

    @FXML
    private VBox rootContainer;

    @FXML
    void addUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AymenViews/AddUser.fxml"));
            Parent root = loader.load();

            AddUserController controller = loader.getController();
            controller.setControllerRef(this);
            Stage stage = new Stage();
            stage.setTitle("Add New Medecin");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Suppression");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un utilisateur à supprimer !");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cet utilisateur ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            UserService userService = new UserService();
            userService.delete(selectedUser.getId());


            loadUsers();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText(null);
            success.setContentText("Utilisateur supprimé avec succès !");
            success.showAndWait();
        }
    }

    @FXML
    void modifUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AymenViews/EditUser.fxml"));
                Parent root = loader.load();

                EditUserController controller = loader.getController();
                controller.setUser(selectedUser);

                Stage stage = new Stage();
                stage.setTitle("Edit User");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a medecin to modify.");
            alert.showAndWait();
        }
    }

    @FXML
    void initialize() {
        assert addMedbtn != null : "fx:id=\"addMedbtn\" was not injected: check your FXML file 'ShowUsers.fxml'.";
        assert deletebtn != null : "fx:id=\"deletebtn\" was not injected: check your FXML file 'ShowUsers.fxml'.";
        assert email != null : "fx:id=\"email\" was not injected: check your FXML file 'ShowUsers.fxml'.";
        assert id != null : "fx:id=\"id\" was not injected: check your FXML file 'ShowUsers.fxml'.";
        assert isblocked != null : "fx:id=\"isblocked\" was not injected: check your FXML file 'ShowUsers.fxml'.";
        assert userTable != null : "fx:id=\"userTable\" was not injected: check your FXML file 'ShowUsers.fxml'.";
        assert modifbtn != null : "fx:id=\"modifbtn\" was not injected: check your FXML file 'ShowUsers.fxml'.";
        assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'ShowUsers.fxml'.";
        assert roles != null : "fx:id=\"roles\" was not injected: check your FXML file 'ShowUsers.fxml'.";
        assert rootContainer != null : "fx:id=\"rootContainer\" was not injected: check your FXML file 'ShowUsers.fxml'.";

        UserService userService = new UserService();
        List<User> userList = userService.getAll();

        userTable.setItems(FXCollections.observableArrayList(userList));

        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        password.setCellValueFactory(new PropertyValueFactory<>("password"));
        roles.setCellValueFactory(new PropertyValueFactory<>("roles"));
        isblocked.setCellValueFactory(new PropertyValueFactory<>("blocked"));


    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void loadUsers() {
        UserService userService = new UserService();
        List<User> users = userService.getAll();

        ObservableList<User> data = FXCollections.observableArrayList(users);
        userTable.getItems().setAll(data);
    }
}
