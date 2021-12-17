package jcog.constraint.continuous;

/**
 * Created by alex on 30/01/15.
 */
class Symbol {

    enum Type {
        INVALID,
        EXTERNAL,
        SLACK,
        ERROR,
        DUMMY
    }

    public final Type type;

    Symbol(Type type) {
        this.type = type;
    }

}
