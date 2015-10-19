package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.SchemaManager;
import de.tud.inf.rn.db.orm.Relation;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.registry.DumpHelper;
import de.tud.inf.rn.registry.RegistryManager;
import de.tud.inf.rn.role.*;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nguonly role 7/10/15.
 */
public class CompartmentPlaysRoleTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    /**
     * Prepare data for testing. It's all about compartment.
     */
    public static class Faculty extends Compartment {
        public void configureBinding(){
            Person p = new Person();
            p.bind(this, Student.class).bind(this, TeachingAssistant.class);
            p.bind(this, Employee.class);
        }
    }

     @Test
    public void compartmentPlaysRoles(){
         //Connection con = DBManager.getConnection();
         RegistryManager registryManager = RegistryManager.getInstance();

         //compartment as a context
         try (Faculty faculty = Compartment.initialize(Faculty.class)) {
             faculty.configureBinding();

             Map<Integer, List<Relation>> maps = registryManager.m_relations.stream()
                     .filter(r->r.compartmentId == faculty.hashCode())
                     .collect(Collectors.groupingBy(r->r.roleId));

             Assert.assertEquals(3, maps.keySet().size());

         }

         //Compartment plays role
         try (Compartment comp = Compartment.initialize(Compartment.class)) {
             Faculty faculty = Player.initialize(Faculty.class);
             faculty.bind(Sponsor.class);

             Optional<Relation> rel = registryManager.m_relations.stream()
                     .filter(r -> r.compartmentId == comp.hashCode())
                     .findFirst();

             Assert.assertTrue(rel.get().roleName.contains("Sponsor"));
         }

    }

    public static class University extends Compartment{
        public void configureBinding(){
            Faculty faculty = new Faculty();
            faculty.configureBinding();

            faculty.bind(this, Sponsor.class);
        }
    }

    public static class Germany extends Compartment{
        public void configureBinding(){
            University tuDresden = new University();
            tuDresden.configureBinding();

            tuDresden.bind(this, Competitor.class);
        }
    }

    @Test
    public void multiLevelCoarseGrained(){
        try(Germany germany = Compartment.initialize(Germany.class)) {
            germany.configureBinding();

            RegistryManager registryManager = RegistryManager.getInstance();
            Map<Integer, List<Relation>> maps = registryManager.m_relations.stream()
                    .collect(Collectors.groupingBy(p -> p.compartmentId));

            //DumpHelper.dumpRelation(registryManager.m_relations);

            Assert.assertEquals(3, maps.keySet().size());
        }
    }
}
