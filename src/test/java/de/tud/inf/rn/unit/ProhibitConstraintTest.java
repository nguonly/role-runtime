package de.tud.inf.rn.unit;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.registry.DumpHelper;
import de.tud.inf.rn.registry.RegistryManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;

/**
 * Created by nguonly on 8/6/15.
 */
public class ProhibitConstraintTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    public static class P extends Player {
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private String name;

        public P(){
            name = "P";
        }
    }

    public static class R1 extends Role {
        private String name;

        public R1(){
            name = "Empty";
        }

        public R1(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }

    public static class R2 extends Role{
        private String name;

        public R2(){
            name = "Empty";
        }

        public R2(String name){
            this.name = name;
        }

        public String getName() { return name; }
    }

    public static class R3 extends Role{

    }

    public static class R4 extends Role{
        private String name;

        public R4(){
            name = "Empty";
        }

        public R4(String name){
            this.name = name;
        }

        public String getName(){ return name; }
    }

    @Test
    public void prohibitOnRole(){
        Compartment comp = new Compartment();
        comp.activate();

        P p = Player.initialize(P.class);
        R1 r1 = p.bind(R1.class);

        r1.prohibit(R2.class);

        DumpHelper.dumpRelation();

        comp.deActivate();
    }

}

