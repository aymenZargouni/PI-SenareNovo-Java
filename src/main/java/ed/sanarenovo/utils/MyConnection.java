package ed.sanarenovo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    // Configuration de la base de données
    private final String url = "jdbc:mysql://localhost:3306/senarenovo?useSSL=false&serverTimezone=UTC";
    private final String login = "root";
    private final String pwd = "";

    private Connection cnx;
    private static MyConnection instance;

    private MyConnection() {
        establishConnection();
    }

    private void establishConnection() {
        try {
            // Chargement du driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Création de la connexion avec des paramètres optimisés
            cnx = DriverManager.getConnection(url, login, pwd);

            // Configuration de la connexion
            cnx.setAutoCommit(true); // Activation de l'auto-commit
            System.out.println("Connexion établie avec succès");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC MySQL introuvable");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'établissement de la connexion :");
            e.printStackTrace();
        }
    }

    public Connection getCnx() {
        try {
            // Vérification que la connexion est toujours valide
            if (cnx == null || cnx.isClosed() || !cnx.isValid(2)) {
                System.out.println("Recréation de la connexion...");
                establishConnection();
            }
        } catch (SQLException e) {
            System.err.println("Erreur de vérification de la connexion :");
            e.printStackTrace();
            establishConnection();
        }
        return cnx;
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            synchronized (MyConnection.class) {
                if (instance == null) {
                    instance = new MyConnection();
                }
            }
        }
        return instance;
    }

    // Méthode pour fermer proprement la connexion
    public static void closeConnection() {
        if (instance != null && instance.cnx != null) {
            try {
                instance.cnx.close();
                System.out.println("Connexion fermée avec succès");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion :");
                e.printStackTrace();
            }
            instance = null;
        }
    }
}