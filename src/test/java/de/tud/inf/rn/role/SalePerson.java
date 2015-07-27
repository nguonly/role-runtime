package de.tud.inf.rn.role;

import de.tud.inf.rn.actor.Role;

/**
 * Created by nguonly role 7/10/15.
 */
public class SalePerson extends Role {
    public String sale(String item, int n){
        return String.format("Sale %s with %d quantities", item, n);
    }
}
