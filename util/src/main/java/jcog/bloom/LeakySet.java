package jcog.bloom;

import jcog.bloom.hash.Hasher;

/**
 * A {@link LeakySet} is a probabilistic data structure allowing for space-efficient membership tests on
 * large streams of data elements. Negative results are definitive but false positives might occur.
 */
public interface LeakySet<E> {

    /**
     * Add an element to the filter.
     * @param element The element must produce a deterministic hash via a {@link Hasher}.
     */
    void add(E element);

    /**
     * Check if an element has been added to the filter.
     * @param element The element must produce a deterministic hash via a {@link Hasher}.
     * @return False means that the element <i>definitively</i> has not been added. True means that the element
     * <i>probably</i> has been added.
     */
    boolean contains(E element);

}
