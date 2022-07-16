package jcog.pri.bag;

import jcog.Util;
import jcog.data.list.Lst;
import jcog.pri.PLink;
import jcog.pri.PriReference;
import jcog.pri.Prioritized;
import jcog.pri.bag.impl.ArrayBag;
import jcog.pri.bag.impl.HijackBag;
import jcog.pri.bag.impl.PriReferenceArrayBag;
import jcog.pri.bag.impl.hijack.PLinkHijackBag;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.signal.Tensor;
import jcog.signal.tensor.ArrayTensor;
import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;
import org.hipparchus.stat.Frequency;
import org.hipparchus.stat.descriptive.StreamingStatistics;
import org.hipparchus.stat.fitting.EmpiricalDistribution;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static jcog.Str.n4;
import static jcog.pri.bag.impl.ArrayBagTest.assertSorted;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author me
 */
public class BagTest {


    public static void testBasicInsertionRemoval(Bag<String, PriReference<String>> c) {


        assertEquals(1, c.capacity());
        if (!(c instanceof PLinkHijackBag)) {
            assertEquals(0, c.size());
            assertTrue(c.isEmpty());
        }


        PLink x0 = new PLink("x", 2 * Prioritized.EPSILON);
        PriReference added = c.put(x0);
        assertSame(added, x0);
        c.commit();

        assertEquals(1, c.size());


        assertEquals(0, c.priMin(), Prioritized.EPSILON * 2);

        PriReference<String> x = c.get("x");
        assertNotNull(x);
        assertSame(x, x0);
        assertTrue(Util.equals(Prioritized.Zero.priElseNeg1(), x.priElseNeg1(), 0.01f));

    }


    public static Random rng() {
        return new XoRoShiRo128PlusRandom(1);
    }


    public static void printDist(EmpiricalDistribution f) {
        System.out.println(f.getSampleStats().toString().replace("\n", " "));
        /*if (s.getN() > 0)*/
        for (StreamingStatistics s : f.getBinStats()) {
            System.out.println(
                    n4(s.getMin()) + ".." + n4(s.getMax()) + ":\t" + s.getN());
        }
    }


//    public static Tensor samplingPriDist(Bag<PLink<String>, PLink<String>> b, int samples, int bins) {
//        return samplingPriDist(b, 1, samples, bins);
//    }

    private static Tensor samplingPriDist(Bag<PLink<String>, PLink<String>> b, int batches, int batchSize, int bins) {

        assert (bins > 1);

        Frequency<String> hits = new Frequency();
        ArrayTensor f = new ArrayTensor(bins);
        assertFalse(b.isEmpty());
        Random rng = new XoRoShiRo128PlusRandom(1);
        float min = b.priMin(), max = b.priMax(), range = max - min;
        Set<String> hit = new TreeSet();
        for (int i = 0; i < batches; i++) {
            b.sample(rng, batchSize, x -> {
                f.data[Util.bin(b.pri(x), bins)]++;
                String s = x.id;
                hits.addValue(s);
                hit.add(s);
            });
        }

        int total = batches * batchSize;
        assertEquals(total, Util.sum(f.data), 0.001f);

        ArrayTensor y = f.multiplyEach(1f/total).get();
        //y = y.multiplyEach(1f/y.maxValue()).get(); //normalize

        //System.out.println(Arrays.toString(y.snapshot()));

        if (hits.getUniqueCount() != b.size()) {

            System.out.println(hits.getUniqueCount() + " != " + b.size());
            Set<String> items = b.stream().map(stringPLink -> stringPLink.id).collect(Collectors.toSet());
            items.removeAll(hit);
            System.out.println("not hit: " + items);
            System.out.println(hits);

            fail("all elements must have been sampled at least once");
        }

        return y;
    }

    public static void testRemoveByKey(Bag<String, PriReference<String>> a) {

        a.put(new PLink("x", 0.1f));
        a.commit();
        assertEquals(1, a.size());

        a.remove("x");
        a.commit();
        assertEquals(0, a.size());
        assertTrue(a.isEmpty());
        if (a instanceof ArrayBag) {
            assertTrue(((List) ((PriReferenceArrayBag) a).stream().collect(toList())).isEmpty());
            //assertTrue(((PLinkArrayBag) a).keySet().isEmpty());
        }

    }


