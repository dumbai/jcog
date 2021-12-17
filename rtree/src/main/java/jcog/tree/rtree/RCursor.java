package jcog.tree.rtree;

import jcog.data.list.Lst;
import jcog.tree.rtree.node.RBranch;
import jcog.tree.rtree.node.RLeaf;
import jcog.tree.rtree.node.RNode;
import org.eclipse.collections.impl.stack.mutable.primitive.IntArrayStack;
import org.jetbrains.annotations.Nullable;

/** TODO not finished */
public abstract class RCursor<X>  {

	public RCursor() {

	}

	public RCursor(RTree<X> tree) {
		run(tree);
	}

	public RCursor(RNode<X> root) {
		run(root);
	}

	public final void run(RTree<X> start) {
		run(start.root());
	}

	public final void run(RNode<X> start) {

		do {

			if (enter(node)) {
				if (node instanceof RBranch) {
					RBranch<X> b = (RBranch<X>) this.node;
					switch(branch(b)) {
						case Stop:
							return;
						case Next:
							break;
						case Recurse:
							int s = this.node.size();
							if (index < s - 1)
								push(b, index+1);
							push(b.items[index], 0);
							//node = b.data[index];
//							for (int i = 0; i < s; i++) {
//								push(b.data[i], 0);
//							}
							break;
					}

				} else {
					RLeaf<X> l = (RLeaf)node;
					if (!leaf(l)) return;
				}
			} else {
				ignore(node);
			}

//			if ((node = nodeStack.poll())==null)
//				break; //done
//			index = indexStack.pop();
		} while (!pop());

	}


	/** may implement functionality for ignored non-matching nodes */
	protected void ignore(RNode<X> node) {

	}

	/** current node */
	@Nullable public final RNode<X> node() {
		return node;
	}

	/** current position within current node */
	public final int index() {
		return index;
	}

	/** nodeStack and indexStack are two columns modified together so they will always be the same size */
	final Lst<RNode<X>> nodeStack = new Lst();
	final IntArrayStack indexStack = new IntArrayStack();

	RNode<X> node;
	int index = 0;

	/** selection region; if null, all are accepted */
	@Nullable private HyperRegion select;
	private RegionMatch mode;

	private enum RegionMatch {
		Equal {
			@Override
			protected boolean match(HyperRegion select, HyperRegion candidate) {
				return select.equals(candidate);
			}
		},
		Intersect {
			@Override
			protected boolean match(HyperRegion select, HyperRegion candidate) {
				return select.intersects(candidate);
			}
		},
		Disjoint {
			@Override
			protected boolean match(HyperRegion select, HyperRegion candidate) {
				return !select.intersects(candidate);
			}
		},
		Contains {
			@Override
			protected boolean match(HyperRegion select, HyperRegion candidate) {
				return select.contains(candidate);
			}
		},
		ContainedBy {
			@Override
			protected boolean match(HyperRegion select, HyperRegion candidate) {
				return candidate.contains(select);
			}
		};

		protected abstract boolean match(HyperRegion select, HyperRegion candidate);
	}

	/** whether the given node matches the current criteria */
	public boolean match(RNode<X> node) {
		if (select == null) return true;
		return mode.match(select, node.bounds());
	}

	protected boolean enter(RNode<X> node) {
		if (this.node!=node) {
			if (!match(node))
				return false;

			this.node = node;
			this.index = 0;
		}
		return true;
	}

//	protected void next() {
//		if (node instanceof RBranch) {
//			if (++index >= node.size())
//			   pop();
//		} else {
//			pop();
//		}
//	}

	private boolean pop() {
		if ((node = nodeStack.poll())!=null) {
			index = indexStack.pop();
			return true;
		}
		return false;
	}

	protected void push(RNode<X> node, int nextIndex) {
		nodeStack.add(node);
		indexStack.push(nextIndex);
	}

	public enum BranchAction {
		Stop,
		Recurse,
		Next
	}


	/** called on entering a branch node */
    protected abstract BranchAction branch(RBranch<X> branch);

	/**
	 * called on entering leaf node
	 * @return false to terminate */
    protected abstract boolean leaf(RLeaf<X> leaf);



	/** update match criteria */
	public RCursor<X> match(HyperRegion r, RegionMatch mode /* TODO generalize */) {
		this.select = r;
		this.mode = mode;
		return this;
	}

//	public void stop() {
//
//	}

}