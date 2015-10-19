package de.tud.inf.rn.tax;

import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;

/**
 * Created by nguonly on 9/9/15.
 */
public class Employee extends Role {
    private double salary=1500;

    public double getSalary(){
        return salary;
    }

    public void work(){
        Player person = (Player)getRootPlayer();
        String name = person.invoke("getName", String.class);

        System.out.println(name + " works");
    }
}
