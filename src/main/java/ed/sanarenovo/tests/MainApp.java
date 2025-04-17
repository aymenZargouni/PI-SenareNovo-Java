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
        try {
            MyConnection.getInstance();
            Parent root = FXMLLoader.load(getClass().getResource("/Blog/Category.fxml"));
            Scene scene = new Scene(root);
            //scene.getStylesheets().add(getClass().getResource("/Youssef_views/design.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            //throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}