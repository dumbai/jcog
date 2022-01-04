package jcog.math;

import jcog.signal.FloatRange;

/** uses 2 alpha parameters: one when the value has increased, and another when the value decreases */
public class FloatMeanPN extends FloatMean {
    /**
     * superclass's alpha is effectively 'alphaIncrease'
     */
    private final FloatRange alphaDecrease;

    public FloatMeanPN(float alphaInc, float alphaDec) {
        this(alphaInc, alphaDec, true);
    }

    public FloatMeanPN(float alphaInc, float alphaDec, boolean lowOrHighPass) {
        super(alphaInc, lowOrHighPass);
        alphaDecrease = FloatRange.unit(alphaDec);
    }

    protected float alpha(float next, float prev) {
        if (prev!=prev)
            return 1;
        if (next!=next)
            return 0;
        if (next >= prev)
            return this.alpha.asFloat();
        else
            return this.alphaDecrease.asFloat();
    }

}