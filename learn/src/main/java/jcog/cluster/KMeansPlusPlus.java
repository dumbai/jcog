package jcog.cluster;

import com.google.common.collect.Iterators;
import jcog.Is;
import jcog.TODO;
import jcog.data.DistanceFunction;
import jcog.data.bit.MetalBitSet;
import jcog.data.list.Lst;
import jcog.random.RandomBits;
import jcog.util.ArrayUtil;
import org.roaringbitmap.PeekableIntIterator;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;

/**
 * adapted from https://github.com/Hipparchus-Math/hipparchus/blob/master/hipparchus-clustering/src/main/java/org/hipparchus/clustering/KMeansPlusPlusClusterer.java
 */
@Is("Determining_the_number_of_clusters_in_a_data_set")
public abstract class KMeansPlusPlus<X> {
    /**
     * The number of clusters.
     */
    public final int k;

    private final int dims;

    private final DistanceFunction distance;

    public final Lst<CentroidCluster<X>> clusters = new Lst<>(CentroidCluster.EmptyCentroidClustersArray);
    private final Lst<CentroidCluster<X>> clustersNext = new Lst<>(CentroidCluster.EmptyCentroidClustersArray);

    public transient Lst<X> values;
    private transient double[][] coords = ArrayUtil.EMPTY_DOUBLE_DOUBLE;
    private transient int[] assignments = ArrayUtil.EMPTY_INT_ARRAY;
    private transient double[] minDistSquared;

    /**
     * Random generator for choosing initial centers.
     */
    @Deprecated public RandomGenerator random;

    /**
     * Selected strategy for empty clusters.
     */
    private final EmptyClusterStrategy emptyStrategy;

    /**
     * Build a clusterer.
     * <p>
     * The default strategy for handling empty clusters that may appear during
     * algorithm iterations is to split the cluster with largest distance variance.
     *
     * @param k             the number of clusters to split the data into
     * @param measure       the distance measure to use
     * @param random        random generator to use for choosing initial centers
     */
    public KMeansPlusPlus(int k, int dims,
                          DistanceFunction measure,
                          RandomGenerator random) {
        this(k, dims, measure, random,
            //EmptyClusterStrategy.MOST_POINTS
            EmptyClusterStrategy.FARTHEST_POINT
            //EmptyClusterStrategy.LARGEST_VARIANCE
        );
    }

    /**
     * Build a clusterer.
     *
     * @param k             the number of clusters to split the data into
     * @param measure       the distance measure to use
     * @param random        random generator to use for choosing initial centers
     * @param emptyStrategy strategy to use for handling empty clusters that
     *                      may appear during algorithm iterations
     */
    public KMeansPlusPlus(int k, int dims,
                          DistanceFunction measure,
                          RandomGenerator random,
                          EmptyClusterStrategy emptyStrategy) {
        if (k < 2)
            throw new UnsupportedOperationException("clusters must be > 1");

        this.distance = measure;
        this.k = k;
        this.dims = dims;

        this.random = random instanceof Random ?
                new RandomBits((Random)random) :
                random;
        this.emptyStrategy = emptyStrategy;
    }

    public final void clusterDirect(Collection<X> points, int iters) {
        clusterDirect((Lst<X>)(points instanceof List ? (List<X>)points : new Lst<>(points)), iters);
    }


    /**
     * Runs the K-means++ clustering algorithm.
     *
     * @param values the points to cluster
     */
    public void clusterDirect(Lst<X> values, int max)  {

        int V = values.size();
        if (V <= 0)
            throw new UnsupportedOperationException();

//        if (V < k)
//            throw new TODO("remove unnecessary centroids"); //remove dependence on field 'k' for actual (only max) # of centroids

        this.values = values;
        realloc(V);

//        if (V == k) {
//            //TODO fast assign each point to its own centroid
//        }

        init();

        var clustersArray = clusters.array();
        var clustersNextArray = clustersNext.array();

        assign(clustersArray, assignments);

        // iterate through updating the centers until we're done
        int iter = 0;
        for ( ; iter < max; iter++) {
            //clustersNext.clear();

            for (int i = 0; i < k; i++) {
                CentroidCluster<X> y = clustersNextArray[i];
                y.clearValues();
                double[] yc = y.center;
                RoaringBitmap x = clustersArray[i].values;
                if (x.isEmpty())
                    coordCopy(emptyStrategy.get(this), yc);
                else
                    center(x, yc);
            }


            int changes = assign(clustersNextArray, assignments);
            if (changes <= 0)
                break; //converged

            for (int i = 0; i < k; i++)
                clustersArray[i].set(clustersNextArray[i]);
        }

        //clustersNext.clear(); //HACK to be sure
    }

