package jcog.pri;

import jcog.Util;
import jcog.pri.bag.Bag;
import jcog.pri.op.PriAdd;
import org.eclipse.collections.api.block.function.primitive.FloatToObjectFunction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static jcog.Util.assertFinite;
import static jcog.Util.unitizeSafe;
import static jcog.pri.Prioritized.EPSILON;

public enum Forgetting {
	;

    public static <Y extends Prioritizable> @Nullable PriAdd<Y> forget(Bag<?,Y> b, float temperature) {
        return forget(b, temperature, z -> new PriAdd<>(z));
    }

    @Nullable public static <Y extends Prioritizable> PriAdd<Y> forget(Bag<?,Y> b, float temperature, FloatToObjectFunction<PriAdd<Y>> updater) {
        assertFinite(temperature);

        int s = b.size();
        if (s > 0) {
            int c = b.capacity();
            if (c > 0) {

                double pressure = b.depressurizePct(1);

                double m = b.mass();
                if (m <= EPSILON)
                    return null;

//                /** pressure upper limit */
//                double pMax =
//                    m;
//                    //m * 0.5;

                double mMax = c * 1.0;

                double mIdeal = c * 0.5;

                double pressureExcess =
                    pressure * (m/mMax);
                    //Util.min(pressure, pMax) * (m / mMax);

                double massExcess = Util.max(0, m - mIdeal);

                double excess =
                    pressureExcess + massExcess;
                    //Math.max(pressureExcess, massExcess);

                float excessEach = (float) (temperature * excess / s);
                if (excessEach > EPSILON)
                    return new PriAdd<>(-excessEach);


//                double forgetRate =
//                        rateBalancedIdealMassFraction(b, temperature, pressure, c);
                        //rateIdealMass(b, temperature, pressure, s, 0.5f);
                        //rateBalancedIdealMassFraction(b, temperature, pressure, s, 0.5f);
                        //rateBalanced(b, temperature, pressure);

//                forgetRate *= ((double)s)/c; //apply in proportion to capacity filled
//
//                if (forgetRate >= EPSILON) {
//                    float mult = (float) (1 - unitizeSafe(forgetRate));
//                    return updater.valueOf(mult);
//                }
            }
        }


        return null;
    }

//    private static double rateBalancedIdealMassFraction(Bag b, float temperature, double pressure, int cap) {
//        double mass = b.mass();
//        if (mass <= EPSILON)
//            return 0;
//
////        float idealPri =
////            1/ Util.sqrt(b.size());
////            //1f/b.size();
////            //0.5f;
////        double excessMass = Util.max(0, mass - (cap * idealPri));
//
//        return temperature * unitizeSafe((pressure /*+ excessMass*/) / mass);
//    }

    /** why/how does this work */
    private static <X, Y> double rateBalancedIdealMassFraction0(Bag<X, Y> b, float temperature, float pressure, int size, float idealPri) {

        double mass = b.mass();
        double load = mass / (size * idealPri);
        double r = pressure * load / size; // = (m p)/(s^2 x)
        return Math.min(1, r) * temperature;

    }


    /** bad */
    private static <X, Y> double rateBalancedIdealMassFraction2(Bag<X, Y> b, float temperature, float pressure, int size, float idealPri) {
        float mass = b.mass();
        double massIdeal = size * idealPri;
        double rate = unitizeSafe((pressure + Math.max(0, mass - massIdeal)) / mass);
        return rate * temperature;
    }



    /** bad */
    private static <X, Y> double rateBalanced(Bag<X, Y> b, float temperature, float pressure) {
        float mass = b.mass();
        double pressurePct = Math.min(1, pressure / mass);
        return pressurePct * temperature;
    }


    /** ideal mass forgetting rate calculation */
    private static <X, Y> double rateIdealMass(Bag<X, Y> b, float temperature, float pressure, int size, float idealPri) {
        float mass = b.mass();
        if (mass > EPSILON) {
            double idealMass =
                    size * idealPri;
                    //mass * Util.PHI_min_1;
                    //Util.lerpSafe(temperature*temperature, mass, mass * Util.PHI_min_1);
//                            cap * 0.5;
                    //1;
                    //1/Math.sqrt(cap);
                    //0.25;
                    //0;
                    //mass/2;
                    //mass*0.9;


            double excess = pressure + Math.max(0, (mass - idealMass));
            excess *= Math.max(0, temperature);

            if (excess > Float.MIN_NORMAL) {

                double forgetRate =
                        excess / mass;
                        //(excess / (mass + excess));

//                        forgetRate *= Math.max(0, temperature);

                return forgetRate;
            }
        }
        return Double.NaN;
    }

