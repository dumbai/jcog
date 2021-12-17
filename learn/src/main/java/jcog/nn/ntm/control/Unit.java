package jcog.nn.ntm.control;

import java.util.function.Consumer;

public class Unit {
    public double value, grad;

    public Unit() {

    }

    public Unit(double value) {
        this.value = value;
    }

    private static Consumer<Unit[]> vectorUpdateAction(Consumer<Unit> updateAction) {
        return (units) -> {
            for (Unit unit : units)
                updateAction.accept(unit);
        };
    }

    static Consumer<Unit[][]> tensor2UpdateAction(Consumer<Unit> updateAction) {
        Consumer<Unit[]> vectorUpdateAction = vectorUpdateAction(updateAction);
        return (units) -> {
            for (Unit[] unit : units)
                vectorUpdateAction.accept(unit);
        };
    }

    static Consumer<Unit[][][]> tensor3UpdateAction(Consumer<Unit> updateAction) {
        Consumer<Unit[][]> tensor2UpdateAction = tensor2UpdateAction(updateAction);
        return (units) -> {
            for (Unit[][] unit : units)
                tensor2UpdateAction.accept(unit);
        };
    }

    public String toString() {
        return "<" + value + ',' + grad + '>';
    }

    public final void setDelta(double target) {
        grad = value - target;
    }

    public double getValue() {
        return value;
    }
}