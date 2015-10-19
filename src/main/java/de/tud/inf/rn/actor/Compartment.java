package de.tud.inf.rn.actor;

import de.tud.inf.rn.registry.RegistryManager;

/**
 * Created by nguonly role 7/10/15.
 */
public class Compartment extends Player implements AutoCloseable{

    public static <T> T initialize(Class<T> compartment){
        return RegistryManager.getInstance().initializeCompartment(compartment, null, null);
    }

    public static <T> T initialize(Class<T> compartment, Class[] constructorArgumentTypes, Object[] constructorArgumentValues){
        return RegistryManager.getInstance().initializeCompartment(compartment, constructorArgumentTypes, constructorArgumentValues);
    }

    final public void activate(){
        m_registryManager.registerCompartment(this);
    }

    final public void deActivate(){
        close();
    }

    @Override
    public void close() {
        try {
            m_registryManager.destroyActiveCompartment(this);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
