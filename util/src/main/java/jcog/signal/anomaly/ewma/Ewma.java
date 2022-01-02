/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcog.signal.anomaly.ewma;

import jcog.math.FloatSupplier;
import org.eclipse.collections.api.block.function.primitive.DoubleToDoubleFunction;

import static java.lang.Math.sqrt;
import static jcog.Util.*;

// TODO Add model warmup param and anomaly level. See e.g. CUSUM, Individuals, PEWMA. [WLW]

/**
 * <p>
 * Anomaly detector based on the exponential weighted moving average (EWMA) chart, a type of control chart used in
 * statistical quality control. This is an online algorithm, meaning that it updates the thresholds incrementally as new
 * data comes in.
 * </p>
 * <p>
 * EWMA is also called "Single Exponential Smoothing", "Simple Exponential Smoothing" or "Basic Exponential Smoothing".
 * </p>
 * <p>
 * It takes a little while before the internal mean and variance estimates converge to something that makes sense. As a
 * rule of thumb, feed the detector 10 data points or so before using it for actual anomaly detection.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/EWMA_chart">EWMA chart</a>
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation">Exponentially weighted moving average and standard deviation</a>
 * @see <a href="https://www.itl.nist.gov/div898/handbook/pmc/section3/pmc324.htm">EWMA Control Charts</a>
 */
public final class Ewma {

    /**
     * Mean estimate.
     */
    private volatile double mean;

    /**
     * Variance estimate.
     */
    private double variance;

    public final EwmaParams param = new EwmaParams();

    double current;

    //TODO fix with FloatNormalizer's half-life calculation
//    /** https://en.wikipedia.org/wiki/Time_constant#Exponential_decay */
//    public static Ewma withHalfLife(double l) {
//        return new Ewma(l / Math.log(2)); //TODO fix
//    }

    public Ewma() {
        this(0.5);
    }

    public Ewma(double alpha) {
        this(alpha, alpha);
    }

    public Ewma(double alphaPos, double alphaNeg) {
        reset(param.meanInit);
        alphaPos(alphaPos);
        alphaNeg(alphaNeg);
    }

    public Ewma period(int halfLifeExpand, int halfLifeContract) {
        return alphaPos(halflifeRate(halfLifeExpand)).
               alphaNeg(halflifeRate(halfLifeContract))
        ;
    }

    public final Ewma period(int halfLife) {
        return alpha(halflifeRate(halfLife));
    }

    public final Ewma alpha(double a) {
        alphaPos(a); alphaNeg(a);
        return this;
    }

    public final Ewma alphaNeg(double a) {
        assertUnitized(a);
        param.alphaNeg = a;
        return this;
    }

    public final  Ewma alphaPos(double a) {
        assertUnitized(a);
        param.alphaPos = a;
        return this;
    }

    public void accept(double what) {
        assertFinite(what);

        final double meanPrev = this.mean;
        if (meanPrev!=meanPrev) {
            //initialize with first finite value
            reset(what);
            return;
        }

        this.current = what;

        //notNull(metricData, "metricData can't be null");

        // https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation
        // http://people.ds.cam.ac.uk/fanf2/hermes/doc/antiforgery/stats.pdf
        double diff = what - meanPrev;
        if (Math.abs(diff) > Double.MIN_NORMAL) {
            double alpha = param.alpha(diff);
            double incr = alpha * diff;
            this.mean = meanPrev + incr;

            // Welford's algorithm for computing the variance online
            // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
            // https://www.johndcook.com/blog/2008/09/26/comparing-three-methods-of-computing-standard-deviation/
            this.variance = (1 - alpha) * (this.variance + diff * incr);
        }
    }

    public Ewma reset(double what) {
        this.current = this.mean = what;
        this.variance = 0;
        return this;
    }

    public AnomalyLevel classify() {
        return classify(current);
    }

    public final DoubleToDoubleFunction fn() {
        return this::acceptAndGetMean;
    }

    public AnomalyLevel classify(double value) {
        double stdDev = sqrt(variance);
        double weakDelta = param.weakSigmas * stdDev;
        double strongDelta = param.strongSigmas * stdDev;

        double mean = this.mean;

        AnomalyThresholds thresholds = new AnomalyThresholds(
                mean + strongDelta,
                mean + weakDelta,
                mean - weakDelta,
                mean - strongDelta
        );
        return thresholds.classify(value);
    }

    public final double acceptAndGetMean(double v) {
        accept(v);
        return mean();
    }

    public final float meanFloat() {
        return (float)mean;
    }
    public final double mean() {
        return mean;
    }

    public final Ewma with(double value) {
        accept(value);
        return this;
    }

    public FloatSupplier on(FloatSupplier floatSupplier) {
        return ()-> (float) acceptAndGetMean(floatSupplier.asFloat());
    }

