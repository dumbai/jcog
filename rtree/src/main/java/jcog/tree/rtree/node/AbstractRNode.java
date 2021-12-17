package jcog.tree.rtree.node;

import jcog.data.iterator.ArrayIterator;
import jcog.tree.rtree.HyperRegion;

import java.util.Iterator;
import java.util.stream.Stream;

public abstract class AbstractRNode<V,D> implements RNode<V> {

    public /*volatile*/ short size = 0;
    public /*volatile*/ HyperRegion bounds;
    public final D[] items;

    protected AbstractRNode(D[] items) {
        this.items = items;
    }

    @Override
    public final int available() {
        return items.length - size;
    }

    @Override
    public final Stream<D> streamLocal() {
        return ArrayIterator.streamNonNull(items, size); //TODO null-terminator iterator eliding 'size'
    }
    @Override
    public final Iterator<D> iterateLocal() {
        return ArrayIterator.iterateN(items, size);
    }


    @Override
    public final HyperRegion bounds() {
        return bounds;
    }

    @Override
    public final int size() {
        return size;
    }


//    public final void drainLayer(Consumer each) {
//        int s = size;
//        D[] data = this.items;
//        for (int i = 0; i < s; i++) {
//            Object x = data[i];
//
//            //"tail-leaf" optimization: inline 1-arity branches for optimization
//            while (x instanceof RLeaf) {
//                RLeaf lx = (RLeaf) x;
//                if (lx.size != 1)
//                    break;
//                x = lx.items[0];
//            }
//
////        //dont filter root node (traversed while plan is null)
////        if ((x instanceof Node) && nodeFilter != null && !plan.isEmpty() && !nodeFilter.tryVisit((Node)x))
////            return null;
//
//            each.accept(x);
//        }
//    }
}