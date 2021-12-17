package jcog.bloom.hash;


/** expect these to be called in sequence, hash2 directly after hash1 */
public interface Hasher<E> {

    int hash1(E element);

    int hash2(E element);

}
