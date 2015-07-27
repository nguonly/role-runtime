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

import java.lang.reflect.Method;

/**
 * Created by nguonly role 7/22/15.
 */
public class ContextActivationScopeTest {
    @Before
    public void setupSchema(){
        SchemaManager.drop();
        SchemaManager.create();
    }

    @After
    public void destroyDBConnection(){
        DBManager.close();
    }

    public static class RoleA extends Role{
        public String getName(){
            return this.getClass().getSimpleName();
        }

        public void abc(){
            System.out.println("abc");
        }

        public void abc(int i){
            System.out.println(i);
        }

        public String abc(int i, int j){
            return "a";
        }
    }

    public static class RoleB extends Role{
        public String getName(){
            return this.getClass().getSimpleName();
        }
    }

    static Person p = Player.initialize(Person.class);

    public static class MyCompartment extends Compartment{

        public void bind(){
            p.bind(this, RoleA.class);
        }

        public void activate(){

        }
    }

    public static class CompartmentA extends Compartment{

        public void bind(){
            p.bind(this, RoleB.class);
        }

        public void activate(){

        }

    }

    @Test
    public void simpleContextScopeActivation(){
        try(MyCompartment comp = Compartment.initialize(MyCompartment.class)){
            comp.bind();

            System.out.println("Entering --- " + comp);
            String retValue = p.invoke("getName", String.class);
            Assert.assertEquals(RoleA.class.getSimpleName(), retValue);
            try(CompartmentA ca = Compartment.initialize(CompartmentA.class)){
                ca.bind();

                System.out.println("Entering --- " + ca);
                retValue = p.invoke("getName", String.class);
                Assert.assertEquals(RoleB.class.getSimpleName(), retValue);
            }

            retValue = p.invoke("getName", String.class);
            Assert.assertEquals(RoleA.class.getSimpleName(), retValue);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void checkReflectionMethodSignature(){
        RoleA r = new RoleA();

        Method[] methods = r.getClass().getDeclaredMethods();
        for(Method m : methods){
            System.out.println(m);
        }
    }

    @Test
    public void test(){
        //Class returnType = String.class;
        Class returnType = null;
        String methodName = "getName";
        //Class[] clazzes = new Class[]{int.class, int.class};
        Class[] clazzes = null;
        StringBuilder sb = new StringBuilder();
        sb.append("%").append(returnType==null?"":" " + returnType.getName());
        sb.append(" %.").append(methodName).append("(");

        if(clazzes!=null) {
            for (int i = 0; i < clazzes.length; i++) {
                sb.append(clazzes[i].getSimpleName());
                if (i < clazzes.length - 1) sb.append(",");
            }
        }
        sb.append(")");

        System.out.println(sb.toString());
    }
}
