//package jcog.tree.rtree.split;
//
//import jcog.tree.rtree.HyperRegion;
//import jcog.tree.rtree.Spatialization;
//import jcog.tree.rtree.node.RLeaf;
//import jcog.tree.rtree.node.RNode;
//
///**
// * EXPERIMENTAL always chooses split in vector's dimensional order, starting at 0
// * TODO needs testing
// * */
//public class OrdinalSplit extends Split {
//
//	public static final Split<?> the = new OrdinalSplit();
//
//	protected OrdinalSplit() { }
//
//	@Override
//	public RNode<?> apply(Object x, RLeaf leaf, Spatialization model) {
//
//		HyperRegion xx = model.bounds(x);
//
//		Object[] y = leaf.items;
//		int nD = xx.dim();
//		int d = 0;
//		dims: for ( ; d < nD; d++) {
//
//			double xxMin = xx.coord(d, false), xxMax = xx.coord(d, true);
//
//			for (Object Y : y) {
//				HyperRegion yy = model.bounds(Y);
//				//TODO better equality test
//				if (yy.coord(d, false)!=xxMin || yy.coord(d, true)!=xxMax)
//					break dims;
//			}
//
//		}
//
//		return AxialSplit.splitAxis(x, leaf, model,
//			d < nD ? d :
//				0 //split on initial dimension if they are all equal
//		);
//	}
//}
