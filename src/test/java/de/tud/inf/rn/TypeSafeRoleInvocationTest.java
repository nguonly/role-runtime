package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.SchemaManager;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.role.Employee;
import de.tud.inf.rn.role.Student;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nguonly role 7/27/15.
 */
public class TypeSafeRoleInvocationTest {
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
    public void simpleInvocation(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            Person p = Player.initialize(Person.class);
            p.bind(Employee.class);
            p.bind(Student.class);

            String course = "Networking";
            String retCourse = "This student takes " + course;
            String retAddress = "Employee printAddress";

            Assert.assertEquals(retCourse, p.role(Student.class).takeCourse("Networking"));
            Assert.assertEquals(retAddress, p.role(Employee.class).getAddress());
        }
    }
}
