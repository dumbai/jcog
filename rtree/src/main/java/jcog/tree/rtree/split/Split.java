package jcog.tree.rtree.split;

import jcog.Util;
import jcog.tree.rtree.HyperRegion;
import jcog.tree.rtree.Spatialization;
import jcog.tree.rtree.node.RLeaf;
import jcog.tree.rtree.node.RNode;

public abstract class Split<X> {

	public abstract RNode<X> apply(X x, RLeaf<X> leaf, Spatialization<X, ?> model);

	/**
	 * Figures out which newly made leaf node (see split method) to add a data entry to.
	 *
	 * @param a     left node
	 * @param b     right node
	 * @param x     data entry to be added
	 * @param model
	 */
    public static <X> void transfer(RNode<X> a, RNode<X> b, X x, Spatialization <X, ?> model) {

		HyperRegion xReg = model.bounds(x);
		//double tCost = xReg.cost();

		HyperRegion aReg = a.bounds();
		HyperRegion aMbr = aReg != null ? xReg.mbr(aReg) : xReg;
		double axCost = aMbr.cost();
		double aCostDelta = axCost - aReg.cost(); //Math.max(axCost - ((/*aReg!=null ? */aReg.cost() /*: 0*/) + tCost), 0.0);

		HyperRegion bReg = b.bounds();
		HyperRegion bMbr = xReg.mbr(bReg);
		double bxCost = bMbr.cost();
		double bCostDelta = bxCost - bReg.cost(); //Math.max(bxCost - ((/*bReg!=null ? */ bReg.cost()/* : 0*/) + tCost), 0.0);

		RNode target;
		double eps = model.epsilon();
		if (Util.equals(aCostDelta, bCostDelta, eps)) {
			if (Util.equals(axCost, bxCost, eps)) {

				double aMbrMargin = aMbr.perimeter(), bMbrMargin = bMbr.perimeter();

				target = Util.equals(aMbrMargin, bMbrMargin, eps) ?
					((a.size() <= b.size()) ? a : b) :
					((aMbrMargin <= bMbrMargin) ? a : b);
			} else
				target = (axCost <= bxCost) ? a : b;
		} else
			target = (aCostDelta <= bCostDelta) ? a : b;


		//target.add(new RInsertion<>(x, true, model));
		RLeaf<X> T = (RLeaf<X>) target;
//		if (T.contains(x, model.bounds(x), model))
//			return; //duplicate present HACK this should be found earlier TEMPORARY
		T.insert(x, model);
		//assert (added[0]); <-- TODO check this
	}



    /** used by linear and quadratic splits */
    public RNode<X> newBranch(X x, Spatialization <X, ?> model, short size, int r1Max, int r2Max, X[] data) {
        RNode<X> l1Node = model.newLeaf().insert(data[r1Max], model);
		RNode<X> l2Node = model.newLeaf().insert(data[r2Max], model);

		if (size > 2) {
			for (int i = 0; i < size; i++) {
				if ((i != r1Max) && (i != r2Max))
					transfer(l1Node, l2Node, data[i], model);
			}
		}

        transfer(l1Node, l2Node, x, model);

//        model.commit(l1Node);
//        model.commit(l2Node);

        return model.newBranch(l1Node, l2Node);
    }
}