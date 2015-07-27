package de.tud.inf.rn.registry;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.DataManager;
import de.tud.inf.rn.db.orm.Relation;
import de.tud.inf.rn.exception.CompartmentAsPlayerInItsContextException;
import de.tud.inf.rn.exception.CompartmentNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by nguonly role 7/10/15.
 */
public class RegistryManager {
    private static RegistryManager m_registryManager;

    private static Hashtable<Integer, Object> m_objects = new Hashtable<>();
    private static Hashtable<Integer, Object> m_roles = new Hashtable<>();
    private static Hashtable<Integer, Object> m_compartments = new Hashtable<>();

    private static Deque<Integer> m_activeCompartments = new ArrayDeque<>();

    private static int m_number_level = 2*3;

    static final Logger log = LogManager.getLogger(RegistryManager.class);

    public static synchronized RegistryManager getInstance(){
        if(m_registryManager==null){
            m_registryManager = new RegistryManager();
        }

        return m_registryManager;
    }

    public <T> T initializePlayer(Class<T> player){
        try {
            T p = player.newInstance();
            m_objects.put(p.hashCode(), p);
            return p;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public <T> T initializeCompartment(Class<T> compartment){
        try {
            T p = compartment.newInstance();
            m_compartments.put(p.hashCode(), p);

            //push current active compartment
            m_activeCompartments.push(p.hashCode());

            return p;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Role bind(Compartment compartment, Player player, Class role){
        try {
            Compartment activeCompartment = compartment;
            if(compartment==null){
                //check in the compartment stacks
                if(m_activeCompartments.isEmpty()) throw new CompartmentNotFoundException();
                activeCompartment = (Compartment)m_compartments.get(m_activeCompartments.peek());
            }

            //Compartment cannot be a player inside their own compartment
            if(activeCompartment.hashCode() == player.hashCode()) throw new CompartmentAsPlayerInItsContextException();

            Object roleInstance = role.newInstance();
            log.debug("Role Id = " + roleInstance.hashCode() + " : " + roleInstance.getClass().getName());

            //Register Role's methods
            Method[] methods = role.getDeclaredMethods();
            for(Method m: methods){
                //System.out.println(m.getName());
                DataManager.insertRoleData(roleInstance.hashCode(), role.getName(), m.toString());
            }

            m_roles.put(roleInstance.hashCode(), roleInstance);

            //Register object's methods
            Class clsPlayer = player.getClass();
            methods = clsPlayer.getDeclaredMethods();
            for(Method m : methods) {
                DataManager.insertPlayerData(player.hashCode(), clsPlayer.getName(), m.getName());
            }


            //find sequence for each level
            String query="SELECT Sequence from Relation where PlayerId=" + player.hashCode() +
                    " order by Sequence desc limit 1";
            Statement st = DBManager.getConnection().createStatement();
            ResultSet rs = st.executeQuery(query);
            int seq = 0;
            if(rs.next()) seq = rs.getInt("Sequence");

            //seq++;
            int c = (int)Math.pow(10, m_number_level);
            if(seq ==0)
                seq = c;
            else {
                seq = ((seq/c) +1)*c;
            }

            //Register play relation
            String playerName = player.getClass().getName();
            Relation relation = new Relation();
            relation.compartmentId= activeCompartment.hashCode();
            relation.compartmentName= activeCompartment.getClass().getName();
            relation.objectId=player.hashCode();
            relation.objectName = playerName;
            relation.playerId = player.hashCode();
            relation.playerName = playerName;
            relation.roleId = roleInstance.hashCode();
            relation.roleName = role.getName();
            relation.level = 1;
            relation.type = 2;
            relation.sequence = seq;
            DataManager.insertRelation(relation);

            //Update ObjectId for retroactive inheritant role
            //DataManager.updateObjectRetroactiveInherit(roleInstance.hashCode(), roleInstance.hashCode());


            return (Role)roleInstance;
        }catch(Exception e) {
            //System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public void inherit(Compartment compartment, Object core, String superRole){
        Connection con = DBManager.getConnection();

        try{
            Class clsSuperRole = Class.forName(superRole);
            Object roleInstance = clsSuperRole.newInstance();

            //put into role list
            m_roles.put(roleInstance.hashCode(), roleInstance);

            String sql = "SELECT ObjectId, ObjectName FROM Relation WHERE RoleId=" + core.hashCode();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int objId = rs.getInt("ObjectId"); //a real core object
            String objName = rs.getString("ObjectName"); // a real core object name

            System.out.println("Core Id=" + objId);

            //Register Role's methods
            Method[] methods = clsSuperRole.getDeclaredMethods();
            for(Method m: methods){
                System.out.println(m.getName());
                DataManager.insertRoleData(roleInstance.hashCode(), clsSuperRole.getName(), m.getName());
            }

            int seq = 0;
            int lvl = 0;
            //check if core has previous bound role


            //find sequence for each level
            String query = "SELECT Level, Sequence from Relation where RoleId=" + core.hashCode() +
                    " order by Sequence desc limit 1";
            rs = stmt.executeQuery(query);

            if (rs.next()) {
                seq = rs.getInt("Sequence");
                lvl = rs.getInt("Level");
            }

            //seq = seq * 100 + 1;
            //lvl++;
            //c =(int)Math.pow(10, m_number_level-2*(lvl-1));
            //seq = ((seq/c) + 1)*c;


            //Register play relation
            String playerName = core.getClass().getName();
            Relation relation = new Relation();
            relation.compartmentId= compartment==null? -1 : compartment.hashCode();
            relation.compartmentName= compartment==null? "" : compartment.getClass().getName();
            relation.objectId=objId;
            relation.objectName = objName;
            relation.playerId = core.hashCode();
            relation.playerName = playerName;
            relation.roleId = roleInstance.hashCode();
            relation.roleName = superRole;
            relation.level = lvl;
            relation.type = 1;
            relation.sequence = seq;
            DataManager.insertRelation(relation);
        }catch(Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }


    public Role rolePlaysRole(Compartment compartment, Object core, Class role){
        Connection con = DBManager.getConnection();

        try{
            Compartment activeCompartment = compartment;
            if(compartment==null){
                //check in the compartment stacks
                if(m_activeCompartments.isEmpty()) throw new CompartmentNotFoundException();
                activeCompartment = (Compartment)m_compartments.get(m_activeCompartments.peek());
            }

            Object roleInstance = role.newInstance();

            m_roles.put(roleInstance.hashCode(), roleInstance);

            String sql = "SELECT ObjectId, ObjectName FROM Relation WHERE RoleId=" + core.hashCode();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int objId = rs.getInt("ObjectId"); //a real core object
            String objName = rs.getString("ObjectName"); // a real core object name

            log.debug("Core Id {}", objId);

            //Register Role's methods
            Method[] methods = role.getDeclaredMethods();
            for(Method m: methods){
                log.debug("{}.{}", role.getName(), m.getName());
                DataManager.insertRoleData(roleInstance.hashCode(), role.getName(), m.toString());
            }

            int seq = 0;
            int lvl = 0;
            //check if core has previous bound role
            String query = "SELECT Level, Sequence from Relation where PlayerId=" + core.hashCode() +
                    " and PlayType!=1 order by Sequence desc limit 1";
            rs = stmt.executeQuery(query);
            int c = 0;
            if(rs.next()){
                lvl = rs.getInt("Level");
                seq = rs.getInt("Sequence");
                c = (int)Math.pow(10, m_number_level - 2*(lvl-1));
                seq = ((seq/c)+1)*c;
            }else {

                //find sequence for each level
                query = "SELECT Level, Sequence from Relation where RoleId=" + core.hashCode() +
                        " order by Sequence desc limit 1";
                rs = stmt.executeQuery(query);

                if (rs.next()) {
                    seq = rs.getInt("Sequence");
                    lvl = rs.getInt("Level");
                }

                //seq = seq * 100 + 1;
                lvl++;
                c =(int)Math.pow(10, m_number_level-2*(lvl-1));
                seq = ((seq/c) + 1)*c;
            }

            String playerName = core.getClass().getName();
            Relation relation = new Relation();
            relation.compartmentId= activeCompartment.hashCode();
            relation.compartmentName= activeCompartment.getClass().getName();
            relation.objectId=objId;
            relation.objectName = objName;
            relation.playerId = core.hashCode();
            relation.playerName = playerName;
            relation.roleId = roleInstance.hashCode();
            relation.roleName = role.getName();
            relation.level = lvl;
            relation.type = 3;
            relation.sequence = seq;
            DataManager.insertRelation(relation);

            return (Role)roleInstance;
        }catch(Exception e) {
            log.error("Role {} cannot be bound!", role.getName());
            log.error("{} : {}", e.getClass().getName(), e.getMessage());
        }

        return null;
    }


    public <T> T playerInvokeRole(Compartment compartment, Object core, String methodName, Class<T> returnType,
                                  Class[] argumentType, Object[] argumentValue) throws RuntimeException{
        Connection con = DBManager.getConnection();

        int compartmentId = compartment==null?m_activeCompartments.peek():compartment.hashCode();
        //returnType = returnType==null? (Class<T>) void.class :returnType;
        try{
            String methodSignature = methodSignature(returnType, methodName, argumentType);

            String query = "SELECT * FROM vRelation WHERE RoleInterface LIKE(?) AND ObjectId=? AND CompartmentId=? " +
                    " ORDER BY  Sequence DESC  ,PlayType desc LIMIT 1";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, methodSignature);
            stmt.setInt(2, core.hashCode());
            stmt.setInt(3, compartmentId);
            ResultSet rs = stmt.executeQuery();
            Object invokingObject;
            if(rs.next()){
                int roleId = rs.getInt("RoleId");
                invokingObject = m_roles.get(roleId);
            }else{
                //Should check our own methods to be invoked
                log.debug("Method {} was not found in roles of {}", methodName, core.getClass().getName());
                log.debug("Now starts looking role the core's methods");
                invokingObject = core;
            }
            //MethodHandle methodHandle =  MethodHandles.lookup().findVirtual(invokingObject.getClass(), methodName, MethodType.methodType(returnType, argumentType));
            //Object objRet = methodHandle.invokeExact(invokingObject, argumentValue);
            Method method = invokingObject.getClass().getMethod(methodName,argumentType);
            Object objRet = method.invoke(invokingObject, argumentValue);
            if(returnType!=null && !returnType.isAssignableFrom(void.class) && !returnType.isAssignableFrom(Void.class)) {
                if(returnType.isPrimitive()){
                    return (T)objRet;
                }
                return returnType.cast(objRet);
            }

        }catch(SQLException e) {
            //System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }  catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
            throw new RuntimeException();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return null;
    }

    public <T> T roleInvokeRole(Role core, String methodName, Class<T> returnType, Class[] argumentType, Object[] argumentValue){
        Connection con = DBManager.getConnection();

        try{
            String methodSignature = methodSignature(returnType, methodName, argumentType);

            String query = "";
            query += "WITH RECURSIVE player_id( i )                            ";
            query += "AS (                                                     ";
            query += "     VALUES(?)                                           ";
            query += "     UNION                                               ";
            query += "     SELECT RoleId FROM Relation, player_id              ";
            query += "     WHERE Relation.PlayerId = player_id.i               ";
            query += ")                                                        ";
            query += "SELECT * FROM vRelation WHERE RoleInterface LIKE(?)      ";
            query += "AND PlayerId IN player_id                                ";
            query += "ORDER BY  Sequence DESC, PlayType DESC LIMIT 1;          ";

            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, core.hashCode());
            stmt.setString(2, methodSignature);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                int roleId = rs.getInt("RoleId");
                Object role = m_roles.get(roleId);
                //Class[] argTypes = new Class[] { String.class };
                Method method = role.getClass().getMethod(methodName, argumentType);
                Object objRet = method.invoke(role, argumentValue);
                if(returnType!=null && !returnType.isAssignableFrom(void.class) && !returnType.isAssignableFrom(Void.class)) {
                    if(returnType.isPrimitive()){
                        return (T)objRet;
                    }
                    return returnType.cast(objRet);
                }
            }else{
                log.error("The {} method was not found", methodName);
            }
        }catch(Exception e) {
            log.error("Database error: {} : {}", e.getClass().getName(), e.getMessage());
        }

        return null;
    }


    /**
     * Invoke based method. It can be either root player or role player
     * @param role Current role
     * @param methodName Method Name role Base
     * @param argumentTypes Type of parameter
     * @param argumentValues Value of parameter
     * @return Object
     */
    public <T> T invokeBase(Role role, String methodName, Class<T> returnType, Class[] argumentTypes, Object[] argumentValues) {
        Connection con = DBManager.getConnection();
        //Check if base is root player (Player) or role player (Role)
        String query = "SELECT * FROM Relation WHERE RoleId=?";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, role.hashCode());
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                Object base;
                if(rs.getInt("ObjectId") == rs.getInt("PlayerId")){
                    //It's root player (Player)
                    base = m_objects.get(rs.getInt("ObjectId"));
                }else{
                    //It's role player (Role)
                    base = m_roles.get(rs.getInt("PlayerId"));
                }
                Method method = base.getClass().getMethod(methodName, argumentTypes);
                Object objRet = method.invoke(base, argumentValues);
                if(returnType!=null && !returnType.isAssignableFrom(void.class) && !returnType.isAssignableFrom(Void.class)) {
                    if(returnType.isPrimitive()){
                        return (T)objRet;
                    }
                    return returnType.cast(objRet);
                }
            }else{
                log.error("{}.{} was not found", role.getClass().getName(), methodName);
            }

        } catch (SQLException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T invokeCompartment(Compartment compartment, boolean isPlayer, Object core, String methodName,
                                   Class<T> returnType, Class[] argumentTypes, Object[] argumentValues){
        //Connection con = DBManager.getConnection();
        try{
            if(m_activeCompartments.isEmpty()) throw new CompartmentNotFoundException();
            int compartmentId = compartment==null?m_activeCompartments.peek():compartment.hashCode();
            //String comp = compartment==null?"":String.format("CompartmentId=%s AND", compartment.hashCode());
//            String query = "SELECT * FROM Relation WHERE CompartmentId=?" + (isPlayer?"ObjectId=?":"RoleId=?");
//            PreparedStatement stmt = con.prepareStatement(query);
//            stmt.setInt(1, compartmentId);
//            stmt.setInt(2, core.hashCode());
//            ResultSet rs = stmt.executeQuery();
//
//            if(rs.next()){
                Object objCompartment = compartment;
                if(compartment==null){
                    //objCompartment = m_compartments.get(rs.getInt("CompartmentId"));
                    objCompartment = m_compartments.get(compartmentId);
                }
                Method method = objCompartment.getClass().getMethod(methodName, argumentTypes);
            Object objRet = method.invoke(objCompartment, argumentValues);
                if(returnType!=null && !returnType.isAssignableFrom(void.class) && !returnType.isAssignableFrom(Void.class)) {
                    if(returnType.isPrimitive()){
                        return (T)objRet;
                    }
                    return returnType.cast(objRet);
                }
//            }else{
//                log.debug("No method found");
//            }
        }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | CompartmentNotFoundException e){
            e.printStackTrace();
        }

        return null;
    }


    public void unbind(Object core, Class role){
        if(log.isDebugEnabled()) {
            log.debug("Before deleting roles in Hashtable");
            Enumeration<Integer> m = m_roles.keys();
            while (m.hasMoreElements()) {
                log.debug(m.nextElement());
            }
        }
        //Get role Id
        int roleId = DataManager.getIdByName(core.hashCode(), "Role", role.getName());
        DataManager.deleteRoleRecord(m_roles, roleId);

        //test whether roles are removed from m_roles hashtable
        if(log.isDebugEnabled()) {
            log.debug("---------- After deleting roles from both Database and hashtable");
            Enumeration<Integer> m = m_roles.keys();
            while (m.hasMoreElements()) {
                log.debug(m.nextElement());
            }
        }
    }

    /**
     * Unbind all the bound roles from a root player
     * @param root a root player
     */
    public void unbindAll(Object root){
        Connection con = DBManager.getConnection();
        int rootId = root.hashCode();
        try{
            /**
             * Role Id = -1 is prohibit relation that require no initialization of role
             */
            String query = "SELECT * FROM Relation WHERE ObjectId=" + rootId + " AND RoleId!=-1";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                m_roles.remove(rs.getInt("RoleId"));
            }

            String sql = "DELETE FROM Relation WHERE ObjectId=" + rootId;
            PreparedStatement preparedStmt = con.prepareStatement(sql);
            preparedStmt.executeUpdate();

        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    //Role Constraints

    /**
     * This is the prohibit constraint placing role roles. However, it can also be applied to root object as well.
     * @param core a Player either (root object or role)
     * @param role a prohibited role
     */

    public void prohibit(Compartment compartment, Object core, Class role){
        Connection con = DBManager.getConnection();
        int coreId = core.hashCode();
        try {
            String sql = "SELECT ObjectId, ObjectName FROM Relation WHERE PlayerId=" + coreId;
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int objId = rs.getInt("ObjectId"); //a real core object
            String objName = rs.getString("ObjectName"); // a real core object name

            int seq = 0;
            int lvl = 0;
            //check if core has previous bound role
            String query = "SELECT Level, Sequence from Relation where PlayerId=" + coreId +
                    " and PlayType!=1 order by Sequence desc limit 1";
            rs = stmt.executeQuery(query);
            int c = 0;
            if(rs.next()){
                lvl = rs.getInt("Level");
                seq = rs.getInt("Sequence");
                c = (int)Math.pow(10, m_number_level - 2*(lvl-1));
                seq = ((seq/c)+1)*c;
            }else {

                //find sequence for each level
                query = "SELECT Level, Sequence from Relation where RoleId=" + core.hashCode() +
                        " order by Sequence desc limit 1";
                rs = stmt.executeQuery(query);

                if (rs.next()) {
                    seq = rs.getInt("Sequence");
                    lvl = rs.getInt("Level");
                }

                //seq = seq * 100 + 1;
                lvl++;
                c =(int)Math.pow(10, m_number_level-2*(lvl-1));
                seq = ((seq/c) + 1)*c;
            }

            String playerName = core.getClass().getName();
            Relation relation = new Relation();
            relation.compartmentId= compartment==null? -1 : compartment.hashCode();
            relation.compartmentName= compartment==null? "" : compartment.getClass().getName();
            relation.objectId=objId;
            relation.objectName = objName;
            relation.playerId = core.hashCode();
            relation.playerName = playerName;
            relation.roleId = -1; //no role instance
            relation.roleName = role.getName();
            relation.level = lvl;
            relation.type = 4;
            relation.sequence = seq;
            DataManager.insertRelation(relation);
        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Transfer role instance from one to another player. It's also possible to transfer to different compartment.
     * @param role
     * @param from
     * @param to
     * @param toCompartment
     */

    public void transfer(Class role, Object from, Object to, Compartment toCompartment){
        int fromObjId = from.hashCode();
        int toObjId = to.hashCode();
        int compartmentId = toCompartment==null? -1: toCompartment.hashCode();
        int roleId = -1;
        String roleName = "";
        int fromCompartmentId = -1;

        Relation relation = new Relation();

        Connection con = DBManager.getConnection();
        String query;
        Statement stmt;

        //Get role
        try{
            query = "SELECT * FROM Relation WHERE PlayerId=" + fromObjId +
                    " AND RoleName='" + role.getCanonicalName() + "'";
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()){
                roleId=rs.getInt("RoleId");
                roleName = rs.getString("RoleName");
                fromCompartmentId = rs.getInt("CompartmentId");
            }else{
                log.warn("::: How possible? :::");
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        query = "SELECT * FROM Relation WHERE PlayerId=" + toObjId +
                (toCompartment==null?"" : " AND CompartmentId=" + compartmentId) +
                " ORDER BY Sequence DESC";
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                relation.compartmentId = rs.getInt("CompartmentId");
                relation.compartmentName = rs.getString("CompartmentName");
                relation.objectId = rs.getInt("ObjectId");
                relation.objectName = rs.getString("ObjectName");
                relation.playerId = rs.getInt("PlayerId");
                relation.playerName = rs.getString("PlayerName");
                relation.roleId = roleId;
                relation.roleName = roleName;
                relation.level = 1;
                relation.type = 2;

                int seq = rs.getInt("Sequence");
                //seq++;
                int c = (int) Math.pow(10, m_number_level);
                if (seq == 0)
                    seq = c;
                else {
                    seq = ((seq / c) + 1) * c;
                }
                relation.sequence = seq;
            }else{
                //no previous role relation
                relation.compartmentId = toCompartment==null? -1 : compartmentId;
                relation.compartmentName = toCompartment==null? "" : toCompartment.getClass().getName();
                relation.objectId = toObjId;
                relation.objectName = to.getClass().getName();
                relation.playerId = toObjId;
                relation.playerName = to.getClass().getName();
                relation.roleId = roleId;
                relation.roleName = roleName;
                relation.level = 1;
                relation.type = 2;
                relation.sequence = (int) Math.pow(10, m_number_level);
            }

            //Persist relation to database
            DataManager.insertRelation(relation);

            //Delete role instance from previous player
            String sql = "DELETE FROM Relation Where CompartmentId=" + fromCompartmentId +
                    " AND PlayerId=" + fromObjId + " AND RoleId=" + roleId;
            //System.out.println(sql);
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void destroyActiveCompartment(Compartment compartment){
        Connection con = DBManager.getConnection();
        String sql = "DELETE FROM Relation WHERE CompartmentId=?";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, compartment.hashCode());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Pop out from active compartment stack
        if(!m_activeCompartments.isEmpty()){
            m_activeCompartments.pop();
        }else{
            log.debug("Active Compartment is Empty");
        }
        m_compartments.remove(compartment.hashCode());
    }

    private String methodSignature(Class returnType, String methodName, Class[] clazzes){
        StringBuilder sb = new StringBuilder();
        sb.append("%").append(returnType==null?"":" " + returnType.getName());
        sb.append(" %.").append(methodName).append("(");

        if(clazzes!=null) {
            for (int i = 0; i < clazzes.length; i++) {
                sb.append(clazzes[i].getName());
                if (i < clazzes.length - 1) sb.append(",");
            }
        }
        sb.append(")");

        return sb.toString();
    }

    public <T> T role(Compartment compartment, Class<T> roleClass){
        Connection con = DBManager.getConnection();
        int compartmentId = compartment==null?m_activeCompartments.peek():compartment.hashCode();
        String query = "SELECT * FROM Relation WHERE CompartmentId=? AND RoleName=?";
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, compartmentId);
            pstmt.setString(2, roleClass.getName());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                Object obj = m_roles.get(rs.getInt("RoleId"));
                return roleClass.cast(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T base(Compartment compartment, Role role, Class<T> baseClass){
        Connection con = DBManager.getConnection();
        int compartmentId = compartment==null?m_activeCompartments.peek():compartment.hashCode();
        //Check if base is root player (Player) or role player (Role)
        String query = "SELECT * FROM Relation WHERE CompartmentId=? AND RoleId=?";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, compartmentId);
            stmt.setInt(2, role.hashCode());
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                Object base;
                if(rs.getInt("ObjectId") == rs.getInt("PlayerId")){
                    //It's root player (Player)
                    base = m_objects.get(rs.getInt("ObjectId"));
                }else{
                    //It's role player (Role)
                    base = m_roles.get(rs.getInt("PlayerId"));
                }
                return baseClass.cast(base);
//                Method method = base.getClass().getMethod(methodName, argumentTypes);
//                Object objRet = method.invoke(base, argumentValues);
//                if(returnType!=null && !returnType.isAssignableFrom(void.class) && !returnType.isAssignableFrom(Void.class)) {
//                    if(returnType.isPrimitive()){
//                        return (T)objRet;
//                    }
//                    return returnType.cast(objRet);
//                }
            }else{
                log.error("{} was not found", role.getClass().getName());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T compartment(Compartment compartment, Class<T> compartmentClass){
        try{
            Object objCompartment = compartment;
            if(compartment==null){
                if(m_activeCompartments.isEmpty()) throw new CompartmentNotFoundException();
                int compartmentId = compartment==null?m_activeCompartments.peek():compartment.hashCode();
                objCompartment = m_compartments.get(compartmentId);
            }
            return compartmentClass.cast(objCompartment);
        }catch(CompartmentNotFoundException e){
            e.printStackTrace();
        }

        return null;
    }
}
