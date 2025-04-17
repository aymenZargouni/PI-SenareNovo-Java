package ed.sanarenovo.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
<<<<<<< HEAD
        Parent root = FXMLLoader.load(getClass().getResource("/views/TechClaims.fxml"));
        primaryStage.setTitle("Gestion des Équipements");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
=======
        Parent root = FXMLLoader.load(getClass().getResource("/takoua_views/main_view.fxml"));
        primaryStage.setTitle("Système de Recrutement");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

>>>>>>> origin/Offre.candidat
    public static void main(String[] args) {
        launch(args);
    }
}