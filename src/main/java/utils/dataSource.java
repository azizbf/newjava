package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class dataSource {
    private String url="jdbc:mysql://localhost:3306/pidevversion2";
    private String username="root";
    private String pwd="";
    private Connection connection;
    private static dataSource instance;
    private dataSource(){
        try {
            connection= DriverManager.getConnection(url,username,pwd);
            System.out.println("✅ Connexion à la base de données réussie !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données !");
            throw new RuntimeException(e);
        }
    }
    public static dataSource getInstance(){
        if(instance==null)
            instance=new dataSource();
        return instance;
    }
    public Connection getConnection() {
        return connection;
    }
}