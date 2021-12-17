package jcog.tree.radix;

import jcog.data.byt.ArrayBytes;
import jcog.data.byt.ByteSequence;
import jcog.data.byt.ProxyBytes;
import jcog.data.list.Lst;
import org.eclipse.collections.api.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.System.arraycopy;
import static org.eclipse.collections.impl.tuple.Tuples.pair;

//import jcog.sort.SortedArray;

/**
 * seh's modifications to radix tree
 * <p>
 * An implementation of {@link RadixTree} which supports lock-free concurrent reads, and allows items to be added to and
 * to be removed from the tree <i>atomically</i> by background thread(s), without blocking reads.
 * <p/>
 * Unlike reads, writes require locking of the tree (locking out other writing threads only; reading threads are never
 * blocked). Currently write locks are coarse-grained; in fact they are tree-level. In future branch-level write locks
 * might be added, but the current implementation is targeted at high concurrency read-mostly use cases.
 *
 * @author Niall Gallagher
 * @modified by seth
 */
public class MyRadixTree<X> /* TODO extends ReentrantReadWriteLock */ implements /*RadixTree<X>,*/Serializable, Iterable<X> {


	private static final Comparator<? super BytePrefixed> NODE_COMPARATOR = Comparator.comparingInt(BytePrefixed::getIncomingEdgeFirstCharacter);

	@Deprecated private static final List<ByteNode> emptyList = Collections.EMPTY_LIST;

	/** TODO TUNE */
	private static final int BINARY_SEARCH_THRESHOLD = 4;

	private final AtomicInteger estSize = new AtomicInteger(0);
	public ByteNode<X> root;

	/**
	 * Creates a new {@link MyRadixTree} which will use the given {@link NodeFactory} to create nodes.
	 *
	 * @param nodeFactory An object which creates {@link ByteNode} objects on-demand, and which might return node
	 *                    implementations optimized for storing the values supplied to it for the creation of each node
	 */
	public MyRadixTree() {
		_clear();
	}

	/**
	 * default factory
	 */
	private static ByteNode createNode(ByteSequence edgeCharacters, Object value, List<ByteNode> childNodes, boolean isRoot) {
		if (edgeCharacters == null) throw new IllegalStateException("The edgeCharacters argument was null");
        else if (!isRoot && edgeCharacters.length() == 0)
            throw new IllegalStateException("Invalid edge characters for non-root node: " + edgeCharacters);
        else if (childNodes == null) throw new IllegalStateException("The childNodes argument was null");
        else return childNodes.isEmpty() ?
                ((value instanceof VoidValue) ?
                    new ByteArrayNodeLeafVoidValue(edgeCharacters) :
                    ((value != null) ?
                        new ByteArrayNodeLeafWithValue(edgeCharacters, value) :
                        new ByteArrayNodeLeafNullValue(edgeCharacters))) :
                ((value instanceof VoidValue) ?
                    innerVoid(edgeCharacters, childNodes) :
                    ((value == null) ?
                        innerNull(edgeCharacters, childNodes) :
                        inner(edgeCharacters, value, childNodes)));
	}

	private static Lst<ByteNode> leafList(List<ByteNode> outs) {
		if (outs.size() > 1)
			outs.sort(NODE_COMPARATOR);
		return (Lst<ByteNode>) outs;
	}

	private static ByteArrayNodeDefault inner(ByteSequence in, Object value, List<ByteNode> outs) {
		return new ByteArrayNodeDefault(in, value, leafList(outs));
	}

	private static ByteArrayNodeNonLeafVoidValue innerVoid(ByteSequence in, List<ByteNode> outs) {
		return new ByteArrayNodeNonLeafVoidValue(in, leafList(outs));
	}

	private static ByteArrayNodeNonLeafNullValue innerNull(ByteSequence in, List<ByteNode> outs) {
		return new ByteArrayNodeNonLeafNullValue(in, leafList(outs));
	}

	private static ByteSequence getCommonPrefix(ByteSequence first, ByteSequence second) {
		if (first == second) return first;

		int minLength = Math.min(first.length(), second.length());

		for (int i = 0; i < minLength; ++i) if (first.at(i) != second.at(i)) return first.subSequence(0, i);

		return first.subSequence(0, minLength);
	}

	private static ByteSequence subtractPrefix(ByteSequence main, ByteSequence prefix) {
		int startIndex = prefix.length();
		int mainLength = main.length();
		return (startIndex > mainLength ? ByteSequence.EMPTY : main.subSequence(startIndex, mainLength));
	}

	private static int search(ByteNode[] a, int size, byte key) {
		if (size == 0) return -1;
        else if (size >= BINARY_SEARCH_THRESHOLD) return binarySearch(key, size, a);
        else return linearSearch(key, a);
	}

	private static int linearSearch(byte key, ByteNode[] a) {
		for (int i = 0, aLength = a.length; i < aLength; i++) {
			ByteNode x = a[i];
			if (x == null)
				break;
			if (x.getIncomingEdgeFirstCharacter() == key)
				return i;
		}
		return -1;
	}

	private static int binarySearch(byte key, int size, ByteNode[] a) {
		int high = size - 1;
		int low = 0;
		while (low <= high) {
			int midIndex = (low + high) >>> 1;
			int cmp = a[midIndex].getIncomingEdgeFirstCharacter() - key;

			if (cmp < 0) low = midIndex + 1;
			else if (cmp > 0) high = midIndex - 1;
			else return midIndex;
		}
		return -(low + 1);
	}

