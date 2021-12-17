package jcog.bloom;

/**
 * In contrast to a {@link LeakySet}, elements can also be removed from {@link CountingLeakySet}s.
 */
public interface CountingLeakySet<E> extends LeakySet<E> {

    /**
     * Remove an element from the filter.
     * @param element Must have been added to the filter before. If not, the method wont fail but unpredictable
     *                side-effects might occur.
     */
    void remove(E element);

    boolean addIfMissing(E element);

}
