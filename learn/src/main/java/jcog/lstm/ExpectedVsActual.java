package jcog.lstm;

import jcog.Str;

import java.util.Arrays;

/**
 * Created by me on 5/23/16.
 */
@Deprecated public final class ExpectedVsActual {

    public double[] expected;
    public double[] actual;
    public double[] predicted; //???

    /** forget rate, between 0 and 1 */
    public float forget;


    public static ExpectedVsActual the(int numActual, int numExpected) {
        return the(new double[numActual], new double[numExpected]);
    }

    public static ExpectedVsActual the(double[] actual, double[] expected) {
        return the(actual, expected, false);
    }

    public static ExpectedVsActual the(double[] actual, double[] expected, boolean reset) {
        ExpectedVsActual i = new ExpectedVsActual();
        i.actual = actual;
        i.expected = expected;
        i.forget = reset ? 1f : 0f;
        return i;
    }

    @Override
    public String toString() {
        return Str.n4(actual) + "    ||    " +
                Str.n4(expected) + "   ||   " +
                Str.n4(predicted)
                
                ;
    }

    public void zero() {
        Arrays.fill(actual, 0);
        if (expected!=null)
            Arrays.fill(expected, 0);
    }
}
