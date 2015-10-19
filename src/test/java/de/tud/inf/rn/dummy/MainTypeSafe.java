package de.tud.inf.rn.dummy;

import de.tud.inf.rn.actor.Compartment;

/**
 * Created by nguonly on 8/12/15.
 */
public class MainTypeSafe {
    public static void main(String[] agrs){
        int n = 1;
        long startTime = System.currentTimeMillis();

        for(int i=0; i<n; i++){
            //Person p = Player.initialize(Person.class);
            Person p = new Person();
            try(Comp comp = Compartment.initialize(Comp.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();

            }

            try(Comp comp = Compartment.initialize(Comp.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();

            }

            try(Comp comp = Compartment.initialize(Comp.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();

            }

            try(Comp comp = Compartment.initialize(Comp.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();

            }

            try(Comp comp = Compartment.initialize(Comp.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();

            }

            /*
            try(Comp comp = Compartment.initialize(Comp.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();

            }

            try(Comp comp = Compartment.initialize(Comp.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();
            }

            try(Comp comp = Compartment.initialize(Comp.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();

            }

            try(Comp comp = Compartment.initialize(Comp.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();

            }

            try(Comp comp = Compartment.initialize(Comp.class)){
                p.bind(Role01.class);
                p.bind(Role02.class);

                p.role(Role01.class).getName();
                p.role(Role02.class).getAddress();

            }
            */
        }

        long duration = System.currentTimeMillis() - startTime;

//        System.out.println("Duration(ms) : " + duration);
//        System.out.println("Duration(s)  ; " + duration/1000);
    }

    public void reflectiveDispatch(){
        Person p = new Person();
        try(Comp comp = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.invoke("getName");
            p.invoke("getAddress");

        }

        try(Comp comp = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.invoke("getName");
            p.invoke("getAddress");
        }

        try(Comp comp = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.invoke("getName");
            p.invoke("getAddress");
        }

        try(Comp comp = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();

            p.invoke("getName");
            p.invoke("getAddress");
        }

        try(Comp comp = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.invoke("getName");
            p.invoke("getAddress");
        }

        /*
        try(Comp comp = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.invoke("getName");
            p.invoke("getAddress");
        }

        try(Comp comp = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.invoke("getName");
            p.invoke("getAddress");
        }

        try(Comp comp = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.invoke("getName");
            p.invoke("getAddress");
        }

        try(Comp comp = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.invoke("getName");
            p.invoke("getAddress");
        }

        try(Comp comp = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.invoke("getName");
            p.invoke("getAddress");
        }
        */
    }

    public void oneCompartment(){
        Person p = new Person();
        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }
    }

    public void twoCompartment(){
        Person p = new Person();
        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }
    }

    public void threeCompartment(){
        Person p = new Person();
        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }
    }

    public void fourCompartment(){
        Person p = new Person();
        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }
    }

    public void fiveCompartment(){
        Person p = new Person();
        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }
    }

    public void sixCompartment(){
        Person p = new Person();
        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }
    }

    public void sevenCompartment(){
        Person p = new Person();
        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }
    }

    public void eightCompartment(){
        Person p = new Person();
        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }
    }

    public void nineCompartment(){
        Person p = new Person();
        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }
    }

    public void tenCompartment(){
        Person p = new Person();
        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }

        try(Comp t01 = Compartment.initialize(Comp.class)){
            p.bind(Role01.class);
            p.bind(Role02.class);

            p.role(Role01.class).getName();
            p.role(Role02.class).getAddress();
        }
    }
}
