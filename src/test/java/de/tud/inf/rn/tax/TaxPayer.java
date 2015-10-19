package de.tud.inf.rn.tax;

import de.tud.inf.rn.actor.Player;
import de.tud.inf.rn.actor.Role;

/**
 * Created by nguonly on 9/9/15.
 */
public class TaxPayer extends Role {
    public void pay(){
        Player company = (Player)getRootPlayer();
        double tax = company.invoke("taxToBePaid", double.class);

        Object[] players = getRootPlayer(TaxEmployee.class);
        for(Object player: players){
            ((Player)player).invoke("collectTax", void.class, new Class[]{double.class}, new Object[]{tax});
        }
    }
}
