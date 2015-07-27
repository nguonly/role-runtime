package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.db.DBManager;
import de.tud.inf.rn.db.SchemaManager;
import de.tud.inf.rn.player.Person;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nguonly role 7/20/15.
 */
public class InvokeCompartmentMethodTest {
    @Before
    public void setupSchema(){
        SchemaManager.drop();
        SchemaManager.create();
    }

    @After
    public void destroyDBConnection(){
        DBManager.close();
    }

    static Person p = Player.initialize(Person.class);

    public static class RoleA extends Role{
        public void setValueInCompartment(String value){
            invokeCompartment("setValue", new Class[]{String.class}, new Object[]{value});
        }
    }

    public static class RoleB extends Role{

    }

    public static class CompartmentA extends Compartment{
        public void activate(){
            p.bind(this, RoleA.class);
        }


        private String value;

        //This will called by role or player
        public void setValue(String value){
            this.value = value;
        }

        public String getValue(){
            return value;
        }

    }

    @Test
    public void invokeCompartmentMethodFromRole(){
        CompartmentA comp = Compartment.initialize(CompartmentA.class);
        comp.activate();
        String value = "Compartment";
        p.invoke(comp, "setValueInCompartment", new Class[]{String.class}, new Object[]{value});

        Assert.assertEquals(value, comp.getValue());
    }

}
