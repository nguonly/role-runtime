package de.tud.inf.rn.actor;

import de.tud.inf.rn.registry.RegistryManager;

/**
 * Created by nguonly role 7/10/15.
 */
public class Compartment extends Player implements AutoCloseable{

    public static <T> T initialize(Class<T> compartment){
        return RegistryManager.getInstance().initializeCompartment(compartment);
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
