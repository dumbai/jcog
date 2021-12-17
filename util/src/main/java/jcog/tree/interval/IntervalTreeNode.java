package jcog.tree.interval;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IntervalTreeNode<K extends Comparable<? super K>,V> {

	boolean isLeaf();
	
	@Nullable
	IntervalTreeNode<K,V> getLeft();
	@Nullable
	IntervalTreeNode<K,V> getRight();
	
	boolean contains(K point);
	boolean contains(Between<K> interval);
	boolean containedBy(Between<K> interval);
	boolean overlaps(K low, K high);
	boolean overlaps(Between<K> interval);
	
	boolean containsValue(V value);
	
	
	K getLow();
	
	K getHigh();
	V getValue();
	
	Between<K> getRange();
	
	
	IntervalTreeNode<K, V> put(Between<K> key, V value);

	void getOverlap(Between<K> range, Consumer<V> accumulator);
	void getOverlap(Between<K> range, Collection<V> accumulator);
	/**
	 * Returns a collection of values that wholly contain the range specified.
	 */
	void getContain(Between<K> range, Collection<V> accumulator);
	void forEachContainedBy(Between<K> range, BiConsumer<Between<K>,V> accumulator);

	/**
	 * Returns a collection of values that are wholly contained by the range specified.
	 */
	void searchContainedBy(Between<K> range, Collection<V> accumulator);
	
	@Nullable
	IntervalTreeNode<K, V> removeOverlapping(Between<K> range);
	@Nullable
	IntervalTreeNode<K, V> removeContaining(Between<K> range);
	@Nullable
	IntervalTreeNode<K, V> removeContainedBy(Between<K> range);
	
	void values(Collection<V> accumulator);
	void entrySet(Set<Entry<Between<K>, V>> accumulator);
	void keySet(Set<Between<K>> accumulator);
	
	@Nullable
	IntervalTreeNode<K, V> remove(V value);
	@Nullable
	IntervalTreeNode<K, V> removeAll(Collection<V> values);

	int size();
	int maxHeight();
	void averageHeight(Collection<Integer> heights, int currentHeight);

	@Nullable
	V getEqual(Between<K> range);

	@Nullable
	V getContain(Between<K> range);
}