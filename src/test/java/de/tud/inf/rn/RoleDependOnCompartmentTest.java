package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.SchemaManager;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.role.Employee;
import de.tud.inf.rn.role.Student;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertTrue;

/**
 * Created by nguonly role 7/10/15.
 */
public class RoleDependOnCompartmentTest {
    @Before
    public void setupSchema(){
        SchemaManager.drop();
        SchemaManager.create();
    }

    @After
    public void destroyDBConnection(){
        DBManager.close();
    }

    class Faculty extends Compartment {
        Faculty(){
            Person p = new Person();
            p.bind(this, Student.class);
            p.bind(this, Employee.class);
        }
    }

    @Test
    public void roleDependsOnCompartment(){
        Faculty faculty = new Faculty();

        Connection con = DBManager.getConnection();
        String query = "SELECT * FROM Relation";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            assertTrue(rs.getInt("CompartmentId") > 0);
            rs.next();
            assertTrue(rs.getString("RoleName").contains("Employee"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