    private void realloc(int c) {
        int c0 = assignments.length;
        if (c0 < c || (c < c0/2)) {
            //realloc
            assignments = new int[c];
            coords = new double[c][dims];
            minDistSquared = new double[c];
        }

        //ERASE
        for (int i = 0; i < c; i++) Arrays.fill(coords[i], Double.NaN);
        Arrays.fill(assignments, 0, c, -1);
        Arrays.fill(minDistSquared, 0, c, 0);

        int cc = clusters.size();
        if (cc != k) {
            if (cc > 0) {
                clusters.clear(); clustersNext.clear();
            }

            for (int i = 0; i < k; i++) {
                clusters.add(new CentroidCluster<>(dims));
                clustersNext.add(new CentroidCluster<>(dims));
            }
        }
    }


    /** by natural order of center vectors */
    public KMeansPlusPlus sortClusters() {
        clusters.sort((X,Y)->Arrays.compare(X.center,Y.center));
        return this;
    }

    /**
     * Adds the given points to the closest {@link Cluster}.
     *
     * @param clusters    the {@link Cluster}s to add the points to
     * @param points      the points to add to the given {@link Cluster}s
     * @param assignments points assignments to clusters
     * @return the number of points assigned to different clusters as the iteration before
     */
    private int assign(CentroidCluster<X>[] clusters, int[] assignments) {
        int changes = 0;

        int P = values.size();
        for (int p = 0; p < P; p++) {
            int c = nearest(p, clusters);
            if (c != assignments[p]) {
                changes++;
                assignments[p] = c;
            }

            clusters[c].add(p);
        }

        return changes;
    }

    public int nearest(X p) {
        int known = indexOf(p);
        var clustersArray = clusters.array();
        return known >= 0 ? nearest(known, clustersArray) : nearest(coord(p, new double[this.dims()]), clustersArray);
    }

    private int dims() {
        return coords[0].length; //HACK
    }

    private int indexOf(X p) {
        return values!=null ? values.indexOf(p) : -1;
    }

    private int nearest(int p, CentroidCluster<X>[] clusters) {
        return nearest(coord(p), clusters);
    }

    private int nearest(double[] p, CentroidCluster<X>[] cc) {
        double minDistance = Double.POSITIVE_INFINITY;
        int i = 0;
        int minCluster = 0;
        for (int j = 0, clustersSize = clusters.size(); j < clustersSize; j++) {
            double d = dist(p, cc[j].center);
            if (d < minDistance) {
                minDistance = d;
                minCluster = i;
            }
            i++;
        }
        return minCluster;
    }

