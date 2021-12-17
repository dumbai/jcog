/* 
 * NOTICE OF LICENSE
 * 
 * This source file is subject to the Open Software License (OSL 3.0) that is 
 * bundled with this package in the file LICENSE.txt. It is also available 
 * through the world-wide-web at http:
 * If you did not receive a copy of the license and are unable to obtain it 
 * through the world-wide-web, please send an email to magnos.software@gmail.com 
 * so we can send you a copy immediately. If you use any of this software please
 * notify me via our website or email, your feedback is much appreciated. 
 * 
 * @copyright   Copyright (c) 2011 Magnos Software (http:
 * @license     http:
 *              Open Software License (OSL 3.0)
 */

package jcog.tree.perfect;

import com.google.common.collect.Iterators;
import jcog.Util;
import jcog.data.iterator.ArrayIterator;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;


/**
 * A TrieNode is an {@link java.util.Map.Entry Entry} in a Trie that stores the
 * sequence (key), value, the starting and ending indices into the sequence, the
 * number of children in this node, and the parent to this node.
 * <p>
 * There are three types of TrieNodes and each have special properties.
 * </p>
 * <ol>
 * <li>Root
 * <ul>
 * <li>{@link #start()} == {@link #end()} == 0</li>
 * <li>{@link #getValue()} == null</li>
 * <li>{@link #getKey()} == {@link #seq()} == null</li>
 * </ul>
 * </li>
 * <li>Naked Branch
 * <ul>
 * <li>{@link #start()} &lt; {@link #end()}</li>
 * <li>{@link #getValue()} == null</li>
 * <li>{@link #getKey()} == {@link #seq()} == (a key of one of it's
 * children or a past child, ignore)</li>
 * </ul>
 * </li>
 * <li>Valued (Branch or Leaf)
 * <ul>
 * <li>{@link #start()} &lt; {@link #end()}</li>
 * <li>{@link #getValue()} == non-null value passed into
 * {@link Trie#put(Object, Object)}</li>
 * <li>{@link #getKey()} == {@link #seq()} == a non-null key passed into
 * {@link Trie#put(Object, Object)}</li>
 * </ul>
 * </li>
 * </ol>
 * <p>
 * You can tell a valued branch or leaf apart by {@link #childCount()}, if it
 * returns 0 then it's a leaf, otherwise it's a branch.
 * </p>
 *
 * @author Philip Diffenderfer
 */
public class TrieNode<S, T> implements Entry<S, T>, Iterable<TrieNode<S,T>> {

    protected TrieNode<S, T> parent;
    protected T value;
    protected S sequence;
    protected final int start;
    protected int end;
    protected PerfectHashMap<TrieNode<S, T>> children;
    protected int size;

    /**
     * Instantiates a new TrieNode.
     *
     * @param parent   The parent to this node.
     * @param value    The value of this node.
     * @param sequence The sequence of this node.
     * @param start    The start of the sequence for this node, typically the end of the
     *                 parent.
     * @param end      The end of the sequence for this node.
     * @param children The intial set of children.
     */
    protected TrieNode(TrieNode<S, T> parent, T value, S sequence, int start, int end, PerfectHashMap<TrieNode<S, T>> children) {
        this.parent = parent;
        this.sequence = sequence;
        this.start = start;
        this.end = end;
        this.children = children;
        size = calculateSize(children);
        setValue(value);
    }

    /**
     * Splits this node at the given relative index and returns the TrieNode with
     * the sequence starting at index. The returned TrieNode has this node's
     * sequence, value, and children. The returned TrieNode is also the only
     * child of this node when this method returns.
     *
     * @param index     The relative index (starting at 0 and going to end - start - 1) in
     *                  the sequence.
     * @param newValue  The new value of this node.
     * @param sequencer The sequencer used to add the returned node to this node.
     * @return The reference to the child node created that's sequence starts at
     * index.
     */
    protected TrieNode<S, T> split(int index, T newValue, TrieSequencer<S> sequencer) {
        TrieNode<S, T> c = new TrieNode<>(this, value, sequence, index + start, end, children);
        c.registerAsParent();

        setValue(null);
        setValue(newValue);
        end = index + start;
        children = null;

        add(c, sequencer);

        return c;
    }

    /**
     * Adds the given child to this TrieNode. The child TrieNode is expected to
     * have had this node's reference passed to it's constructor as the parent
     * parameter. This needs to be done to keep the size calculations accurate.
     *
     * @param child     The TrieNode to add as a child.
     * @param sequencer The sequencer to use to determine the place of the node in the
     *                  children PerfectHashMap.
     */
    protected void add(TrieNode<S, T> child, TrieSequencer<S> sequencer) {
        int hash = sequencer.hashOf(child.sequence, end);

        if (children == null) {
            children = new PerfectHashMap<>(hash, child);
        } else {
            children.put(hash, child);
        }
    }

    @Override public Iterator<TrieNode<S,T>> iterator() {
        if (children == null) return Util.emptyIterator;
        Object[] vv = children.value;
        if ((vv == null) || (vv.length == 0)) return Util.emptyIterator;
        return Iterators.transform( ArrayIterator.iterateNonNull(vv), x -> (TrieNode<S,T>)x);
    }

//    @Override public void forEach(Consumer<? super TrieNode<S, T>> eachChild) {
//        if (children == null) return;
//
//        Object[] vv = children.values;
//        if ((vv == null) || (vv.length == 0)) return;
//
//        for (Object x : vv) {
//            if (x != null)
//                eachChild.accept((TrieNode<S, T>) x);
//        }
//    }

