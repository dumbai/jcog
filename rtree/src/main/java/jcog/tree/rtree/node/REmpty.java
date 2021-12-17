package jcog.tree.rtree.node;

import jcog.tree.rtree.RInsertion;
import jcog.util.ArrayUtil;

public class REmpty extends RLeaf {

	public static final REmpty the = new REmpty();

	private REmpty() {
		super(ArrayUtil.EMPTY_OBJECT_ARRAY);
	}

	@Override
	public final RNode add(RInsertion x) {
		x.write();
		return x.model.newLeaf().add(x);
	}

}
