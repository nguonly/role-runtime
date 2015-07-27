package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.SchemaManager;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.role.Employee;
import de.tud.inf.rn.role.Student;
import de.tud.inf.rn.role.SysAdmin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by nguonly role 7/10/15.
 */
public class RoleTransferTest {
    @Before
    public void setupSchema(){
        SchemaManager.drop();
        SchemaManager.create();
    }

    @After
    public void destroyDBConnection(){
        DBManager.close();
    }

    @Test
    public void transferRoleInAnonymousCompartment(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person alice = Player.initialize(Person.class);
            Person bob = Player.initialize(Person.class);

            alice.bind(Employee.class);
            Role sysAdmin = alice.bind(SysAdmin.class);

            bob.bind(Employee.class);

            Connection con = DBManager.getConnection();
            String query = "SELECT * FROM Relation Where ObjectId=" + alice.hashCode() +
                    " AND RoleName LIKE('%SysAdmin')";

            //Assert that SysAdmin role has been bound to Alice
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                assertEquals(rs.getInt("RoleId"), sysAdmin.hashCode());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //Transfer role
            alice.transfer(SysAdmin.class, bob);

            query = "SELECT * FROM Relation Where ObjectId=" + bob.hashCode() +
                    " AND RoleName LIKE('%SysAdmin')";

            //Assert that role instance has been transferred to bob
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                assertEquals(sysAdmin.hashCode(), rs.getInt("RoleId"));
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //Assert that SysAdmin role has been deleted from alice
            query = "SELECT * FROM Relation Where ObjectId=" + alice.hashCode() +
                    " AND RoleName LIKE('%SysAdmin')";
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                assertEquals(false, rs.next());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Test role transferring in the same compartment
     */

    class Company extends Compartment {
        public Person alice = new Person();
        public Person bob = new Person();

        Company(){
            alice.bind(this, Employee.class);
            alice.bind(this, SysAdmin.class);

            bob.bind(this, Employee.class);
        }

        public void transfer(){
            alice.transfer(SysAdmin.class, bob);
        }
    }


    @Test
    public void transferRoleWithInACompartment(){
        Company company = new Company();

        company.transfer();

        //Assert that SysAdmin role instance has been transferred
        Connection con = DBManager.getConnection();
        String query = "SELECT * FROM Relation WHERE PlayerId=" + company.bob.hashCode() +
                " AND RoleName LIKE('%SysAdmin')";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            assertTrue(rs.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    /**
     * Test role transferring in different compartment
     */

    Person m_alice = new Person();

    class Faculty extends Compartment{

        public void activate(){
            m_alice.bind(this, Employee.class);
            m_alice.bind(this, Student.class);
        }
    }

    class Mensa extends Compartment{
        public void activate(){
            m_alice.transfer(Student.class, m_alice, this);
        }
    }

    @Test
    public void transferRoleInDifferentCompartments(){
        Faculty faculty = new Faculty();
        faculty.activate();

        Mensa alteMensa = new Mensa();
        alteMensa.activate();

        //Assert that alice has Student role instance in Mensa compartment
        Connection con = DBManager.getConnection();
        String query = "SELECT * FROM Relation WHERE PlayerId=" + m_alice.hashCode() +
                " AND CompartmentId=" + alteMensa.hashCode() +
                " AND RoleName LIKE('%Student')";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            assertTrue(rs.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


