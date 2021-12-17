/*
 *  Array2DIntgIterator implements Iterator interface and provides the functionality 
 *  to iterate through the elements of a 2 dimensional int array in an   
 *  clockwise inward spiral starting from the top left cell.
 * 
 *  A [3,4] array of characters with these values:
 *             1   2   3   4
 *             10  11  l2  5
 *             9   8   7   6
 *  would iterate as:
 *             1 2 3 4 5 6 7 8 9 10 11 12
 * 
 */
package jcog.data.iterator;

import jcog.Util;
import jcog.data.list.Lst;
import jcog.math.GilbertCurve;

import java.util.Arrays;
import java.util.Iterator;

import static jcog.data.iterator.ArrayIterator.iterateN;

/**
 * from: https:
 *
 * @version 1.0
 * @author Sol
 * @since December, 2013
 */
public class Array2DIterable<X> implements Iterable<X> {

    public final X[] order;

    public Array2DIterable(X[][] x, boolean spaceFilling) {
        int cols = x[0].length;
        int rows = x.length;
        int area = rows * cols;

        Lst<X> tmp = new Lst<>(0, Arrays.copyOf(x[0], area));

        this.order = spaceFilling ? gilbertCurve(x, tmp) : rowMajor(x, tmp);
    }

    /** https://en.wikipedia.org/wiki/Moore_curve */
    private X[] gilbertCurve(X[][] a, Lst<X> t) {
        GilbertCurve.gilbertCurve(a[0].length, a.length, (x, y)-> t.add(a[y][x]));
        return t.array();
    }

    /** or is this col major? */
    private static <X> X[] rowMajor(X[][] x, Lst<X> t) {
        for (X[] xes : x)
            t.addAll(xes);
        return t.array();
    }

    @Override
    public final Iterator<X> iterator() {
        return ArrayIterator.iterate(order);
    }

    public final X get(int i) {
        return order[i];
    }

    /** iterate a range */
    public Iterator<X> iterator(int s, int e) {
        if (s == e)
            return Util.emptyIterator;

        X[] a = order;
        return s < e ? iterateN(a, s, e) :
            Concaterator.concat(iterateN(a, s, order.length), iterateN(a, 0, e)); //wrap-around
    }
}