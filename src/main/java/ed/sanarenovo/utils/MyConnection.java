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

    public MyConnection(){
        try {
            cnx =  DriverManager.getConnection(url,login,pwd);
            System.out.println("Connexion established...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public static MyConnection getInstance(){
        if(instance == null){
            instance = new MyConnection();
        }
        return instance;
    }
}
