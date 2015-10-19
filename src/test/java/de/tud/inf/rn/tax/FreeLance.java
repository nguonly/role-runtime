package de.tud.inf.rn.tax;

import de.tud.inf.rn.actor.Role;

/**
 * Created by nguonly on 9/10/15.
 */
public class FreeLance extends Role {
    private double salary;

    public void earn(double amount){
        salary += amount;
    }

    public double getMoney(){
        return salary;
    }

    public double taxToBePaid(){
        double tax = salary*10/100;

        salary -= tax;

        return tax;
    }
}
