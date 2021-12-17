package jcog.constraint.continuous;

public abstract class BoundVar<X> extends DoubleVar {

    public BoundVar(String name) {
        super(name);

    }

    public void load() {
        super.value(get());
    }
    public void save() {
        set(floatValue());
    }

    protected abstract double get();
    protected abstract void set(double next);
}
