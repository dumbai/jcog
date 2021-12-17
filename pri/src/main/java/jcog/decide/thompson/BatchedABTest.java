package jcog.decide.thompson;

import jcog.random.XoRoShiRo128PlusRandom;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

import java.util.List;
import java.util.Random;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;

public class BatchedABTest implements BatchedBandit {
    private static final double[] P0 = {-59.96335010141079D, 98.00107541859997D, -56.67628574690703D, 13.931260938727968D, -1.2391658386738125D};
    private static final double[] Q0 = {1.9544885833814176D, 4.676279128988815D, 86.36024213908905D, -225.46268785411937D, 200.26021238006066D, -82.03722561683334D, 15.90562251262117D, -1.1833162112133D};
    private static final double[] P1 = {4.0554489230596245D, 31.525109459989388D, 57.16281922464213D, 44.08050738932008D, 14.684956192885803D, 2.1866330685079025D, -0.1402560791713545D, -0.03504246268278482D, -8.574567851546854E-4D};
    private static final double[] Q1 = {15.779988325646675D, 45.39076351288792D, 41.3172038254672D, 15.04253856929075D, 2.504649462083094D, -0.14218292285478779D, -0.03808064076915783D, -9.332594808954574E-4D};
    private static final double[] P2 = {3.2377489177694603D, 6.915228890689842D, 3.9388102529247444D, 1.3330346081580755D, 0.20148538954917908D, 0.012371663481782003D, 3.0158155350823543E-4D, 2.6580697468673755E-6D, 6.239745391849833E-9D};
    private static final double[] Q2 = {6.02427039364742D, 3.6798356385616087D, 1.3770209948908132D, 0.21623699359449663D, 0.013420400608854318D, 3.2801446468212774E-4D, 2.8924786474538068E-6D, 6.790194080099813E-9D};
    private java.util.Random Random = new XoRoShiRo128PlusRandom(); //TODO
    private double confidenceLevel = 0.95;
    private double baselineConversionRate = 0.01;
    private double minimumDetectableEffect = 0.5;
    private double statisticalPower = 0.80;
    private boolean requiresMinSamples = true;

    /**
     * from: colt
     */
    private static double polevl(double var0, double[] var2, int var3) throws ArithmeticException {
        double var4 = var2[0];

        for (int var6 = 1; var6 <= var3; ++var6) {
            var4 = var4 * var0 + var2[var6];
        }

        return var4;
    }

    /**
     * from: colt
     */
    private static double p1evl(double var0, double[] var2, int var3) throws ArithmeticException {
        double var4 = var0 + var2[0];

        for (int var6 = 1; var6 < var3; ++var6) {
            var4 = var4 * var0 + var2[var6];
        }

        return var4;
    }

