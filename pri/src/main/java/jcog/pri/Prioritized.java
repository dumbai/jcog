package jcog.pri;


import jcog.Is;
import jcog.Str;
import jcog.data.bit.FixedPoint;
import org.jetbrains.annotations.Nullable;

/**
 * something which has a priority floating point value
 *      reports a priority scalar value (32-bit float precision)
 *      NaN means it is 'deleted' which is a valid and testable state
 */
@FunctionalInterface
@Is({"Demand", "Microeconomics", "Macroeconomics"})
public interface Prioritized  {

    /**
     * global minimum difference necessary to indicate a significant modification in budget float number components
     * TODO find if there is a better number
     *
     * 32-bit float has about 7 decimal digits of reliable precision
     * https://en.wikipedia.org/wiki/Floating-point_arithmetic#Internal_representation
     */
    float EPSILON =
        (float) FixedPoint.Epsilon16;
        //0.00001f;
        //0.000001f;
        //0.00000001f;


    /**
     * returns the local (cached) priority value
     * if the value is NaN, then it means this has been deleted
     */
    float pri();

    /**
     * common instance for a 'Deleted budget'.
     */
    Prioritized Deleted = new PriRO(Float.NaN);
    /**
     * common instance for a 'full budget'.
     */
    Prioritized One = new PriRO(1f);
    /**
     * common instance for a 'half budget'.
     */
    Prioritized Half = new PriRO(0.5f);
    /**
     * common instance for a 'zero budget'.
     */
    Prioritized Zero = new PriRO(0);


    static String toString(Prioritized b) {
        return toStringBuilder(null, Str.n4(b.pri())).toString();
    }

    
    static StringBuilder toStringBuilder(@Nullable StringBuilder sb, String priorityString) {
        int c = 1 + priorityString.length();
        if (sb == null)
            sb = new StringBuilder(c);
        else {
            sb.ensureCapacity(c);
        }

        return sb.append('$').append(priorityString);
    }

    


    default float priElse(float valueIfDeleted) {
        float p = pri();
        return p == p ? p : valueIfDeleted;
    }

    default float priElseZero() {
        return priElse(0);
    }

    default float priElseNeg1() {
        return priElse(-1);
    }


    default String getBudgetString() {
        return Prioritized.toString(this);
    }


    /**
     * Briefly display the BudgetValue
     *
     * @return String representation of the value with 2-digit accuracy
     */
    default Appendable toBudgetStringExternal() {
        return toBudgetStringExternal(null);
    }

    default StringBuilder toBudgetStringExternal(StringBuilder sb) {
        return Prioritized.toStringBuilder(sb, Str.n2(pri()));
    }

    default String toBudgetString() {
        return toBudgetStringExternal().toString();
    }

//    default float priNormalized(float priMin, float priMax) {
//        final float p = pri();
//        return p!=p ? Float.NaN : Util.normalizeSafe(p, priMin, priMax);
//    }


//    static float sum(Prioritized... src) {
//        return Util.sum(Prioritized::priElseZero, src);
//    }
//    static float max(Prioritized... src) {
//        return Util.max(Prioritized::priElseZero, src);
//    }
//
//    static <X extends Prioritizable> void normalize(X[] xx, float target) {
//        int l = xx.length;
//        assert (target == target);
//        assert (l > 0);
//
//        float ss = sum(xx);
//        if (ss <= ScalarValue.EPSILON)
//            return;
//
//        float factor = target / ss;
//
//        for (X x : xx)
//            x.priMult(factor);
//
//    }
}