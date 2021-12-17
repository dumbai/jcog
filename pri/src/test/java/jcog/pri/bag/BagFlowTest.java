//package jcog.pri.bag;
//
//import jcog.Texts;
//import jcog.pri.PLink;
//import jcog.pri.bag.impl.PriArrayBag;
//import jcog.pri.bag.util.ProxyBag;
//import jcog.pri.op.PriMerge;
//import jcog.random.XoRoShiRo128PlusRandom;
//import org.HdrHistogram.ConcurrentHistogram;
//import org.HdrHistogram.Histogram;
//import org.junit.jupiter.api.Test;
//
//public class BagFlowTest {
//
//    static class InstrumentedPLink<X> extends PLink<X> {
//
//        final long created;
//
//        InstrumentedPLink(X x, float p, long creation) {
//            super(x, p);
//            this.created = creation;
//        }
//
//
//        /** when a new insert matches with this as an existing, give an opportunity to record the boost
//         * @param pri*/
//        void recordBoost(float pri) {
//            //System.out.println("boost: " + pri);
//
//        }
//    }
//
//    abstract static class BagFlow<X,Y> extends ProxyBag<X, InstrumentedPLink<Y>> {
//
//        final Histogram activeDuration = new ConcurrentHistogram(5);
//
//        BagFlow(Bag<X, InstrumentedPLink<Y>> delegate) {
//            super(delegate);
//        }
//
//        InstrumentedPLink<Y> put(Y y, float pri) {
//            MyInstrumentedPLink x = new MyInstrumentedPLink(y, pri);
//            InstrumentedPLink<Y> z = put(x);
//            if (z!=null && z!=x && !z.isDeleted()) {
//                //assert(x.isDeleted());
//                z.recordBoost(pri);
//            }
//            return z;
//        }
//
//        abstract long now();
//
//
//
//        void record(long created, long deleted) {
//            long span = deleted - created;
//            activeDuration.recordValue(span);
//        }
//
//        class MyInstrumentedPLink extends InstrumentedPLink<Y> {
//
//            MyInstrumentedPLink(Y y, float p) {
//                super(y, p, now());
//            }
//
//            @Override
//            public boolean delete() {
//
//                if (super.delete()) {
//                    long deleted = now();
//                    record(created, deleted);
//                    return true;
//                }
//                return false;
//            }
//        }
//    }
//    @Test
//    void test1() {
//        int cap = 16;
//        int variety = cap * 2;
//        int batchSize = cap;
//        final long[] time = new long[1];
//        BagFlow f = new BagFlow<String,String>(new PriArrayBag(PriMerge.plus)) {
//            @Override long now() {
//                return time[0];
//            }
//        };
//        f.setCapacity(cap);
//
//        XoRoShiRo128PlusRandom rng = new XoRoShiRo128PlusRandom(1);
//        for (int e = 0; e < 10; e++) {
//            for (int i = 0; i < batchSize; i++) {
//                f.put("x" + rng.nextInt(variety), rng.nextFloat());
//            }
//
//            f.commit();
//
//            System.out.println(time[0] + " " + f.size() + '/' + f.capacity());
//            //f.print();
//            Texts.histogramPrint(f.activeDuration, System.out);
//
//            time[0]++;
//        }
//    }
//
//}
