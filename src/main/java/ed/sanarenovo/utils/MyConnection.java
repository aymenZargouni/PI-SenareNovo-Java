package ed.sanarenovo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private String url = "jdbc:mysql://localhost:3306/senarenovojavapi";
    private String login = "root";
    private String password = "";

    private Connection cnx;
    private static MyConnection instance;

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, login, password);
            System.out.println("Connection successful");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }
}
