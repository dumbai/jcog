/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package jcog.data.graph;

import jcog.TODO;
import jcog.data.bit.MetalBitSet;
import org.eclipse.collections.api.block.procedure.primitive.IntProcedure;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

/**
 * This class implements a graph which uses a bitmatrix as inner representation
 * of edges.
 */
public abstract class BitMatrixGraph extends IntIndexedGraph<Integer, Boolean> {


    private final boolean directed;


    /**
     * Constructs an graph with the given number of nodes.
     * The graph has no edges initially.
     *
     * @param n        size of graph
     * @param directed if true graph is directed
     */
    public BitMatrixGraph(boolean directed) {
        this.directed = directed;
    }

    /**
     * Returns null always
     */
    @Override
    public Integer vertex(int v) {
        return v;
    }


    /**
     * Returns null always.
     */
    @Override
    public Boolean edge(int i, int j) {
        return isEdge(i, j);
    }


    public abstract boolean getAndSet(int i, int j, boolean v);

    @Override
    public boolean directed() {
        return directed;
    }


    public final void set(int i, int j, boolean next) {
        validCell(i, j);

        _set(i, j, next);
        if (!directed)
            _set(j, i, next);
    }

    protected abstract void _set(int i, int j, boolean next);

    public final boolean setIfChanged(int i, int j, boolean next) {
        validCell(i, j);

        boolean changed = getAndSet(i, j, next) != next;
        if (!directed)
            changed |= getAndSet(j, i, next) != next;

        return changed;
    }

    private void validCell(int i, int j) {
//        if (i==j)
//            throw new UnsupportedOperationException("loop");

        int s = size();
        if (i > s || j > s || i < 0 || j < 0) throw new IndexOutOfBoundsException();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(64);
        int n = size();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                s.append(isEdge(r, c) ? '1':'0');
                if (c < n-1) s.append(' ');
            }
            if (r < n - 1) s.append('\n');
        }
        return s.toString();
    }

    @Override
    public final boolean removeEdge(int i, int j) {
        return setIfChanged(i, j, false);
    }

    public abstract void removeRow(int c);

    public abstract void removeCol(int c);

    public final void removeNode(int node) {
        removeRow(node);
        removeCol(node);
    }

    public abstract int firstColumn(int r, boolean b);
    public abstract int lastColumn(int r, boolean b);

    public abstract int rowCardinality(int r);

    public abstract int edgeCount();

    public boolean isEmpty() {
        return edgeCount() == 0;
    }


    public static class BitSetMatrixGraph extends BitMatrixGraph {


        private final MetalBitSet data;
        private final int size;

        public BitSetMatrixGraph(int n, boolean directed) {
            super(directed);
            this.data = MetalBitSet.bits(n*n);
            this.size = n;
        }

        @Override
        public int edgeCount() {
            return data.cardinality();
        }

        @Override
        protected void _set(int r, int c, boolean next) {
            data.set(rc(r,c), next);
        }

        @Override
        public boolean getAndSet(int r, int c, boolean v) {
            return data.getAndSet(rc(r, c), v);
        }

        @Override
        public boolean isEdge(int r, int c) {
            return data.test(rc(r, c));
        }

        private int rc(int r, int c) {
            return r * size + c;
        }

        @Override
        public void removeRow(int r) {
            for (int c = 0; c < size; c++)
                set(r, c, false);
        }

        @Override
        public void removeCol(int c) {
            for (int r = 0; r < size; r++)
                set(r, c, false);
        }

        @Override
        public IntHashSet neighborsOut(int r) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int degreeOut(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int firstColumn(int r, boolean b) {
            int n = size;
            for (int c = 0; c < n; c++)
                if (isEdge(r, c)==b) return c;
            return -1;
        }
        @Override
        public int lastColumn(int r, boolean b) {
            int n = size;
            for (int c = n-1; c >=0; c--)
                if (isEdge(r, c)==b) return c;
            return -1;
        }

        @Override
        public int rowCardinality(int r) {
            int sum = 0;
            for (int c = 0, n = this.size; c < n; c++) {
                if (isEdge(r, c)) sum++;
            }
            return sum;
        }
    }

    public static class BitSetRowsMatrixGraph extends BitMatrixGraph {

        private final MetalBitSet[] rows;

        public BitSetRowsMatrixGraph(int n, boolean directed) {
            super(directed);
            rows = new MetalBitSet[n];
            for (int i = 0; i < n; ++i)
                rows[i] = MetalBitSet.bits(n);
        }

        public MetalBitSet row(int i) {
            return rows[i];
        }

        @Override
        public boolean isEdge(int r, int c) {
            return rows[r].test(c);
        }

        @Override
        public boolean getAndSet(int i, int j, boolean v) {
            return rows[i].getAndSet(j, v);
        }

        @Override
        protected void _set(int i, int j, boolean v) {
            rows[i].set(j, v);
        }

        @Override
        public int size() {
            return rows.length;
        }

        @Override
        public int edgeCount() {
            throw new TODO();
        }

        @Override
        public int degreeOut(int i) {
            return rows[i].cardinality();
        }

        /**
         * removes all edges with row=j or col=j
         */
        public void removeRow(int r) {
            rows[r].clear();
        }


        public void removeCol(int c) {
            for (MetalBitSet row : rows)
                row.clear(c);
        }

        @Override
        public int firstColumn(int r, boolean b) {
            return row(r).first(b);
        }

        @Override
        public int lastColumn(int r, boolean b) {
            throw new TODO();
        }

        @Override
        public int rowCardinality(int r) {
            return row(r).cardinality();
        }

        @Override
        public IntHashSet neighborsOut(int r) {

            IntHashSet result = new IntHashSet();
            MetalBitSet I = rows[r];
            int max = size();
            for (int j = 0; j < max; ++j) {
                if (I.test(j)) result.add(j);
            }

            return result;
        }

        @Override
        public void neighborsOut(int r, IntProcedure c) {
            MetalBitSet I = rows[r];
            int max = size();
            for (int j = 0; j < max; ++j) {
                if (I.test(j)) c.value(j);
            }
        }
    }

}