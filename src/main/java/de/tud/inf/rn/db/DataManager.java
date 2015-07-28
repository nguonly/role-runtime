package de.tud.inf.rn.db;

import java.sql.*;
import java.util.HashMap;
import java.util.Hashtable;

import de.tud.inf.rn.db.orm.Relation;

/**
 * Created by nguonly role 7/10/15.
 */
public class DataManager {
    public static void insertPlayerRoleData(String table, int objId, String name, String method){
        Connection con = DBManager.getConnection();

        try{
            String prop = table.equalsIgnoreCase("Role")?"RoleId":"PlayerId";
            String sql = "INSERT INTO " + table + " ("+prop+ ", Name, Interface) VALUES(?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, objId);
            stmt.setString(2, name);
            stmt.setString(3, method);

            stmt.executeUpdate();
        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void insertRoleData(int roleId, String name, String method){
        insertPlayerRoleData("Role", roleId, name, method);
    }

    public static void insertPlayerData(int playerId, String name, String method){
        insertPlayerRoleData("Player", playerId, name, method);
    }

    public static void insertRelation(Relation relation){
        Connection con = DBManager.getConnection();

        try{

            String sql = "INSERT INTO Relation (CompartmentId, ObjectId, PlayerId, RoleId, Level, " +
                    " PlayType, Sequence, CompartmentName, ObjectName, PlayerName, RoleName) " +
                    " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, relation.compartmentId);
            stmt.setInt(2, relation.objectId);
            stmt.setInt(3, relation.playerId);
            stmt.setInt(4, relation.roleId);
            stmt.setInt(5, relation.level);
            stmt.setInt(6, relation.type);
            stmt.setInt(7, relation.sequence);
            stmt.setString(8, relation.compartmentName);
            stmt.setString(9, relation.objectName);
            stmt.setString(10, relation.playerName);
            stmt.setString(11, relation.roleName);

            stmt.executeUpdate();
        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void updateObjectRetroactiveInherit(Object core, Object superRole){
        Connection con = DBManager.getConnection();
        System.out.println("upd object Id = " + core.hashCode());
        try{
            String sql = "SELECT * FROM vRelation WHERE RoleId=" + core.hashCode();
            sql += " limit 1";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            int objectId = 0;
            while(rs.next()) {
                objectId = rs.getInt("ObjectId");
                System.out.println("ObjectId = " + objectId);
            }

            sql = "UPDATE";

        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void deleteRoleRecord(HashMap<Integer, Object> roles, int roleId){
        Connection con = DBManager.getConnection();
        try{
            String query = "SELECT * FROM Relation WHERE PlayerId=" + roleId;


            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);


            while(rs.next()){
                //int roleId = rs.getInt("RoleId");
                deleteRoleRecord(roles, rs.getInt("RoleId")); //Recursive call
            }

            String sql = "DELETE FROM Relation WHERE RoleId=" + roleId;
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.executeUpdate();

            roles.remove(roleId);
        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    //Type is Object, Player, Role
    public static int getIdByName(int rootId, String type, String name){
        Connection con = DBManager.getConnection();
        int id = 0;
        try{
            String query = String.format("SELECT * FROM Relation WHERE PlayerId=%d AND %sName='%s'", rootId, type, name);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                id = rs.getInt(String.format("%sId", type));
            }
        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return id;
    }

    public static void transfer(int roleId, int fromId, int toId){
        Connection con = DBManager.getConnection();
        String query = "SELECT Count(Id) FROM Relation WHERE PlayerId=" + toId;
        String sql;
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            if(rs.getInt(1)>0){
                //perform update roleId
                sql = "UPDATE Relation SET RoleId=" + roleId + " WHERE PlayerId=" + toId;
            }else{
                //perform insert transferable roleId
                sql = "INSERT ";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
