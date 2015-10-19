package de.tud.inf.rn.otj;

import de.tud.inf.rn.actor.Compartment;
import de.tud.inf.rn.actor.Player;

/**
 * Created by nguonly on 8/12/15.
 */
public class MainTypeSafe {
    public static void main(String[] agrs){
        int n = 1_0000;
        long startTime = System.currentTimeMillis();

        for(int i=0; i<n; i++){
            Person p = Player.initialize(Person.class);
            //Person p = new Person();
            try(Compartment comp = Compartment.initialize(Compartment.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();
                //p.invoke("getName");
                //p.invoke("getAddress");
                //Role01 r1 = new Role01();
//                Role02 r2 = new Role02();
//                r1.getName();
//                r2.getAddress();
            }

            try(Compartment comp = Compartment.initialize(Compartment.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();
//                Role01 r1 = new Role01();
//                Role02 r2 = new Role02();
//                r1.getName();
//                r2.getAddress();
            }

            try(Compartment comp = Compartment.initialize(Compartment.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();
//                Role01 r1 = new Role01();
//                Role02 r2 = new Role02();
//                r1.getName();
//                r2.getAddress();
            }

            try(Compartment comp = Compartment.initialize(Compartment.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();

//                Role01 r1 = new Role01();
//                Role02 r2 = new Role02();
//                r1.getName();
//                r2.getAddress();
            }

            try(Compartment comp = Compartment.initialize(Compartment.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();
//                Role01 r1 = new Role01();
//                Role02 r2 = new Role02();
//                r1.getName();
//                r2.getAddress();
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("Duration(ms) : " + duration);
        System.out.println("Duration(s)  ; " + duration/1000);
    }
}