	private static ByteSequence getPrefix(ByteSequence input, int endIndex) {
		return endIndex > input.length() ? input : input.subSequence(0, endIndex);
	}

	private static ByteSequence getSuffix(ByteSequence input, int startIndex) {
		return (startIndex >= input.length() ? ByteSequence.EMPTY : input.subSequence(startIndex, input.length()));
	}

	private static ByteSequence concatenate(ByteSequence a, ByteSequence b) {
		int aLen = a.length(), bLen = b.length();
		byte[] c = new byte[aLen + bLen];
		a.toArray(c, 0);
		b.toArray(c, aLen);
		return new ArrayBytes(c);
	}

	private static int _size(ByteNode n) {
		int sum = 0;
		Object v = n.get();
		if (aValue(v))
			sum++;

		List<ByteNode> l = n.out();
		for (int i = 0, lSize = l.size(); i < lSize; i++) sum += _size(l.get(i));

		return sum;
	}

	/**
	 * as soon as the limit is exceeded, it returns -1 to cancel the recursion iteration
	 */
	private static int _sizeIfLessThan(ByteNode n, int limit) {
		int sum = 0;
		Object v = n.get();
		if (aValue(v))
			sum++;

		List<ByteNode> l = n.out();
		for (int i = 0, lSize = l.size(); i < lSize; i++) {
			int s = _size(l.get(i));
			if (s < 0)
				return -1;
			sum += s;
			if (sum > limit)
				return -1;
		}

		return sum;
	}

	private static boolean aValue(Object v) {
		return (v != null) && v != VoidValue.the;
	}

	private static void prettyPrint(ByteNode<?> node, Appendable sb, String prefix, boolean isTail, boolean isRoot) {
		try {
			StringBuilder ioException = new StringBuilder();
			if (isRoot) {
				ioException.append('○');
				if (node.in().length() > 0) ioException.append(' ');
			}

			ioException.append(node.in());
//            if (node.getValue() != null) {
//                ioException.append(" (").append(node.getValue()).append(")");
//            }

			sb.append(prefix).append(isTail ? (isRoot ? "" : "└── ○ ") : "├── ○ ").append(ioException).append("\n");
			List children = node.out();

			for (int i = 0; i < children.size() - 1; ++i)
                prettyPrint((ByteNode) children.get(i), sb, tailPrint(prefix, isTail, isRoot), false, false);

			if (!children.isEmpty())
                prettyPrint((ByteNode) children.get(children.size() - 1), sb, tailPrint(prefix, isTail, isRoot), true, false);

		} catch (IOException var8) {
			throw new IllegalStateException(var8);
		}
	}

	private static String tailPrint(String prefix, boolean isTail, boolean isRoot) {
		return prefix + (isTail ? (isRoot ? "" : "    ") : "│   ");
	}

	static Object putInternal(CharSequence key, Object value, boolean overwrite) {
		throw new UnsupportedOperationException();
	}

	public static SearchResult random(SearchResult at, float descendProb, Random rng) {

		ByteNode current = at.found;
		ByteNode parent = at.parentNode;
		ByteNode parentParent = at.parentNodesParent;
		return random(current, parent, parentParent, descendProb, rng);
	}

	public static SearchResult random(ByteNode current, ByteNode parent, ByteNode parentParent, float descendProb, Random rng) {


		while (true) {
			List<ByteNode> c = current.out();
			int s = c.size();
			if (s == 0) break;
            else if (rng.nextFloat() < descendProb) {
                int which = rng.nextInt(s);
                ByteNode next = c.get(which);

                parentParent = parent;
                parent = current;
                current = next;
            } else break;
		}

		return new SearchResult(current, parent, parentParent);
	}

	/**
	 * Returns a lazy iterable which will return {@link CharSequence} keys for which the given key is a prefix.
	 * The results inherently will not contain duplicates (duplicate keys cannot exist in the tree).
	 * <p/>
	 * Note that this method internally converts {@link CharSequence}s to {@link String}s, to avoid set equality issues,
	 * because equals() and hashCode() are not specified by the CharSequence API contract.
	 */
	@SuppressWarnings("JavaDoc")
	private static Iterable<ByteSequence> getDescendantKeys(ByteSequence startKey, ByteNode startNode) {
		return new DescendantKeys(startKey, startNode);
	}

	/**
	 * Returns a lazy iterable which will return values which are associated with keys in the tree for which
	 * the given key is a prefix.
	 */
	@SuppressWarnings("JavaDoc")
	private static <O> Iterable<O> descendantValues(ByteSequence startKey, ByteNode startNode) {
		return () -> new LazyIterator<>() {

			Iterator<NodeKeyPair> descendantNodes;

			@Override
			protected O computeNext() {
				if (descendantNodes == null) descendantNodes = lazyTraverseDescendants(startKey, startNode).iterator();

				while (descendantNodes.hasNext()) {
					NodeKeyPair nodeKeyPair = descendantNodes.next();
					Object value = nodeKeyPair.node.get();
					if (value != null) return (O) value;
				}

				return endOfData();
			}
		};
	}

