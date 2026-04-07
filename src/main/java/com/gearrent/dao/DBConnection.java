package com.gearrent.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection.
 * Reads connection details from /resources/db.properties.
 */
public class DBConnection {

    private static Connection connection;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Properties props = new Properties();
                try (InputStream in = DBConnection.class
                        .getResourceAsStream("/db.properties")) {
                    if (in != null) {
                        props.load(in);
                    }
                }
                String url      = props.getProperty("db.url",      "jdbc:mysql://localhost:3306/gearrent_pro?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                String user     = props.getProperty("db.username", "root");
                String password = props.getProperty("db.password", "root");

                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (Exception e) {
            throw new SQLException("Cannot connect to database: " + e.getMessage(), e);
        }
        return connection;
    }

    /** Call this to start a transaction manually. */
    public static void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }

    public static void commit() throws SQLException {
        getConnection().commit();
        getConnection().setAutoCommit(true);
    }

    public static void rollback() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
                connection.setAutoCommit(true);
            }
        } catch (SQLException ignored) {}
    }
}
