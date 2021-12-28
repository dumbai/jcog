package jcog.cluster;

import com.google.common.collect.Iterators;
import jcog.Is;
import jcog.TODO;
import jcog.data.DistanceFunction;
import jcog.data.bit.MetalBitSet;
import jcog.data.list.Lst;
import jcog.random.RandomBits;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.util.ArrayUtil;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.PeekableIntIterator;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;
import java.util.function.Consumer;

/**
 * adapted from https://github.com/Hipparchus-Math/hipparchus/blob/master/hipparchus-clustering/src/main/java/org/hipparchus/clustering/KMeansPlusPlusClusterer.java
 */
@Is("Determining_the_number_of_clusters_in_a_data_set")
public abstract class KMeansPlusPlus<X> {
    private final DistanceFunction distance;

    // The resulting list of initial centers.
    public final Lst<CentroidCluster<X>> clusters = new Lst<>(EmptyCentroidClustersArray);
    private final Lst<CentroidCluster<X>> clustersNext = new Lst<>(EmptyCentroidClustersArray);
    private static final CentroidCluster[] EmptyCentroidClustersArray = new CentroidCluster[0];

    /**
     * The number of clusters.
     */
    public final int k;

    private final int dims;


    /**
     * Random generator for choosing initial centers.
     */
    @Deprecated public Random random;

    /**
     * Selected strategy for empty clusters.
     */
    private final EmptyClusterStrategy emptyStrategy;


    public transient Lst<X> values;
    private transient double[][] coords = ArrayUtil.EMPTY_DOUBLE_DOUBLE;
    private transient int[] assignments = ArrayUtil.EMPTY_INT_ARRAY;
    private transient double[] minDistSquared;

    /**
     * Build a clusterer.
     * <p>
     * The default strategy for handling empty clusters that may appear during
     * algorithm iterations is to split the cluster with largest distance variance.
     * <p>
     * The euclidean distance will be used as default distance measure.
     *
     * @param k the number of clusters to split the data into
     *          https://en.wikipedia.org/wiki/Determining_the_number_of_clusters_in_a_data_set
     */
    public KMeansPlusPlus(int k, int dims) {
        this(k, dims, DistanceFunction::distanceCartesianSq);
    }

