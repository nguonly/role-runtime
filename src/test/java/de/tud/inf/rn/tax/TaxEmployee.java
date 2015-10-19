package de.tud.inf.rn.tax;

import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;

/**
 * Created by nguonly on 9/9/15.
 */
public class TaxEmployee extends Role {
    public void collectTax(double amount){
        Player taxPerson = (Player)getRootPlayer();
        String taxPersonName = taxPerson.invoke("getName", String.class);

        invokeCompartment("setRevenue", void.class, new Class[]{double.class}, new Object[]{amount});

        System.out.println(taxPersonName + " gets " + amount + " of tax");
    }
}
