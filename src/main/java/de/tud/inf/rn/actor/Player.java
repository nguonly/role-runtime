package de.tud.inf.rn.actor;

import de.tud.inf.rn.registry.RegistryManager;

/**
 * Created by nguonly role 7/9/15.
 */
public class Player {
    protected final RegistryManager m_registryManager = RegistryManager.getInstance();

    //public int Id;

    //private String m_packageStr = "de.tud.inf.rn.role.%s";
    //private String m_packageStr = "de.tud.inf.rn.role.%s";

    public Role bind(Compartment compartment, Class role){

        return m_registryManager.bind(compartment, this, role);
    }


    public Role bind(Class role){
        return bind(null, role);
    }

    /* Invoke with implicit compartment */
    public void invoke(String methodName){
        m_registryManager.playerInvokeRole(null, this, methodName, null, null, null);
    }

    public <T> T invoke(String methodName, Class<T> returnType){
        return m_registryManager.playerInvokeRole(null, this, methodName, returnType, null, null);
    }

    public <T> T invoke(String methodName, Class<T> returnType, Class[] argumentTypes,  Object[] argumentValues){
        return m_registryManager.playerInvokeRole(null, this, methodName, returnType, argumentTypes, argumentValues);
    }

    public void invoke(String methodName, Class[] argumentTypes,  Object[] argumentValues){
        m_registryManager.playerInvokeRole(null, this, methodName, null, argumentTypes, argumentValues);
    }
    /* End of Invoke with implicit compartment */

    /* Invoke with compartment explicit */
    public void invoke(Compartment compartment, String methodName){
        m_registryManager.playerInvokeRole(compartment, this, methodName, null, null, null);
    }

    public <T> T invoke(Compartment compartment, String methodName, Class<T> returnType){
        return m_registryManager.playerInvokeRole(compartment, this, methodName, returnType, null, null);
    }

    public <T> T invoke(Compartment compartment, String methodName, Class<T> returnType, Class[] argumentTypes,  Object[] argumentValues){
        return m_registryManager.playerInvokeRole(compartment, this, methodName, returnType, argumentTypes, argumentValues);
    }

    public void invoke(Compartment compartment, String methodName, Class[] argumentTypes,  Object[] argumentValues){
        m_registryManager.playerInvokeRole(compartment, this, methodName, null, argumentTypes, argumentValues);
    }
    /* End of Invoke with compartment explicit */

    /* Invoke compartment method */
    public <T> T invokeCompartment(Compartment compartment, String methodName, Class<T> returnType, Class[] argumentTypes,  Object[] argumentValues){
        return m_registryManager.invokeCompartment(compartment, true, this, methodName, returnType, argumentTypes, argumentValues);
    }

    public <T> T invokeCompartment(Compartment compartment, String methodName, Class<T> returnType){
        return m_registryManager.invokeCompartment(compartment, true, this, methodName, returnType, null, null);
    }

    public void invokeCompartment(Compartment compartment, String methodName, Class[] argumentTypes, Object[] argumentValues){
        m_registryManager.invokeCompartment(compartment, true, this, methodName, null, argumentTypes, argumentValues);
    }

    public void invokeCompartment(Compartment compartment, String methodName){
        m_registryManager.invokeCompartment(compartment, true, this, methodName, null, null, null);
    }

    /* End of invoke compartment method */

    /* invoke compartment method with current active compartment */
    public <T> T invokeCompartment(String methodName, Class<T> returnType, Class[] arguementTypes,  Object[] argumentValues){
        return m_registryManager.invokeCompartment(null, true, this, methodName, returnType, arguementTypes, argumentValues);
    }

    public <T> T invokeCompartment(String methodName, Class<T> returnType){
        return m_registryManager.invokeCompartment(null, true, this, methodName, returnType, null, null);
    }

    public void invokeCompartment(String methodName, Class[] argumentType, Object[] argumentValues){
        m_registryManager.invokeCompartment(null, true, this, methodName, null, argumentType, argumentValues);
    }

    public void invokeCompartment(String methodName){
        m_registryManager.invokeCompartment(null, true, this, methodName, null, null, null);
    }
    /* end of invoke compartment method with current active compartment */


    public void unbind(Class role) {
        m_registryManager.unbind(this, role);
    }

    public void unbindAll(){
        m_registryManager.unbindAll(this);
    }

    public void prohibit(Compartment compartment, Class role){
        m_registryManager.prohibit(compartment, this, role);
    }

    public void prohibit(Class role){
        prohibit(null, role);
    }

    /**
     * Transfer
     */
    public void transfer(Class role, Object to){
        transfer(role, to, null);
    }

    public void transfer(Class role, Object to, Compartment toCompartment){
        m_registryManager.transfer(role, this, to, toCompartment);
    }

    public static <T> T initialize(Class<T> player){

        return RegistryManager.getInstance().initializePlayer(player);
    }

    public <T> T role(Compartment compartment, Class<T> roleClass){
        return m_registryManager.role(compartment, roleClass);
    }

    public <T> T role(Class<T> roleClass){
        return m_registryManager.role(null, roleClass);
    }
}
