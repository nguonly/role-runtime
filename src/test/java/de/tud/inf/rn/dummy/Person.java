package de.tud.inf.rn.dummy;

import de.tud.inf.rn.actor.Player;

/**
 * Created by nguonly on 8/12/15.
 */
public class Person extends Player {
    public String getName(){
        return this.getClass().getName();
    }

    public String getAddress(){
        return "Player Address";
    }
}
