package jcog.activation;

import static jcog.Util.lerpSafe;
import static jcog.Util.unitizeSafe;

/**
 * linear sigmoid-like variant
 * TODO abstract to generic list of piecewise-linear segments for different shapes
 */
public class SigLinearActivation implements DiffableFunction {

    /**
     * "bandwidth".  in sigmoid, it is effectively infinity
     */
    private final float xMin, xMax, xRange;
    private final float yMin, yMax;
    private final float slope;

    public SigLinearActivation() {
        this(-1, 1, 0, 1);
    }

    public SigLinearActivation(float xMin, float xMax, float yMin, float yMax) {
        this.xMin = xMin; this.xMax = xMax; this.yMin = yMin; this.yMax = yMax;
        this.xRange = xMax - xMin;

        float yRange = yMax - yMin;
        this.slope = yRange / xRange;
    }

    @Override
    public double derivative(double x) {
        return
            x <= xMin || x >= xMax ?
            //x < xMin || x > xMax ?
              0 : slope;
    }

    @Override
    public double valueOf(double x) {
        return lerpSafe(unitizeSafe((x - xMin) / xRange), yMin, yMax);
    }
}