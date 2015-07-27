package de.tud.inf.rn.db;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by nguonly role 7/9/15.
 */
public class SchemaManager {
    public static boolean create(){
        boolean sucessfulCreated = true; //return value

        Connection con = DBManager.getConnection();

        String sqlRole = "CREATE TABLE Role " +
                "(Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " RoleId INT NOT NULL," +
                " Name     TEXT    NOT NULL," +
                " Interface    TEXT    NOT NULL)";

        String sqlPlayer = "CREATE TABLE Player " +
                "(Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " PlayerId INT NOT NULL," +
                " Name     TEXT    NOT NULL," +
                " Interface    TEXT    NOT NULL)";

        String sqlRelation = "CREATE TABLE Relation " +
                "(Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " CompartmentId INT, " +
                " ObjectId INT NOT NULL," +
                " PlayerId INT NOT NULL," +
                " RoleId INT NOT NULL," +
                " Level INT," +
                " PlayType     INT    NOT NULL," +
                " Sequence    INTEGER," +
                " CompartmentName TEXT, " +
                " ObjectName TEXT," +
                " PlayerName TEXT," +
                " RoleName TEXT)";

        String sqlRelationView = "CREATE VIEW vRelation AS " +
                "SELECT rel.*, " +
                //"r.Name AS RoleName, " +
                "r.Interface AS RoleInterface " +
                "FROM Relation rel " +
                "INNER JOIN Role r ON rel.RoleId = r.RoleId";

        Statement stmt = null;
        try {
            stmt = con.createStatement();

            stmt.executeUpdate(sqlRole);
            stmt.executeUpdate(sqlPlayer);
            stmt.executeUpdate(sqlRelation);
            stmt.executeUpdate(sqlRelationView);

            stmt.close();

        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            sucessfulCreated = false;
        }

        return sucessfulCreated;
    }

    public static void drop(){
        Connection con = DBManager.getConnection();

        Statement stmt = null;
        try{
            stmt = con.createStatement();

            stmt.executeUpdate("DROP TABLE IF EXISTS Role;");
            stmt.executeUpdate("DROP TABLE IF EXISTS Player;");
            stmt.executeUpdate("DROP TABLE IF EXISTS Relation;");

            stmt.executeUpdate("DROP VIEW IF EXISTS vRelation;");

            stmt.close();

        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void createRelationView(){
        Connection con = DBManager.getConnection();

        Statement stmt = null;
        try{
            stmt = con.createStatement();

            String sql;

            sql = "CREATE VIEW vRelation AS " +
                    "SELECT rel.*, " +
                    //"r.Name AS RoleName, " +
                    "r.Interface AS RoleInterface " +
                    "FROM Relation rel " +
                    "INNER JOIN Role r ON rel.RoleId = r.RoleId";

            stmt.executeUpdate(sql);

            stmt.close();

        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }
}
