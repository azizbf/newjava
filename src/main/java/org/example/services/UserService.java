package org.example.services;

import utils.dataSource;

import java.sql.*;

public class UserService {
    
    /**
     * Gets a valid user ID from the database
     * @return Valid user ID or null if no valid user found
     * @throws SQLException if a database error occurs
     */
    public Integer getValidUserId() throws SQLException {
        String query = "SELECT id FROM user LIMIT 1";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dataSource.getInstance().getConnection();
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
            
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) dataSource.getInstance().releaseConnection(conn);
        }
    }
    
    /**
     * Gets a valid user ID or creates a new user if none exists
     * @return Valid user ID
     * @throws SQLException if a database error occurs
     */
    public Integer getOrCreateValidUserId() throws SQLException {
        Integer userId = getValidUserId();
        
        if (userId != null) {
            return userId;
        }
        
        // Vérifions d'abord la structure de la table en sélectionnant une ligne
        String checkQuery = "SELECT * FROM user LIMIT 1";
        Connection checkConn = null;
        PreparedStatement checkStmt = null;
        ResultSet checkRs = null;
        boolean hasUsernameColumn = false;
        
        try {
            checkConn = dataSource.getInstance().getConnection();
            checkStmt = checkConn.prepareStatement(checkQuery);
            checkRs = checkStmt.executeQuery();
            ResultSetMetaData metaData = checkRs.getMetaData();
            
            // Vérifier les noms de colonnes
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                if ("username".equalsIgnoreCase(columnName)) {
                    hasUsernameColumn = true;
                }
                System.out.println("Column found: " + columnName);
            }
        } catch (SQLException e) {
            System.out.println("Error checking table structure: " + e.getMessage());
        } finally {
            if (checkRs != null) try { checkRs.close(); } catch (SQLException e) { /* ignore */ }
            if (checkStmt != null) try { checkStmt.close(); } catch (SQLException e) { /* ignore */ }
            if (checkConn != null) dataSource.getInstance().releaseConnection(checkConn);
        }
        
        // No valid user found, create a new user
        String insertQuery;
        if (hasUsernameColumn) {
            insertQuery = "INSERT INTO user (username, email, password, role) VALUES (?, ?, ?, ?)";
        } else {
            insertQuery = "INSERT INTO user (email, password, role) VALUES (?, ?, ?)";
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dataSource.getInstance().getConnection();
            stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            
            if (hasUsernameColumn) {
                stmt.setString(1, "forum_user");
                stmt.setString(2, "forum_user@example.com");
                stmt.setString(3, "password"); // In a real app, this should be hashed
                stmt.setString(4, "USER");
            } else {
                stmt.setString(1, "forum_user@example.com");
                stmt.setString(2, "password"); // In a real app, this should be hashed
                stmt.setString(3, "USER");
            }
            
            stmt.executeUpdate();
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            throw new SQLException("Failed to create default user");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) dataSource.getInstance().releaseConnection(conn);
        }
    }
    
    /**
     * Gets the username for a given user ID
     * @param userId The user ID to look up
     * @return The username, or "Unknown User" if not found
     * @throws SQLException if a database error occurs
     */
    public String getUsernameById(int userId) throws SQLException {
        String query = "SELECT email FROM user WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dataSource.getInstance().getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("email");
            }
            
            return "Unknown User";
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) dataSource.getInstance().releaseConnection(conn);
        }
    }
} 