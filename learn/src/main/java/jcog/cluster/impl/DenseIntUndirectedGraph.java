package jcog.cluster.impl;

import org.eclipse.collections.api.block.predicate.primitive.IntPredicate;
import org.eclipse.collections.api.block.procedure.primitive.ShortIntProcedure;
import org.eclipse.collections.api.block.procedure.primitive.ShortProcedure;

import java.util.Arrays;

/**
 * WARNING not finished yet, doesnt seem to work the same as SemiDense which was the original implementation
 * a value of zero means no edge.
 * a positive value represents the weight of an edge
 */
public class DenseIntUndirectedGraph implements ShortUndirectedGraph {

    public final int[][] data;

    public DenseIntUndirectedGraph(short dim) {
        this.data = new int[dim][dim];
    }

    @Override
    public String toString() {
        return Arrays.deepToString(data);
    }

    @Override
    public void clear() {
        for (int[] x : data)
            Arrays.fill(x, 0);
    }

    @Override
    public void setEdge(short x, short y, int value) {
        data[x][y] = value;
        if (x!=y)
            data[y][x] = value;
    }

    private void addToEdge(short x, short y, int d) {
        int e = Math.max(0, data[x][y] + d);  //assert(data[y][x] == data[x][y]
        setEdge(x, y, e);
    }

    @Override
    public void edgesOf(short vertex, ShortIntProcedure eachKeyValue) {
        int[] a = data[vertex];
        for (short i = 0; i < a.length; i++) {
            int aa = a[i];
            if (aa != 0)
                eachKeyValue.value(i, aa);
        }
    }

    @Override
    public void edgesOf(short vertex, ShortProcedure eachKey) {
        int[] a = data[vertex];
        for (short i = 0; i < a.length; i++) {
            int aa = a[i];
            if (aa != 0)
                eachKey.value(i);
        }
    }

    @Override
    public void removeEdgeIf(IntPredicate filter) {
        int n = data.length;
        for (short i = 0; i < n; i++) {
            if (filter.accept(i)) {
                for (short j = 0; j < n; j++) {
                    setEdge(i, j, 0);
                }
            }
        }
    }

    @Override
    public void addToEdges(short v, int d) {
        for (short i = 0; i < data.length; i++)
            addToEdge(v, i, d);
    }


    @Override
    public void removeVertex(short v) {
        Arrays.fill(data[v], 0);
        for (int[] ee : data)
            ee[v] = 0;
    }

    @Override
    public void removeEdge(short first, short second) {
        setEdge(first, second, 0);
    }
}
