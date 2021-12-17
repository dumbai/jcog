package jcog.pri;

import jcog.data.list.Lst;
import jcog.random.XoRoShiRo128PlusRandom;
import org.HdrHistogram.Histogram;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static jcog.Str.n4;

@Disabled
class FuzzyReferenceTest {

    long BASE = Long.MAX_VALUE - Integer.MAX_VALUE*2L;

    @Test
    public void test1() {

        int n = 24*1024;
        List<FuzzyReference> l = new Lst<>(n);
        Random rng =
                new XoRoShiRo128PlusRandom();
//                new XorShift128PlusRandom();
                //new Random();
//        List<String> held = new Lst<>(n);
        //for (int i = 0; i < n; i++) {
        for (int i = n-1; i >= 0; i--) {
            int k = rng.nextInt(Integer.MAX_VALUE);
            l.add(new FuzzyReference(new Object[256], k + BASE));
//            held.add(I);
        }
//        count(l);
//        held.clear(); System.gc();
        System.gc();
        count(l);
    }

    private void count(List<FuzzyReference> l) {
        int collected = 0;
        long minPri = Integer.MAX_VALUE, maxPri = Integer.MIN_VALUE;
        Histogram c = new Histogram(5);
        Histogram r = new Histogram(5);
        for (FuzzyReference x : l) {
            long p = x.pri() - BASE;
            if (x.get()==null) {
                //System.out.println(x.pri());
                collected++;
                assert(p < Integer.MAX_VALUE);
                minPri = Math.min(p, minPri);
                maxPri = Math.max(p, maxPri);
                c.recordValue(p);
            } else
                r.recordValue(p);
        }
        System.out.println("collected: " + collected + " / " + l.size() + " (" + n4(collected/((float)l.size())*100f) + "%)");
//        System.out.println("  min: " + minPri);
//        System.out.println("  max: " + maxPri);
//        Str.histogramPrint(c, System.out);
        System.out.println("mean collected: " + c.getMean() /Integer.MAX_VALUE);
        System.out.println("   mean remain: " + r.getMean() /Integer.MAX_VALUE);
    }
}