    /**
     * Use K-means++ to choose the initial centers.
     *
     * @param points the points to choose the initial centers from
     */
    private void init() {

        // The number of points in the list.
        int P = values.size();

        // Set the corresponding element in this array to indicate when
        // elements of pointList are no longer available.
        MetalBitSet taken = MetalBitSet.bits(P);

        // Choose one center uniformly at random from among the data points.
        int firstPointIndex = random.nextInt(P);


        double[] firstPointC = coord(firstPointIndex);

        int cc = 0;
        clusters.get(cc++).set(firstPointC);

        // Must mark it as taken
        taken.set(firstPointIndex, true);

        // To keep track of the minimum distance squared of elements of
        // pointList to elements of resultSet.


        // Initialize the elements.  Since the only point in resultSet is firstPoint,
        // this is very easy.
        final double[] minDistSquared = this.minDistSquared;
        for (int i = 0; i < P; i++) {
            if (i != firstPointIndex) // That point isn't considered
                minDistSquared[i] = dist(i, firstPointC);
        }

        while (cc < k) {

            // Sum up the squared distances for the points in pointList not
            // already taken.
            double distSqSum = 0;
            for (int i = 0; i < P; i++) {
                if (!taken.test(i))
                    distSqSum += minDistSquared[i];
            }

            // Add one new data point as a center. Each point x is chosen with
            // probability proportional to D(x)2
            double r = random.nextDouble() * distSqSum;

            // The index of the next point to be added to the resultSet.
            int p = -1;

            // Sum through the squared min distances again, stopping when
            // sum >= r.
            double sum = 0;
            for (int i = 0; i < P; i++) {
                if (!taken.test(i)) {
                    sum += minDistSquared[i];
                    if (sum >= r) {
                        p = i;
                        break;
                    }
                }
            }

            // If it's not set to >= 0, the point wasn't found in the previous
            // for loop, probably because distances are extremely small.  Just pick
            // the last available point.
            if (p == -1) {
                for (int i = P - 1; i >= 0; i--) {
                    if (!taken.test(i)) {
                        p = i;
                        break;
                    }
                }
            }

            // We found one.
            if (p >= 0) {

                clusters.get(cc++).set(coord(p));

                // Mark it as taken.
                taken.set(p, true);

                if (clusters.size() < k) {
                    // Now update elements of minDistSquared.  We only have to compute
                    // the distance to the new center to do this.
                    double[] pC = coord(p);
                    for (int j = 0; j < P; j++) {
                        // Only have to worry about the points still not taken.
                        if (!taken.test(j)) {
                            double d2 = dist(j, pC);
                            if (d2 < minDistSquared[j])
                                minDistSquared[j] = d2;
                        }
                    }
                }

            } else {
                // None found --
                // Break from the while loop to prevent
                // an infinite loop.
                break;
            }
        }

    }

    private double dist(int x, double[] y) {
        return dist(coord(x), y);
    }

    private double dist(double[] x, double[] y) {
        return distance.distance(x, y);
    }


    public double[] coord(int x) {
        double[] c = coords[x];
        double c0 = c[0];
        return c0 == c0 ? c : coord(values.get(x), c);
    }

    public void coordCopy(int x, double[] target) {
        double[] c = coords[x];
        double c0 = c[0];
        if (c0 == c0) {
            System.arraycopy(c, 0, target, 0, c.length);
        } else {
            coord(values.get(x), target);
        }
    }
    public abstract double[] coord(X x, double[] coords);

    /**
     * Computes the centroid for a set of points.
     *
     * @param points    the set of points
     * @param dims the point dimension
     * @return the computed centroid for the set of points
     */
    private double[] center(RoaringBitmap points, double[] c) {
        int dims = c.length;
        int n = 0;

        Arrays.fill(c, 0);

        PeekableIntIterator pp = points.getIntIterator();
        while (pp.hasNext()) {
            int p = pp.next();
            double[] point = coord(p);
            for (int i = 0; i < dims; i++)
                c[i] += point[i];
            n++;
        }

        if (n>1) {
            for (int i = 0; i < dims; i++)
                c[i] /= n;
        }

        return c;
    }

//    public int nearest(X x) {
//        double minDistance = Double.POSITIVE_INFINITY;
//        int clusterIndex = 0;
//        int minCluster = 0;
//        for (final CentroidCluster<X> c : clusters) {
//            final double distance1 = distSq(x, c.center);
//            if (distance1 < minDistance) {
//                minDistance = distance1;
//                minCluster = clusterIndex;
//            }
//            clusterIndex++;
//        }
//        return minCluster;
//    }

    public void clear() {
        clusters.forEach(CentroidCluster::clear);
        //clusters.clear();
        coords = null;
        assignments = null;
        minDistSquared = null;
    }

    public int centroid(X instance) {
        return centroid(indexOf(instance));
    }

