package jcog.cluster;

import com.google.common.base.Joiner;
import jcog.cluster.impl.DenseIntUndirectedGraph;
import jcog.cluster.impl.ShortUndirectedGraph;
import jcog.data.Centroid;
import jcog.data.DistanceFunction;
import jcog.data.OnlineClustering;
import jcog.data.iterator.ArrayIterator;
import jcog.pri.Prioritized;
import jcog.signal.FloatRange;
import jcog.tree.rtree.point.DoubleND;
import jcog.tree.rtree.rect.HyperRectDouble;
import jcog.tree.rtree.rect.MutableHyperRectDouble;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * from: https:
 * TODO use a graph for incidence structures to avoid some loops
 */
public class NeuralGasNet<C extends Centroid> /*extends SimpleGraph<N, Connection<N>>*/ implements OnlineClustering<C> {


    public final int dimension;
    public final ShortUndirectedGraph edges;
    public final Centroid[] centroids;
    private final DistanceFunction distanceSq;
    public final FloatRange alpha = new FloatRange(0.01f, 0, 1f);
    public final FloatRange beta = new FloatRange(0.01f, 0, 1f);
    /**
     * the bounds of all the centroids in all dimensions (ex: for normalizing their movement and/or distance functions)
     * stored as a 1D double[] with every pair of numbers corresponding
     * to min/max bounds of each dimension, so it will have dimension*2 elements
     */
//    private final MutableHyperRectDouble rangeMinMax;
    private int iteration;
    private int lambdaPeriod;
    private int ttl;
    /**
     * faster point learning for the winner node
     */
    private double winnerUpdateRate;

    /**
     * slower point learning for the neighbors of a winner node
     */
    private double winnerNeighborUpdateRate;
    private transient double maxError;
    private transient short _maxErrorNeighbour;
    private transient int centroidsInactive;


    public NeuralGasNet(int dimension, int centroids) {
        this(dimension, centroids, DistanceFunction::distanceCartesianSq);
    }

    public NeuralGasNet(int dim, int centroids, DistanceFunction distanceSq) {
        super();

        this.edges = new DenseIntUndirectedGraph((short) centroids);

        this.centroids = new Centroid[centroids];
        for (int i = 0; i < centroids; i++)
            this.centroids[i] = newCentroid(i, dim);

        centroidsInactive = centroids;
//        this.rangeMinMax = new MutableHyperRectDouble(dimension);

        this.distanceSq = distanceSq;

        this.iteration = 0;
        this.dimension = dim;

        population(centroids*2); //estimate
    }

    /** set learning parameters for a given expected population size */
    public NeuralGasNet population(int popSize) {

        int C = centroids.length;

        alpha.set(1f/(popSize));
        beta.set(1f/(popSize));

        setLambdaPeriod(popSize*2);
        setMaxEdgeAge(popSize*4);

        float a =
                //0.5f;
                //0.25f;
                1f/popSize;

        setWinnerUpdateRate(a / C, a / 2 / C);

        return this;
    }

    protected Centroid newCentroid(int i, int dim) {
        return new Centroid(i, dim);
    }


    /**
     * lifespan of an node
     */
    public void setLambdaPeriod(int lambdaPeriod) {
        this.lambdaPeriod = lambdaPeriod;
    }

    public void setWinnerUpdateRate(double rate, double neighborRate) {
        this.winnerUpdateRate = rate;
        this.winnerNeighborUpdateRate = neighborRate;
    }

    public void setBeta(float beta) {
        this.beta.set(beta);
    }

    public void setMaxEdgeAge(int maxAge) {
        this.ttl = maxAge;
    }

    public int getTtl() {
        return ttl;
    }

    public void forEachCentroid(Consumer<C> each) {
        for (Centroid n : centroids)
            each.accept((C) n);
    }

    public void clear() {
        edges.clear();

        for (Centroid centroid : centroids) centroid.clear();
    }

    @Override
    public Iterable/*<C>*/ centroids() {
        return ArrayIterator.iterable(centroids);
    }

    /** closest centroid */
    @Override public C get(double[] x) {

        double minDistSq = Double.POSITIVE_INFINITY;
        Centroid closest = null;

        //TODO shuffle order?
        for (Centroid n : centroids) {
            double dist;
            if ((dist = distanceSq.distance(n.getDataRef(), x)) < minDistSq) {
                closest = n;
                minDistSq = dist;
            }
        }

        return (C) closest;
    }

    public HyperRectDouble bounds() {
        MutableHyperRectDouble b = null;
        //double[] err = new double[dimension];

        for (Centroid n : centroids) {
            if (n.active()) {
                if (b == null) {
                    b = new MutableHyperRectDouble(new DoubleND(n.toArray()));
                } else {
                    b.mbrSelf(n.getDataRef());
                }
                //err[i] = n.localError();
            }
        }
//        for (int k = 0; k < dimension; k++) {
//            b.grow(k, err[k]);
//        }
        return b;
    }

    /**
     * translates all nodes uniformly
     */
    public void translate(double[] x) {
        for (Centroid n : centroids) {
            n.add(x);
        }
    }


