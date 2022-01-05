package jcog;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static jcog.Util.*;

/**
 * Fuzzy Logic functions and utilities
 * operations on fuzzy truth scalars in real domain: 0..+1
 *
 * https://en.wikipedia.org/wiki/Fuzzy_set_operations
 * https://en.wikipedia.org/wiki/Fuzzy_set#Fuzzy_set_operations
 * https://en.wikipedia.org/wiki/T-norm
 * http://www.math.sk/fsta2014/presentations/VemuriHareeshSrinath.pdf
 */
@Is("Fuzzy_logic") public enum Fuzzy {
    ;

    /**
     * intersection
     */
    public static double and(double a, double b) {
        return a * b;
    }

    public static float and(float a, float b) {
        return (float) and((double) a, b);
    }

    public static float and(float a, float b, float c) {
        return (float) (((double) a) * b * c);
    }

    /** negation */
    public static double not(double x) {
        return 1 - x;
    }



    /**
     * union
     */
    public static double or(double a, double b) {
        //return 1.0 - ((1.0 - a) * (1.0 - b));
        return fma(a - 1, 1 - b, +1);
    }
    public static float or(float a, float b) {
        return (float) or((double) a, b);
    }
    public static float or(float a, float b, float c) {
        return (float) (1 - ((1.0 - a) * (1.0 - b) * (1.0 - c)));
    }
    public static float orFast(float a, float b) {
        //return 1.0 - ((1.0 - a) * (1.0 - b));
        return fma(a - 1, 1 - b, +1f);
    }



    /**
     * symmetric difference: S(x,y) = (x & --y) | (y & --x)
     */
    public static double symmetricDifference(double x, double y) {
        return or(and(x, 1 - y), and(y, 1 - x));
    }

    public static double symmetricSimilarity(double x, double y) {
        return not(symmetricDifference(x, y));
    }

    /**
     * estimates y from: (x&y), x
     *
     *   XY = X * Y
     *    Y = XY / X
     *
     * this is a fuzzy t-norm approximation of division (multiplication inverse)
     *
     *      y ~= 1-(x * (1 - xy))
     *
     *      https://www.wolframalpha.com/input/?i=f%28x%2Cz%29+%3D+1-%28x+*+%281+-+z%29%29%2C+x+in+%5B0%2C1%5D%2C+z+in+%5B0%2C1%5D
     *
     * the 3d surface plots of y(xy, x) for x=0..1, xy=0..1, y=0..1
     * appear VERY similar to the pure division function
     *      y = xy/x
     * yet is completely bounded by the domain unlike division which can grow asymptotically y>1 for half the domain
     */
    public static double divideLike(double xy, double x) {
        //return 1 - ((1 - xy) * x);
        return fma(-x, 1-xy, +1);
    }



    public static double alignment(double a, double b, double epsilon) {
        return alignment(a, b, 0.5, epsilon);
    }

    /** absolute difference, normalized to the maximum polarity */
    public static double alignment(double a, double b, double center, double epsilon) {
        double y = Util.equals(a, b, epsilon) ? 1 :
                1 - abs(a - b) / (max(abs(a - center), abs(b - center)) * 2);
        //assertUnitized(y);
        return y;
    }

    public static double polarity(double freq) {
        return abs(freq - 0.5) * 2;
    }

    /** "XNR" (NOT XOR) - hyperbolic paraboloid
     * https://www.wolframalpha.com/input/?i=y+*+x+%2B+%281-y%29*%281-x%29
     */
    public static double xnr(double x, double y) {
        //return (x * y) + ((1-x)*(1-y));
        return fma(x, y, ((1-x)*(1-y)));
    }
    @Deprecated public static double xnrNorm(double x, double y) {
        double d =
                max(max(x, y), max(1-x, 1-y));

        return d < Float.MIN_NORMAL ? 0.5 : xnr(x/d,y/d);
    }
    public static double eqNorm(double x, double y) {
        double p =
                max(polarity(x), polarity(y));

        return p < Float.MIN_NORMAL ? 1 :
            equals(polarNorm(x,p), polarNorm(y,p));

    }

    private static double polarNorm(double x, double p) {
        return unpolarize(polarize(x)/p);
    }