    public static void sampleLinear(Bag<PLink<String>, PLink<String>> bag, float batchSizeProp) {
        sampleLinear(bag, batchSizeProp, 1);
    }

    public static float sampleLinear(Bag<PLink<String>, PLink<String>> bag, float batchSizeProp, float sharp) {
        fillLinear(bag, bag.capacity());

        if (bag instanceof ArrayBag) {
            ((ArrayBag)bag).sharp = sharp;
        } else {
            //TODO
        }
//        if (bag instanceof ArrayBag) {
//            System.out.println("\tArrayBag histogram: " + ((ArrayBag)bag).hist);
//        }
        Tensor h = sample(bag, batchSizeProp);
        double err = 0;
        int n = h.volume();
        double[] ideals = Util.arrayOf(i ->(i+1.0)/(n+1), new double[n]);
        Util.pow(sharp, ideals);
        Util.mul(1f/Util.sum(ideals), ideals);
        for (int i = 0; i < n; i++) {
            double ideal = ideals[i];
            double actual = h.getAt(i);
            double e = Math.abs(ideal - actual);
            //System.out.println(n2(ideal) + "\t" + n4(actual) + "\t err=" + n4(e));
            err += e;
        }
        err /= n;
        //System.out.println("cap=" + bag.capacity() + "\tsharp=" + sharp + "\terr=" + n4(err));

        assertTrue( err < 0.25f, "excessive error: " + err);
        return (float) err;
    }

    public static void sampleSquashed(Bag<PLink<String>, PLink<String>> bag, float batchSizeProp) {
        fill(bag, bag.capacity(), (x) -> (x / 2f) + 0.5f);
        sample(bag, batchSizeProp);
    }

    public static void sampleCurved(Bag<PLink<String>, PLink<String>> bag, float batchSizeProp) {
        fill(bag, bag.capacity(), (x) -> 1 - (1 - x) * (1 - x));
        sample(bag, batchSizeProp);
    }

    public static void sampleRandom(ArrayBag<PLink<String>, PLink<String>> bag, float batchSizeProp) {
        fillRandom(bag);
        sample(bag, batchSizeProp);
    }

    public static Tensor sample(Bag<PLink<String>, PLink<String>> bag, float batchSizeProp) {

        int cap = bag.capacity();
        assert (cap > 1);

        int batchSize = Math.max(1, (int) Math.ceil(batchSizeProp * cap));


        int bins =
                cap;
                //Math.max(4, Util.sqrtInt(cap) + 1);


        int batches = 192 * cap;

        Frequency<String> hits = new Frequency();
        ArrayTensor f = new ArrayTensor(bins);
        assertFalse(bag.isEmpty());
        Random rng = new XoRoShiRo128PlusRandom(1);
        float min = bag.priMin(), max = bag.priMax(), range = max - min;
        Set<String> hit = new TreeSet();
        for (int i = 0; i < batches; i++) {
            bag.sample(rng, batchSize, x -> {
                f.data[Util.bin(bag.pri(x), bins)]++;
                String s = x.id;
                hits.addValue(s);
                hit.add(s);
            });
        }

        int total = batches * batchSize;
        assertEquals(total, Util.sum(f.data), 0.001f);

        ArrayTensor y = f.multiplyEach(1f/total).get();
        //y = y.multiplyEach(1f/y.maxValue()).get(); //normalize

        //System.out.println(Arrays.toString(y.snapshot()));

        if (hits.getUniqueCount() != bag.size()) {

            System.out.println(hits.getUniqueCount() + " != " + bag.size());
            Set<String> items = bag.stream().map(stringPLink -> stringPLink.id).collect(Collectors.toSet());
            items.removeAll(hit);
            System.out.println("not hit: " + items);
            System.out.println(hits);

            fail("all elements must have been sampled at least once");
        }


//        String h = "cap=" + cap + " total=" + (batches * batchSize);
//        System.out.println(h + ":\n\t" + f1.tsv2());
//        System.out.println();

//        float[] ff = ((Tensor) y).snapshot();
//        System.out.println(Arrays.toString(ff));


//        int n = ff.length;
//        if (ff[n-1] == 0)
//            n--; //skip last empty histogram cell HACK
//        if (ff[n-1] == 0)
//            n--; //skip last empty histogram cell HACK

//        float orderThresh = 0.1f;
//        for (int a = 0; a < n; a++) {
//            //assertNotEquals(0, ff[a]); //no zero bins
//            for (int b = a + 1; b < n; b++) {
//                float diff = ff[a] - ff[b];
//                boolean unordered = diff > orderThresh;
//                if (unordered) {
////                    bag.print();
////                    if (bag instanceof ArrayBag) {
////                        System.out.println(((ArrayBag) bag).hist);
////                    }
//                    fail("sampling distribution not ordered");
//                }
//            }
//        }

//        final float MIN_RATIO = 1.5f;

//        for (int lows : n > 4 ? new int[]{0, 1} : new int[]{0}) {
//            for (int highs : n > 4 ? new int[]{n - 1, n - 2} : new int[]{n - 1}) {
//                if (lows != highs) {
//                    float maxMinRatio = ff[highs] / ff[lows];
//                    assertTrue(
//                            maxMinRatio > MIN_RATIO,
//                            () -> maxMinRatio + " ratio between max and min"
//                    );
//                }
//            }
//        }


        return y;
    }


//
//    private static float maxMinRatio(EmpiricalDistribution d) {
//        List<SummaryStatistics> bins = d.getBinStats();
//        return ((float) bins.get(bins.size() - 1).getN() / (bins.get(0).getN()));
//    }


