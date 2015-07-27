package de.tud.inf.rn.atm;

import de.tud.inf.rn.actor.Compartment;

/**
 * Created by nguonly role 7/18/15.
 */
public class SpecialConditions extends Compartment {
    Account account;
    public void participate(Account account){
        this.account = account;
        account.bind(BonusAccount.class);
    }

    public void activate(){

    }


}