    public KMeansPlusPlus(int k, int dims, DistanceFunction measure) {
        this(k, dims, measure, new XoRoShiRo128PlusRandom());
    }

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
                          Random random) {
        this(k, dims, measure, random,
            //EmptyClusterStrategy.POINTS_MOST
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
                          Random random,
                          EmptyClusterStrategy emptyStrategy) {
        if (k < 2)
            throw new UnsupportedOperationException("clusters must be > 1");

        this.distance = measure;
        this.k = k;
        this.dims = dims;

        this.random = random;
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


        clusters.clear();

        if (V == k) {
            //TODO fast assign each point to its own centroid
        }


        init();

        assign(clusters, assignments);

        // iterate through updating the centers until we're done
        int iter = 0;
        for ( ; iter < max; iter++) {
            clustersNext.clear();
            for (CentroidCluster<X> cluster : clusters) {
                RoaringBitmap v = cluster.values;
                clustersNext.add(new CentroidCluster(v.isEmpty() ?
                        coord(emptyStrategy.get(this)) :
                        center(v, cluster.center.length)));
            }

            int changes = assign(clustersNext, assignments);
            //System.out.println(iter + " " + n2(((float)changes)/V));
            if (changes <= 0)
                break; //converged

            clusters.clear();
            clusters.addAll(clustersNext);
        }

        clustersNext.clear(); //HACK to be sure
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
    private int assign(Lst<CentroidCluster<X>> clusters, int[] assignments) {
        int changes = 0;

        int P = values.size();
        for (int p = 0; p < P; p++) {
            int c = nearest(p, clusters);
            if (c != assignments[p]) {
                changes++;
                assignments[p] = c;
            }

            clusters.get(c).add(p);
        }

        return changes;
    }

    public int nearest(X p) {
        int known = indexOf(p);
        return known >= 0 ? nearest(known, clusters) : nearest(coord(p, new double[this.dims()]), clusters);
    }

    private int dims() {
        return coords[0].length; //HACK
    }

    private int indexOf(X p) {
        return values!=null ? values.indexOf(p) : -1;
    }

    private int nearest(int p, Lst<CentroidCluster<X>> clusters) {
        return nearest(coord(p), clusters);
    }

    private int nearest(double[] p, Lst<CentroidCluster<X>> clusters) {
        double minDistance = Double.POSITIVE_INFINITY;
        int i = 0;
        int minCluster = 0;
        CentroidCluster<X>[] cc = clusters.array();
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

        clusters.add(new CentroidCluster(firstPointC));

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

        while (clusters.size() < k) {

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

                clusters.add(new CentroidCluster(coord(p)));

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

    public abstract double[] coord(X x, double[] coords);

    /**
     * Computes the centroid for a set of points.
     *
     * @param points    the set of points
     * @param dim the point dimension
     * @return the computed centroid for the set of points
     */
    private double[] center(RoaringBitmap points, int dim) {
        int n = points.getCardinality();

        double[] c = new double[dim];
        PeekableIntIterator pp = points.getIntIterator();
        while (pp.hasNext()) {
            int p = pp.next();
            double[] point = coord(p);
            for (int i = 0; i < dim; i++)
                c[i] += point[i];
        }

        if (n>1) {
            for (int i = 0; i < dim; i++)
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
        clusters.clear();
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
        clusters.sortThisByFloat(i -> (float)i.variance(this), true);
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

    public void valuesSampleN(int c, int n, Consumer<X> each, RandomBits rng) {
        clusters.get(c).sampleN(this, n, each, rng);
    }

    public final Iterator<X> valueIterator(int cluster) {
        return Iterators.transform(valueIDIterator(cluster), values::get);
    }

    private Iterator<Integer> valueIDIterator(int cluster) {
        return clusters.get(cluster).values.iterator();
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


                    double variance = cluster.variance(k);

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
        POINTS_MOST() {
            @Override
            public <X> int get(KMeansPlusPlus<X> k) {
                var clusters = k.clusters;
                int max = 0;
                CentroidCluster<X> selected = null;
                int kk = clusters.size();
                int offset = k.random.nextInt(kk); //random offset
                for (int i = 0; i < kk; i++) {
                    int I = (i + offset) % kk;
                    CentroidCluster<X> cluster = clusters.get(I);

                    // get the number of points of the current cluster
                    int cs = cluster.size();

                    // select the cluster with the largest number of points
                    if (cs > max) {
                        max = cs;
                        selected = cluster;
                    }
                }

//                // did we find at least one non-empty cluster ?
//                if (selected == null)
//                    throw new RuntimeException();

                // extract a random point from the cluster
                var vals = selected.values;
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
            @Override
            public <X> int get(KMeansPlusPlus<X> k) {
                var clusters = k.clusters;
                double maxDistance = Double.NEGATIVE_INFINITY;
                CentroidCluster<X> selectedCluster = null;
                int selectedPoint = -1;
                int cc = clusters.size();
                for (int j = 0; j < cc; j++) {

                    CentroidCluster<X> c = clusters.get(j);
                    double[] clusterCenter = c.center;
                    RoaringBitmap v = c.values;
                    IntIterator vv = k.random.nextBoolean() ?
                            v.getReverseIntIterator() : v.getIntIterator();
                    while (vv.hasNext()) {
                        int i = vv.next();
                        double distance = k.dist(i, clusterCenter);
                        if (distance > maxDistance) {
                            maxDistance = distance;
                            selectedCluster = c;
                            selectedPoint = i;
                        }
                    }

                }

                // did we find at least one non-empty cluster ?
                selectedCluster.remove(selectedPoint);
                return selectedPoint;
            }
        };

        abstract public <X> int get(KMeansPlusPlus<X> c);

    }

    public static final class CentroidCluster<X> {
        public final RoaringBitmap values = new RoaringBitmap();
        public final double[] center;

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

//        /** from inclusive, to exclusive */
//        public void values(KMeansPlusPlus<X> k, Consumer<X> each, int from, int to) {
//            PeekableIntIterator vv = values.getIntIterator();
//
//            //HACK
//            for (int i = 0; i < from; i++) { if (!vv.hasNext()) return; vv.next();  }
//
//            List<X> kv = k.values;
//            int r = to-from;
//            while (r-- > 0 && vv.hasNext()) { each.accept(kv.get(vv.next())); }
//        }

        private Lst<X> valueList(KMeansPlusPlus<X> k) {
            PeekableIntIterator vv = values.getIntIterator();
            List<X> kv = k.values;
            Lst<X> l = new Lst<>(size());
            while (vv.hasNext()) { l.addFast(kv.get(vv.next())); }
            return l;
        }

        private double variance(KMeansPlusPlus k) {

//            final Variance stat = new Variance();
//            values.forEach((int v) -> stat.increment(dist(v, center)));
//            return stat.getResult();

            double variance = 0;
            int n = 0;
            PeekableIntIterator vv = values.getIntIterator();
            while (vv.hasNext()) {
                variance += k.dist(vv.next(), center);
                n++;
            }
            return variance / n;
        }

        int size() {
            return values.getCardinality();
        }


        public void sampleN(KMeansPlusPlus<X> k, int max, Consumer<X> each, RandomBits rng) {
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
    }


}