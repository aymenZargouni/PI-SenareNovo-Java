package ed.sanarenovo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private String url="jdbc:mysql://localhost:3306/senarenovo";
    private String login="root";
    private String pwd="";

    private Connection cnx;
    public static MyConnection instance;


    private MyConnection() {
        createConnection();
    }

    private void createConnection() {

        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            cnx.setAutoCommit(true); // Active la reconnexion automatique
            System.out.println("Connexion established...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                createConnection();
            }
        } catch (SQLException e) {
            System.out.println("Error checking connection: " + e.getMessage());
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
