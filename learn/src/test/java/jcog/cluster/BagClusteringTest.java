package jcog.cluster;

import jcog.data.DistanceFunction;
import org.junit.jupiter.api.Test;

import static jcog.pri.op.PriMerge.max;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BagClusteringTest {

    @Test void testBagCluster1() {
        final int centroids = 3;
        BagClustering<String> c = newClusteredBag(centroids, 8);
        c.put("aaaa", 0.5f);
        c.put("aaab", 0.5f);
        c.put("s", 0.5f);
        c.put("q", 0.5f);
        c.put("r", 0.5f);
        c.put("x", 0.5f);
        c.put("y", 0.5f);
        assertEquals(7, c.size());
        c.put("z", 0.5f);
        assertEquals(8, c.size());

        c.learn(0.5f, 1);

        assertEquals(8, c.size());

//        c.bag.print();
//        System.out.println();
//        c.print();

//        assertEquals(centroids, c.bag.stream().mapToInt(z -> z.centroid).distinct().count(), "all centroids are associated with some items");
    }

    private static BagClustering<String> newClusteredBag(int clusters, int cap) {
        return new BagClustering<>(new StringFeatures(), clusters, cap, max);
    }

    private static class StringFeatures extends BagClustering.Dimensionalize<String> {

        StringFeatures() {
            super(2);
        }

        @Override
        public void accept(String t, double[] d) {
            d[0] = t.length();

            int x = 0;
            for (int i = 0; i < t.length(); i++) {
                char c = t.charAt(i);
                if (Character.isAlphabetic(c)) {
                    x += Character.toLowerCase(c) - 'a';
                }
            }
            d[1] = Math.sin( Math.PI * x%26 / 26.0);
        }


        @Override
        public double distance(double[] a, double[] b) {
            return DistanceFunction.distanceCartesianSq(a, b);
        }
    }
}