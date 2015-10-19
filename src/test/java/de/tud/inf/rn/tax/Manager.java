package de.tud.inf.rn.tax;

import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;
import de.tud.inf.rn.role.*;

/**
 * Created by nguonly on 9/9/15.
 */
public class Manager extends Role {
    private double salary = 2000;

    public double getSalary(){
        return salary;
    }

    public void assignTask(){
        Player manager = (Player)getRootPlayer();
        String managerName = manager.invoke("getName", String.class);

        System.out.println(managerName + " assigns work to Employee");

        Object[] employees = getRootPlayer(Employee.class);
        for(Object obj: employees){
            ((Player)obj).invoke("work");
        }
    }
}
