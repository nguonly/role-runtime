package de.tud.inf.rn.actor;

import de.tud.inf.rn.registry.RegistryManager;

/**
 * Created by nguonly role 7/10/15.
 */
public class Role {
    public int Id;

    private String m_packageStr = "de.tud.inf.rn.role.%s";

    RegistryManager registryManager = RegistryManager.getInstance();

    public void inherit(Compartment compartment, String superRole){
        String fullRole = String.format(m_packageStr, superRole);
        registryManager.inherit(compartment, this, fullRole);
    }

    public void inherit(String superRole){
        inherit(null, superRole);
    }

    public Role bind(Compartment compartment, Class role){

        //String fullRole = String.format(m_packageStr, role);
        return registryManager.rolePlaysRole(compartment, this, role);
    }

    public Role bind(Class role){
        return bind(null, role);
    }

    public void unbind(Class role){

        registryManager.unbind(this, role);
    }

    public void prohibit(Compartment compartment, Class role){
        registryManager.prohibit(compartment, this, role);
    }

    public void prohibit(Class role){
        prohibit(null, role);
    }

    public void invoke(String methodName){
        registryManager.roleInvokeRole(this, methodName, null, null, null);
    }

    public void invoke(String methodName, Class[] argumentTypes,  Object[] argumentValus){
        registryManager.roleInvokeRole(this, methodName, null, argumentTypes, argumentValus);
    }

    public <T> T invoke(String methodName, Class<T> returnType){
        return registryManager.roleInvokeRole(this, methodName, returnType, null, null);
    }
    public <T> T invoke(String methodName, Class<T> returnType, Class[] argumentTypes,  Object[] argumentValues){
        return registryManager.roleInvokeRole(this, methodName, returnType, argumentTypes, argumentValues);
    }

    public <T> T invokeBase(String methodName, Class<T> returnType, Class[] argumentTypes, Object[] argumentValues){
        return registryManager.invokeBase(this, methodName, returnType, argumentTypes, argumentValues);
    }

    public void invokeBase(String methodName, Class[] argumentTypes, Object[] argumentValues){
        registryManager.invokeBase(this, methodName, null, argumentTypes, argumentValues);
    }

    public <T> T invokeBase(String methodName, Class<T> returnType){
        return registryManager.invokeBase(this, methodName, returnType, null, null);
    }

    public void invokeBase(String methodName){
        registryManager.invokeBase(this, methodName, null, null, null);
    }

    /* Invoke compartment method */
    public <T> T invokeCompartment(Compartment compartment, String methodName, Class<T> returnType, Class[] arguementTypes,  Object[] argumentValues){
        return registryManager.invokeCompartment(compartment, false, this, methodName, returnType, arguementTypes, argumentValues);
    }

    public <T> T invokeCompartment(Compartment compartment, String methodName, Class<T> returnType){
        return registryManager.invokeCompartment(compartment, false, this, methodName, returnType, null, null);
    }

    public void invokeCompartment(Compartment compartment, String methodName, Class[] argumentTypes, Object[] argumentValues){
        registryManager.invokeCompartment(compartment, false, this, methodName, null, argumentTypes, argumentValues);
    }

    public void invokeCompartment(Compartment compartment, String methodName){
        registryManager.invokeCompartment(compartment, false, this, methodName, null, null, null);
    }

    /* End of invoke compartment mehtod */

    /* invoke compartment method with current active compartment */
    public <T> T invokeCompartment(String methodName, Class<T> returnType, Class[] arguementTypes,  Object[] argumentValues){
        return registryManager.invokeCompartment(null, false, this, methodName, returnType, arguementTypes, argumentValues);
    }

    public <T> T invokeCompartment(String methodName, Class<T> returnType){
        return registryManager.invokeCompartment(null, false, this, methodName, returnType, null, null);
    }

    public void invokeCompartment(String methodName, Class[] argumentType, Object[] argumentValues){
        registryManager.invokeCompartment(null, false, this, methodName, null, argumentType, argumentValues);
    }

    public void invokeCompartment(String methodName){
        registryManager.invokeCompartment(null, false, this, methodName, null, null, null);
    }
    /* end of invoke compartment method with current active compartment */

    /* type safe base reference */
    public <T> T base(Compartment compartment, Class<T> baseClass){
        return registryManager.base(compartment, this, baseClass);
    }

    public <T> T base(Class<T> baseClass){
        return registryManager.base(null, this, baseClass);
    }

    public <T> T compartment(Compartment compartment, Class<T> compartmentClass){
        return registryManager.compartment(compartment, compartmentClass);
    }

    public <T> T compartment(Class<T> compartmentClass){
        return registryManager.compartment(null, compartmentClass);
    }
}
