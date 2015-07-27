package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.SchemaManager;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.role.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by nguonly role 7/10/15.
 */
public class ObjectPlaysRoleTest {
    @Before
    public void setupSchema(){
        SchemaManager.drop();
        SchemaManager.create();
    }

    @After
    public void destroyDBConnection(){
        DBManager.close();
    }

    public static class CompartmentA extends Compartment{
        public void bind(){

        }
    }

    @Test
    public void objectPlaysRole(){
        try(CompartmentA comp = Compartment.initialize(CompartmentA.class)){
            Person p = Player.initialize(Person.class);
            Role emp = p.bind(comp, Employee.class);
            emp.bind(comp, SalePerson.class);

            String item = "Coffee";
            int quantity = 40;
            String retStr = p.invoke("sale", String.class, new Class[]{String.class, int.class}, new Object[]{item, quantity});
            Assert.assertEquals(String.format("Sale %s with %d quantities", item, quantity), retStr);

            String ret = p.invoke("getAddress", String.class);
            Assert.assertEquals("Employee printAddress", ret);
        }
    }

    @Test
    public void unbindRoleFromObject(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = new Person();
            p.bind(Employee.class).bind(SysAdmin.class);
            p.bind(Student.class);
            p.bind(Teacher.class);

            p.unbind(Employee.class);

            try {
                Connection con = DBManager.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Relation");
                rs.next();
                Assert.assertEquals(Student.class.getCanonicalName(), rs.getString("RoleName"));
                rs.next();
                Assert.assertEquals(Teacher.class.getCanonicalName(), rs.getString("RoleName"));
            } catch (Exception e) {

            }
        }
    }

    @Test
    public void unbindAll(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = new Person();
            p.bind(Employee.class).bind(SysAdmin.class);
            p.bind(Student.class);
            p.bind(Teacher.class);

            p.unbindAll();

            try {
                Connection con = DBManager.getConnection();
                String query = "SELECT * FROM Relation FROM ObjectId=" + p.hashCode();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                Assert.assertEquals(false, rs.next());

            } catch (Exception e) {
            }
        }
    }

    @Test
    public void rebind(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = new Person();
            p.bind(Employee.class).bind(TeamLeader.class);
            p.bind(Student.class);
            p.unbind(Employee.class);
            p.bind(Employee.class);

            Connection con = DBManager.getConnection();
            String query = "SELECT * FROM Relation ORDER BY Id";
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                System.out.println(rs.getString("RoleName"));
                Assert.assertTrue(rs.getString("RoleName").contains("Student"));
                rs.next();
                Assert.assertTrue(rs.getString("RoleName").contains("Employee"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void prohibitConstraint(){
        Person p = new Person();
        p.bind(Employee.class);
        p.prohibit(Student.class);

        try{
            Connection con = DBManager.getConnection();
            Statement stmt = con.createStatement();
            String query = "SELECT * FROM Relation WHERE RoldId=-1 AND RoleName LIKE('%Student')";
            ResultSet rs = stmt.executeQuery(query);
            Assert.assertEquals(true, rs.next());
        }catch(Exception e){

        }
    }

}
