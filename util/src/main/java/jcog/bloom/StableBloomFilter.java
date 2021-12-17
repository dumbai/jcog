package jcog.bloom;

import jcog.bloom.hash.Hasher;

import java.util.Random;

/**
 * Stable Bloom Filters continuously "reset" random fields in the filter.
 * Deng and Rafiei have shown that by doing this, the FPR can be stabilised [1].
 * The disadvantage of this approach is that it introduces false negatives.
 * <p>
 * Created by jeff on 14/05/16.
 */
public class StableBloomFilter<E> extends MetalBloomFilter<E> implements CountingLeakySet<E> {

    private final Random rng;

    /**
     * Set the unlearning rate to make the {@link LeakySet} stable. The unlearning rate represents
     * a percentage of filter cells that will be "unlearned" with each write operation.
     *
     * @param unlearningRate Must be between 0.0 and 1.0.
     * @return {@link BloomFilterBuilder} For chaining.
     */
    private final int forget;

    public StableBloomFilter(int numberOfCells,
                             int numberOfHashes,
                             float forget,
                             Random rng,
                             Hasher<E> hasher) {
        super(hasher, numberOfCells, numberOfHashes);
        this.forget = forget(forget);
        this.rng = rng;
    }

    protected int forget(float forget) {
        return (int) Math.ceil(numberOfCells * forget);
    }

    /**
     * if the element isnt contained, add it. return true if added, false if possibly present.
     */
    public boolean addIfMissing(E element) {
        return addIfMissing(element, 1);
    }

    public boolean addIfMissing(E element, float unlearnIfNew) {
        int[] hash = hash(element);
        boolean c = contains(hash);
        if (!c) {
            if (unlearnIfNew > 0)
                forget(unlearnIfNew, rng);
            add(hash);
            return true;
        }
        return false;
    }


    @Override
    public void remove(E element) {
        int[] indices = hash(element);
        remove(indices);
    }

    public void remove(int[] indices) {
        for (int i = 0; i < numberOfHashes; i++) {
            decrement(indices[i]);
        }
    }


    public void forget(float forgetFactor, Random rng) {
        double nForget = Math.ceil(forget * forgetFactor);
        for (int i = 0; i < nForget; i++) {
            decrement(rng.nextInt(numberOfCells));
        }
    }


    private void decrement(int idx) {
        byte[] c = this.cells;
        if (c[idx] > 0)
            c[idx] -= 1;
    }

}