    public int centroid(int instance) {
        int k = clusters.size();
        for (int i = 0; i < k; i++) {
            CentroidCluster<X> ci = clusters.get(i);
            if (ci!=null && ci.values.contains(instance))
                return i;
        }
        return -1;
    }

    public void sortClustersByVariance() {
        //clusters.sortThisByDouble(i -> i.variance(this));
        clusters.sortThisByFloat(i -> (float)i.meanDistanceToCenter(this), true);
    }

    public final Lst<X> valueList(int c) {
        return clusters.get(c).valueList(this);
    }
    public final void values(int c, Consumer<X> each) {
        clusters.get(c).values(this, each);
    }
    public double[] center(int c) {
        return clusters.get(c).center;
    }

    public final int valueCount(int c) {
        return clusters.get(c).size();
    }

    public void valuesSampleN(int c, int n, Consumer<X> each, RandomGenerator rng) {
        clusters.get(c).sampleN(this, n, each, rng);
    }

    public final Iterator<X> valueIterator(int cluster) {
        return Iterators.transform(valueIDIterator(cluster), values::get);
    }

    private Iterator<Integer> valueIDIterator(int cluster) {
        return clusters.get(cluster).values.iterator();
    }

    public void sortClustersRandom() {
        clusters.shuffleThis(random);
    }

    public void close() {
        clusters.delete();
        clear();
    }

    /**
     * Strategies to use for replacing an empty cluster.
     */
    public enum EmptyClusterStrategy {

        /**
         * Split the cluster with largest distance variance.
         */
        LARGEST_VARIANCE() {
            @Override
            public <X> int get(KMeansPlusPlus<X> k) {
                var clusters = k.clusters;
                double maxVariance = Double.NEGATIVE_INFINITY;
                CentroidCluster<X> selected = null;
                for (CentroidCluster<X> cluster : clusters) {
                    if (cluster.values.isEmpty())
                        continue;


                    double variance = cluster.meanDistanceToCenter(k);

                    // select the cluster with the largest variance
                    if (variance > maxVariance) {
                        maxVariance = variance;
                        selected = cluster;
                    }

                }

                // did we find at least one non-empty cluster ?
                if (selected == null) {
                    throw new RuntimeException();
                }

                // extract a random point from the cluster
//        final List<X> selectedPoints = selected.values;
//        return selectedPoints.remove(random.nextInt(selectedPoints.size()));
                throw new TODO();
            }
        },

        /**
         * Split the cluster with largest number of points.
         */
        MOST_POINTS() {
            @Override
            public <X> int get(KMeansPlusPlus<X> k) {
                var clusters = k.clusters;
                int max = 0;
                CentroidCluster<X> biggest = null;
                int kk = clusters.size();
                int I = k.random.nextInt(kk); //random offset

                for (int i = 0; i < kk; i++) {
                    CentroidCluster<X> cluster = clusters.get(I++); if (I == kk) I = 0; //wraparound
                    int cs = cluster.size();
                    if (cs > max) {
                        max = cs;
                        biggest = cluster;
                    }
                }

//                // did we find at least one non-empty cluster ?
//                if (selected == null)
//                    throw new RuntimeException();

                // extract a random point from the cluster
                var vals = biggest.values;
                int vn = vals.getCardinality();
                int whichIth = k.random.nextInt(vn);
                int which;
                if (whichIth == 0)
                    which = vals.first();
                else if (whichIth == vn-1){
                    which = vals.last();
                } else {
                    var ii = vals.getIntIterator();
                    int ith = 0;
                    which = -1; //HACK
                    while (ii.hasNext()) {
                        int w = ii.next();
                        if (whichIth == ith++) {
                            which = w;
                            break;
                        }
                    }
                }
                vals.remove(which);
                return which;
            }
        },

