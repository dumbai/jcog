package jcog.tree.rtree;

import jcog.Is;
import jcog.data.list.Lst;
import jcog.sort.FloatRank;
import jcog.sort.RankedN;
import jcog.tree.rtree.node.RBranch;
import jcog.tree.rtree.node.RLeaf;
import jcog.tree.rtree.node.RNode;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * BFS that descends through RTree visiting nodes and leaves in an order determined
 * by a score function that ranks the next nodes to either provide via an Iterator<X>-like interface
 * or to expand the ranked buffer to find more results.
 * <p>
 * TODO generalize for non-rtree uses, subclass that specifically for RTree and implement an abstract growth method for its lazy Node<> iteration
 * <p>
 * beamSearch(problemSet, ruleSet, memorySize)
 * openMemory = new memory of size memorySize
 * nodeList = problemSet.listOfNodes
 * node = root or initial search node
 * Add node to openMemory;
 * while (node is not a goal node)
 * Delete node from openMemory;
 * Expand node and obtain its children, evaluate those children;
 * If a child node is pruned according to a rule in ruleSet, delete it;
 * Place remaining, non-pruned children into openMemory;
 * If memory is full and has no room for new nodes, remove the worst
 * node, determined by ruleSet, in openMemory;
 * node = the least costly node in openMemory;
 */
@Is("Beam_search")
@Deprecated
public abstract class HyperIterator<X> implements Iterable<X>, FloatRank {

	/**
	 * at each level, the plan is slowly popped from the end growing to the beginning (sorted in reverse)
	 */
	public final RankedN<RNode<X>> nodes;
	public final RankedN<X> values;

	/** double buffer for nodes pending evaluation so that the nodes buffer is clear for reuse */
	private final Lst<RNode<X>> nodeTmp;

	public HyperIterator(int nodeBeamWidth, X[] valueBuffer) {
		this.nodes = new RankedN<>(new RNode[nodeBeamWidth], this);
		this.values = new RankedN<>(valueBuffer, this);
		this.nodeTmp = new Lst<>(0);
	}

	@Override
	public final float rank(Object o, float min) {
		return o instanceof RNode ? rankNode((RNode<X>) o, min) : rankItem((X) o, min);
	}

	protected abstract float rankItem(X o, float min);

	protected abstract float rankNode(RNode<X> o, float min);

	public final void add(RTree<X> tree) {
		add(tree.root());
	}

	public void bfs() {

		Consumer<RNode<X>> n2Add = nodeTmp::addFast;
		Consumer<RNode<X>> collect = this::add;

		RankedN<RNode<X>> n = this.nodes;
		Lst<RNode<X>> n2 = this.nodeTmp;
		do {
			n2.ensureCapacity(n.size());
			n.forEach(n2Add);
			n.clearFast();

			n2.forEach(collect);
			n2.clearFast();
		} while (!n.isEmpty());

//		nodes.clear();
//		beam.clear();
	}

	public final void add(RNode<X> l) {
		if (!accept(l))
			return;

		if (l instanceof RBranch<X> b) {
            RNode<X>[] bb = b.items;
			int s = b.size;

			if (s == 1 && bb[0] instanceof RLeaf) {
				l = bb[0]; //tail call optimization
			} else {
				RankedN<RNode<X>> n = nodes;
				int i;
				for (i = 0; i < s-1; i++)
					n.add(bb[i]);

				RNode<X> y = bb[i];
				if (!(y instanceof RLeaf)) {
					n.add(y);
					return;
				}

				l = y; //tail call optimization
			}
		}

		leaf((RLeaf<X>) l);
	}

	public boolean accept(RNode<X> l) {
		return true;
	}

	private void leaf(RLeaf<X> l) {
		X[] items = l.items;
		int s = l.size;
		RankedN<X> v = this.values;
		for (int i = 0; i < s; i++)
			v.add(items[i]);
	}