	/**
	 * Returns a lazy iterable which will return {@link KeyValuePair} objects each containing a key and a value,
	 * for which the given key is a prefix of the key in the {@link KeyValuePair}. These results inherently will not
	 * contain duplicates (duplicate keys cannot exist in the tree).
	 * <p/>
	 * Note that this method internally converts {@link CharSequence}s to {@link String}s, to avoid set equality issues,
	 * because equals() and hashCode() are not specified by the CharSequence API contract.
	 */
	@SuppressWarnings("JavaDoc")
	private static <O> Iterable<Pair<ByteSequence, O>> getDescendantKeyValuePairs(ByteSequence startKey, ByteNode startNode) {
		return () -> new LazyIterator<>() {
			Iterator<NodeKeyPair> descendantNodes;

			@Override
			protected Pair<ByteSequence, O> computeNext() {

				if (descendantNodes == null)
					descendantNodes = lazyTraverseDescendants(startKey, startNode).iterator();

				while (descendantNodes.hasNext()) {
					NodeKeyPair nodeKeyPair = descendantNodes.next();
					Object value = nodeKeyPair.node.get();
					if (value != null) return pair(transformKeyForResult(nodeKeyPair.key), (O) value);
				}

				return endOfData();
			}
		};
	}

	/**
	 * Traverses the tree using depth-first, preordered traversal, starting at the given node, using lazy evaluation
	 * such that the next node is only determined when next() is called on the iterator returned.
	 * The traversal algorithm uses iteration instead of recursion to allow deep trees to be traversed without
	 * requiring large JVM stack sizes.
	 * <p/>
	 * Each node that is encountered is returned from the iterator along with a key associated with that node,
	 * in a NodeKeyPair object. The key will be prefixed by the given start key, and will be generated by appending
	 * to the start key the edges traversed along the path to that node from the start node.
	 *
	 * @param startKey  The key which matches the given start node
	 * @param startNode The start node
	 * @return An iterator which when iterated traverses the tree using depth-first, preordered traversal,
	 * starting at the given start node
	 */
	private static Iterable<NodeKeyPair> lazyTraverseDescendants(ByteSequence startKey, ByteNode startNode) {
		Deque<NodeKeyPair> stack = new ArrayDeque();
		stack.push(new NodeKeyPair(startNode, startKey));
		return () -> new LazyIterator<>() {

			@Override
			protected NodeKeyPair computeNext() {

				if (stack.isEmpty()) return endOfData();
				NodeKeyPair current = stack.pop();
				List<ByteNode> childNodes = current.node.out();


				for (int i = childNodes.size() - 1; i >= 0; i--) {
					ByteNode child = childNodes.get(i);
					stack.push(new NodeKeyPair(child,
						concatenate(current.key, child.in())
					));
				}
				return current;
			}
		};
	}

	/**
	 * A hook method which may be overridden by subclasses, to transform a key just before it is returned to
	 * the application, for example by the {@link #getKeysStartingWith(CharSequence)} or the
	 * {@link #getKeyValuePairsForKeysStartingWith(CharSequence)} methods.
	 * <p/>
	 * This hook is expected to be used by  {@link com.googlecode.concurrenttrees.radixreversed.ReversedRadixTree}
	 * implementations, where keys are stored in the tree in reverse order but results should be returned in normal
	 * order.
	 * <p/>
	 * <b>This default implementation simply returns the given key unmodified.</b>
	 *
	 * @param rawKey The raw key as stored in the tree
	 * @return A transformed version of the key
	 */
	private static ByteSequence transformKeyForResult(ByteSequence rawKey) {
		return rawKey;
	}

	private void _clear() {
		this.root = createNode(ByteSequence.EMPTY, null, emptyList, true);
	}

	public final X put(Pair<ByteSequence, X> value) {
		return put(value.getOne(), value.getTwo());
	}

	public X put(X value) {
		throw new UnsupportedOperationException("subclasses can implement this by creating their own key and calling put(k,v)");
	}

	/**
	 * {@inheritDoc}
	 */
	public final X put(ByteSequence key, X value) {


		return compute(key, value, (k, r, existing, v) -> v);
	}

	/**
	 * {@inheritDoc}
	 */
	public final X putIfAbsent(ByteSequence key, X newValue) {
		return compute(key, newValue, (k, r, existing, v) ->
			existing != null ? existing : v
		);
	}

	public final X putIfAbsent(byte[] key, Supplier<X> newValue) {
		return putIfAbsent(new ArrayBytes(key), newValue);
	}

	public final X putIfAbsent(ByteSequence key, Supplier<X> newValue) {
		return compute(key, newValue, (k, r, existing, v) ->
			existing != null ? existing : v.get()
		);
	}

