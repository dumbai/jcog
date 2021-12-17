package jcog.tree.radix;

/**
 * Created by me on 11/14/16.
 */
public interface KeyValuePair<O> {

    /**
     * Returns the key with which the value is associated
     *
     * @return The key with which the value is associated
     */
    CharSequence getKey();

    /**
     * Returns the value associated with the key
     *
     * @return The value associated with the key
     */
    O getValue();

    /**
     * Compares this {@link com.googlecode.concurrenttrees.common.KeyValuePair} object with another for equality.
     * <p/>
     * This is implemented based on equality of the keys.
     *
     * @param o The other object to compare
     * @return True if the other object is also a {@link com.googlecode.concurrenttrees.common.KeyValuePair} and is equal to this one as specified above
     */
    @Override
    boolean equals(Object o);

    /**
     * Returns a hash code for this object.
     */
    @Override
    int hashCode();

    /**
     * Returns a string representation as {@code (key, value)}.
     *
     * @return A string representation as {@code (key, value)}
     */
    @Override
    String toString();
}
