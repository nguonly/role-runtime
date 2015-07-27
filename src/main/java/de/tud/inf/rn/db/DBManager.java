package de.tud.inf.rn.db;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by nguonly role 7/9/15.
 */
public class DBManager {
    private static Connection m_connection;

    public static synchronized Connection getConnection() {
        if(m_connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");

                SQLiteConfig config = new SQLiteConfig();
                config.setSharedCache(true);
                config.enableRecursiveTriggers(true);
                //m_connection = DriverManager.getConnection("jdbc:sqlite::memory:", config.toProperties());
                //m_connection = DriverManager.getConnection("jdbc:sqlite::memory:");
                m_connection = DriverManager.getConnection("jdbc:sqlite:role.db");
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        }

        return m_connection;
    }

    public static synchronized void close(){
        try {
            m_connection.close();
            m_connection=null;
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
