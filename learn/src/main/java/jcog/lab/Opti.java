package jcog.lab;

import java.util.List;
import java.util.function.Supplier;

/** simple Optimization<X> wrapper */
public class Opti<X> extends Optimize<X,X> {

    public Opti(Supplier<X> subj,
                Goal<X> goal,
                List<Var<X, ?>> vars /* unsorted */,
                List<Sensor<X, ?>> sensors) {
        super(subj, Supplier::get, goal, vars, sensors);
    }

    public Opti<X> run(int iterations) {
        runSync(iterations);
        return this;
    }

}