    public synchronized C put(double... x) {
        if (x.length != dimension)
            throw new ArrayIndexOutOfBoundsException();

        int iteration = this.iteration++;

        if (centroidsInactive > 0)
            return learnInactive(x);

        Centroid[] centroids = this.centroids;

        int N = centroids.length;

        short closest = -1, furthest = -1;
        double minDist = Double.POSITIVE_INFINITY, maxDist = Double.NEGATIVE_INFINITY;
        //visit in shuffled order in case distances are equal
        int seed = Math.abs(Double.hashCode(x[iteration%x.length])/2);
        for (short ii = 0; ii < N; ii++) {


            short i = (short) ((ii + seed) % N);
            double d = centroids[i].learn(x, distanceSq);

            if (d > maxDist) {
                furthest = i;
                maxDist = d;
            }
            if (d < minDist) {
                closest = i;
                minDist = d;
            }
        }

        if (closest == -1)
            throw new RuntimeException();

        centroids[closest].updateLocalError(x, winnerUpdateRate);
        edges.edgesOf(closest, (connection, age) ->
                centroids[connection].lerp(x, winnerNeighborUpdateRate));
        edges.addToEdges(closest, -1);


        short nextClosest = nextClosest(closest, furthest);
        if (nextClosest!=-1)
            edges.setEdge(closest, nextClosest, ttl);

        float alpha = this.alpha.floatValue();
        if (alpha >= Prioritized.EPSILON && lambdaPeriod != 0) {
            if (iteration % lambdaPeriod == 0)
                regrow(alpha, centroids, furthest);

            forget();
        }

        return (C) centroids[closest];
    }

    private short nextClosest( short closest, short furthest) {
        int N = centroids.length;
        double nextMinDist = Double.POSITIVE_INFINITY;
        short nextClosest = -1;
        for (short i = 0; i < N; i++) { //TODO shuffle order
            if (i == furthest || i == closest) continue;
            Centroid ci = centroids[i];
            if (ci.active()) {
                double dd = ci.tmpDistance();
                if (dd < nextMinDist) {
                    nextClosest = i;
                    nextMinDist = dd;
                }
            }
        }
        return nextClosest;
    }

    @NotNull
    private C learnInactive(double[] x) {
        for (Centroid c : centroids) {
            if (!c.active()) {
                //assign to uninitialized centroid
                c.learn(x, null);
                centroidsInactive--;
                return (C) c;
            }
        }
        throw new RuntimeException();
    }

    private void forget() {
        float beta = this.beta.floatValue();
        for (Centroid n : centroids)
            n.mulLocalError(beta);
    }

    private void regrow(float alpha, Centroid[] centroids, short furthest) {
        edges.removeVertex(furthest);
        removed((C) centroids[furthest]);

        short worstID = -1;
        {
        double maxError = Double.NEGATIVE_INFINITY;
        for (int i = 0, N = centroids.length; i < N; i++) {
            Centroid n = centroids[i];
            if (i == furthest)
                continue;
            if (n.localError() > maxError) {
                worstID = (short) i;
                maxError = n.localError();
            }
        }

        if (worstID == -1)
            throw new RuntimeException();
        }


        maxError = Double.NEGATIVE_INFINITY;
        _maxErrorNeighbour = -1;

        edges.edgesOf(worstID, (i) -> {
            double e = centroids[i].localError();
            if (e > maxError) {
                _maxErrorNeighbour = i;
                maxError = e;
            }
        });

        if (_maxErrorNeighbour != -1) {

            short worstNeighborID = _maxErrorNeighbour;


            edges.removeEdge(worstID, worstNeighborID);


            Centroid worstNode = centroids[worstID];
            Centroid worstNeighbor = centroids[worstNeighborID];

            centroids[furthest].mix(worstNode, worstNeighbor);

            if (worstID == furthest)
                throw new RuntimeException("new node has same id as max error node");

            edges.setEdge(worstID, furthest, ttl);
            edges.setEdge(worstNeighborID, furthest, ttl);

            worstNode.mulLocalError(alpha);
            worstNeighbor.mulLocalError(alpha);
        }
    }

//    public void randomizeCentroid(MutableHyperRectDouble bounds, N newNode) {
//        for (int i = 0; i < dimension; i++)
//            newNode.randomizeUniform(i, bounds.coord(i, false), bounds.coord(i, true));
//    }


    public final C node(int i) {
        return (C) centroids[i];
    }

//    private short randomNode() {
//        return (short) (Math.random() * centroids.length);
//    }

    /**
     * called before a node will be removed
     */
    protected void removed(C furthest) {

    }

    public Stream<C> nodeStream() {
        return Stream.of(centroids).filter(Centroid::active).map(n -> (C) n);
    }

    public void compact() {
        edges.compact();
    }

    public int size() {
        return centroids.length;
    }

    @Override
    public String toString() {
        return Joiner.on("\n").join(centroids);
    }


    public int centroidCount() {
        return centroids.length;
    }
}