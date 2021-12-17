package jcog.decide.thompson;


import jcog.random.XoRoShiRo128PlusRandom;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.hipparchus.stat.descriptive.rank.Percentile;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class BatchedThompsonSampling implements BatchedBandit {
    private int numberOfDraws = 1000;
    private Random random = new XoRoShiRo128PlusRandom(); //TODO //new MersenneTwister(new Date());
    private double confidenceLevel = 0.95;
    private double experimentValueQuitLevel = 0.01;
//    private int minimumConversionsPerArm = 5;


    public void setRandom(Random Random) {
        this.random = Random;
    }


    @Override
    public BanditStatistics getBanditStatistics(BanditPerformance performance) {

        List<ObservedArmPerformance> performances = performance.getPerformances();

        int n = performances.size();

        double[][] table = new double[numberOfDraws][n];

        int[] wins = wins(performances, table);

        DoubleArrayList armWeights = new DoubleArrayList(n);
        int bestArm = -1;
        double bestWeight = Double.NEGATIVE_INFINITY;
        for (int j = 0; j < n; j++) {
            double weight = (1.0 * wins[j]) / numberOfDraws;
            if (weight > bestWeight) {
                bestWeight = weight;
                bestArm = j;
            }
            armWeights.add(weight);
        }
        if (bestWeight > confidenceLevel)
            return new BanditStatistics(armWeights, bestArm);

        double[] valueRemaining = new double[numberOfDraws];
        for (int i = 0; i < numberOfDraws; i++) {
            double[] tableI = table[i];

            double maxValue = Double.NEGATIVE_INFINITY;
            int winningArm = -1;
            for (int j = 0; j < n; j++)
                if (tableI[j] > maxValue) {
                    maxValue = tableI[j];
                    winningArm = j;
                }
          valueRemaining[i] = winningArm == bestArm ? 0.0 : (maxValue - tableI[bestArm]) / tableI[bestArm];
        }

        Percentile percentile = new Percentile();
        percentile.setData(valueRemaining);

        return new BanditStatistics(armWeights,
                percentile.evaluate(confidenceLevel * 100.0) <
                        experimentValueQuitLevel ? bestArm : -1);
    }

    private int[] wins(Collection<ObservedArmPerformance> performances, double[][] table) {
        Beta[] pdfs = performances.stream().map(armPerformance -> {
            double alpha = armPerformance.successes() + 1;
            double beta = armPerformance.failures() + 1;
            return new Beta(alpha, beta);
        }).toArray(Beta[]::new);
        int n = table[0].length;
        int[] wins = new int[n];
        for (int i = 0; i < numberOfDraws; i++) {
            double maxValue = Double.NEGATIVE_INFINITY;
            int winningArm = -1;
            for (int j = 0; j < n; j++) {
              double ij = pdfs[j].nextDouble(random);
              if ((table[i][j] = ij) > maxValue) {
                    maxValue = ij;
                    winningArm = j;
                }
            }
            wins[winningArm]++;
        }
        return wins;
    }


    static class Beta {
        
        double alpha;
        double beta;
//        double PDF_CONST;
        double a_last = 0.0D;
        double b_last = 0.0D;
        double a_;
        double b_;
        double t;
        double fa;
        double fb;
        double p1;
        double p2;
        double c;
        double ml;
        double mu;
        double p_last = 0.0D;
        double q_last = 0.0D;
        double a;
        double b;
        double s;
        double m;
        double D;
        double Dl;
        double x1;
        double x2;
        double x4;
        double x5;
        double f1;
        double f2;
        double f4;
        double f5;
        double ll;
        double lr;
        double z2;
        double z4;
        double p3;
        double p4;

        Beta(double var1, double var3) {
            //this.setRandomGenerator(var5);
            
            this.setState(var1, var3);
        }

        private static double f(double var0, double var2, double var4, double var6) {
            return Math.exp(var2 * Math.log(var0 / var6) + var4 * Math.log((1.0D - var0) / (1.0D - var6)));
        }

        double b00(double var1, double var3, Random var5) {
            if (var1 != this.a_last || var3 != this.b_last) {
                this.a_last = var1;
                this.b_last = var3;
                this.a_ = var1 - 1.0D;
                this.b_ = var3 - 1.0D;
                this.c = var3 * this.b_ / (var1 * this.a_);
                this.t = this.c == 1.0D ? 0.5D : (1.0D - Math.sqrt(this.c)) / (1.0D - this.c);
                this.fa = Math.exp(this.a_ * Math.log(this.t));
                this.fb = Math.exp(this.b_ * Math.log(1.0D - this.t));
                this.p1 = this.t / var1;
                this.p2 = (1.0D - this.t) / var3 + this.p1;
            }

            double var10;
            while (true) {
                double var6;
                double var8;
                double var12;
                if ((var6 = var5.nextFloat() * this.p2) <= this.p1) {
                    var12 = explog(var1, var6, this.p1);
                    var10 = this.t * var12;
                    if ((var8 = var5.nextFloat() * this.fb) <= 1.0D - this.b_ * var10 || var8 <= 1.0D + (this.fb - 1.0D) * var12 && Math.log(var8) <= this.b_ * Math.log(1.0D - var10))
                        break;
                } else {
                    var12 = explog(var3, var6 - this.p1, this.p2 - this.p1);
                    var10 = 1.0D - (1.0D - this.t) * var12;
                    if ((var8 = var5.nextFloat() * this.fa) <= 1.0D - this.a_ * (1.0D - var10) || var8 <= 1.0D + (this.fa - 1.0D) * var12 && Math.log(var8) <= this.a_ * Math.log(var10))
                        break;
                }
            }

            return var10;
        }

        double b01(double var1, double var3, Random var5) {
            if (var1 != this.a_last || var3 != this.b_last) {
                this.a_last = var1;
                this.b_last = var3;
                this.a_ = var1 - 1.0D;
                this.b_ = var3 - 1.0D;
                this.t = this.a_ / (var1 - var3);
                this.fb = Math.exp((this.b_ - 1.0D) * Math.log(1.0D - this.t));
                this.fa = var1 - (var1 + this.b_) * this.t;
                this.t -= (this.t - (1.0D - this.fa) * (1.0D - this.t) * this.fb / var3) / (1.0D - this.fa * this.fb);
                this.fa = Math.exp(this.a_ * Math.log(this.t));
                this.fb = Math.exp(this.b_ * Math.log(1.0D - this.t));
                if (this.b_ <= 1.0D) {
                    this.ml = (1.0D - this.fb) / this.t;
                    this.mu = this.b_ * this.t;
                } else {
                    this.ml = this.b_;
                    this.mu = 1.0D - this.fb;
                }

                this.p1 = this.t / var1;
                this.p2 = this.fb * (1.0D - this.t) / var3 + this.p1;
            }

            double var10;
            while (true) {
                double var6;
                double var8;
                double var12;
                if ((var6 = var5.nextFloat() * this.p2) <= this.p1) {
                    var12 = explog(var1, var6, this.p1);
                    var10 = this.t * var12;
                    if ((var8 = var5.nextFloat()) <= 1.0D - this.ml * var10 || var8 <= 1.0D - this.mu * var12 && Math.log(var8) <= this.b_ * Math.log(1.0D - var10))
                        break;
                } else {
                    var12 = explog(var3, var6 - this.p1, this.p2 - this.p1);
                    var10 = 1.0D - (1.0D - this.t) * var12;
                    if ((var8 = var5.nextFloat() * this.fa) <= 1.0D - this.a_ * (1.0D - var10) || var8 <= 1.0D + (this.fa - 1.0D) * var12 && Math.log(var8) <= this.a_ * Math.log(var10))
                        break;
                }
            }

            return var10;
        }

        private static double explog(double var3, double p, double p2) {
            return Math.exp(Math.log((p) / (p2)) / var3);
        }

        double b1prs(double var1, double var3, Random var5) {
            if (var1 != this.p_last || var3 != this.q_last) {
                this.p_last = var1;
                this.q_last = var3;
                this.a = var1 - 1.0D;
                this.b = var3 - 1.0D;
                this.s = this.a + this.b;
                this.m = this.a / this.s;
                if (this.a > 1.0D || this.b > 1.0D) this.D = Math.sqrt(this.m * (1.0D - this.m) / (this.s - 1.0D));

                if (this.a <= 1.0D) {
                    this.x2 = this.Dl = this.m * 0.5D;
                    this.x1 = this.z2 = 0.0D;
                    this.f1 = this.ll = 0.0D;
                } else {
                    this.x2 = this.m - this.D;
                    this.x1 = this.x2 - this.D;
                    this.z2 = this.x2 * (1.0D - (1.0D - this.x2) / (this.s * this.D));
                    if (this.x1 > 0.0D && (this.s - 6.0D) * this.x2 - this.a + 3.0D <= 0.0D) this.Dl = this.D;
                    else {
                        this.x1 = this.z2;
                        this.x2 = (this.x1 + this.m) * 0.5D;
                        this.Dl = this.m - this.x2;
                    }

                    this.f1 = f(this.x1, this.a, this.b, this.m);
                    this.ll = this.x1 * (1.0D - this.x1) / (this.s * (this.m - this.x1));
                }

                this.f2 = f(this.x2, this.a, this.b, this.m);
                if (this.b <= 1.0D) {
                    this.x4 = 1.0D - (this.D = (1.0D - this.m) * 0.5D);
                    this.x5 = this.z4 = 1.0D;
                    this.f5 = this.lr = 0.0D;
                } else {
                    this.x4 = this.m + this.D;
                    this.x5 = this.x4 + this.D;
                    this.z4 = this.x4 * (1.0D + (1.0D - this.x4) / (this.s * this.D));
                    if (this.x5 >= 1.0D || (this.s - 6.0D) * this.x4 - this.a + 3.0D < 0.0D) {
                        this.x5 = this.z4;
                        this.x4 = (this.m + this.x5) * 0.5D;
                        this.D = this.x4 - this.m;
                    }

                    this.f5 = f(this.x5, this.a, this.b, this.m);
                    this.lr = this.x5 * (1.0D - this.x5) / (this.s * (this.x5 - this.m));
                }

                this.f4 = f(this.x4, this.a, this.b, this.m);
                this.p1 = this.f2 * (this.Dl + this.Dl);
                this.p2 = jcog.Util.fma(this.f4, (this.D + this.D), this.p1);
                this.p3 = jcog.Util.fma(this.f1, this.ll, this.p2);
                this.p4 = jcog.Util.fma(this.f5, this.lr, this.p3);
            }

            double var10;
            double var12;
            label98:
            do {
                double var6;
                double var8;
                double var14;
                while ((var6 = var5.nextFloat() * this.p4) > this.p1) {
                    if (var6 <= this.p2) {
                        var6 -= this.p1;
                        if ((var10 = var6 / D - f4) <= 0.0D) return this.m + var6 / this.f4;

                        if (var10 <= f5) return this.x4 + var10 / this.f5 * this.D;

                        var8 = D * (var6 = var5.nextFloat());
                        var12 = x4 + var8;
                        var14 = x4 - var8;
                        if (var10 * (z4 - x4) <= f4 * (z4 - var12)) return var12;

                        if ((var8 = this.f4 + this.f4 - var10) < 1.0D) {
                            if (var8 <= this.f4 + (1.0D - this.f4) * var6) return var14;

                            if (var8 <= f(var14, a, b, m)) return var14;
                        }
                        continue label98;
                    }

                    if (var6 <= this.p3) {
                        var14 = Math.log(var6 = (var6 - this.p2) / (this.p3 - this.p2));
                        if ((var12 = this.x1 + this.ll * var14) > 0.0D) {
                            var10 = var5.nextFloat() * var6;
                            if (var10 <= 1.0D + var14) return var12;

                            var10 *= this.f1;
                            continue label98;
                        }
                    } else {
                        var14 = Math.log(var6 = (var6 - this.p3) / (this.p4 - this.p3));
                        if ((var12 = this.x5 - this.lr * var14) < 1.0D) {
                            var10 = var5.nextFloat() * var6;
                            if (var10 <= 1.0D + var14) return var12;

                            var10 *= this.f5;
                            continue label98;
                        }
                    }
                }

                if ((var10 = var6 / this.Dl - this.f2) <= 0.0D) return this.m - var6 / this.f2;

                if (var10 <= this.f1) return this.x2 - var10 / this.f1 * this.Dl;

                var8 = this.Dl * (var6 = var5.nextFloat());
                var12 = this.x2 - var8;
                var14 = this.x2 + var8;
                if (var10 * (this.x2 - this.z2) <= this.f2 * (var12 - this.z2)) return var12;

                if ((var8 = this.f2 + this.f2 - var10) < 1.0D) {
                    if (var8 <= this.f2 + (1.0D - this.f2) * var6) return var14;

                    if (var8 <= f(var14, this.a, this.b, this.m)) return var14;
                }
            } while (Math.log(var10) > jcog.Util.fma(this.a, Math.log(var12 / this.m), this.b * Math.log((1.0D - var12) / (1.0D - this.m))));

            return var12;
        }


        double nextDouble(Random rng) {
            return this.nextDouble(this.alpha, this.beta, rng);
        }

        double nextDouble(double var1, double var3, Random rng) {
            
            if (var1 > 1.0D) {
                if (var3 > 1.0D) return this.b1prs(var1, var3, rng);

                if (var3 < 1.0D) return 1.0D - this.b01(var3, var1, rng);

                if (var3 == 1.0D) return Math.exp(Math.log(rng.nextFloat()) / var1);
            }

            if (var1 < 1.0D) {
                if (var3 > 1.0D) return this.b01(var1, var3, rng);

                if (var3 < 1.0D) return this.b00(var1, var3, rng);

                if (var3 == 1.0D) return Math.exp(Math.log(rng.nextFloat()) / var1);
            }

            if (var1 == 1.0D) if (var3 != 1.0D) return 1.0D - Math.exp(Math.log(rng.nextFloat()) / var3);
            else return rng.nextFloat();

            return 0.0D;
        }

        void setState(double var1, double var3) {
            this.alpha = var1;
            this.beta = var3;
//            this.PDF_CONST = Gamma.logGamma(var1 + var3) - Gamma.logGamma(var1) - Gamma.logGamma(var3);
        }



    }

}