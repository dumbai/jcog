package jcog.pri;

import jcog.data.Centroid;
import jcog.data.OnlineClustering;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * prioritized centroid link
 * a) reference to an item
 * b) priority
 * c) vector coordinates
 * d) current centroid id(s)
 */
public final class CLink<X> extends HashedPLink<X> {

    /**
     * feature vector representing the item as learned by clusterer
     */
    public final double[] coord;

    /**
     * current centroid
     * TODO if allowing multiple memberships this will be a RoaringBitmap or something
     */
    public int centroid = -1;


//    public VLink(X t, float pri, double[] coord) {
//        super(t, pri);
//        this.coord = coord;
//    }

    public CLink(X t, float pri, int dims) {
        super(t, pri);
        Arrays.fill(this.coord = new double[dims], Double.NaN);
    }

    @Override
    public String toString() {
        return toBudgetString() + ' ' + id + '<' + Arrays.toString(coord) + '@' + centroid+ '>';
    }


    public void update(@Nullable Consumer<CLink<X>> f) {
        X tt = id;
        if ((tt instanceof Deleteable) && ((Deleteable) tt).isDeleted())
            delete();
        else {
            if (f!=null)
                f.accept(this);
        }
    }

    public Centroid train(OnlineClustering net, BiConsumer<X,double[]> model) {
        double[] c = this.coord;

        double c0 = c[0];
        if (c0 != c0) //invalidated?
            model.accept(id, c);

        Centroid y = net.put(c);
        centroid = y.id;
        return y;
    }

}