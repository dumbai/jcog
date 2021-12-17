package jcog.tree.rtree;

import jcog.tree.rtree.node.RBranch;
import jcog.tree.rtree.node.RLeaf;
import jcog.tree.rtree.node.RNode;
import jcog.tree.rtree.split.Split;

import java.util.Arrays;

public abstract class Spatialization<X, R extends HyperRegion> {

	public static final double EPSILON =
		//Math.pow(Float.MIN_NORMAL, +8);
        Float.MIN_NORMAL; //E-38
		//Math.pow(Float.MIN_NORMAL, 1f / 4); //E-10
	    //Math.pow(Float.MIN_NORMAL, 1f/3); //E-15?
	    //Math.pow(Float.MIN_NORMAL, 1/2); //E-19

	public static final float EPSILONf = //(float) EPSILON;
		Float.MIN_NORMAL;

	/**
	 * leaf/branch capacity
	 */
	public final short nodeCapacity;
	private final Split<X> split;

	public Spatialization(Split<X> split, int nodeCapacity) {
		this.nodeCapacity = (short) nodeCapacity;
		this.split = split;
	}

	/** tolerance for equality testing */
	public double epsilon() {
		return EPSILON;
	}

	public abstract R bounds(/*@NotNull*/ X x);

    /*@Deprecated Function<X, HyperRegion> bounds
    public HyperRegion bounds(X x) {
		return bounds.apply(x);
	}*/

	public final RLeaf<X> newLeaf() {
		return newLeaf(nodeCapacity);
	}

	public RLeaf<X> newLeaf(int capacity) {
		return new RLeaf<>(capacity);
	}

	@SafeVarargs
	public final RBranch<X> newBranch(RNode<X>... l) {
		return new RBranch<>(nodeCapacity, l);
	}

	public RNode<X> split(X x, RLeaf<X> leaf) {
		return split.apply(x, leaf, this);
	}

	public RLeaf<X> transfer(X[] src, int from, int to) {
		short cap = this.nodeCapacity;
		X[] tgt = Arrays.copyOf(src, cap);
		int newSize = to - from;
		System.arraycopy(src, from, tgt, 0, newSize);
		if (newSize < cap)
			Arrays.fill(tgt, newSize, cap, null);

		HyperRegion b = mbr(tgt);
		RLeaf x = new RLeaf<>(tgt, b, newSize);
//		commit(x);
		return x;
	}




//	/**
//	 * one-way merge for containment test.
//	 * container and content will not be the same instance.
//	 * default implementation simply tests for equality
//	 */
//    public boolean mergeContain(X container, X content) {
//		return container.equals(content);
//	}

	public HyperRegion mbr(X[] data) {
		//return HyperRegion.mbr(this, data);
		HyperRegion bounds = bounds(data[0]);
		for (int k = 1; k < data.length; k++) {
			X kk = data[k];
			if (kk == null)
				break; //null terminator
			bounds = bounds.mbr(bounds(kk));
		}
		return bounds;
	}

	/**
	 * whether merge is possible
	 */
	public boolean canMerge() {
		return false;
	}

	public boolean canMergeStretch() {
		return false;
	}

	public RInsertion insertion(X t, boolean addOrMerge) {
		return new RInsertion<>(t, addOrMerge, this);
	}

//	/**
//	 * callback when leaf needs updated after insertion or split
//	 */
//	public void commit(RNode<X> l) {
//
//	}


	/** the final value inserted into a leaf */
	public X item(X x) {
		return x;
	}
}