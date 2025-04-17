package ed.sanarenovo.controllers.Admin;

import ed.sanarenovo.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class TopBarController {

    @FXML
    private Label userEmailLabel;

    public void initialize() {
        if (UserSession.getInstance() != null) {
            userEmailLabel.setText(UserSession.getInstance().getUser().getEmail());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.logout();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AymenViews/Login.fxml"));
            Stage stage = (Stage) userEmailLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