	@Override
	public Iterator<X> iterator() {
		return values.iterator();
	}

//    public void rank(Predicate whle, Random random) {
//        int leaves = nodes.size();
//        if (leaves == 0)
//            return;
//        if (leaves == 1)
//            leaf((RLeaf<X>) nodes.first(), whle, random);
//        else {
//            bfsRoundRobin(whle, random);
//            //sample(whle, random);
//        }
//    }

//    private void sample(Predicate whle, Random random) {
//        Object[] leafs = nodes.items;
//        float[] value = nodes.value;
//        value = Arrays.copyOf(value, nodes.size()); //HACK
//        MutableRoulette.runN(value, random, (i)->i/2 /* HACK */, x->{
//            RLeaf leaf = (RLeaf) leafs[x];
//            short s = leaf.size;
//            return whle.test(leaf.data[s > 1 ? random.nextInt(s) : 0]); //TODO sub-roulette based on supplied entry ranker
//        });
//    }

//    private void bfsRoundRobin(Predicate whle, Random random) {
//        int leaves = nodes.size(); //assert(leaves > 0);
//        int[] remain = new int[leaves];
//        Object[] pp = nodes.items;
//        int n =  0;
//        for (int i = 0; i < leaves; i++)
//            n += (remain[i] = ((RLeaf) pp[i]).size);
//        int c = 0;
//        int k = 0;
//        int o = random.nextInt(n * leaves); //shuffles the inner-leaf visiting order
//        //TODO shuffle inner visit order differently for each leaf for even more fairness
//        do {
//            int pk = remain[k];
//            if (pk > 0) {
//                RLeaf<X> lk = (RLeaf<X>) pp[k];
//                if (!whle.test( lk.data[ (--remain[k] + o ) % lk.size ] ))
//                    break;
//            }
//            if (++k == leaves) { k = 0; o++; }
//        } while (++c < n);
//    }

//    /** TODO sample leaf by value */
//    private static <X> void leaf(RLeaf<X> rl, Predicate whle, Random random) {
//        short ls = rl.size;
//        X[] rld = rl.data;
//        if (ls <= 1) {
//            whle.test(rld[0]);
//        } else {
//            short[] order = new short[ls];
//            for (short i = 0; i < ls; i++)
//                order[i] = i;
//            ArrayUtil.shuffle(order, random);
//            for (short i = 0; i < ls; i++) {
//                if (!whle.test(rld[order[i]]))
//                    break;
//            }
//        }
//    }


	//    /**
//     * surveys the contents of the node, producing a new 'stack frame' for navigation
//     */
//    private void expand(Node<X> at) {
//        at.forEachLocal(this::push);
//    }


//	public final boolean hasNext() {
//
//		Object z;
//		while ((z = nodes.pop()) != null) {
//			if (!(z instanceof RNode))
//				break;
//
//			((AbstractRNode) z).drainLayer(nodes);
//		}
//
//		return (this.next = (X) z) != null;
//	}
//
//	public final X next() {
//		//        if (n == null)
////            throw new NoSuchElementException();
//		return this.next;
//	}

//	public void intersecting(RTree<X> t, HyperRegion r) {
//		t.root().intersectingNodes(r, (x) -> {
//			if (x instanceof RLeaf)
//				nodes.add(x);
//			return true;
//		}, t.model);
//	}

//	@Deprecated public static final class HyperRegionRanker<X, R extends HyperRegion> implements FloatRank {
//		private final Function<X, R> bounds;
//		private final FloatRank<R> rank;
//
//		public HyperRegionRanker(Function<X, R> bounds, FloatRank<R> rank) {
//			this.bounds = bounds;
//			this.rank = rank;
//		}
//
//		@Override
//		public float rank(Object r, float min) {
//			HyperRegion y =
//				r instanceof HyperRegion ?
//					((HyperRegion) r)
//					:
//					(r instanceof RNode ?
//						((RNode) r).bounds()
//						:
//						bounds.apply((X) r)
//					);
//
//			return y == null ? Float.NaN : rank.rank((R) y, min);
//		}
//	}


//    public static final HyperIterator2 Empty = new HyperIterator2() {
//
//        @Override
//        public boolean hasNext() {
//            return false;
//        }
//
//        @Override
//        public Object next() {
//            throw new NoSuchElementException();
//        }
//    };


}