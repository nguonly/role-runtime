package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.SchemaManager;
import de.tud.inf.rn.db.orm.Relation;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.registry.RegistryManager;
import de.tud.inf.rn.role.Employee;
import de.tud.inf.rn.role.Student;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by nguonly role 7/27/15.
 */
public class TypeSafeRoleInvocationTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
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

    @Test
    public void testCollection(){
        Collection<Relation> relations = new ArrayList<>();
        Deque<Relation> deque = new ArrayDeque<>();

        //relations.stream().filter()
        HashMap<Integer, Relation> map = new HashMap<>();

    }
}
