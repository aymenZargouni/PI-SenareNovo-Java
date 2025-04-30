package ed.sanarenovo.tests;

import ed.sanarenovo.utils.MyConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainAppBlogTest extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Établir la connexion à la base de données
        try {
            MyConnection.getInstance();
            System.out.println("Connexion à la base de données établie avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la connexion à la base de données: " + e.getMessage());
            return;
        }

        try {
            Parent login = FXMLLoader.load(getClass().getResource("/AymenViews/Login.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(login));
            loginStage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de Login.fxml: " + e.getMessage());
        }

        try {
            Parent blogRoot = FXMLLoader.load(getClass().getResource("/Blog/Blog.fxml"));
            Stage blogStage = new Stage();
            Scene blogScene = new Scene(blogRoot);
            blogScene.getStylesheets().add(getClass().getResource("/Blog/style.css").toExternalForm());
            blogStage.setTitle("Gestion Blog");
            blogStage.setScene(blogScene);
            blogStage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de Blog.fxml: " + e.getMessage());
        }

        try {
            Parent categoryRoot = FXMLLoader.load(getClass().getResource("/Blog/Category.fxml"));
            Stage categoryStage = new Stage();
            categoryStage.setTitle("Gestion Category");
            categoryStage.setScene(new Scene(categoryRoot));
            categoryStage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de Category.fxml: " + e.getMessage());
        }

        try {
            Parent BclientRoot = FXMLLoader.load(getClass().getResource("/Blog/BlogClient.fxml"));
            Stage bClientStage = new Stage();
            bClientStage.setTitle("BlogClient");
            bClientStage.setScene(new Scene(BclientRoot));
            bClientStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de BlogClient.fxml");
        }

        try {
            Parent chatRoot = FXMLLoader.load(getClass().getResource("/Blog/chatBot_view.fxml"));
            Stage chatStage = new Stage();
            chatStage.setTitle("Chat Boot AI");
            chatStage.setScene(new Scene(chatRoot));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de ChatBloot.fxml");
        }

        try {
            Parent chatBootSante = FXMLLoader.load(getClass().getResource("/Blog/ChatbootSante.fxml"));
            Stage chatSante = new Stage();
            chatSante.setTitle("Chat Boot Santé");
            chatSante.setScene(new Scene(chatBootSante));
            chatSante.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de ChatBloot.fxml");
        }

        try {
            Parent BlogStatsCategory = FXMLLoader.load(getClass().getResource("/Blog/BlogStatsCategory.fxml"));
            Stage stageBlogStatsCategory = new Stage();
            stageBlogStatsCategory.setTitle("Statistique par catgories ");
            stageBlogStatsCategory.setScene(new Scene(BlogStatsCategory));
            stageBlogStatsCategory.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de ChatBloot.fxml");
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}