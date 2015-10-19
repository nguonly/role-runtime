package de.tud.inf.rn.registry;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.db.orm.Relation;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nguonly on 7/28/15.
 */
public class DumpHelper {

    public static void dumpRelation(Deque<Relation> relations){
        System.out.println("-----------------------------");
        for(Relation r : relations){
            System.out.format("%10d %10d %10d %10d %d %d %d %s %s %s\n", r.compartmentId, r.objectId,
                    r.playerId, r.roleId, r.sequence, r.type, r.level,
                    r.compartmentName, r.roleName, r.methodName);
        }

    }

    public static void dumpRelation(){
        Deque<Relation> relations = RegistryManager.getInstance().getRelations();
        System.out.println("-----------------------------");
        System.out.format("%10s %10s %10s %10s %18s %s %s %15s %20s %20s\n",
                    "Compment", "Root", "Player", "Role", "Sequence",
                    "Type", "Level", "CompName", "RoleName", "MethName");
        for(Relation r : relations){
            String compartmentName = getSimpleName(r.getCompartmentName());
            String roleName = getSimpleName(r.getRoleName());
            String methodName = getSimpleName(r.getMethodName());
            System.out.format("%10d %10d %10d %10d %18d %4d %5d %15s %20s %20s\n",
                    r.compartmentId, r.objectId,
                    r.playerId, r.roleId, r.sequence, r.type, r.level,
                    compartmentName, roleName, methodName);
        }

    }

    /**
     * Get name without package name "."
     * @param name
     * @return
     */
    private static String getSimpleName(String name){
        int lastIndex;

        if(name==null) return "";

        int lastIndexOfDollar = name.lastIndexOf('$'); // for internal class
        if(lastIndexOfDollar>0) lastIndex = lastIndexOfDollar;
        else lastIndex = name.lastIndexOf('.');

        return name.substring(lastIndex+1);
    }


    public static void printTree(Deque<Relation> relations, Compartment compartment){
        System.out.println("***** Tree Display ******");
        int compartmentId = compartment.hashCode();
        //find root
        Map<Integer, List<Relation>> roots = relations.stream()
                .filter(r -> r.compartmentId == compartmentId
                        && r.objectId == r.playerId)
                .collect(Collectors.groupingBy(r -> r.objectId));

        roots.forEach((objectId, list) ->{
            Optional<Relation> objectName = relations.stream()
                    .filter(r -> r.compartmentId == compartmentId
                            && r.objectId == objectId
                            && r.playerId == objectId)
                    .findFirst();

            String objName = objectName.get().objectName;
            int idx = objName.lastIndexOf('$'); //in case of inner class
            if(idx<0) idx = objName.lastIndexOf('.');
            String oName = objName.substring(idx+1);
            System.out.format("%s:%d\n", oName, objectId);
            print(relations, compartmentId, objectId, objectId, 1);
        });
        System.out.println("***** End of Tree Display ******");
    }

    private static void print(Deque<Relation> relations, int compartmentId, int objectId, int playerId, int level){

        Map<Integer, List<Relation>> rel = relations.stream()
                .filter(r -> r.objectId == objectId
                        && r.playerId == playerId
                        && r.level == level)
                .collect(Collectors.groupingBy(r -> r.roleId));

        if(rel.size()>0){
            rel.forEach((roleId, list) -> {
                Optional<Relation> objectNameRel = relations.stream()
                        .filter(r -> r.compartmentId == compartmentId
                                && r.objectId == objectId
                                && r.playerId == playerId
                                && r.roleId == roleId
                                && r.level == level)
                        .findFirst();

                String objName = objectNameRel.get().roleName;
                int idx = objName.lastIndexOf('$'); //in case of inner class
                if(idx<0) idx = objName.lastIndexOf('.');
                String oName = objName.substring(idx+1);

                System.out.format("%s%s:%d\n", getSpaces(level), oName, roleId);
                print(relations, compartmentId, objectId, roleId, level + 1);
            });

        }
    }

    private static String getSpaces(int depth){
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<depth;i++){
            sb.append("  ");
        }
        return sb.toString();
    }
}
