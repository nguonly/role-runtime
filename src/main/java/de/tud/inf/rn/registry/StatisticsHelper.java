package de.tud.inf.rn.registry;

import de.tud.inf.rn.db.orm.PlayRelationEnum;
import de.tud.inf.rn.db.orm.Relation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nguonly on 8/3/15.
 */
public class StatisticsHelper {
    public static Deque<Relation> getRelations(){
        return RegistryManager.getInstance().getRelations();
    }

    public static int rolesCount(int playerId){
        int sum = 0;
        Deque<Relation> relations = getRelations();

        //find root
        Map<Integer, List<Relation>> roots = relations.stream()
                .filter(r -> r.getPlayerId() == playerId
                        && (r.getType() == PlayRelationEnum.OBJECT_PLAYS_ROLE.getCode()
                        || r.getType() == PlayRelationEnum.ROLE_PLAYS_ROLE.getCode()))
                .collect(Collectors.groupingBy(Relation::getRoleId));

        sum += roots.size();
        for(Iterator<Integer> roleItr = roots.keySet().iterator();roleItr.hasNext();){
            Integer roleId = roleItr.next();
            sum += rolesCount(roleId);
        }

        return sum;
    }
}
