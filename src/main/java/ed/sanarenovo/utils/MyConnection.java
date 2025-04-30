package ed.sanarenovo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private String url="jdbc:mysql://localhost:3306/senarenovo";
    private String login="root";
    private String pwd="";

    private Connection cnx; // objet pour comminiquer a la BDD
    public static MyConnection instance; //objet : singleton


    private MyConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            createConnection();
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur de chargement du driver MySQL: " + e.getMessage());
        }
    }

    private void createConnection() {
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            cnx.setAutoCommit(true);
            System.out.println("Connexion établie avec succès...");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
            System.err.println("URL: " + url);
            System.err.println("Login: " + login);
            // Ne pas initialiser cnx à null ici, laissez-le null pour que getCnx() puisse le détecter
        }
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                createConnection();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la connexion: " + e.getMessage());
            createConnection(); // Tentative de reconnexion
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
}
