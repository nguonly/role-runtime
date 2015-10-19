package de.tud.inf.rn.otj;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;

/**
 * Created by nguonly on 8/12/15.
 */
public class Main {
    public static void main(String[] args){
        int n = 1_0000;
        long startTime = System.currentTimeMillis();

        for(int i=0; i<n; i++){
            Person p = Player.initialize(Person.class);
            try(Compartment comp = Compartment.initialize(Compartment.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.invoke("getName");
                p.invoke("getAddress");
            }

            try(Compartment comp = Compartment.initialize(Compartment.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.invoke("getName");
                p.invoke("getAddress");
            }

            try(Compartment comp = Compartment.initialize(Compartment.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.invoke("getName");
                p.invoke("getAddress");
            }

            try(Compartment comp = Compartment.initialize(Compartment.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.invoke("getName");
                p.invoke("getAddress");
            }

            try(Compartment comp = Compartment.initialize(Compartment.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.invoke("getName");
                p.invoke("getAddress");
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("Duration(ms) : " + duration);
        System.out.println("Duration(s)  ; " + duration/1000);
    }
}
