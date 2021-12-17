package jcog.tree.rtree;

import jcog.tree.rtree.node.RBranch;
import jcog.tree.rtree.node.RLeaf;
import jcog.tree.rtree.rect.RectDouble;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static jcog.tree.rtree.RCursor.BranchAction.Recurse;

class RCursorTest {

	@Disabled
	@Test
	void visit1() {

		RTree<RectDouble> rTree = RTree2DTest.createRect2DTree(RTree2DTest.DefaultSplits.LINEAR, 3);
		for (RectDouble rect : RTree2DTest.generateRandomRects(16))
			rTree.add(rect);

		rTree.stats().print();

		new RCursor<RectDouble>() {

			@Override
			protected BranchAction branch(RBranch<RectDouble> branch) {
				System.out.println(index + " branch: " + System.identityHashCode(branch));
				return Recurse;
			}

			@Override
			protected boolean leaf(RLeaf<RectDouble> leaf) {
				System.out.println("\tleaf: " + System.identityHashCode(leaf));
				return true;
			}

		}.run(rTree);

	}
}