    /**
     * from: colt
     */
    private static double normalInverse(double var0) throws ArithmeticException {
        double var15 = sqrt(6.283185307179586D);
        if (var0 <= 0.0D) {
            throw new IllegalArgumentException();
        } else if (var0 >= 1.0D) {
            throw new IllegalArgumentException();
        } else {
            boolean var14 = true;
            double var4 = var0;
            if (var0 > 0.8646647167633873D) {
                var4 = 1.0D - var0;
                var14 = false;
            }

            double var2;
            if (var4 > 0.1353352832366127D) {
                var4 -= 0.5D;
                double var8 = var4 * var4;
                var2 = var4 + var4 * (var8 * polevl(var8, P0, 4) / p1evl(var8, Q0, 8));
                var2 *= var15;
            } else {
                var2 = sqrt(-2.0D * Math.log(var4));
                double var10 = var2 - Math.log(var2) / var2;
                double var6 = 1.0D / var2;
                double var12;
                if (var2 < 8.0D) {
                    var12 = var6 * polevl(var6, P1, 8) / p1evl(var6, Q1, 8);
                } else {
                    var12 = var6 * polevl(var6, P2, 8) / p1evl(var6, Q2, 8);
                }

                var2 = var10 - var12;
                if (var14) {
                    var2 = -var2;
                }

            }
            return var2;
        }
    }
    /** from colt */
    private static double chiSquare(double var0, double var2) throws ArithmeticException {
        return var2 >= 0.0D && var0 >= 1.0D ? incompleteGamma(var0 / 2.0D, var2 / 2.0D) : 0.0D;
    }
    /** from colt */
    private static double incompleteGamma(double var0, double var2) throws ArithmeticException {
        if (var2 > 0.0D && var0 > 0.0D) {
            if (var2 > 1.0D && var2 > var0) {
                return 1.0D - incompleteGammaComplement(var0, var2);
            } else {
                double var6 = var0 * Math.log(var2) - var2 - logGamma(var0);
                if (var6 < -709.782712893384D) {
                    return 0.0D;
                } else {
                    var6 = Math.exp(var6);
                    double var10 = var0;
                    double var8 = 1.0D;
                    double var4 = 1.0D;

                    do {
                        ++var10;
                        var8 *= var2 / var10;
                        var4 += var8;
                    } while(var8 / var4 > 1.1102230246251565E-16D);

                    return var4 * var6 / var0;
                }
            }
        } else {
            return 0.0D;
        }
    }
    /* from colt */
    private static double incompleteGammaComplement(double var0, double var2) throws ArithmeticException {
        if (var2 > 0.0D && var0 > 0.0D) {
            if (var2 >= 1.0D && var2 >= var0) {
                double var6 = var0 * Math.log(var2) - var2 - logGamma(var0);
                if (var6 < -709.782712893384D) {
                    return 0.0D;
                } else {
                    var6 = Math.exp(var6);
                    double var16 = 1.0D - var0;
                    double var18 = var2 + var16 + 1.0D;
                    double var8 = 0.0D;
                    double var24 = 1.0D;
                    double var30 = var2;
                    double var22 = var2 + 1.0D;
                    double var28 = var18 * var2;
                    double var4 = var22 / var28;

                    double var14;
                    do {
                        ++var8;
                        ++var16;
                        var18 += 2.0D;
                        double var10 = var16 * var8;
                        double var20 = var22 * var18 - var24 * var10;
                        double var26 = var28 * var18 - var30 * var10;
                        if (var26 != 0.0D) {
                            double var12 = var20 / var26;
                            var14 = Math.abs((var4 - var12) / var12);
                            var4 = var12;
                        } else {
                            var14 = 1.0D;
                        }

                        var24 = var22;
                        var22 = var20;
                        var30 = var28;
                        var28 = var26;
                        if (Math.abs(var20) > 4.503599627370496E15D) {
                            var24 *= 2.220446049250313E-16D;
                            var22 = var20 * 2.220446049250313E-16D;
                            var30 *= 2.220446049250313E-16D;
                            var28 = var26 * 2.220446049250313E-16D;
                        }
                    } while(var14 > 1.1102230246251565E-16D);

                    return var4 * var6;
                }
            } else {
                return 1.0D - incompleteGamma(var0, var2);
            }
        } else {
            return 1.0D;
        }
    }

    /* from colt */
    private static double logGamma(double var0) throws ArithmeticException {
        double[] var10 = {8.116141674705085E-4D, -5.950619042843014E-4D, 7.936503404577169E-4D, -0.002777777777300997D, 0.08333333333333319D};
        double[] var11 = {-1378.2515256912086D, -38801.631513463784D, -331612.9927388712D, -1162370.974927623D, -1721737.0082083966D, -853555.6642457654D};
        double[] var12 = {-351.81570143652345D, -17064.210665188115D, -220528.59055385445D, -1139334.4436798252D, -2532523.0717758294D, -2018891.4143353277D};
        double var2;
        double var4;
        double var8;
        if (var0 < -34.0D) {
            var4 = -var0;
            double var6 = logGamma(var4);
            var2 = Math.floor(var4);
            if (var2 == var4) {
                throw new ArithmeticException("lgam: Overflow");
            } else {
                var8 = var4 - var2;
                if (var8 > 0.5D) {
                    ++var2;
                    var8 = var2 - var4;
                }

                var8 = var4 * Math.sin(3.141592653589793D * var8);
                if (var8 == 0.0D) {
                    throw new ArithmeticException("lgamma: Overflow");
                } else {
                    var8 = 1.1447298858494002D - Math.log(var8) - var6;
                    return var8;
                }
            }
        } else if (var0 >= 13.0D) {
            if (var0 > 2.556348E305D) {
                throw new ArithmeticException("lgamma: Overflow");
            } else {
                var4 = (var0 - 0.5D) * Math.log(var0) - var0 + 0.9189385332046728D;
                if (!(var0 > 1.0E8D)) {
                    var2 = 1.0D / (var0 * var0);
                    if (var0 >= 1000.0D) {
                        var4 += ((7.936507936507937E-4D * var2 - 0.002777777777777778D) * var2 + 0.08333333333333333D) / var0;
                    } else {
                        var4 += polevl(var2, var10, 4) / var0;
                    }

                }
                return var4;
            }
        } else {
            for(var8 = 1.0D; var0 >= 3.0D; var8 *= var0) {
                --var0;
            }

            while(var0 < 2.0D) {
                if (var0 == 0.0D) {
                    throw new ArithmeticException("lgamma: Overflow");
                }

                var8 /= var0;
                ++var0;
            }

            if (var8 < 0.0D) {
                var8 = -var8;
            }

            if (var0 == 2.0D) {
                return Math.log(var8);
            } else {
                var0 -= 2.0D;
                var2 = var0 * polevl(var0, var11, 5) / p1evl(var0, var12, 6);
                return Math.log(var8) + var2;
            }
        }
    }

