package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

/**
 * Created by nguonly role 7/10/15.
 */
public class RoleDependOnCompartmentTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
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
        //try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Faculty faculty = new Faculty();

            RegistryManager registryManager = RegistryManager.getInstance();
            Iterator<Relation> iterator = registryManager.getRelations().iterator();
            Assert.assertTrue(iterator.next().getCompartmentId()>0);
            Assert.assertTrue(iterator.next().getRoleName().contains("Employee"));
        //}
    }
}