    public void forEach(BiConsumer<TrieNode<S, T>, TrieNode<S, T>> parentChildConsumer) {
        if (children == null) return;

        Object[] vv = children.value;
        if ((vv == null) || (vv.length == 0)) return;

        for (Object x /*TrieNode<S,T> x*/ : vv) {
            if (x == null) continue;
            TrieNode<S, T> xx = (TrieNode<S, T>) x;
            parentChildConsumer.accept(this, xx);
            xx.forEach(parentChildConsumer);
        }
    }

    /**
     * Removes this node from the Trie and appropriately adjusts it's parent and
     * children.
     *
     * @param sequencer The sequencer to use to determine the place of this node in this
     *                  nodes sibling PerfectHashMap.
     */
    protected void remove(TrieSequencer<S> sequencer) {
        
        setValue(null);

        int childCount = (children == null ? 0 : children.size);


        switch (childCount) {
            case 0 -> parent.children.remove(sequencer.hashOf(sequence, start));
            case 1 -> {
                @SuppressWarnings("RedundantCast") TrieNode child = (TrieNode) ((Object[]) children.value)[0]; //HACK
                children = child.children;
                value = (T) child.value;
                sequence = (S) child.sequence;
                end = child.end;
                child.children = null;
                child.parent = null;
                child.sequence = null;
                child.value = null;
                registerAsParent();
            }
        }
    }

    /**
     * Adds the given size to this TrieNode and it's parents.
     *
     * @param amount The amount of size to addAt.
     */
    private void addSize(int amount) {
        TrieNode<S, T> curr = this;

        while (curr != null) {
            curr.size += amount;
            curr = curr.parent;
        }
    }

    /**
     * Sums the sizes of all non-null TrieNodes in the given map.
     *
     * @param nodes The map to calculate the total size of.
     * @return The total size of the given map.
     */
    private int calculateSize(PerfectHashMap<TrieNode<S,T>> nodes) {
        return (nodes != null) ?
             IntStream.iterate(nodes.capacity() - 1, i -> i >= 0, i -> i - 1)
                .mapToObj(nodes::value)
                .filter(Objects::nonNull)
                .mapToInt(n -> n.size).sum() : 0;
    }

    /**
     * Ensures all child TrieNodes to this node are pointing to the correct
     * parent (this).
     */
    private void registerAsParent() {
        if (children != null) {
            for (int i = 0; i < children.capacity(); i++) {
                TrieNode<S, T> c = children.value(i);
                if (c != null)
                    c.parent = this;
            }
        }
    }

    /**
     * Returns whether this TrieNode has children.
     *
     * @return True if children exist, otherwise false.
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * Returns the parent of this TrieNode. If this TrieNode doesn't have a
     * parent it signals that this TrieNode is the root of a Trie and null will
     * be returned.
     *
     * @return The reference to the parent of this node, or null if this is a
     * root node.
     */
    public TrieNode<S, T> getParent() {
        return parent;
    }

    /**
     * The value of this TrieNode.
     *
     * @return The value of this TrieNode or null if this TrieNode is a branching
     * node only (has children but the sequence in this node was never
     * directly added).
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * The complete sequence of this TrieNode. The actual sequence
     * is a sub-sequence that starts at {@link #start()} (inclusive) and ends
     * at {@link #end()} (exclusive).
     *
     * @return The complete sequence of this TrieNode.
     */
    public S seq() {
        return sequence;
    }

    /**
     * The start of the sequence in this TrieNode.
     *
     * @return The start of the sequence in this TrieNode, greater than or equal
     * to 0 and less than {@link #end()}. In the case of
     * the root node: {@link #start()} == {@link #end()}.
     */
    public int start() {
        return start;
    }

    /**
     * The end of the sequence in this TrieNode.
     *
     * @return The end of the sequence in this TrieNode, greater than
     * {@link #start()}. In the case of the root node:
     * {@link #start()} == {@link #end()}.
     */
    public int end() {
        return end;
    }

    /**
     * Returns the number of non-null values that exist in ALL child nodes
     * (including this node's value).
     *
     * @return The number of non-null values and valid sequences.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the number of direct children.
     *
     * @return The number of direct children in this node.
     */
    public int childCount() {
        return (children == null ? 0 : children.size);
    }

    /**
     * Calculates the root node by traversing through all parents until it found
     * it.
     *
     * @return The root of the {@link Trie} this TrieNode.
     */
    public TrieNode<S, T> root() {
        TrieNode<S, T> p = parent;
        while (p.parent != null)
            p = p.parent;

        return p;
    }

    /**
     * @return True if this node is a root, otherwise false.
     */
    public boolean isRoot() {
        return (parent == null);
    }

//    /**
//     * @return True if this node is a root or a naked (branch only) node,
//     * otherwise false.
//     */
//    public boolean isNaked() {
//        return (value == null);
//    }
//
//    /**
//     * @return True if this node has a non-null value (is not a root or naked
//     * node).
//     */
//    public boolean hasValue() {
//        return (value != null);
//    }

    @Override
    public S getKey() {
        return sequence;
    }

    @Override
    public T setValue(T newValue) {
        T previousValue = value;

        boolean nulled = (value = newValue) == null;

        if (!nulled && previousValue == null)
            addSize(1);
        else if (nulled && previousValue != null)
            addSize(-1);

        return previousValue;
    }

    @Override
    public int hashCode() {
        return (sequence == null ? 0 : sequence.hashCode())
                ^ (value == null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return sequence + "=" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this==o)
            return true;
        if (/*o == null ||*/ !(o instanceof TrieNode node))
            return false;


        Object ns = node.sequence;
        S ts = sequence;
        if (!(ts == ns || ts.equals(ns)))
            return false;
        Object nv = node.value;
        T tv = value;
        return (tv == nv || (tv != null && nv != null && tv.equals(nv)));
    }

    public void seq(S s) {
        sequence = s;
    }

}