    public static final class EwmaParams  {

        /**
         * Smoothing param. Somewhat misnamed because higher values lead to less smoothing, but it's called the
         * smoothing parameter in the literature.
         * TODO FloatRange
         */
        private double alphaPos;
        private double alphaNeg;

        /**
         * Weak threshold sigmas.
         */
        private double weakSigmas = 3.0;

        /**
         * Strong threshold sigmas.
         */
        private double strongSigmas = 4.0;

        /**
         * Initial mean estimate.
         */
        private double meanInit = Double.NaN;

        public double alpha(double diff) {
            return diff >= 0 ? alphaPos : alphaNeg;
        }

//        public void validate() {
//            assert(0.0 <= alpha && alpha <= 1.0): "Required: alpha in the range [0, 1]";
//            assert(weakSigmas > 0.0): "Required: weakSigmas > 0.0";
//            assert(strongSigmas > weakSigmas): "Required: strongSigmas > weakSigmas";
//        }
    }

    /**
     * Anomaly level enum.
     */
    public enum AnomalyLevel {

        /**
         * Normal data point (not an anomaly).
         */
        NORMAL,

        /**
         * Weak outlier.
         */
        WEAK,

        /**
         * Strong outlier.
         */
        STRONG,

        /**
         * No classification because the model is warming up.
         */
        MODEL_WARMUP,

        /**
         * Unknown outlier. Should be used when we are not sure about the anomaly level. e.g. during the warm up period.
         */
        UNKNOWN
    }
    
    /**
     * Weak and strong thresholds to support both one- and two-tailed tests.
     */
    private static final class AnomalyThresholds {
        private final double upperStrong;
        private final double upperWeak;
        private final double lowerStrong;
        private final double lowerWeak;

        AnomalyThresholds(double upperStrong, double upperWeak, double lowerStrong, double lowerWeak) {
            this.upperStrong = upperStrong;
            this.upperWeak = upperWeak;
            this.lowerStrong = lowerStrong;
            this.lowerWeak = lowerWeak;
        }

        //
    //    @JsonCreator
    //    public AnomalyThresholds(
    //            @JsonProperty("upperStrong") Double upperStrong,
    //            @JsonProperty("upperWeak") Double upperWeak,
    //            @JsonProperty("lowerWeak") Double lowerWeak,
    //            @JsonProperty("lowerStrong") Double lowerStrong) {
    //
    //        isFalse(upperStrong == null && upperWeak == null && lowerWeak == null && lowerStrong == null,
    //                "At least one of the thresholds must be not null");
    //
    //        if (upperStrong != null) {
    //            isTrue(upperWeak == null || upperStrong >= upperWeak, String.format("Required: upperStrong (%f) >= upperWeak (%f)", upperStrong, upperWeak));
    //            isTrue(lowerWeak == null || upperStrong >= lowerWeak, String.format("Required: upperStrong (%f) >= lowerWeak (%f)", upperStrong, lowerWeak));
    //            isTrue(lowerStrong == null || upperStrong >= lowerStrong, String.format("Required: upperStrong (%f) >= lowerStrong (%f)", upperStrong, lowerStrong));
    //        }
    //        if (upperWeak != null) {
    //            isTrue(lowerWeak == null || upperWeak >= lowerWeak, String.format("Required: upperWeak (%f) >= lowerWeak (%f)", upperWeak, lowerWeak));
    //            isTrue(lowerStrong == null || upperWeak >= lowerStrong, String.format("Required: upperWeak (%f) >= lowerStrong (%f)", upperWeak, lowerStrong));
    //        }
    //        if (lowerWeak != null) {
    //            isTrue(lowerStrong == null || lowerWeak >= lowerStrong, String.format("Required: lowerWeak (%f) >= lowerStrong (%f)", lowerWeak, lowerStrong));
    //        }
    //
    //        this.upperStrong = upperStrong;
    //        this.upperWeak = upperWeak;
    //        this.lowerStrong = lowerStrong;
    //        this.lowerWeak = lowerWeak;
    //    }

        public AnomalyLevel classify(double value) {
            if (value!=value) return AnomalyLevel.UNKNOWN;

            if (upperStrong==upperStrong && value >= upperStrong) {
                return AnomalyLevel.STRONG;
            } else if (upperWeak==upperWeak && value >= upperWeak) {
                return AnomalyLevel.WEAK;
            } else if (lowerStrong==lowerStrong && value <= lowerStrong) {
                return AnomalyLevel.STRONG;
            } else if (lowerWeak==lowerWeak && value <= lowerWeak) {
                return AnomalyLevel.WEAK;
            } else {
                return AnomalyLevel.NORMAL;
            }
        }
    }
}