    public static void testPutMinMaxAndUniqueness(Bag<Integer, PriReference<Integer>> a) {
        float pri = 0.5f;
        int n = a.capacity() * 16;


        for (int i = 0; i < n; i++) {
            a.put(new PLink((i), pri));
        }

        a.commit(null);
        assertEquals(a.capacity(), a.size());
        if (a instanceof ArrayBag) assertSorted((ArrayBag) a);


        List<Integer> keys = new Lst(a.capacity());
        a.forEachKey(keys::add);
        assertEquals(a.size(), keys.size());
        assertEquals(new HashSet(keys).size(), keys.size());

        assertEquals(pri, a.priMin(), 0.01f);
        assertEquals(a.priMin(), a.priMax(), 0.08f);

        if (a instanceof ArrayBag)
            assertTrue(((HijackBag) a).density() > 0.75f);
    }

    public static void populate(Bag<String, PriReference<String>> b, Random rng, int count, int dimensionality, float minPri, float maxPri, float qua) {
        populate(b, rng, count, dimensionality, minPri, maxPri, qua, qua);
    }

    private static void populate(Bag<String, PriReference<String>> a, Random rng, int count, int dimensionality, float minPri, float maxPri, float minQua, float maxQua) {
        float dPri = maxPri - minPri;
        for (int i = 0; i < count; i++) {
            a.put(new PLink<>(
                    "x" + i,
                    rng.nextFloat() * dPri + minPri)
            );
        }
        a.commit(null);
        if (a instanceof ArrayBag) assertSorted((ArrayBag) a);
    }

    /**
     * fill it exactly to capacity
     */
    public static void fillLinear(Bag<PLink<String>, PLink<String>> bag, int c) {
        fill(bag, c, (x) -> x);
        //assertEquals(1f / (c + 1), bag.priMin(), 0.03f);
        //assertEquals(1 - 1f / (c + 1), bag.priMax(), 0.03f);
    }

    public static void fill(Bag<PLink<String>, PLink<String>> bag, int c, FloatToFloatFunction priCurve) {
        assertTrue(bag.isEmpty());


        for (int i = c - 1; i >= 0; i--) {
            float x = (i + 1f) / (c+1); //center of index
            PLink inserted = bag.put(new PLink(i + "x", priCurve.valueOf(x)));
            if (!(bag instanceof HijackBag))
                assertNotNull(inserted, "inserted");
        }

        bag.commit(null);
        if (!(bag instanceof HijackBag))
            assertEquals(c, bag.size());
        if (bag instanceof ArrayBag) assertSorted((ArrayBag) bag);
    }

    private static void fillRandom(ArrayBag<PLink<String>, PLink<String>> bag) {
        assertTrue(bag.isEmpty());

        int c = bag.capacity();

        Random rng = new XoRoShiRo128PlusRandom(1);


        for (int i = c - 1; i >= 0; i--) {
            PLink inserted = bag.put(new PLink(i + "x", rng.nextFloat()));
            assertNotNull(inserted);
            assertSorted(bag);
        }
        bag.commit(null);
        assertEquals(c, bag.size());
        assertSorted(bag);
    }


}