    /**
     * XOR formulas
     * //
     * //  fXORa(x,y) = abs(x-y)
     * //  fXORb(x,y) = max(min(x,1-y), min(1-x,y))
     * //
     * //  fXOR1(x,y) = S(x−T(x,y),y−T(x,y))
     * //  fXOR2(x,y) = S(T(x,N(y)),T(y,N(x)))
     * //  fXOR3(x,y) = T(S(x,y),N(T(x,y)))
     * //  fXOR4(x,y) = T(S(x,y),S(N(x),N(y)))
     * //  fXOR5(x,y) = S(x,y)−T(S(x,y),T(x,y))
     */
    public static double xor(double x, double y) {
        return 1 - xnr(x, y);
    }

    public static double mean(double e, double i) {
        return Util.mean(e,i);
    }
    public static float mean(float e, float i) {
        return Util.mean(e, i);
    }

//    /**
//     * @param x in 0..1
//     * @param pow exponent
//     * balanced sqrt, 0.5 -> 0.5, 0 -> 0, 1 -> 1 .. but curved in between */
//    public static double pow(double x, double pow) {
//        return (Math.pow(polarity(x), pow) * (x < 0.5 ? -1 : +1))/2+0.5;
//    }

    public static double equals(double x, double y) {
        assertUnitized(x); assertUnitized(y);
        return 1 - abs(x-y);
    }

    /** https://stats.stackexchange.com/questions/398551/logistic-function-with-a-slope-but-no-asymptotes */
    public static double equalsPolar(double x, double y, float extreme) {
        return equals(polarCurve(x, extreme), polarCurve(y, extreme));
    }

    /** z is an extreme factor */
    private static double polarCurve(double x, float z) {
        x = (x - 0.5) * 2;
        // on 0..1 must be normalized
        // y * log(1 + z*x)
        // 1 = y * log(1 + z*1)
        //    y = 1/log(z+1))
        //return Math.signum(x) * Math.log(1 + Math.abs(x));

        double y = Math.signum(x) * Math.pow(abs(x), 1/z);
        y = (y/2)+0.5;
        return y;
    }

    /**
     * geometric mean
     *
     * @param arr The inputs, each in [0, 1]
     * @return The geometric average the inputs
     */
    public static float meanGeo(float... arr) {
        double product = 1;
        for (float f : arr) {
            if (f == 0) return 0;
            product *= f;
        }
        return (float) Math.pow(product, 1.0 / arr.length);
    }

    public static float meanGeo(float a, float b) {
        return (float) meanGeo((double)a, b);
    }

    @Is("Geometric_mean") public static double meanGeo(double a, double b) {
        return Math.sqrt(a * b);
    }

    @Is("Harmonic_mean") public static double meanHarmonic(double a, double b) {
        return 2*a*b/(a+b);
    }

    /** https://academo.org/demos/3d-surface-plotter/?expression=(x%2By)%2F(1%2Bx*y)&xRange=0%2C1&yRange=0%2C1&resolution=27 */
    public static double einsteinSum(double x, double y) {
        return (x+y)/(1+x*y);
    }

    public static int meanGeo(int a, int b) {
        return Math.round(meanGeo((float)a, b));
    }

    /** rounds down */
    public static int mean(int a, int b) {
        //return (a + b) / 2;
        return a + (b - a) / 2;
    }

    /** this should be overflow safe */
    public static long mean(long a, long b) {
        //return (a + b) / 2;
        return a + (b - a) / 2L;
    }

    /** includes exact 0.5-unit sub-intervals */
    public static double meanDouble(long a, long b) {
        long mLong = mean(a, b);
        return (b - a) % 2 != 0 ?
            mLong + 0.5 :
            mLong;
    }

    /** maps 0..1 to -1..+1 */
    public static float polarize(float u) {
        return u == u ? unitizeSafe(u) * 2 - 1 : Float.NaN;
    }

    /** maps 0..1 to -1..+1 */
    public static double polarize(double u) {
        return u == u ? unitizeSafe(u) * 2 - 1 : Double.NaN;
    }

    /** maps -1..+1 to 0..1 */
    public static double unpolarize(double u) {
        return u == u ? /*unitizeSafe*/((clampSafe(u, -1, +1) + 1) / 2) : Double.NaN;
    }
    public static float unpolarize(float u) {
        return u == u ? /*unitizeSafe*/((clampSafe(u, -1, +1) + 1) / 2) : Float.NaN;
    }

    public static double intersect(double x, double y) {
        return and(x,y);
        //return xnr(x,y);
        //return mean(x,y);
    }

    public static double divide(double xy, double x) {
        return xy/x;
        //return xy*2 - x;
    }

}