package jcog.data.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * indexed cartesian product iterator
 * from: https://stackoverflow.com/a/10946027 */
public class CartesianProductIndex implements Iterable<int[]>, Iterator<int[]> {

    private final int[] _lengths;
    private final int[] _indices;
    private boolean _hasNext = true;

    public CartesianProductIndex(int[] lengths) {
        _lengths = lengths;
        _indices = new int[lengths.length];
    }

    public boolean hasNext() {
        return _hasNext;
    }

    private transient int[] result;
    public int[] next() {
        int[] indices = _indices;
        int n = indices.length;

        int[] result = this.result;
        if (result == null) result = this.result = new int[n];

        System.arraycopy(indices, 0, result, 0, n);

        for (int i = n - 1; i >= 0; i--) {
            if (indices[i] == _lengths[i] - 1) {
                indices[i] = 0;
                if (i == 0)
                    _hasNext = false;
            } else {
                indices[i]++;
                break;
            }
        }

        return result;
    }

    public Iterator<int[]> iterator() {
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}