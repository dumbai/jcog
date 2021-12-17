package jcog.bloom;

import jcog.bloom.hash.BytesHasher;
import jcog.bloom.hash.Hasher;

import java.util.function.Function;
import java.util.stream.IntStream;

public class MetalBloomFilter<E> {
    protected final Hasher<E> hasher;
    protected final byte[] cells;
    protected final int numberOfCells;
    protected final int numberOfHashes;

    public MetalBloomFilter(Function<E,byte[]> hashProvider, int numberOfCells, int numberOfHashes) {
        this(new BytesHasher(hashProvider), numberOfCells, numberOfHashes);
    }

    public MetalBloomFilter(Hasher<E> hasher, int numberOfCells, int numberOfHashes) {
        this.hasher = hasher;
        this.cells = new byte[numberOfCells];
        this.numberOfCells = numberOfCells;
        this.numberOfHashes = numberOfHashes;
    }

    public void add(E element) {
        add(hash(element));
    }

    /** possibly contains */
    public boolean contains(E element) {
        return contains(hash(element));
    }

    public void add(int[] indices) {
        for (int i = 0; i < numberOfHashes; i++) {
            increment(indices[i]);
        }
    }

    /** possibly contains */
    public boolean contains(int[] indices) {

        return IntStream.range(0, numberOfHashes).anyMatch(i -> cells[indices[i]] > 0);
    }

    public int[] hash(E element) {
        int[] hashes = new int[numberOfHashes];

        int h1 = hasher.hash1(element);
        int h2 = hasher.hash2(element);
        for (int i = 0; i < numberOfHashes; i++) {
            hashes[i] = Math.abs(((h1 + i * h2) % numberOfCells));
        }

        return hashes;
    }

    private void increment(int idx) {
        if (cells[idx] < Byte.MAX_VALUE) {
            cells[idx]++;
        }
    }
}
