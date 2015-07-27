package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.SchemaManager;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.role.*;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by nguonly role 7/10/15.
 */
public class CompartmentPlaysRoleTest {
    @Before
    public void setupSchema(){
        SchemaManager.drop();
        SchemaManager.create();
    }

    @After
    public void destroyDBConnection(){
        DBManager.close();
    }

    /**
     * Prepare data for testing. It's all about compartment.
     */
    public static class Faculty extends Compartment {
        public void activate(){
            Person p = new Person();
            p.bind(this, Student.class).bind(this, TeachingAssistant.class);
            p.bind(this, Employee.class);
        }
    }

     @Test
    public void compartmentPlaysRoles(){
         Connection con = DBManager.getConnection();

         //compartment as a context
         try (Faculty faculty = Compartment.initialize(Faculty.class)) {
             faculty.activate();

             String sql = "SELECT Count(Id) FROM Relation WHERE CompartmentId=%s ORDER BY Id";
             String query = String.format(sql, faculty.hashCode());
             try {
                 //Assert roles being played inside a compartment
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(query);
                 Assert.assertEquals(3, rs.getInt(1));
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }

         //Compartment plays role
         try (Compartment comp = Compartment.initialize(Compartment.class)) {
             Faculty faculty = Player.initialize(Faculty.class);
             faculty.bind(Sponsor.class);

             try {
                 Statement stmt = con.createStatement();

                 //assert compartment plays role
                 String query = String.format("SELECT * FROM Relation WHERE CompartmentId=%s ORDER BY Id", comp.hashCode());
                 ResultSet rs = stmt.executeQuery(query);
                 rs.next();
                 Assert.assertTrue(rs.getString("RoleName").contains("Sponsor"));
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }

    }

    public static class University extends Compartment{
        public void activate(){
            Faculty faculty = new Faculty();
            faculty.activate();

            faculty.bind(this, Sponsor.class);
        }
    }

    public static class Germany extends Compartment{
        public void activate(){
            University tuDresden = new University();
            tuDresden.activate();

            tuDresden.bind(this, Competitor.class);
        }
    }

    @Test
    public void multiLevelCoarseGrained(){
        try(Germany germany = Compartment.initialize(Germany.class)) {
            germany.activate();

            //play roles in an anonymous compartment
            germany.bind(EUMember.class);

            Connection con = DBManager.getConnection();
            String query = "SELECT Count(CompartmentId) FROM (SELECT CompartmentId FROM Relation WHERE CompartmentId>0 GROUP BY CompartmentId)";
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                //Assert number of explicit compartments
                Assert.assertEquals(3, rs.getInt(1));

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
