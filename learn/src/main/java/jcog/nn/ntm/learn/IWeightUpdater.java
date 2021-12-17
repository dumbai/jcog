package jcog.nn.ntm.learn;

import jcog.nn.ntm.NTM;
import jcog.nn.ntm.control.UMatrix;
import jcog.nn.ntm.control.UVector;
import jcog.nn.ntm.control.Unit;

public interface IWeightUpdater {
    void reset();

    void update(Unit data);

    void update(UVector data);

    default void update(Unit[] data) {
        for (Unit unit : data) update(unit);
    }

    default void update(Unit[][] data) {
        for (Unit[] units : data) update(units);
    }

    default void update(Unit[][][] data) {
        for (Unit[][] units : data) update(units);
    }

    default void update(UMatrix data) {
        for (UVector v : data.row) update(v);
    }

    default void apply(NTM x) {
        reset();
        x.update(this);
    }

    IWeightUpdater GradientReset = new IWeightUpdater() {

        @Override
        public void reset() {
        }
        @Override
        public void update(Unit data) {
            data.grad = 0;
        }
        @Override
        public void update(UVector data) {
            data.clearGrad();
        }
    };
}