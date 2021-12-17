package jcog.math;


public final class NumberException extends RuntimeException {


    private final Number value;

    public NumberException(String message, Number value) {
        super(message);
        this.value = value;
    }

    public static NumberException NaN(Number x) {
        return new NumberException("NaN", x);
    }

    @Override
    public String toString() {
        return super.toString() + ": " + value;
    }
}
