package de.tud.inf.rn.registry;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.db.orm.PlayRelationEnum;
import de.tud.inf.rn.db.orm.Relation;
import de.tud.inf.rn.exception.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by nguonly role 7/10/15.
 */
public class RegistryManager {
    private static RegistryManager m_registryManager;

    private static HashMap<Integer, Object> objectsMap = new HashMap<>();
    private static HashMap<Integer, Object> rolesMap = new HashMap<>();
    private static HashMap<Integer, Object> compartmentsMap = new HashMap<>();

    public static Deque<Relation> m_relations = new ArrayDeque<>();

    private static ArrayDeque<Integer> m_activeCompartments = new ArrayDeque<>();

    private static int m_number_level = 4*4 + 1;

    //static final Logger log = LogManager.getLogger(RegistryManager.class);

    private static final ReentrantLock lock = new ReentrantLock();

    public static RegistryManager getInstance(){
        lock.lock();
        try {
            if (m_registryManager == null) {
                m_registryManager = new RegistryManager();
            }
        }finally {
            lock.unlock();
        }

        return m_registryManager;
    }

    public void setRelations(Deque<Relation> relations){
        m_relations = relations;
    }

    public Deque<Relation> getRelations(){
        return m_relations;
    }

    public <T> T initializePlayer(Class<T> player, Class[] constructorArgumentTypes, Object[] constructorArgumentValues) {
        try {
            T p;
            if(constructorArgumentTypes==null || constructorArgumentValues==null){
                p = player.newInstance();
            }else {
                Constructor<T> constructor = player.getConstructor(constructorArgumentTypes);
                p = constructor.newInstance(constructorArgumentValues);
            }
            //objectsMap.put(p.hashCode(), p);
            return p;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public <T> T initializeCompartment(Class<T> compartment, Class[] constructorArgumentTypes, Object[] constructorArgumentValues){
        try {
            T p;
            if(constructorArgumentTypes == null || constructorArgumentValues == null){
                p = compartment.newInstance();
            }else{
                Constructor<T> constructor = compartment.getConstructor(constructorArgumentTypes);
                p = constructor.newInstance(constructorArgumentValues);
            }

            compartmentsMap.put(p.hashCode(), p);

            //push current active compartment
            m_activeCompartments.push(p.hashCode());

            return p;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void registerCompartment(Compartment compartment){
        int compartmentId = compartment.hashCode();
        if(compartmentsMap.get(compartmentId)==null) compartmentsMap.put(compartmentId, compartment);

        if(!m_activeCompartments.contains(compartmentId)) m_activeCompartments.push(compartmentId);
    }

    public <T extends Role> T bind(Compartment compartment, Player player,
                                   Class<T> role,
                                   Class[] constructorArgumentTypes,
                                   Object[] constructorArgumentValues) throws RuntimeException{

        Compartment activeCompartment = compartment;
        if(compartment==null){
            //check in the compartment stacks
            if(m_activeCompartments.isEmpty()) throw new CompartmentNotFoundException();
            activeCompartment = (Compartment) compartmentsMap.get(m_activeCompartments.peek());
        }
        int activeCompartmentId = activeCompartment.hashCode();

        //Compartment cannot be a player inside their own compartment
        if(activeCompartmentId == player.hashCode()) throw new CompartmentAsPlayerInItsContextException();

        //Check whether this role type has been played at the same level
        Optional<Relation> existingRole = m_relations.stream()
                .filter(r -> r.getCompartmentId() == activeCompartmentId
                        && r.getPlayerId() == player.hashCode()
                        && r.getRoleName().equals(role.getName()))
                .findFirst();
        if(existingRole.isPresent()) throw new BindTheSameRoleTypeException();

        //Put player into objectMap
        if(objectsMap.get(player.hashCode())==null){
            objectsMap.put(player.hashCode(), player);
        }

        //TODO: Prohibit constraint

        try {
            T roleInstance;
            if(constructorArgumentTypes == null || constructorArgumentValues == null){
                roleInstance = role.newInstance();
            }else{
                Constructor<T> constructor = role.getConstructor(constructorArgumentTypes);
                roleInstance = constructor.newInstance(constructorArgumentValues);
            }

            //log.debug("Role Id = " + roleInstance.hashCode() + " : " + roleInstance.getClass().getName());

            rolesMap.put(roleInstance.hashCode(), roleInstance);

            //find sequence for each level
            Optional<Relation> distinct = m_relations.stream()
                    .filter(c -> c.getCompartmentId() == activeCompartmentId
                            && c.getPlayerId() == player.hashCode())
                    .sorted(RelationSortHelper.SEQUENCE_DESC)
                    .findFirst();

            long seq = 0;
            if(distinct.isPresent()) seq = distinct.get().getSequence();

            //seq++;
            long c = (long)Math.pow(10, m_number_level);
            if(seq ==0) {
                seq = c;
            }else {
                seq = ((seq/c) +1)*c;
            }

            //Register Role's methods
            String playerName = player.getClass().getName();
            Method[] methods = role.getDeclaredMethods();

            Relation relation = new Relation();
            relation.setCompartmentId(activeCompartment.hashCode());
            relation.setCompartmentName(activeCompartment.getClass().getName());
            relation.setObjectId(player.hashCode());
            relation.setObjectName(playerName);
            relation.setPlayerId(player.hashCode());
            relation.setPlayerName(playerName);
            relation.setRoleId(roleInstance.hashCode());
            relation.setRoleName(role.getName());
            relation.setLevel(1);
            relation.setType(PlayRelationEnum.OBJECT_PLAYS_ROLE.getCode());
            relation.setSequence(seq);

            //Register Role's methods
            if(methods.length>0) {
                for (Method m : methods) {
                    relation.methodName = m.toString();

                    m_relations.add(new Relation(relation));
                }
            }else{
                relation.setMethodName(""); //if null then, the match (RegEx) won't work on method invocation
                m_relations.add(new Relation(relation));
            }

            //DumpHelper.dumpRelation(m_relations);

            return roleInstance;

        }catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public <T extends Role> T inherit(Compartment compartment, Object core, Class<T> superRole,
                                      Class[] constructorArgumentTypes,
                                      Object[] constructorArgumentValues) throws RuntimeException{

        Object activeCompartment = compartment==null?compartmentsMap.get(m_activeCompartments.peek()):
                    compartment;
        int compartmentId = activeCompartment.hashCode();
        int coreId = core.hashCode();

        //Cannot inherit itself
        if(core.getClass().getName().equals(superRole.getName())) throw new InheritItselfException();

        //Allow only single inheritance
        //Check for existing role type available
        Optional<Relation> existingRole = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getPlayerId() == coreId
                        && r.getType() == PlayRelationEnum.INHERITANCE.getCode())
                .findFirst();

        if(existingRole.isPresent()) throw new SingleInheritanceException();

        try{
            T roleInstance;
            if(constructorArgumentTypes == null || constructorArgumentValues == null){
                roleInstance = superRole.newInstance();
            }else{
                Constructor<T> constructor = superRole.getConstructor(constructorArgumentTypes);
                roleInstance = constructor.newInstance(constructorArgumentValues);
            }

            //put into role list
            rolesMap.put(roleInstance.hashCode(), roleInstance);

            Optional<Relation> coreRelation = m_relations.stream()
                    .filter(r -> r.getCompartmentId() == compartmentId
                                && r.getRoleId() == coreId)
                    .sorted(RelationSortHelper.SEQUENCE_DESC)
                    .findFirst();

            int objId = coreRelation.get().getObjectId(); //a real core object
            String objName = coreRelation.get().getObjectName(); // a real core object name

//            log.debug("Core Id=" + objId);

            long seq = coreRelation.get().getSequence();
            int lvl = coreRelation.get().getLevel();

            //Register Role's methods
            String playerName = core.getClass().getName();
            Method[] methods = superRole.getDeclaredMethods();
            Relation relation = new Relation();
            relation.setCompartmentId(compartmentId);
            relation.setCompartmentName(activeCompartment.getClass().getName());
            relation.setObjectId(objId);
            relation.setObjectName(objName);
            relation.setPlayerId(core.hashCode());
            relation.setPlayerName(playerName);
            relation.setRoleId(roleInstance.hashCode());
            relation.setRoleName(superRole.getName());
            relation.setLevel(lvl);
            relation.setType(PlayRelationEnum.INHERITANCE.getCode());
            relation.setSequence(seq);

            if(methods.length>0){
                for(Method m: methods){
                    relation.setMethodName(m.toString());

                    m_relations.add(new Relation(relation));
                }
            }else{
                relation.setMethodName("");
                m_relations.add(relation);
            }

            return roleInstance;
        }catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public <T extends Role> T rolePlaysRole(Compartment compartment, Object core,
                                            Class<T> role,
                                            Class[] constructorArgumentTypes,
                                            Object[] constructorArgumentValues){

            Compartment activeCompartment = compartment;
            if(compartment==null){
                //check in the compartment stacks
                if(m_activeCompartments.isEmpty()) throw new CompartmentNotFoundException();
                activeCompartment = (Compartment) compartmentsMap.get(m_activeCompartments.peek());
            }
            int activeCompartmentId = activeCompartment.hashCode();

        Optional<Relation> existingRole = m_relations.stream()
                .filter(x -> x.getCompartmentId() == activeCompartmentId
                        && x.getPlayerId() == core.hashCode()
                        && x.getRoleName().equals(role.getName())
                        && x.getType() != PlayRelationEnum.INHERITANCE.getCode())
                .sorted(RelationSortHelper.SEQUENCE_DESC)
                .findFirst();

        if(existingRole.isPresent()) throw new BindTheSameRoleTypeException();

        try{
            //Initialize role
            T roleInstance;
            if(constructorArgumentTypes == null || constructorArgumentValues == null){
                roleInstance = role.newInstance();
            }else{
                Constructor<T> constructor = role.getConstructor(constructorArgumentTypes);
                roleInstance = constructor.newInstance(constructorArgumentValues);
            }

            rolesMap.put(roleInstance.hashCode(), roleInstance);

            Optional<Relation> coreObjRelation = m_relations.stream()
                    .filter(c -> c.getRoleId() == core.hashCode()
                            && c.getCompartmentId() == activeCompartmentId)
                    .sorted(RelationSortHelper.SEQUENCE_DESC)
                    .findFirst();

            long seq = 0;
            int lvl = 0;
            long c;
            int objId=0;
            String objName = "";
            if(coreObjRelation.isPresent()){
                objId = coreObjRelation.get().objectId;
                objName = coreObjRelation.get().objectName;
            }

//            log.debug("Core Id {}", objId);

            //check if core has previous bound role
            Optional<Relation> latestRole = m_relations.stream()
                    .filter(x -> x.getCompartmentId() == activeCompartmentId
                            && x.getPlayerId() == core.hashCode()
                            && x.getType() != PlayRelationEnum.INHERITANCE.getCode())
                    .sorted(RelationSortHelper.SEQUENCE_DESC)
                    .findFirst();

            if(latestRole.isPresent()){
                lvl = latestRole.get().getLevel();
                seq = latestRole.get().getSequence();
                c = (long)Math.pow(10, m_number_level - 2*(lvl-1));
                seq = ((seq/c)+1)*c;
            }else {
                //find sequence for each level
                if(coreObjRelation.isPresent()){
                    seq = coreObjRelation.get().getSequence();
                    lvl = coreObjRelation.get().getLevel();
                }

                lvl++;
                c =(long)Math.pow(10, m_number_level-2*(lvl-1));
                seq = ((seq/c) + 1)*c;
            }

            String playerName = core.getClass().getName();

            //Register Role's methods
            Relation relation = new Relation();
            relation.setCompartmentId(activeCompartment.hashCode());
            relation.setCompartmentName(activeCompartment.getClass().getName());
            relation.setObjectId(objId);
            relation.setObjectName(objName);
            relation.setPlayerId(core.hashCode());
            relation.setPlayerName(playerName);
            relation.setRoleId(roleInstance.hashCode());
            relation.setRoleName(role.getName());
            relation.setLevel(lvl);
            relation.setType(PlayRelationEnum.ROLE_PLAYS_ROLE.getCode());
            relation.setSequence(seq);

            Method[] methods = role.getDeclaredMethods();
            if(methods.length>0) {
                for (Method m : methods) {
                    //log.debug("{}.{}", role.getName(), m.getName());

                    relation.setMethodName(m.toString());

                    m_relations.add(new Relation(relation));
                }
            }else{
                relation.setMethodName(""); //if null then, the match (RegEx) won't work on method invocation
                m_relations.add(new Relation(relation));
            }

            //DumpHelper.dumpRelation(m_relations);

            return roleInstance;
        }catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public <T> T playerInvokeRole(Compartment compartment, Object core, String methodName, Class<T> returnType,
                                  Class[] argumentType, Object[] argumentValue) throws RuntimeException{

        int compartmentId = compartment==null?m_activeCompartments.peek() : compartment.hashCode();
        try{
            String methodSignature = methodSignature(returnType, methodName, argumentType);

            Optional<Relation> rel = m_relations.stream()
                    .filter(c -> c.getCompartmentId() == compartmentId
                            && c.getObjectId() == core.hashCode()
                            && c.getMethodName().matches(methodSignature))
                    .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                    .findFirst();

            Object invokingObject;
            if(rel.isPresent()){
                int roleId = rel.get().getRoleId();
                invokingObject = rolesMap.get(roleId);
            }else{
                //Should check our own methods to be invoked
//                log.debug("Method {} was not found in roles of {}", methodName, core.getClass().getName());
//                log.debug("Now starts looking role the core's methods");
                invokingObject = core;
            }
            //MethodHandle methodHandle =  MethodHandles.lookup().findVirtual(invokingObject.getClass(), methodName, MethodType.methodType(returnType, argumentType));
            //Object objRet = methodHandle.invokeExact(invokingObject, argumentValue);
            Method method = invokingObject.getClass().getMethod(methodName,argumentType);
            Object objRet = method.invoke(invokingObject, argumentValue);
            if(returnType!=null && !returnType.isAssignableFrom(void.class) && !returnType.isAssignableFrom(Void.class)) {
                if(returnType.isPrimitive()){
                    return (T)objRet;
                }
                return returnType.cast(objRet);
            }

        }  catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
            throw new RuntimeException();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return null;
    }

    public <T> T roleInvokeRole(Role core, String methodName, Class<T> returnType, Class[] argumentType, Object[] argumentValue){
        try{
            String methodSignature = methodSignature(returnType, methodName, argumentType);

            int compartmentId = m_activeCompartments.peek();
            int rolePlayerId = core.hashCode();

            Optional<Relation> rr = m_relations.stream()
                    .filter(c -> c.getCompartmentId() == compartmentId
                            && (c.getPlayerId() == rolePlayerId)
                            && c.getMethodName().matches(methodSignature))
                    .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                    .findFirst();

            //Calculate the sequence of current role to cascade down
            final long c = (long)Math.pow(10, m_number_level - 2*(rr.get().getLevel()-1));
            final long  seq = (rr.get().getSequence()/c);

            //All role (including itself) relations down from the cascaded sequence
            Optional<Relation> rel = m_relations.stream()
                    .filter(x -> x.getCompartmentId() == compartmentId
                            && ((x.getSequence() / c) == seq || x.getRoleId() == rolePlayerId)
                            && x.getMethodName().matches(methodSignature))
                    .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                    .findFirst();

            if(rel.isPresent()){
                int roleId = rel.get().getRoleId();
                Object role = rolesMap.get(roleId);
                Method method = role.getClass().getMethod(methodName, argumentType);
                Object objRet = method.invoke(role, argumentValue);
                if(returnType!=null && !returnType.isAssignableFrom(void.class) && !returnType.isAssignableFrom(Void.class)) {
                    if(returnType.isPrimitive()){
                        return (T)objRet;
                    }
                    return returnType.cast(objRet);
                }
            }else{
//                log.error("The {} method was not found", methodName);
            }
        }catch(Exception e) {
//            log.error("Database error: {} : {}", e.getClass().getName(), e.getMessage());
        }

        return null;
    }


    /**
     * Invoke based method. It can be either root player or role player
     * @param role Current role
     * @param methodName Method Name role Base
     * @param argumentTypes Type of parameter
     * @param argumentValues Value of parameter
     * @return Object
     */
    public <T> T invokeBase(Role role, String methodName, Class<T> returnType, Class[] argumentTypes, Object[] argumentValues) {
        Optional<Relation> rel = m_relations.stream()
                .filter(c -> c.getRoleId() == role.hashCode()).findFirst();
        try {
            if(rel.isPresent()){
                Object base;
                if(rel.get().getObjectId() == rel.get().getPlayerId()){
                    //It's root player (Player)
                    base = objectsMap.get(rel.get().getObjectId());
                }else{
                    //It's role player (Role)
                    base = rolesMap.get(rel.get().getPlayerId());
                }
                Method method = base.getClass().getMethod(methodName, argumentTypes);
                Object objRet = method.invoke(base, argumentValues);
                if(returnType!=null && !returnType.isAssignableFrom(void.class) && !returnType.isAssignableFrom(Void.class)) {
                    if(returnType.isPrimitive()){
                        return (T)objRet;
                    }
                    return returnType.cast(objRet);
                }
            }else{
//                log.error("{}.{} was not found", role.getClass().getName(), methodName);
            }

        } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * The method is to invoke root (core object) methods
     * @param role a role
     * @param methodName case-sentitive method name
     * @param returnType
     * @param argumentTypes
     * @param argumentValues
     * @param <T>
     * @return
     */
    public <T> T invokeCore(Role role, String methodName, Class<T> returnType, Class[] argumentTypes, Object[] argumentValues){
        try{
            int roleId = role.hashCode();
            Optional<Relation> rootRel = m_relations.stream()
                    .filter(r -> r.getRoleId() == roleId)
                    .findFirst();
            if(rootRel.isPresent()){
                Object objRoot = objectsMap.get(rootRel.get().getObjectId());
                Method method = objRoot.getClass().getMethod(methodName, argumentTypes);
                Object objRet = method.invoke(objRoot, argumentValues);
                if(returnType!=null && !returnType.isAssignableFrom(void.class) && !returnType.isAssignableFrom(Void.class)) {
                    if(returnType.isPrimitive()){
                        return (T)objRet;
                    }
                    return returnType.cast(objRet);
                }
            }else{
                throw new RuntimeException("Root or Core Object was not found");
            }
        }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | CompartmentNotFoundException e){
            e.printStackTrace();
        }

        return null;
    }

    public <T> T invokeCompartment(boolean isPlayer, Object core, String methodName,
                                   Class<T> returnType, Class[] argumentTypes, Object[] argumentValues){
        try{
            int coreId = core.hashCode();
            Optional<Relation> compartmentRel = m_relations.stream()
                    .filter(r -> isPlayer ? r.getPlayerId() == coreId : r.getRoleId() == coreId)
                    .findFirst();

            if(compartmentRel.isPresent()){
                Object objCompartment = compartmentsMap.get(compartmentRel.get().getCompartmentId());
                Method method = objCompartment.getClass().getMethod(methodName, argumentTypes);
                Object objRet = method.invoke(objCompartment, argumentValues);
                if(returnType!=null && !returnType.isAssignableFrom(void.class) && !returnType.isAssignableFrom(Void.class)) {
                    if(returnType.isPrimitive()){
                        return (T)objRet;
                    }
                    return returnType.cast(objRet);
                }
            }else{
                throw new CompartmentNotFoundException();
            }
        }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | CompartmentNotFoundException e){
            e.printStackTrace();
        }

        return null;
    }



    public void unbind(Object core,  Class role){
//        if(log.isDebugEnabled()) {
//            log.debug("Before deleting roles in Hashtable");
//        }
        //Get role Id

        //Find concrete role relation
        int compartmentId = m_activeCompartments.peek();
        Optional<Relation> concreteRoleRelation = m_relations.stream()
                .filter(c -> c.getCompartmentId() == compartmentId
                        //&& isPlayer?(c.playerId == rolePlayerId):(c.roleId == rolePlayerId)
                        && c.getPlayerId() == core.hashCode()
                        && c.getRoleName().equals(role.getName()))
                .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                .findFirst();

        //Find role list in play relation by concrete role relation
        final long c = (long)Math.pow(10, m_number_level - 2*(concreteRoleRelation.get().getLevel()-1));
        final long  seq = (concreteRoleRelation.get().getSequence()/c);

        Map<Integer, List<Relation>> uniqueRoleListToBeRemoved = m_relations.stream()
                .filter(x -> x.getCompartmentId() == compartmentId
                        && ((x.getSequence() / c) == seq))
                .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                .collect(Collectors.groupingBy(v -> v.getRoleId()));

        //Actual removing process in m_relation
        for(Iterator<Relation> itr = m_relations.iterator(); itr.hasNext();){
            Relation r = itr.next();
            for (Integer roleId : uniqueRoleListToBeRemoved.keySet()) {
                if (r.getRoleId() == roleId) {
                    itr.remove();
                    //itrRoleToBeRemoved.remove(); //To prevent from overloading method not removing
                    break;
                }
            }
        }

        //test whether roles are removed from rolesMap hashtable
//        if(log.isDebugEnabled()) {
//            log.debug("---------- After deleting roles from both Database and hashtable");
//        }
    }

    /**
     * Unbind all the bound roles from a root player
     * @param root a root player
     */
    public void unbindAll(Object root){
        int rootId = root.hashCode();

        /**
         * Role Id = -1 is prohibit relation that require no initialization of role
         */
        m_relations.stream()
                .filter(c -> c.getObjectId() == rootId)
                .forEach(relation -> rolesMap.remove(relation.getRoleId()));

        m_relations.removeIf(relation -> relation.getObjectId() == rootId);
    }

    //Role Constraints

    /**
     * This prohibit constraint is applied for Role.
     * It seems not need at this moment.
     * @param core a Player either (root object or role)
     * @param role a prohibited role
     */

    public void prohibit(Compartment compartment, Object core, Class role){
        int coreId = core.hashCode();
        int compartmentId = compartment==null?m_activeCompartments.peek():compartment.hashCode();

        Optional<Relation> coreObject = m_relations.stream()
                .filter(x -> x.getCompartmentId() == compartmentId
                        && x.getRoleId() == coreId)
                .sorted(RelationSortHelper.SEQUENCE_DESC)
                .findFirst();

        int objId = coreObject.get().getObjectId();
        String objName = coreObject.get().getObjectName();
        long seq = coreObject.get().getSequence();
        int lvl = coreObject.get().getLevel();

        String playerName = core.getClass().getName();
        Relation relation = new Relation();
        relation.setCompartmentId(compartmentId);
        relation.setCompartmentName(compartment==null?
                                        compartmentsMap.get(m_activeCompartments.peek()).getClass().getName()
                                        : compartment.getClass().getName());
        relation.setObjectId(objId);
        relation.setObjectName(objName);
        relation.setPlayerId(core.hashCode());
        relation.setPlayerName(playerName);
        relation.setRoleId(-1); //no role instance
        relation.setRoleName(role.getName());
        relation.setLevel(lvl);
        relation.setType(PlayRelationEnum.PROHIBIT.getCode());
        relation.setSequence(seq);
        relation.setMethodName(""); //no required methods to be stored

        m_relations.add(relation);
    }

    /**
     * Transfer role instance from one to another player. It's also possible to transfer to different compartment.
     * @param role
     * @param from
     * @param to
     * @param toCompartment
     */

    public void transfer(Class role, Object from, Compartment fromCompartment, Object to, Compartment toCompartment){
        int fromObjId = from.hashCode();
        int toObjId = to.hashCode();
        final int toCompartmentId = toCompartment==null? m_activeCompartments.peek(): toCompartment.hashCode();
        final int fromCompartmentId = fromCompartment==null? m_activeCompartments.peek(): fromCompartment.hashCode();

        //find transferring role to do cascading later
        Optional<Relation> transferringRoleRel = m_relations.stream()
                .filter(r -> r.getCompartmentId() == fromCompartmentId
                    && r.getPlayerId() == fromObjId
                    && r.getRoleName().equals(role.getName()))
                .findFirst();

        //Check if the [To] player has current bound roles. If so, get the latest
        Optional<Relation> latestRoleTo = m_relations.stream()
                .filter(r -> r.getCompartmentId() == toCompartmentId
                        && r.getPlayerId() == toObjId)
                .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                .findFirst();

        //Get current relation of To player if available
        Optional<Relation> currentTo = m_relations.stream()
                .filter(r -> r.getCompartmentId() == toCompartmentId)
                .filter(r -> (to instanceof Role) ?
                        r.getRoleId() == toObjId :
                        r.getPlayerId() == toObjId)
                .findFirst();

        //Compute sequence to search the lower cascading
        final long c = (long)Math.pow(10, m_number_level - 2*(transferringRoleRel.get().getLevel()));
        final long  seq = (transferringRoleRel.get().getSequence()/c);

        //Get all the roles in the play-relation
        List<Relation> uniqueRoleList = m_relations.stream()
                .filter(r -> r.getCompartmentId() == fromCompartmentId
                        && r.getObjectId() == fromObjId
                        && (r.getSequence() / c) >= seq)
                .collect(Collectors.toList());
                //.collect(Collectors.groupingBy(r -> r.roleId));

        //Remove role and its children from previous bound player
        for(Iterator<Relation> itr = m_relations.iterator();itr.hasNext();){
            Relation r = itr.next();
            for (Relation removingRoleId : uniqueRoleList) {
                if (r.getRoleId() == removingRoleId.getRoleId()) {
                    itr.remove();
                    break;
                }
            }
        }

        //loop in uniqueRoleList and construct relation to be added
        long seq1;
        int lvl;
        long c1;
        if(latestRoleTo.isPresent()){
            lvl = latestRoleTo.get().getLevel();
            seq1 = latestRoleTo.get().getSequence();
            c1 = (long)Math.pow(10, m_number_level - 2*(lvl-1));
            seq1 = ((seq1 / c1) + 1) * c1;
        }else{
            //No previous bound role
            //find the current object whether it's a role or a core object.
            //If it's a role then find the level by lvl = role.level + 1
            if(to instanceof Role){
                //currentTo is not always empty because it's a role
                lvl = currentTo.get().getLevel() + 1;
                c1 = (long) Math.pow(10, m_number_level - 2 * (lvl - 1));
                seq1 = (currentTo.get().getSequence()/c1 + 1)*c1;
            }else {
                lvl = 1;
                seq1 = (long) Math.pow(10, m_number_level);
            }
        }

        //find the difference between old and new
        int levelOffset = transferringRoleRel.get().getLevel() - lvl;

        //Check if the target player has previous role type the same as one of uniqueRoleList

        for (Relation r : uniqueRoleList) {
            Relation relation = new Relation();
            relation.setCompartmentId(toCompartmentId);
            relation.setCompartmentName(toCompartment == null ?
                    compartmentsMap.get(toCompartmentId).getClass().getName() :
                    toCompartment.getClass().getName());
            relation.setObjectId((to instanceof Role)
                    ? currentTo.get().getObjectId() : toObjId);
            relation.setObjectName((to instanceof Role) ? currentTo.get().getObjectName() :
                    to.getClass().getName());

            if (r.getRoleId() == transferringRoleRel.get().getRoleId()) {
                //This the transferring root role
                relation.setPlayerId((to instanceof Role) ? currentTo.get().getObjectId()
                        : toObjId);
                relation.setPlayerName((to instanceof Role) ? currentTo.get().getObjectName()
                        : to.getClass().getName());
                relation.setLevel(lvl);
                relation.setType((to instanceof Role) ?
                        PlayRelationEnum.ROLE_PLAYS_ROLE.getCode() :
                        PlayRelationEnum.OBJECT_PLAYS_ROLE.getCode());
                relation.setSequence(seq1);
            } else {
                relation.setPlayerId(r.getPlayerId());
                relation.setPlayerName(r.getPlayerName());
                relation.setType(r.getType()); //remain the same

                int newLevel = r.getLevel() - levelOffset;
                long remainderOfSequence = r.getSequence() % (long) Math.pow(10, m_number_level - 2 * (transferringRoleRel.get().getLevel() - 1));
                long newSeq = seq1 + remainderOfSequence * (long) Math.pow(10, 2 * (levelOffset));
                relation.setLevel(newLevel);
                relation.setSequence(newSeq);
            }

            relation.setRoleId(r.getRoleId());
            relation.setRoleName(r.getRoleName()); //rolesMap.get(r.roleId).getClass().getName();

            relation.setMethodName(r.getMethodName());

            m_relations.add(new Relation(relation));
        }
    }

    public void destroyActiveCompartment(Compartment compartment){
        int compartmentId = compartment.hashCode();
        m_relations.removeIf(r -> r.getCompartmentId() == compartmentId);

        //Pop out from active compartment stack
        if(!m_activeCompartments.isEmpty()){
            m_activeCompartments.pop();
        }else{
//            log.debug("Active Compartment is Empty");
        }
        compartmentsMap.remove(compartmentId);
    }

    private String methodSignature(Class returnType, String methodName, Class[] clazzes){
        StringBuilder sb = new StringBuilder();
        sb.append(".*").append(returnType==null?"":" " + returnType.getName());
        sb.append(" .*.").append(methodName).append("\\(");

        if(clazzes!=null) {
            for (int i = 0; i < clazzes.length; i++) {
                sb.append(clazzes[i].getName());
                if (i < clazzes.length - 1) sb.append(",");
            }
        }
        sb.append("\\)");

        return sb.toString();
    }

    public <T> T role(Compartment compartment, Object player, Class<T> roleClass){
        int compartmentId = compartment == null ? m_activeCompartments.peek() : compartment.hashCode();

        int playerId = player.hashCode();
        String roleName = roleClass.getName();

        /**
         * Double filter work in this case. I don't know why.
         * One filter with complex condition the r.roleName.euqals(..) is not evaluated. Sic!!
         */
        //TODO: Check the itnery condition
        Optional<Relation> roleRel = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId)
                .filter(r -> (player instanceof Role) ?
                        r.getPlayerId() == playerId :
                        r.getObjectId() == playerId)
                .filter(r -> r.getRoleName().equals(roleName))
                .findFirst();

        if (roleRel.isPresent()) {
            Object obj = rolesMap.get(roleRel.get().getRoleId());
            return roleClass.cast(obj);
        }

        return null;
    }

    public <T> T base(Compartment compartment, Role role, Class<T> baseClass){
        int compartmentId = compartment == null ? m_activeCompartments.peek() : compartment.hashCode();
        //Check if base is root player (Player) or role player (Role)
        Optional<Relation> baseRelation = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getRoleId() == role.hashCode())
                .findFirst();

        if (baseRelation.isPresent()) {
            Object base;
            if (baseRelation.get().getObjectId() == baseRelation.get().getPlayerId()) {
                //It's root player (Player)
                base = objectsMap.get(baseRelation.get().getObjectId());
            } else {
                //It's role player (Role)
                base = rolesMap.get(baseRelation.get().getPlayerId());
            }
            return baseClass.cast(base);
        } else {
//            log.error("{} was not found", role.getClass().getName());
        }

        return null;
    }

    public <T> T compartment(Compartment compartment, Class<T> compartmentClass){
        try{
            Object objCompartment = compartment;
            if(compartment==null){
                if(m_activeCompartments.isEmpty()) throw new CompartmentNotFoundException();
                int compartmentId = m_activeCompartments.peek();
                objCompartment = compartmentsMap.get(compartmentId);
            }
            return compartmentClass.cast(objCompartment);
        }catch(CompartmentNotFoundException e){
            e.printStackTrace();
        }

        return null;
    }

    public Object getRootPlayer(Compartment compartment, Role role){
        if(m_activeCompartments.isEmpty()) throw new CompartmentNotFoundException();
        //System.out.println(role.hashCode());
        int compartmentId = compartment == null ? m_activeCompartments.peek() : compartment.hashCode();
        //Check if base is root player (Player) or role player (Role)
        Optional<Relation> baseRelation = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getRoleId() == role.hashCode())
                .findFirst();
        if(baseRelation.isPresent()){
            int objId = baseRelation.get().getObjectId();
            return objectsMap.get(objId);
        }

        return null;
    }

    public Object[] getRootPlayer(Compartment compartment, Class roleClass){
        if(m_activeCompartments.isEmpty()) throw new CompartmentNotFoundException();

        int compartmentId = compartment == null ? m_activeCompartments.peek() : compartment.hashCode();
        //Check if base is root player (Player) or role player (Role)

        ArrayDeque<Object> lstObjects = new ArrayDeque<>();

        m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getRoleName().equals(roleClass.getName()))
                .forEach(c->{
                    Object p = objectsMap.get(c.getObjectId());
                    if(!lstObjects.contains(p)) lstObjects.add(p);
                });

        return lstObjects.toArray();
    }

    public Object getPlayer(Compartment compartment, Role role){
        int compartmentId = compartment == null ? m_activeCompartments.peek() : compartment.hashCode();
        //Check if base is root player (Player) or role player (Role)
        Optional<Relation> baseRelation = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getRoleId() == role.hashCode())
                .findFirst();

        if (baseRelation.isPresent()) {
            Object base;
            if (baseRelation.get().getObjectId() == baseRelation.get().getPlayerId()) {
                //It's root player (Player)
                base = objectsMap.get(baseRelation.get().getObjectId());
            } else {
                //It's role player (Role)
                base = rolesMap.get(baseRelation.get().getPlayerId());
            }
            return base;
        } else {
//            log.error("{} was not found", role.getClass().getName());
        }

        return null;
    }

    public Object getCompartment(Object obj){
        int compartmentId = m_activeCompartments.peek();
        int objId = obj.hashCode();
        Optional<Relation> compartmentRel = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId)
                .filter(r -> (obj instanceof Role)?
                                r.getRoleId() == objId:
                                r.getPlayerId() == objId)
                .findFirst();

        if(compartmentRel.isPresent()){
            return compartmentsMap.get(compartmentRel.get().getCompartmentId());
        }

        return null;
    }
}
