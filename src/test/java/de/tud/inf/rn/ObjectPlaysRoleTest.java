package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.SchemaManager;
import de.tud.inf.rn.db.orm.Relation;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.registry.DumpHelper;
import de.tud.inf.rn.registry.RegistryManager;
import de.tud.inf.rn.role.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * Created by nguonly role 7/10/15.
 */
public class ObjectPlaysRoleTest {
    @Before
    public void setupSchema(){
        RegistryManager registryManager = RegistryManager.getInstance();
        registryManager.setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager registryManager = RegistryManager.getInstance();
        registryManager.setRelations(null);
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

            RegistryManager registryManager = RegistryManager.getInstance();
            Iterator<Relation> iterator = registryManager.getRelations().iterator();

            Assert.assertTrue(iterator.next().getRoleName().contains("Student"));
            Assert.assertTrue(iterator.next().getRoleName().contains("Teacher"));
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

            RegistryManager registryManager = RegistryManager.getInstance();

            Assert.assertTrue(registryManager.getRelations().isEmpty());
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

            RegistryManager registryManager = RegistryManager.getInstance();

            Iterator<Relation> iterator = registryManager.getRelations().iterator();

            Assert.assertTrue(iterator.next().getRoleName().contains("Student"));
            Assert.assertTrue(iterator.next().getRoleName().contains("Employee"));
        }
    }

    @Test
    public void prohibitConstraint(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = new Person();
            p.bind(Employee.class).prohibit(Student.class);
            //p.prohibit(Student.class); //This is not a case

            RegistryManager registryManager = RegistryManager.getInstance();

            //DumpHelper.dumpRelation(registryManager.m_relations);
        }
    }

}
