package jcog.math;

import jcog.Is;
import jcog.Util;

import static jcog.Util.sqr;
import static jcog.Util.unitizeSafe;

/**
 * decides the truth value of a 'digit'. returns frequency float
 *
 * @param conceptIndex the 'digit' concept
 * @param x            the value being input
 * @maxDigits the total size of the set of digits being calculated
 */
@FunctionalInterface
@Is({"Digitization",
    "Analog-to-digital_converter",
    "Quantization_(signal_processing)",
    "Data_binning", "Fuzzy_logic"
})
public interface Digitize {
    /**
     * "HARD" - analogous to a filled volume of liquid
     * <p>
     * [ ] [ ] [ ] [ ] 0.00
     * [x] [ ] [ ] [ ] 0.25
     * [x] [x] [ ] [ ] 0.50
     * [x] [x] [x] [ ] 0.75
     * [x] [x] [x] [x] 1.00
     * <p>
     * key:
     * [ ] = freq 0
     * [x] = freq 1,
     * TODO
     */
    Digitize Fluid = (v, iDiscrete, n) -> {

        int vDiscrete = Util.bin(v, n);

        float f;
        if (iDiscrete < vDiscrete) {
            //below
            f = 1;
        }else if (iDiscrete == vDiscrete) {
            //partial boundary
            float i = ((float)iDiscrete) / n;
            float x = (v - i) * n;
            f = unitizeSafe(x);
        } else {
            //above
            f = 0;
        }

        return f;

    };
    /**
     * hard
     */
    Digitize BinaryNeedle = (v, i, indices) -> {
        float vv = v * indices;
        int which = (int) vv;
        return i == which ? 1 : 0;
    };
    /**
     * analogous to a needle on a guage, the needle being the triangle spanning several of the 'digits'
     * /          |       \
     * /         / \        \
     * /        /   \         \
     * + + +    + + +     + + +
     * TODO need to analyze the interaction of the produced frequency values being reported by all concepts.
     */
    Digitize FuzzyNeedle = (v, i, indices) -> {
        float dr = 1f / (indices - 1);
        return Math.max(0, 1 - Math.abs((i * dr) - v) / dr);
    };
    /**
     * http://www.personal.reading.ac.uk/~sis01xh/teaching/ComputerControl/fcslide3.pdf
     *
     * */
    @Is("Radial_basis_function")
    Digitize FuzzyGaussian = (v, i, indices) -> {
       double dx = v - (((float)i)/(indices-1));
       double width = 0.5f/indices;
       return (float) Math.exp(-sqr(dx)/(2*sqr(width)));
    };
    Digitize FuzzyNeedleCurve = (v, i, indices) -> {
        float dr = 1f / (indices - 1);
        return Math.max(0, 1f - sqr(Math.abs((i * dr) - v) / dr));
        //return  Util.sqrt(Math.max(0,1f - Math.abs((i * dr) - v) / dr));
    };
    /**
     * TODO not quite working yet. it is supposed to recursively subdivide like a binary number, and each concept represents the balance corresponding to each radix's progressively increasing sensitivity
     */
    Digitize FuzzyBinary = (v, i, indices) -> {


        float b = v;
        float dv = 1f;
        for (int j = 0; j < i; j++) {
            dv /= 2f;
            b = Math.max(0, b - dv);
        }


        return b / (dv);
    };
    Digitize TriState_2ary = (v, i, indices) -> {
        //assert(indices==2);
        if (i == 0) {
            float pos = 2 * Math.max(0, v - 0.5f);
            return pos;
        } else {
            float neg = 2 * Math.max(0, 0.5f - v);
            return neg;
        }
    };

    float digit(float x, int digit, int maxDigits);

    default float defaultTruth() {
        return Float.NaN;
    }
}