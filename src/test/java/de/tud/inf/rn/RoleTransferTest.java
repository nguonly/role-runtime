package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.db.orm.Relation;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.registry.DumpHelper;
import de.tud.inf.rn.registry.RegistryManager;
import de.tud.inf.rn.registry.StatisticsHelper;
import de.tud.inf.rn.role.Employee;
import de.tud.inf.rn.role.Student;
import de.tud.inf.rn.role.SysAdmin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by nguonly role 7/10/15.
 */
public class RoleTransferTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    @Test
    public void transferRoleInAnonymousCompartment(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person alice = Player.initialize(Person.class);
            Person bob = Player.initialize(Person.class);

            alice.bind(Employee.class);
            Role sysAdmin = alice.bind(SysAdmin.class);

            bob.bind(Employee.class);

            RegistryManager registryManager = RegistryManager.getInstance();
            Optional<Relation> sysAdminRel = registryManager.m_relations.stream()
                    .filter(r -> r.objectId == alice.hashCode() && r.roleName.equals(sysAdmin.getClass().getName()))
                    .findFirst();
            assertEquals(sysAdminRel.get().roleId, sysAdmin.hashCode());

            //Transfer role
            alice.transfer(SysAdmin.class, bob);

            sysAdminRel = registryManager.m_relations.stream()
                    .filter(r -> r.objectId == bob.hashCode() && r.roleName.equals(sysAdmin.getClass().getName()))
                    .findFirst();
            assertEquals(sysAdminRel.get().roleId, sysAdmin.hashCode());

            sysAdminRel = registryManager.m_relations.stream()
                    .filter(r -> r.objectId == alice.hashCode() && r.roleName.equals(sysAdmin.getClass().getName()))
                    .findFirst();
            assertTrue(!sysAdminRel.isPresent());
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
            alice.transfer(SysAdmin.class, this, bob, this);
        }
    }


    @Test
    public void transferRoleWithInACompartment(){
        Company company = new Company();

        company.transfer();

        RegistryManager registryManager = RegistryManager.getInstance();

        Optional<Relation> sysAdminRel = registryManager.getRelations().stream()
                .filter(r -> r.playerId == company.bob.hashCode()
                        && r.roleName.matches(".*SysAdmin"))
                .findFirst();
        assertTrue(sysAdminRel.isPresent());

        //Assert that SysAdmin role instance has been transferred

    }




    /**
     * Test role transferring in different compartment
     */

    Person m_alice = new Person();

    class Faculty extends Compartment{

        public void configureBinding(){
            m_alice.bind(this, Employee.class);
            m_alice.bind(this, Student.class);
        }
    }

    public static class Mensa extends Compartment{

    }

    @Test
    public void transferRoleInDifferentCompartments() {
        Faculty faculty = new Faculty();
        faculty.configureBinding();

        try (Mensa alteMensa = Compartment.initialize(Mensa.class)) {
            m_alice.transfer(Student.class, faculty, m_alice, alteMensa);

            RegistryManager registryManager = RegistryManager.getInstance();
            Optional<Relation> studentRel = registryManager.m_relations.stream()
                    .filter(r -> r.playerId == m_alice.hashCode() && r.compartmentId == alteMensa.hashCode()
                            && r.roleName.matches(".*Student"))
                    .findFirst();

            assertTrue(studentRel.isPresent());
        }

    }

    public static class A extends Role{
        public String whatName(){
            //invoke method role other roles being in the play line
            return invoke("getName", String.class);
        }

        public String getName(){
            return "A";
        }

        public String callBase(){
            return invokeBase("getName", String.class);
        }
    }

    public static class B extends Role{
        public String getName(){
            return "B";
        }

        //Invoke Base for roleInvokeBaseWhichIsRole
        public String callBase(){
            return invokeBase("getName", String.class);
        }
    }

    public static class C extends Role{
        public String getName(){
            return "C";
        }
    }

    public static class D extends Role{
        public String getName(){
            return "D";
        }
    }

    public static class E extends Role{
        public String me(){
            return "E";
        }

//        public String getName(){
//            return "E";
//        }
    }

    public static class F extends Role{
        public String getName() { return "F";}
    }

    @Test
    public void checkSequenceAndLevelAfterTransferring(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            /*
            alice--->A---->B----->E
                      \--->D----->C
             */
            Person alice = Player.initialize(Person.class);

            Role a = alice.bind(A.class);
            a.bind(B.class).bind(E.class);
            Role d = a.bind(D.class);
            d.bind(C.class);

            RegistryManager registryManager = RegistryManager.getInstance();
            //DumpHelper.dumpRelation(registryManager.getRelations());

            Person bob = Player.initialize(Person.class);
            bob.bind(F.class);
            //bob.bind(A.class).bind(B.class).bind(C.class);
            alice.transfer(A.class, bob);

//            System.out.println("After transfer");
//            DumpHelper.dumpRelation(registryManager.getRelations());

            //DumpHelper.printTree(registryManager.getRelations(), comp);

            //Assert that no role on previous player alice
            Assert.assertEquals(0, StatisticsHelper.rolesCount(alice.hashCode()));

            //Assert that there 6 roles (including F.class) on bob
            Assert.assertEquals(6, StatisticsHelper.rolesCount(bob.hashCode()));

            //Assert A.class has 4 roles under its relation
            Assert.assertEquals(4, StatisticsHelper.rolesCount(a.hashCode()));

        }
    }
}


