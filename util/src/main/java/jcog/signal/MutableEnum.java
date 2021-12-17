package jcog.signal;

public class MutableEnum<C extends Enum<C>> extends IntRange {

    public final Class<? extends Enum> klass;
    private final Enum[] konstants;

    public MutableEnum(C initialValue) {
        super(initialValue.ordinal(), 0, initialValue.getDeclaringClass().getEnumConstants().length);
        this.klass = initialValue.getDeclaringClass();
        this.konstants = klass.getEnumConstants();
    }

    public MutableEnum<C> set(C c) {
        set(c.ordinal());
        return this;
    }

    public C get() {
        return (C) konstants[getAsInt()];
    }
}