        /**
         * Create a cluster around the point farthest from its centroid.
         */
        FARTHEST_POINT() {

            static class MaxDist<X> implements org.roaringbitmap.IntConsumer {
                final KMeansPlusPlus<X> k;
                final boolean fwd;

                int selectedPoint = -1;
                double maxDistance = Double.NEGATIVE_INFINITY;
                private transient CentroidCluster<X> selectedCluster = null;
                private transient double[] clusterCenter = null;
                private transient CentroidCluster<X> c;

                MaxDist(KMeansPlusPlus<X> k, boolean fwd) {
                    this.k = k;
                    this.fwd = fwd;
                }

                @Override
                public void accept(int i) {
                    double distance = k.dist(i, clusterCenter);
                    if ((fwd && distance > maxDistance) || (!fwd && distance >= maxDistance)) {
                        maxDistance = distance;
                        selectedCluster = c;
                        selectedPoint = i;
                    }

                }

                public int commit() {
                    selectedCluster.remove(selectedPoint);
                    this.c = null;
                    this.clusterCenter = null;
                    this.selectedCluster = null;
                    return selectedPoint;
                }

                public void apply(CentroidCluster<X> c) {
                    this.c = c;
                    this.clusterCenter = c.center;
                    c.values.forEach(this);
                }
            }

            @Override
            public <X> int get(KMeansPlusPlus<X> k) {
                var cc = k.clusters;

                var m = new MaxDist<>(k,  k.random.nextBoolean());

                int n = cc.size();
                for (int j = 0; j < n; j++)
                    m.apply(cc.get(j));

                return m.commit();
            }
        };

        abstract public <X> int get(KMeansPlusPlus<X> c);

    }

    private static final class CentroidCluster<X> {

        private static final CentroidCluster[] EmptyCentroidClustersArray = new CentroidCluster[0];

        public final RoaringBitmap values = new RoaringBitmap();
        public final double[] center;

        CentroidCluster(int dims) {
            center = new double[dims];
            Arrays.fill(center, Double.NaN);
        }

        CentroidCluster(double[] center) {
            this.center = center;
        }

//        @Override
//        public String toString() {
//            return center + "=" + valueList(KMeansPlusPlus.this);
//        }

        void add(int i) {
            values.add(i);
        }

        void remove(int i) {
            values.remove(i);
        }

        public void values(KMeansPlusPlus<X> k, Consumer<X> each) {
            PeekableIntIterator vv = values.getIntIterator();
            List<X> kv = k.values;
            while (vv.hasNext()) { each.accept(kv.get(vv.next())); }
        }

        private Lst<X> valueList(KMeansPlusPlus<X> k) {
            PeekableIntIterator vv = values.getIntIterator();
            List<X> kv = k.values;
            Lst<X> l = new Lst<>(size());
            while (vv.hasNext()) { l.addFast(kv.get(vv.next())); }
            return l;
        }

        private double meanDistanceToCenter(KMeansPlusPlus k) {

//            final Variance stat = new Variance();
//            values.forEach((int v) -> stat.increment(dist(v, center)));
//            return stat.getResult();

            double distSum = 0;
            int n = 0;
            PeekableIntIterator vv = values.getIntIterator();
            while (vv.hasNext()) {
                distSum += k.dist(vv.next(), center);
                n++;
            }
            return n == 0 ? Double.POSITIVE_INFINITY : distSum / n;
        }

        int size() {
            return values.getCardinality();
        }


        public void sampleN(KMeansPlusPlus<X> k, int max, Consumer<X> each, RandomGenerator rng) {
            int s = size();
            if (s < max) {
                values(k, each);
            } else {
                //choose subset
                short[] o = new short[s];
                PeekableIntIterator vv = values.getIntIterator();

                {
                    int i = 0;
                    while (vv.hasNext()) {
                        o[i++] = (short) vv.next();
                    }
                }

                ArrayUtil.shuffle(o, rng);

                for (int i = 0; i < max; i++)
                    each.accept(k.values.get(o[i]));

            }
        }

        public void set(CentroidCluster<X> c) {
            set(c.center);
            clearValues();
            values.or(c.values);
        }

        public void set(double[] c) {
            System.arraycopy(c, 0, center, 0, c.length);
        }

        public void clear() {
            clearValues();
            Arrays.fill(center, Double.NaN);
        }

        public void clearValues() {
            //if (!values.isEmpty()) //HACK this prevents unnecessary container realloc
            values.clear();
        }


    }


}