package ed.sanarenovo.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        /*try {
            Parent categoryRoot = FXMLLoader.load(getClass().getResource("/Category.fxml"));
            Stage categoryStage = new Stage();
            categoryStage.setTitle("Gestion Catgory");
            categoryStage.setScene(new Scene(categoryRoot));
            categoryStage.show();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }*/

        try {
            Parent blogRoot = FXMLLoader.load(getClass().getResource("/Blog/Blog.fxml"));
            Stage blogStage = new Stage();
            blogStage.setTitle("Gestion Blog");
            blogStage.setScene(new Scene(blogRoot));
            blogStage.show();
            
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        /*try {
            Parent commentRoot = FXMLLoader.load(getClass().getResource("/Comment.fxml"));
            Stage commentStage = new Stage();
            commentStage.setTitle("Gestion Comment");
            commentStage.setScene(new Scene(commentRoot));
            commentStage.show();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }*/

        try {
            Parent chatRoot = FXMLLoader.load(getClass().getResource("/Blog/chatbot_view.fxml"));
            Stage chatStage = new Stage();
            chatStage.setTitle("SanareNovo ChatBot");
            chatStage.setScene(new Scene(chatRoot));
            chatStage.show();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