	/**
	 * {@inheritDoc}
	 */
	public X value(ByteSequence key) {
		acquireReadLockIfNecessary();
		try {
			SearchResult searchResult = searchTree(key);
			if (searchResult.classification == SearchResult.Classification.EXACT_MATCH)
				return (X) searchResult.found.get();

			return null;
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	protected void acquireReadLockIfNecessary() {

	}

	protected void releaseReadLockIfNecessary() {

	}

	public void releaseWriteLock() {

	}

	public int acquireWriteLock() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterable<ByteSequence> getKeysStartingWith(ByteSequence prefix) {
		acquireReadLockIfNecessary();
		try {
			SearchResult searchResult = searchTree(prefix);
			ByteNode nodeFound = searchResult.found;
			switch (searchResult.classification) {
				case EXACT_MATCH:
					return getDescendantKeys(prefix, nodeFound);
				case KEY_ENDS_MID_EDGE:


					ByteSequence edgeSuffix = getSuffix(nodeFound.in(), searchResult.charsMatchedInNodeFound);
					prefix = concatenate(prefix, edgeSuffix);
					return getDescendantKeys(prefix, nodeFound);
				default:

					return Collections.EMPTY_SET;
			}
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterable<X> valuesStartingWith(ByteSequence prefix) {
		acquireReadLockIfNecessary();
		try {
			SearchResult searchResult = searchTree(prefix);
			ByteNode Found = searchResult.found;
			return switch (searchResult.classification) {
				case EXACT_MATCH -> descendantValues(prefix, Found);
				case KEY_ENDS_MID_EDGE -> descendantValues(
						concatenate(
								prefix,
								getSuffix(
										Found.in(),
										searchResult.charsMatchedInNodeFound)),
						Found);
				default -> Collections.EMPTY_SET;
			};
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterable<Pair<ByteSequence, X>> getKeyValuePairsForKeysStartingWith(ByteSequence prefix) {
		acquireReadLockIfNecessary();
		try {
			SearchResult searchResult = searchTree(prefix);
			SearchResult.Classification classification = searchResult.classification;
			ByteNode f = searchResult.found;
			switch (classification) {
				case EXACT_MATCH:
					return getDescendantKeyValuePairs(prefix, f);
				case KEY_ENDS_MID_EDGE:


					ByteSequence edgeSuffix = getSuffix(f.in(), searchResult.charsMatchedInNodeFound);
					prefix = concatenate(prefix, edgeSuffix);
					return getDescendantKeyValuePairs(prefix, f);
				default:

					return Collections.EMPTY_SET;
			}
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean remove(ByteSequence key) {
		acquireWriteLock();
		try {
			SearchResult searchResult = searchTree(key);
			return removeWithWriteLock(searchResult, false);
		} finally {
			releaseWriteLock();
		}
	}

	public boolean remove(SearchResult searchResult, boolean recurse) {
		acquireWriteLock();
		try {
			return removeWithWriteLock(searchResult, recurse);
		} finally {
			releaseWriteLock();
		}
	}

	/**
	 * allows subclasses to override this to handle removal events. return true if removal is accepted, false to reject the removal and reinsert
	 */
	protected boolean onRemove(X removed) {

		return true;
	}

	public boolean removeWithWriteLock(SearchResult searchResult, boolean recurse) {
		SearchResult.Classification classification = searchResult.classification;
		switch (classification) {
			case EXACT_MATCH:
				ByteNode found = searchResult.found;
				ByteNode parent = searchResult.parentNode;

				Object v = found.get();
				if (!recurse && ((v == null) || (v == VoidValue.the))) return false;

				/** TODO make this a field and thread-safe. doesnt matter which threaad ends up re-adding */
				List<X> reinsertions = new Lst<>(0);

				if (v != null && v != VoidValue.the) {
					X xv = (X) v;
					boolean removed = tryRemove(xv);
					if (!recurse) {
						if (!removed)
							return false;
					} else if (!removed) reinsertions.add(xv);
				}


				List<ByteNode> childEdges = found.out();
				int numChildren = childEdges.size();
				if (numChildren > 0) if (!recurse) if (numChildren > 1) parent.out(
                    createNode(found.in(), null, found.out(), false)
                );
                else /*if (numChildren == 1)*/ {

                    ByteNode child = childEdges.get(0);

                    parent.out(
                        createNode(
                            concatenate(found.in(), child.in()),
                            child.get(), child.out(), false)
                    );
                }
                else {
                    forEach(found, (k, f) -> {
                        if (!tryRemove(f))
                            reinsertions.add(f);
                    });
                    numChildren = 0;
                }

				if (numChildren == 0) {

					if (reinsertions.size() == 1) return false;


					List<ByteNode> currentEdgesFromParent = parent.out();


					int cen = currentEdgesFromParent.size();

					List<ByteNode> newEdgesOfParent = new Lst<>(0, new ByteNode[cen]);
					boolean differs = false;
					for (int i = 0; i < cen; i++) {
						ByteNode node = currentEdgesFromParent.get(i);
						if (node != found) newEdgesOfParent.add(node);
                        else differs = true;
					}
					if (!differs)
						newEdgesOfParent = currentEdgesFromParent;


					boolean parentIsRoot = (parent == root);
					ByteNode newParent;
					if (newEdgesOfParent.size() == 1 && parent.get() == null && !parentIsRoot) {

						ByteNode parentsRemainingChild = newEdgesOfParent.get(0);

						ByteSequence concatenatedEdges = concatenate(parent.in(), parentsRemainingChild.in());
						newParent = createNode(concatenatedEdges, parentsRemainingChild.get(), parentsRemainingChild.out(),
							false /*parentIsRoot*/);
					} else newParent = createNode(parent.in(), parent.get(), newEdgesOfParent, parentIsRoot);

					if (parentIsRoot) this.root = newParent;
                    else searchResult.parentNodesParent.out(newParent);
				}


				for (X reinsertion : reinsertions) put(reinsertion);

				return true;
			default:
				return false;
		}
	}

	private boolean tryRemove(X v) {
		estSize.decrementAndGet();
		return onRemove(v);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterable<ByteSequence> getClosestKeys(ByteSequence candidate) {
		acquireReadLockIfNecessary();
		try {
			SearchResult searchResult = searchTree(candidate);
			SearchResult.Classification classification = searchResult.classification;
			switch (classification) {
				case EXACT_MATCH:
					return getDescendantKeys(candidate, searchResult.found);
				case KEY_ENDS_MID_EDGE:


					ByteSequence edgeSuffix = getSuffix(searchResult.found.in(), searchResult.charsMatchedInNodeFound);
					candidate = concatenate(candidate, edgeSuffix);
					return getDescendantKeys(candidate, searchResult.found);
				case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {


					ByteSequence keyOfParentNode = getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
					ByteSequence keyOfNodeFound = concatenate(keyOfParentNode, searchResult.found.in());
					return getDescendantKeys(keyOfNodeFound, searchResult.found);
				}
				case INCOMPLETE_MATCH_TO_END_OF_EDGE:
					if (searchResult.charsMatched == 0) break;


					ByteSequence keyOfNodeFound = getPrefix(candidate, searchResult.charsMatched);
					return getDescendantKeys(keyOfNodeFound, searchResult.found);
			}
			return Collections.EMPTY_SET;
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterable<X> getValuesForClosestKeys(ByteSequence candidate) {
		acquireReadLockIfNecessary();
		try {
			SearchResult searchResult = searchTree(candidate);
			SearchResult.Classification classification = searchResult.classification;
			switch (classification) {
				case EXACT_MATCH:
					return descendantValues(candidate, searchResult.found);
				case KEY_ENDS_MID_EDGE:


					ByteSequence edgeSuffix = getSuffix(searchResult.found.in(), searchResult.charsMatchedInNodeFound);
					candidate = concatenate(candidate, edgeSuffix);
					return descendantValues(candidate, searchResult.found);
				case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {


					ByteSequence keyOfParentNode = getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
					ByteSequence keyOfNodeFound = concatenate(keyOfParentNode, searchResult.found.in());
					return descendantValues(keyOfNodeFound, searchResult.found);
				}
				case INCOMPLETE_MATCH_TO_END_OF_EDGE:
					if (searchResult.charsMatched == 0) break;

                    return descendantValues(getPrefix(candidate, searchResult.charsMatched), searchResult.found);
			}
			return Collections.EMPTY_SET;
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterable<Pair<ByteSequence, Object>> getKeyValuePairsForClosestKeys(ByteSequence candidate) {
		acquireReadLockIfNecessary();
		try {
			SearchResult searchResult = searchTree(candidate);

            switch (searchResult.classification) {
				case EXACT_MATCH:
					return getDescendantKeyValuePairs(candidate, searchResult.found);
				case KEY_ENDS_MID_EDGE:
				    ByteSequence edgeSuffix = getSuffix(searchResult.found.in(), searchResult.charsMatchedInNodeFound);
					candidate = concatenate(candidate, edgeSuffix);
					return getDescendantKeyValuePairs(candidate, searchResult.found);
				case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {


					ByteSequence keyOfParentNode = getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
					ByteSequence keyOfNodeFound = concatenate(keyOfParentNode, searchResult.found.in());
					return getDescendantKeyValuePairs(keyOfNodeFound, searchResult.found);
				}
				case INCOMPLETE_MATCH_TO_END_OF_EDGE:
					if (searchResult.charsMatched == 0) break;


					ByteSequence keyOfNodeFound = getPrefix(candidate, searchResult.charsMatched);
					return getDescendantKeyValuePairs(keyOfNodeFound, searchResult.found);
			}
			return Collections.EMPTY_SET;
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return size(this.root);
	}

	public int size(ByteNode n) {
		acquireReadLockIfNecessary();
		try {
			return _size(n);
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	public int sizeIfLessThan(ByteNode n, int limit) {
		acquireReadLockIfNecessary();
		try {
			return _sizeIfLessThan(n, limit);
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	/**
	 * estimated size
	 */
	public int sizeEst() {
		return estSize.getOpaque();
	}

	@Override
	public void forEach(Consumer<? super X> action) {
		forEach(this.root, action);
	}

	public final void forEach(ByteNode<X> start, Consumer<? super X> action) {
		Object v = start.get();
		if (aValue(v))
			action.accept((X) v);

        for (ByteNode<X> child : start.out())
			forEach(child, action);
	}

	public final void forEach(ByteNode<X> start, BiConsumer<ByteSequence, ? super X> action) {
		Object v = start.get();
		if (aValue(v))
			action.accept(start.in(), (X) v);

		List<ByteNode<X>> l = start.out();
		for (int i = 0, lSize = l.size(); i < lSize; i++)
		    forEach(l.get(i), action);
	}

	public SearchResult random(float descendProb, Random rng) {
		return random(root, null, null, descendProb, rng);
	}

	public SearchResult random(ByteNode subRoot, float descendProb, Random rng) {
		return random(subRoot, root, null, descendProb, rng);
	}

	/**
	 * Atomically adds the given value to the tree, creating a node for the value as necessary. If the value is already
	 * stored for the same key, either overwrites the existing value, or simply returns the existing value, depending
	 * on the given value of the <code>overwrite</code> flag.
	 *
	 * @param key       The key against which the value should be stored
	 * @param newValue  The value to store against the key
	 * @param overwrite If true, should replace any existing value, if false should not replace any existing value
	 * @return The existing value for this key, if there was one, otherwise null
	 */
	private <V> X compute(ByteSequence key, V value, QuadFunction<ByteSequence, SearchResult, X, V, X> computeFunc) {


		acquireReadLockIfNecessary();
		try {


			SearchResult result = searchTree(key);
			ByteNode found = result.found;
			int matched = result.charsMatched;
			Object foundValue = found != null ? found.get() : null;
			X foundX = ((matched == key.length()) && (foundValue != VoidValue.the)) ? ((X) foundValue) : null;

			X newValue = computeFunc.apply(key, result, foundX, value);

			if (newValue != foundX) {

				int version = beforeWrite();
				int version2 = acquireWriteLock();
				try {

//                    if (version + 1 != version2) {
//
//                        result = searchTree(key);
//                        found = result.found;
//                        matched = result.charsMatched;
//                        foundValue = found != null ? found.getValue() : null;
//                        foundX = ((matched == key.length()) && (foundValue != VoidValue.the)) ? ((X) foundValue) : null;
//                        if (foundX == newValue)
//                            return newValue;
//                    }

					SearchResult.Classification classification = result.classification;

					if (foundX == null)
						estSize.incrementAndGet();

					List<ByteNode> oedges = found.out();
					switch (classification) {
						case EXACT_MATCH:
							if (newValue != foundValue) cloneAndReattach(result, found, foundValue, oedges);
							break;
						case KEY_ENDS_MID_EDGE: {


							ByteSequence keyCharsFromStartOfNodeFound = key.subSequence(matched - result.charsMatchedInNodeFound, key.length());
							ByteSequence commonPrefix = getCommonPrefix(keyCharsFromStartOfNodeFound, found.in());
							ByteSequence suffixFromExistingEdge = subtractPrefix(found.in(), commonPrefix);


							ByteNode newChild = createNode(suffixFromExistingEdge, foundValue, oedges, false);

							ByteNode newParent = createNode(commonPrefix, newValue, new Lst(new ByteNode[]{newChild}), false);


							result.parentNode.out(newParent);

							break;
						}
						case INCOMPLETE_MATCH_TO_END_OF_EDGE:


							ByteSequence keySuffix = key.subSequence(matched, key.length());

							ByteNode newChild = createNode(keySuffix, newValue, emptyList, false);


							int numEdges = oedges.size();
							ByteNode[] edgesArray;
							if (numEdges > 0) {
								edgesArray = new ByteNode[numEdges + 1];
								arraycopy(((Lst) oedges).array(), 0, edgesArray, 0, numEdges);
								edgesArray[numEdges] = newChild;
							} else edgesArray = new ByteNode[]{newChild};

							cloneAndReattach(result, found, foundValue, new Lst(edgesArray.length, edgesArray));

							break;

						case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE:


							ByteSequence suffixFromKey = key.subSequence(matched, key.length());


							ByteNode n1 = createNode(suffixFromKey, newValue, emptyList, false);

							ByteSequence keyCharsFromStartOfNodeFound = key.subSequence(matched - result.charsMatchedInNodeFound, key.length());
							ByteSequence commonPrefix = getCommonPrefix(keyCharsFromStartOfNodeFound, found.in());
							ByteSequence suffixFromExistingEdge = subtractPrefix(found.in(), commonPrefix);

							ByteNode n2 = createNode(suffixFromExistingEdge, foundValue, oedges, false);
							@SuppressWarnings("NullableProblems")
							ByteNode n3 = createNode(commonPrefix, null, new Lst(2, new ByteNode[]{n1, n2}), false);

							result.parentNode.out(n3);


							break;

						default:

							throw new IllegalStateException("Unexpected classification for search result: " + result);
					}
				} finally {
					releaseWriteLock();
				}
			}
			return newValue;
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	protected int beforeWrite() {
		return -1;
	}

	private void cloneAndReattach(SearchResult searchResult, ByteNode found, Object foundValue, List<ByteNode> edges) {
		ByteSequence ie = found.in();
		boolean root = ie.length() == 0;

		ByteNode clonedNode = createNode(ie, foundValue, edges, root);


		if (root) this.root = clonedNode;
        else searchResult.parentNode.out(clonedNode);
	}

	/**
	 * Traverses the tree and finds the node which matches the longest prefix of the given key.
	 * <p/>
	 * The node returned might be an <u>exact match</u> for the key, in which case {@link SearchResult#charsMatched}
	 * will equal the length of the key.
	 * <p/>
	 * The node returned might be an <u>inexact match</u> for the key, in which case {@link SearchResult#charsMatched}
	 * will be less than the length of the key.
	 * <p/>
	 * There are two types of inexact match:
	 * <ul>
	 * <li>
	 * An inexact match which ends evenly at the boundary between a node and its children (the rest of the key
	 * not matching any children at all). In this case if we we wanted to add nodes to the tree to represent the
	 * rest of the key, we could simply add child nodes to the node found.
	 * </li>
	 * <li>
	 * An inexact match which ends in the middle of a the characters for an edge stored in a node (the key
	 * matching only the first few characters of the edge). In this case if we we wanted to add nodes to the
	 * tree to represent the rest of the key, we would have to split the node (let's call this node found: NF):
	 * <ol>
	 * <li>
	 * Create a new node (N1) which will be the split node, containing the matched characters from the
	 * start of the edge in NF
	 * </li>
	 * <li>
	 * Create a new node (N2) which will contain the unmatched characters from the rest of the edge
	 * in NF, and copy the original edges from NF unmodified into N2
	 * </li>
	 * <li>
	 * Create a new node (N3) which will be the new branch, containing the unmatched characters from
	 * the rest of the key
	 * </li>
	 * <li>
	 * Add N2 as a child of N1
	 * </li>
	 * <li>
	 * Add N3 as a child of N1
	 * </li>
	 * <li>
	 * In the <b>parent node of NF</b>, replace the edge pointing to NF with an edge pointing instead
	 * to N1. If we do this step atomically, reading threads are guaranteed to never see "invalid"
	 * data, only either the old data or the new data
	 * </li>
	 * </ol>
	 * </li>
	 * </ul>
	 * The {@link SearchResult#classification} is an enum value based on its classification of the
	 * match according to the descriptions above.
	 *
	 * @param key a key for which the node matching the longest prefix of the key is required
	 * @return A {@link SearchResult} object which contains the node matching the longest prefix of the key, its
	 * parent node, the number of characters of the key which were matched in total and within the edge of the
	 * matched node, and a {@link SearchResult#classification} of the match as described above
	 */
	private SearchResult searchTree(ByteSequence key) {
		ByteNode parentNodesParent = null;
		ByteNode parentNode = null;
		ByteNode currentNode = root;
		int charsMatched = 0, charsMatchedInNodeFound = 0;

		int keyLength = key.length();
		outer_loop:
		while (charsMatched < keyLength) {
			ByteNode nextNode = currentNode.out(key.at(charsMatched));
			if (nextNode == null) break outer_loop;

			parentNodesParent = parentNode;
			parentNode = currentNode;
			currentNode = nextNode;
			charsMatchedInNodeFound = 0;
			ByteSequence cc = currentNode.in();
			for (int i = 0, ccM = cc.length(); i < ccM && charsMatched < keyLength; i++) {
				if (cc.at(i) != key.at(charsMatched))
				    break outer_loop;
				charsMatched++;
				charsMatchedInNodeFound++;
			}
		}
		return new SearchResult(key, currentNode, charsMatched, charsMatchedInNodeFound, parentNode, parentNodesParent);
	}

	@Override
	public Iterator<X> iterator() {
		return valuesStartingWith(ByteSequence.EMPTY).iterator();
	}

	public String prettyPrint() {
		StringBuilder sb = new StringBuilder(4096);
		prettyPrint(root.out().size() == 1 ? root.out().get(0) : root, sb, "", true, true);
		return sb.toString();
	}

	public void prettyPrint(Appendable appendable) {
		prettyPrint(root, appendable, "", true, true);
	}

	public final X get(ByteSequence k) {
		return value(k);
	}

	public void clear() {
		acquireWriteLock();
		try {
			_clear();
		} finally {
			releaseWriteLock();
		}
	}


	@FunctionalInterface
	interface BytePrefixed {
		byte getIncomingEdgeFirstCharacter();
	}


	public interface ByteNode<X> extends BytePrefixed, Serializable {

		ByteSequence in();

		@Nullable Object get();

		@Nullable ByteNode<X> out(byte var1);

		/**
		 * update outgoing edge
		 */
		void out(ByteNode<X> var1);

		List<ByteNode<X>> out();
	}

	@FunctionalInterface
	public interface QuadFunction<A, B, C, D, R> {
		R apply(A a, B b, C c, D d);
	}

	static final class ByteArrayNodeDefault<X> extends NonLeafNode<X> {
		private final X value;

		ByteArrayNodeDefault(ByteSequence edgeCharSequence, X value, Lst<ByteNode> outgoingEdges) {
			super(edgeCharSequence, outgoingEdges);
			this.value = value;
		}

		@Override
		public Object get() {
			return this.value;
		}

	}

	static class ByteArrayNodeLeafVoidValue extends ProxyBytes implements ByteNode {


		ByteArrayNodeLeafVoidValue(ByteSequence edgeCharSequence) {
			super(edgeCharSequence);

		}

		@Override
		public ByteSequence in() {
			return this;
		}

		@Override
		public final byte getIncomingEdgeFirstCharacter() {
			return this.at(0);
		}

		@Override
		public Object get() {
			return VoidValue.the;
		}

		@Override
		public ByteNode out(byte edgeFirstCharacter) {
			return null;
		}

		@Override
		public void out(ByteNode childNode) {
			throw new IllegalStateException("Cannot update the reference to the following child node for the edge starting with '" + childNode.getIncomingEdgeFirstCharacter() + "', no such edge already exists: " + childNode);
		}

		@Override
		public List<ByteNode> out() {
			return emptyList;
		}

		public String toString() {
			return String.valueOf(ref)
				/*.append('=').append(VoidValue.the)*/;
		}
	}

	abstract static class NonLeafNode<X> extends Lst /*CopyOnWriteArrayList*/<ByteNode<X>> implements ByteNode<X> {

		final ByteSequence incomingEdgeCharArray;

		NonLeafNode(ByteSequence incomingEdgeCharArray, Lst<ByteNode> outs) {
			super(outs.size(), outs.array());
			this.incomingEdgeCharArray = incomingEdgeCharArray;
		}

		@Override
		public ByteSequence in() {
			return this.incomingEdgeCharArray;
		}

		@Override
		public byte getIncomingEdgeFirstCharacter() {
			return this.incomingEdgeCharArray.at(0);
		}

		@Override
		public final @Nullable ByteNode<X> out(byte edgeFirstCharacter) {
			ByteNode[] a = array() /* items */;
			int index = MyRadixTree.search(a, size, edgeFirstCharacter);
			return index < 0 ? null : a[index];
		}

		@Override
		public final void out(ByteNode<X> childNode) {
			int index = MyRadixTree.search(array(), size, childNode.getIncomingEdgeFirstCharacter());
			if (index < 0)
				throw new IllegalStateException("Cannot update the reference to the following child node for the edge starting with '" + childNode.getIncomingEdgeFirstCharacter() + "', no such edge already exists: " + childNode);

			setFast(index, childNode);
		}

		@Override
		public List<ByteNode<X>> out() {
			return this;
		}

		public String toString() {
			return String.valueOf(this.in()) + '=' + get();
		}
	}

	static final class ByteArrayNodeNonLeafNullValue extends NonLeafNode {

		ByteArrayNodeNonLeafNullValue(ByteSequence incomingEdgeCharArray, Lst<ByteNode> outgoingEdges) {
			super(incomingEdgeCharArray, outgoingEdges);
		}

		@Override
		public Object get() {
			return null;
		}

	}

	static final class ByteArrayNodeLeafWithValue<X> extends ByteArrayNodeLeafVoidValue {

		private final X value;

		ByteArrayNodeLeafWithValue(ByteSequence edgeCharSequence, X value) {
			super(edgeCharSequence);
			this.value = value;
		}

		@Override
		public Object get() {
			return this.value;
		}

		public String toString() {
			return String.valueOf(ref) + '=' + this.value;
		}
	}

	static final class ByteArrayNodeLeafNullValue extends ByteArrayNodeLeafVoidValue {

		ByteArrayNodeLeafNullValue(ByteSequence edgeCharSequence) {
			super(edgeCharSequence);
		}

		@Override
		public Object get() {
			return null;
		}

		public String toString() {
			return ref +
				"=null";
		}
	}

	static final class ByteArrayNodeNonLeafVoidValue extends NonLeafNode {


		ByteArrayNodeNonLeafVoidValue(ByteSequence edgeCharSequence, Lst<ByteNode> outgoingEdges) {
			super(edgeCharSequence, outgoingEdges);
		}

		@Override
		public Object get() {
			return VoidValue.the;
		}

	}

	/**
	 * Encapsulates a node and its associated key. Used internally by {@link #lazyTraverseDescendants}.
	 */
	protected static final class NodeKeyPair {
		public final ByteNode node;
		public final ByteSequence key;

		NodeKeyPair(ByteNode node, ByteSequence key) {
			this.node = node;
			this.key = key;
		}
	}

	/**
	 * Encapsulates results of searching the tree for a node for which a given key is a prefix. Encapsulates the node
	 * found, its parent node, its parent's parent node, and the number of characters matched in the current node and
	 * in total.
	 * <p/>
	 * Also classifies the search result so that algorithms in methods which use this SearchResult, when adding nodes
	 * and removing nodes from the tree, can select appropriate strategies based on the classification.
	 */
	public static final class SearchResult {
		public final ByteSequence key;
		public final ByteNode found;
		public final Classification classification;
		final int charsMatched;
		final int charsMatchedInNodeFound;
		final ByteNode parentNode;
		final ByteNode parentNodesParent;

		SearchResult(ByteNode found, ByteNode parentNode, ByteNode parentParentNode) {
			this(null, found, -1, -1, parentNode, parentParentNode, found != null ? Classification.EXACT_MATCH : Classification.INVALID);
		}

		SearchResult(ByteSequence key, ByteNode found, int charsMatched, int charsMatchedInNodeFound, ByteNode parentNode, ByteNode parentNodesParent) {
			this(key, found, charsMatched, charsMatchedInNodeFound, parentNode, parentNodesParent, classify(key, found, charsMatched, charsMatchedInNodeFound));
		}

		SearchResult(ByteSequence key, ByteNode found, int charsMatched, int charsMatchedInNodeFound, ByteNode parentNode, ByteNode parentNodesParent, Classification c) {
			this.key = key;
			this.found = found;
			this.charsMatched = charsMatched;
			this.charsMatchedInNodeFound = charsMatchedInNodeFound;
			this.parentNode = parentNode;
			this.parentNodesParent = parentNodesParent;


			this.classification = c;
		}

		protected static Classification classify(ByteSequence key, ByteNode nodeFound, int charsMatched, int charsMatchedInNodeFound) {
			int len = nodeFound.in().length();
			int keyLen = key.length();
			if (charsMatched == keyLen) {
				if (charsMatchedInNodeFound == len) return Classification.EXACT_MATCH;
                else if (charsMatchedInNodeFound < len) return Classification.KEY_ENDS_MID_EDGE;
			} else if (charsMatched < keyLen)
                if (charsMatchedInNodeFound == len) return Classification.INCOMPLETE_MATCH_TO_END_OF_EDGE;
                else if (charsMatchedInNodeFound < len) return Classification.INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE;
			throw new IllegalStateException("Unexpected failure to classify SearchResult");
		}

		@Override
		public String toString() {
			return "SearchResult{" +
				"key=" + key +
				", nodeFound=" + found +
				", charsMatched=" + charsMatched +
				", charsMatchedInNodeFound=" + charsMatchedInNodeFound +
				", parentNode=" + parentNode +
				", parentNodesParent=" + parentNodesParent +
				", classification=" + classification +
				'}';
		}

		enum Classification {
			EXACT_MATCH,
			INCOMPLETE_MATCH_TO_END_OF_EDGE,
			INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE,
			KEY_ENDS_MID_EDGE,
			INVALID
		}
	}

	private static class DescendantKeys extends LazyIterator<ByteSequence> implements Iterable<ByteSequence> {
		private final ByteSequence startKey;
		private final ByteNode startNode;
		private Iterator<NodeKeyPair> descendantNodes;

		DescendantKeys(ByteSequence startKey, ByteNode startNode) {
			this.startKey = startKey;
			this.startNode = startNode;
		}

		@Override
		public Iterator<ByteSequence> iterator() {
			descendantNodes = lazyTraverseDescendants(startKey, startNode).iterator();
			return this;
		}

		@Override
		protected ByteSequence computeNext() {

			Iterator<NodeKeyPair> nodes = this.descendantNodes;
			while (nodes.hasNext()) {
				NodeKeyPair nodeKeyPair = nodes.next();
				Object value = nodeKeyPair.node.get();
				if (value != null) return transformKeyForResult(nodeKeyPair.key);
			}

			return endOfData();
		}


	}

}