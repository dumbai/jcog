/**
 * created 23.01.2008
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2008 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http:
 *   EPL: http:
 *   See the LICENSE file in the project's top-level directory for details.
 *   https:
 */

package jcog.data.graph;

import jcog.util.ArrayUtil;

import java.util.Arrays;

/**
 * This class represents a lower (or upper) triangle matrix that stores ints.
 * 
 * @author Thorsten Meinl (Thorsten.Meinl@informatik.uni-erlangen.de)
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
class HalfIntMatrix {
	/** the array holding the complete triangle matrix */
	private final int[] matrix;

	private final int size;
	private final int initialValue;

	/**
	 * Creates a new HalfIntMatrix that is an exact copy of the given template
	 * 
	 * @param template
	 *            a HalfIntMatrix that should be copied
	 */
    HalfIntMatrix(HalfIntMatrix template) {
		this(template, 0);
	}

	/**
	 * Creates a new HalfIntMatrix that is an exact copy of the given template
	 * 
	 * @param template
	 *            a HalfIntMatrix that should be copied
	 * @param reserveNewNodes
	 *            the number of new nodes for which space should be reserved or
	 *            removed
	 */
    private HalfIntMatrix(HalfIntMatrix template, int reserveNewNodes) {
		this.size = (template.size + reserveNewNodes);
		this.initialValue = template.initialValue;
		this.matrix = new int[((size * size + size) / 2)];
		System.arraycopy(template.matrix, 0, matrix, 0,
			(reserveNewNodes >= 0 ? template.matrix : matrix).length);
		for (int i = template.matrix.length; i < matrix.length; i++) {
			matrix[i] = initialValue;
		}
	}

	/**
	 * Creates a new HalfIntMatrix with the given size and initial values
	 * 
	 * @param initialSize
	 *            the size of the matrix in rows (or columns)
	 * @param initialValue
	 *            the initial value of each matrix element
	 */
    HalfIntMatrix(int initialSize, int initialValue) {
		this.size = initialSize;
		this.initialValue = initialValue;
		this.matrix = new int[((size * size + size) / 2)];
		Arrays.fill(matrix, initialValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntMatrix#exchangeRows(int, int)
	 */
	public void exchangeRows(int rowA, int rowB) {
		if (rowA == rowB) {
			return;
		}
		if (rowA > rowB) {
			int t = rowA;
			rowA = rowB;
			rowB = t;
		}
		int i = 0;
		for (; i < rowA; i++) {
			swap(rowA, i, rowB, i);
		}
		for (i++; i < rowB; i++) {
			swap(rowA, i, rowB, i);
		}
		for (i++; i < size; i++) {
			swap(rowA, i, rowB, i);
		}
		swap(rowA, rowA, rowB, rowB);
		swap(rowA, rowB, rowB, rowA);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntMatrix#get(int, int)
	 */
	public int get(int row, int col) {


		return matrix[idx(row, col)];
	}

	private static int idx(int row, int col) {
        return row < col ?
                col * (col + 1) / 2 + row
                :
                row * (row + 1) / 2 + col;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntMatrix#setAt(int, int, int)
	 */
	public void set(int row, int col, int value) {
		assert row >= 0 && col >= 0 && row < size && col < size : "row/col out of bounds: "
				+ row + '/' + col + " size: " + size;
		matrix[idx(row, col)] = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntMatrix#size()
	 */
	public int size() {
		return size;
	}

	private void swap(int r1, int c1, int r2, int c2) {
	    ArrayUtil.swapInt(matrix,  idx(r1, c1), idx(r2, c2));
	}
}