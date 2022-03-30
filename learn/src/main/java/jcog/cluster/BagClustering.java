package jcog.cluster;

import jcog.data.DistanceFunction;
import jcog.data.list.Lst;
import jcog.pri.PLink;
import jcog.pri.bag.Bag;
import jcog.pri.bag.impl.PriReferenceArrayBag;
import jcog.pri.op.PriMerge;
import jcog.random.XoRoShiRo128PlusRandom;

import java.util.function.BiConsumer;


/**
 * clusterjunctioning
 * TODO abstract into general purpose "Cluster of Bags" class
 */
public class BagClustering<X> {


    public final Bag<X, PLink<X>> bag;
    public final KMeansPlusPlus<PLink<X>> net;
    /**
     * each option here has its own subtle consequences. be careful
     */
    final PriMerge merge;
    final Dimensionalize<X> model;
    /**
     * TODO allow dynamic change
     */


    public BagClustering(Dimensionalize<X> model, int centroids, int initialCap, PriMerge merge) {
        this.merge = merge;
        this.model = model;

        this.net = new KMeansPlusPlus<>(centroids, model.dims, model, new XoRoShiRo128PlusRandom()) {
            @Override public double[] coord(PLink<X> x, double[] y) {
                model.accept(x.id, y);
                return y;
            }
        };

        PriReferenceArrayBag<X, PLink<X>> _b = new PriReferenceArrayBag<>(merge, initialCap);
        /*{
            @Override
            protected int histogramBins(int s) {
                return 0; //disabled
            }
        };*/
        this.bag =
                //new SimpleBufferedBag<>(_b);
                _b;

    }
//
//    public void print() {
//        print(System.out);
//    }
//
//    public void print(PrintStream out) {
//        forEachCluster(c -> {
//            out.println(c);
//            stream(c._id).forEach(i -> {
//                out.print("\t");
//                out.println(i);
//            });
//        });
//        out.println(net.edges);
//    }
//
//    public void forEachCluster(Consumer<Centroid> c) {
//        for (Centroid b : net.centroids) {
//            c.accept(b);
//        }
//    }

    public int size() {
        return bag.size();
    }

//    /**
//     * TODO re-use a centroid buffer array of lists stored in thredlocal Centroid conjoiner
//     */
//    public <L extends Lst<X>> void forEachCentroid(L[] centroidList, int minPerCentroid, int maxCentroids, Random rng) {
//
//        int s = bag.size();
//        if (s == 0) return;
//
//        int numCentroids = net.centroidCount();
//
//        maxCentroids = Math.min(numCentroids, maxCentroids);
//
//        int[] ready = new int[1];
//        Iterator<CLink<X>> i = bag.sampleUniqueIterator(rng);
//        while (i.hasNext()) {
//            CLink<X> x = i.next();
//            int xc = x.centroid;
//            if (xc < 0) continue;
//
//            L cc = centroidList[xc % numCentroids];
//
//            cc.add(x.id); //round robin populate the buffer
//
//            if (cc.size() == minPerCentroid) {
//                //threshold hit
//                if ((++ready[0]) >= maxCentroids)
//                    break;
//            }
//
//        }
//
//    }

    protected void learn(int clusterIterations) {

        int s = bag.size();
        if (s <= net.k)
            return;

        Lst<PLink<X>> l = net.values==null ? new Lst<>(s) : net.values.cleared();

        bag.addAllTo(l);

        net.clusterDirect(l, clusterIterations);
    }

    public void forget(float forgetRate) {
        bag.commit(bag.forget(forgetRate));
    }

//    public int centroid(X x) {
//        double[] v = new double[model.dims];
//        model.accept(x, v);
//        Centroid c = net.centroid(v);
//        return c != null ? c.id : -1;
//    }

//    private List<VLink<X>> itemsSortedByCentroid(Random rng) {
//
//        int s = bag.size();
//        if (s == 0)
//            return List.of();
//
//        FasterList<VLink<X>> x = new FasterList<>(s);
//        bag.forEach(x::add);
//
//
//        s = x.size();
//        if (s > 2) {
//
//            int shuffle = rng.nextInt();
//            IntToIntFunction shuffler = (c) -> c ^ shuffle;
//
//            ArrayUtils.quickSort(0, s,
//                    (a, b) -> a == b ? 0 : Integer.compare(
//                            shuffler.applyAsInt(x.get(a).centroid),
//                            shuffler.applyAsInt(x.get(b).centroid)),
//                    x::swap);
//
//        }
//
//
//        return x;
//    }

    public void clear() {
        synchronized (bag) {
            bag.clear();
            net.clear();
        }
    }

    public final PLink<X> put(PLink<X> X) {
        return bag.put(X);
    }

    public final PLink<X> put(X x, float pri) {
        return put(new PLink<>(x, pri));
    }

    public final void remove(X x) {
        bag.remove(x);
    }

    public int centroid(PLink<X> y) {
        return net.centroid(y);
    }

//    /**
//     * TODO this is O(N) not great
//     */
//    public Stream<PLink<X>> stream(int internalID) {
//        return bag.stream().filter(y -> y.centroid == internalID);
//    }

//    /**
//     * returns NaN if either or both of the items are not present
//     */
//    public double distance(X x, X y) {
//        //assert (!x.equals(y));
//        //assert (x != y);
//        if (x == y) return 0;
//
//        @Nullable VLink<X> xx = bag.get(x);
//        if (xx != null && xx.centroid >= 0) {
//            @Nullable VLink<X> yy = bag.get(y);
//            if (yy != null && yy.centroid >= 0) {
//                return Math.sqrt(net.distanceSq.distance(xx.coord, yy.coord));
//            }
//        }
//        return Double.POSITIVE_INFINITY;
//    }
//
//    public Stream<CLink<X>> neighbors(X x) {
//        @Nullable CLink<X> link = bag.get(x);
//        if (link != null) {
//            int centroid = link.centroid;
//            if (centroid >= 0) {
//                Centroid[] nodes = net.centroids;
//                if (centroid < nodes.length)
//                    return stream(centroid)
//                            .filter(y -> !y.equals(x))
//                            ;
//            }
//        }
//        return Stream.empty();
//    }

    /**
     * how to interpret the bag items as vector space data
     */
    public abstract static class Dimensionalize<X> implements BiConsumer<X, double[]>, DistanceFunction {

        final int dims;

        protected Dimensionalize(int dims) {
            this.dims = dims;
        }

        public abstract void accept(X t, double[] d);

    }

//    private static final Comparator<List> ListSizeSorter =(x, y)-> {
//        if (x == null) return +1;
//        if (y == null) return -1;
//        return Integer.compare(y.size(), x.size());
//    };

}