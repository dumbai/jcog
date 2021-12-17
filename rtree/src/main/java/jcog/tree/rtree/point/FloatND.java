package jcog.tree.rtree.point;


import jcog.signal.tensor.ArrayTensor;

import java.io.Serializable;
import java.util.Arrays;


/**
 * Created by me on 12/21/16.
 */
public class FloatND extends ArrayTensor implements HyperPoint, Serializable, Comparable<FloatND> {

//    private final int hash;

    public FloatND(FloatND copy) {
        this(copy.data.clone());
    }

    public FloatND(float... coord) {
        super(coord);
//        this.hash = Arrays.hashCode(coord);
    }

    public static FloatND fill(int dims, float value) {
        float[] a = new float[dims];
        Arrays.fill(a, value);
        return new FloatND(a);
    }

    @Override
    public int dim() {
        return data.length;
    }

    @Override
    public Float coord(int d) {
        return data[d];
    }

    @Override
    public double distance(HyperPoint h) {
        if (this == h) return 0;
        FloatND p = (FloatND) h;
        float sumSq = 0;
        for (int i = 0; i < data.length; i++) {
            float x = data[i];
            float y = p.data[i];
            float xMinY = x - y;
            sumSq += xMinY * xMinY;
        }
        return Math.sqrt(sumSq);
    }

    @Override
    public double distance(HyperPoint p, int i) {
        return this==p ? 0 : Math.abs(data[i] - ((FloatND) p).data[i]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FloatND floatND)) return false;

        return /*hash == floatND.hashCode() && */Arrays.equals(data, floatND.data);
    }

    @Override
    public int hashCode() {
        //return hash;
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        return '(' + Arrays.toString(data) + ')';
    }


    @Override
    public int compareTo(FloatND o) {
        return Arrays.compare(data, o.data);
    }
}