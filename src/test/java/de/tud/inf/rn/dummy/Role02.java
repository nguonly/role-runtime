package de.tud.inf.rn.dummy;

import de.tud.inf.rn.actor.Role;

/**
 * Created by nguonly on 8/12/15.
 */
public class Role02 extends Role {
    public String getAddress(){
        return "Address of " + Role02.class.getName();
    }
}
