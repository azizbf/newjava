package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class dataSource {
    private String url="jdbc:mysql://localhost:3232/pidev3";
    private String username="root";
    private String pwd="";
    private static dataSource instance;
    private final ConcurrentLinkedQueue<Connection> connectionPool = new ConcurrentLinkedQueue<>();
    private static final int INITIAL_POOL_SIZE = 5;
    private static final boolean DEBUG = true;
    
    private dataSource() {
        try {
            debug("Initializing database connection pool...");
            debug("URL: " + url);
            debug("Username: " + username);
            
            // Load JDBC Driver explicitly
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                debug("MySQL JDBC Driver loaded successfully");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ MySQL JDBC Driver not found!");
                throw new RuntimeException("MySQL JDBC Driver not found!", e);
            }
            
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                Connection conn = DriverManager.getConnection(url, username, pwd);
                connectionPool.add(conn);
                debug("Connection " + (i+1) + " created successfully");
            }
            System.out.println("✅ Connection pool initialized with " + INITIAL_POOL_SIZE + " connections!");
        } catch (SQLException e) {
            System.err.println("❌ Error initializing connection pool: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    private void debug(String message) {
        if (DEBUG) {
            System.out.println("[DB-DEBUG] " + message);
        }
    }
    
    public static dataSource getInstance() {
        if (instance == null) {
            instance = new dataSource();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        Connection connection = connectionPool.poll();
        if (connection == null || connection.isClosed()) {
            debug("Creating new database connection (pool empty or connection closed)");
            connection = DriverManager.getConnection(url, username, pwd);
        } else {
            debug("Reusing connection from pool");
        }
        return connection;
    }
    
    public void releaseConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                debug("Returning connection to pool");
                connectionPool.add(connection);
            } else {
                debug("Cannot return closed or null connection to pool");
            }
        } catch (SQLException e) {
            System.err.println("Error releasing connection: " + e.getMessage());
        }
    }
    
    public void closeAllConnections() {
        int closedCount = 0;
        Connection conn;
        debug("Closing all connections in pool");
        while ((conn = connectionPool.poll()) != null) {
            try {
                conn.close();
                closedCount++;
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        debug("Closed " + closedCount + " connections");
    }
}