    public Random getRandom() {
        return Random;
    }

    public void setRandom(Random Random) {
        this.Random = Random;
    }

    public double getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public double getBaselineConversionRate() {
        return baselineConversionRate;
    }

    public void setBaselineConversionRate(double baselineConversionRate) {
        this.baselineConversionRate = baselineConversionRate;
    }

    public double getMinimumDetectableEffect() {
        return minimumDetectableEffect;
    }

    public void setMinimumDetectableEffect(double minimumDetectableEffect) {
        this.minimumDetectableEffect = minimumDetectableEffect;
    }

    public double getStatisticalPower() {
        return statisticalPower;
    }

    public void setStatisticalPower(double statisticalPower) {
        this.statisticalPower = statisticalPower;
    }

    public boolean requiresMinSamples() {
        return requiresMinSamples;
    }

    public void setRequiresMinSamples(boolean requiresMinSamples) {
        this.requiresMinSamples = requiresMinSamples;
    }

    private static double square(double x) {
        return x * x;
    }

    private long numberOfSamples() {
        double p = baselineConversionRate;
        double delta = baselineConversionRate * minimumDetectableEffect;
        double alpha = 1.0 - confidenceLevel;
        double beta = 1.0 - statisticalPower;
        double v1 = p * (1 - p);
        double v2 = (p + delta) * (1 - p - delta);
        double sd1 = sqrt(2 * v1);
        double sd2 = sqrt(v1 + v2);
        double tAlpha2 = normalInverse(alpha / 2.0);
        double tBeta = normalInverse(beta);
        double n = square(tAlpha2 * sd1 + tBeta * sd2) / square(delta);
        return round(n);
    }

    private static DoubleArrayList getWeights(int arms) {
        DoubleArrayList weights = new DoubleArrayList(arms);
        for (int i = 0; i < arms; i++)
            weights.add(1.0 / arms);
        return weights;
    }

    @Override
    public BanditStatistics getBanditStatistics(BanditPerformance performance) {
        List<ObservedArmPerformance> performances = performance.getPerformances();
        int pn = performances.size();
        if (requiresMinSamples) {
            long n = numberOfSamples();
            for (ObservedArmPerformance p : performances) {
                if (p.failures() + p.successes() < n) {
                    return new BanditStatistics(getWeights(pn), -1);
                }
            }
        }
        int t = pn;
        int bestArm = -1;
        double bestConversion = -1.0;
//        List<Long> groupTotals = Lists.newArrayList();
        long totalSuccesses = 0;
        long totalFailures = 0;
        for (int i = 0; i < t; i++) {
            ObservedArmPerformance p = performances.get(i);
            totalSuccesses += p.successes();
            totalFailures += p.failures();
//            long total = totalSuccesses + totalFailures;
//            groupTotals.add(total);
            double conversion = p.successes() * 1.0 / (p.failures() + p.successes());
            if (conversion > bestConversion) {
                bestConversion = conversion;
                bestArm = i;
            }
        }
        long totalSamples = totalSuccesses + totalFailures;
        double chiSquared = 0.0;
        for (int i = 0; i < t; i++) {
            ObservedArmPerformance p = performances.get(i);
            double samples = 1.0 * (p.failures() + p.successes());
            double expectedFailure = samples * totalFailures / totalSamples;
            double expectedSuccess = samples * totalSuccesses / totalSamples;
            if (expectedFailure < 5 || expectedSuccess < 5) {
                return new BanditStatistics(getWeights(pn), -1);
            }
            double failFactor = square(p.failures() - expectedFailure) / expectedFailure;
            double successFactor = square(p.successes() - expectedSuccess) / expectedSuccess;
            chiSquared += failFactor + successFactor;
        }
        if (chiSquare(1.0, chiSquared) >= confidenceLevel) {
            return new BanditStatistics(getWeights(pn), bestArm);
        }
        if (requiresMinSamples) {
            return new BanditStatistics(getWeights(pn), bestArm);
        } else {
            return new BanditStatistics(getWeights(pn), -1);
        }
    }
}
