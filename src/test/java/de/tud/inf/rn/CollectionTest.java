package de.tud.inf.rn;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.db.orm.Relation;
import de.tud.inf.rn.player.Person;
import de.tud.inf.rn.registry.DumpHelper;
import de.tud.inf.rn.registry.RegistryManager;
import de.tud.inf.rn.registry.RelationSortHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nguonly on 7/28/15.
 */
public class CollectionTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        //DBManager.close();
        RegistryManager.getInstance().setRelations(null);
    }

    public static class RoleA extends Role {
        public String getName(){
            return this.getClass().getName();
        }

        public void setName(String value){

        }
    }

    public static class RoleB extends Role{
        public String getName(){
            return this.getClass().getName();
        }

        public void setName(String value){

        }
    }

    public static class RoleC extends Role{
        public String getName(){
            return this.getClass().getName();
            //return 0;
        }

        public void setName(String value){

        }
    }

    public static class RoleD extends Role{
        public String getName(){
            return this.getClass().getName();
        }

        public void setName(String value){

        }
    }

    public static class RoleE extends Role{
        public String getName(){
            return this.getClass().getName();
        }

        public void setName(String value){

        }
    }

    @Test
    public void simpleBind(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            Person p = Player.initialize(Person.class);
            Role a = p.bind(RoleA.class);
            Role b = a.bind(RoleB.class);
            b.bind(RoleD.class);
            a.bind(RoleC.class);
            b.bind(RoleE.class);
            //p.bind(RoleB.class);

            String retStr = p.invoke("getName", String.class);
            Assert.assertEquals(RoleC.class.getName(), retStr);

            RegistryManager registryManager = RegistryManager.getInstance();

            String methodName = ".* java.lang.String .*.getName\\(\\)";
            int compartmentId = comp.hashCode();
            int objId = p.hashCode();

            Optional<Relation> rel = registryManager.getRelations().stream()
                    .filter(c -> c.compartmentId == compartmentId
                            && c.objectId == objId
                            && c.methodName.matches(methodName))
                    .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                    .findFirst();

            Assert.assertTrue(rel.isPresent());

            Assert.assertTrue(rel.get().getRoleName().equals(RoleC.class.getName()));

        }
    }

    @Test
    public void roleInvokeRole(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = Player.initialize(Person.class);
            Role a = p.bind(RoleA.class);
            Role b = a.bind(RoleB.class);
            b.bind(RoleD.class);
            a.bind(RoleC.class);
            b.bind(RoleE.class);

            RegistryManager registryManager = RegistryManager.getInstance();

            int compartmentId = comp.hashCode();
            int rolePlayerId = b.hashCode();
            String methodSignature = ".* java.lang.String .*.getName\\(\\)";

            List<Relation> rel = registryManager.m_relations.stream().filter(c -> c.compartmentId == compartmentId && (c.playerId == rolePlayerId || c.roleId==rolePlayerId)
                    && c.methodName.matches(methodSignature)).sorted((s1, s2) -> s1.sequence > s2.sequence ? -1 : s1.sequence == s2.sequence ? 0 : +1)
                    .sorted((s1, s2) -> s1.type > s2.type ? -1 : s1.type == s2.type ? 0 : +1)
                    .collect(Collectors.toList());

            System.out.println("==============");
            for(Relation r : rel){
                System.out.format("%d %d %d %d %d %d %s %s\n", r.compartmentId, r.objectId,
                    r.playerId, r.roleId, r.sequence, r.type,
                    r.roleName, r.methodName);
            }
        }
    }

    @Test
    public void unbindAll(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            Person p = Player.initialize(Person.class);

            Role a = p.bind(RoleA.class);
            Role b = a.bind(RoleB.class);
            b.bind(RoleD.class);
            a.bind(RoleC.class);
            b.bind(RoleE.class);

            p.unbindAll();

            Assert.assertTrue(RegistryManager.getInstance().getRelations().isEmpty());
        }
    }
}