    private static class PriDecay<Y extends Prioritizable> implements Consumer<Y> {
        private final double dividend;
        private final float rate;

        PriDecay(double dividend, float rate) {
            this.dividend = dividend;
            this.rate = rate;
        }

        @Override public void accept(Y y) {
            double p = y.pri();
            float f = (float)  (1 - Math.min(1, (rate + p) / dividend) );
            y.priMul(f);
        }
    }

    //abstract protected @Nullable Consumer forget(float temperature, int size, int cap, float pressure, float mass);

//    /** temporally oblivious; uses only incoming pressure to determine forget amounts */
//    public static class AsyncForgetting extends Forgetting {
//
//
////        public final FloatRange tasklinkForgetRate = new FloatRange(1f, 0f, 1f);
////
////
////
////        protected Consumer<TaskLink> forgetTasklinks(Concept c, Bag<Tasklike, TaskLink> tasklinks) {
////            return forget(tasklinks, 1f, tasklinkForgetRate.floatValue());
////        }
////
////
//
//    }

//    /** experimental */
//    @Deprecated public static class TimedForgetting extends Forgetting {
//
//        /**
//         * number of clock durations composing a unit of short target memory decay (used by bag forgetting)
//         */
//        public final FloatRange memoryDuration = new FloatRange(1f, 0f, 64f);
//
//
//
//        @Override
//        protected Consumer forget(float temperature, int size, int cap, float pressure, float mass) {
//            return PriForget.forgetIdeal(temperature,
//                                        ScalarValue.EPSILON * cap,
//                                        //1f/size,
//                                        //1f/cap,
//                                        //0.1f,
//                                        //0.5f,
//                                        size, cap, pressure, mass);
//        }
//
//
////        @Override
////        public void updateConcepts(Bag<Term, Activate> active, long dt, NAR n) {
////            float temperature = 1f - (float) Math.exp(-(((double) dt) / n.dur()) / memoryDuration.floatValue());
////            active.commit(active.forget(temperature));
////        }
//
//        public void update(Concept c, NAR n) {
//
//
//            int dur = n.dur();
//
//            Consumer<TaskLink> tasklinkUpdate;
//            Bag<Tasklike, TaskLink> tasklinks = c.tasklinks();
//
//            long curTime = n.time();
//            Long prevCommit = c.meta("C", curTime);
//            if (prevCommit != null) {
//                if (curTime - prevCommit > 0) {
//
//                    double deltaDurs = ((double) (curTime - prevCommit)) / dur;
//
//                    //deltaDurs = Math.min(deltaDurs, 1);
//
//                    float forgetRate = (float) (1 - Math.exp(-deltaDurs / memoryDuration.doubleValue()));
//
//                    //System.out.println(deltaDurs + " " + forgetRate);
//                    tasklinkUpdate = tasklinks.forget(forgetRate);
//
//                } else {
//                    //dont need to commit, it already happened in this cycle
//                    return;
//                }
//            } else {
//                tasklinkUpdate = null;
//
//            }
//
//            tasklinks.commit(tasklinkUpdate);
//
//        }
//    }

    /** untested */
//    public static <Y extends Prioritizable> @Nullable Consumer<Y> forgetL1WeightDecay(Bag<?,Y> b, float rate) {
//        assertFinite(rate);
//
//        int s = b.size();
//        if (s > 0) {
//            float depressurizationRate = 1;
//            float pressure = b.depressurizePct(depressurizationRate);
//
//            double dividend = b.mass() - rate;
//            if (dividend < EPSILON)
//                return null;
//
//            return new PriDecay<>(dividend, rate);
////            int c = b.capacity();
////            if (c > 0) {
////
////
////                double forgetRate =
////                        rateBalancedIdealMassFraction(b, temperature, pressure, c, 0.5f);
////                //rateIdealMass(b, temperature, pressure, s, 0.5f);
////                //rateBalancedIdealMassFraction(b, temperature, pressure, s, 0.5f);
////                //rateBalanced(b, temperature, pressure);
////
////                if (Double.isFinite(forgetRate) && forgetRate >= EPSILON)
////                    return updater.valueOf((float) (1 - unitizeSafe(forgetRate)));
////            }
//        }
//
//
//        return null;
//    }
}