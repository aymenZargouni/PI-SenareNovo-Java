package ed.sanarenovo.tests;

import ed.sanarenovo.utils.MyConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/mahdyviews/affichage.fxml"));
        primaryStage.setTitle("Gestion des Équipements");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
  
    public static void main(String[] args) {
        launch(args);
    }
}