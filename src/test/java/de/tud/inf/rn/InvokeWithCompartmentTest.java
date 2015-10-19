package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.SchemaManager;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;

/**
 * Created by nguonly role 7/20/15.
 */
public class InvokeWithCompartmentTest {
    @Before
    public void setupSchema(){
//        SchemaManager.drop();
//        SchemaManager.create();
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        //DBManager.close();
        RegistryManager.getInstance().setRelations(null);
    }

    Person p = Player.initialize(Person.class);
    public static class RoleA extends Role{
        public String getName(){
            return this.getClass().getSimpleName();
        }
    }

    public static class RoleB extends Role{
        public String getName(){
            return this.getClass().getSimpleName();
        }
    }

    public static class CompartmentA extends Compartment{
        public CompartmentA(){

        }
    }

    @Test
    public void basicInvokeWithNullCompartment(){
        try(CompartmentA compA = Compartment.initialize(CompartmentA.class)) {
            p.bind(compA, RoleA.class);
            String retValue = p.invoke("getName", String.class);

            Assert.assertEquals(RoleA.class.getSimpleName(), retValue);
        }
    }
}
