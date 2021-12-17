/**
 * Copyright 2014 Prasanth Jayachandran
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http:
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcog.bloom;

import jcog.bloom.hash.Murmur3Hash;
import jcog.data.bit.LongArrayBitSet;
import jcog.data.bit.MetalBitSet;

import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;


/**
 * BloomFilter is a probabilistic data structure for set membership check. BloomFilters are
 * highly space efficient when compared to using a HashSet. Because of the probabilistic nature of
 * bloom filter false positive (element not present in bloom filter but test() says true) are
 * possible but false negatives are not possible (if element is present then test() will never
 * say false). The false positive probability is configurable (default: 5%) depending on which
 * storage requirement may increase or decrease. Lower the false positive probability greater
 * is the space requirement.
 * Bloom filters are sensitive to number of elements that will be inserted in the bloom filter.
 * During the creation of bloom filter expected number of entries must be specified. If the number
 * of insertions exceed the specified initial number of entries then false positive probability will
 * increase accordingly.
 * <p/>
 * Internally, this implementation of bloom filter uses Murmur3 fast non-cryptographic hash
 * algorithm. Although Murmur2 is slightly faster than Murmur3 in Java, it suffers from hash
 * collisions for specific sequence of repeating bytes. Check the following link for more info
 * https:
 */
public class LongBitsetBloomFilter {
    public static final float DEFAULT_FPP = 0.05f;
    private MetalBitSet bitSet;
    private final int m;
    private final int k;
    private final double fpp;
    private final long n;

    public LongBitsetBloomFilter(long maxNumEntries) {
        this(maxNumEntries, DEFAULT_FPP);
    }

    public LongBitsetBloomFilter(long maxNumEntries, double fpp) {
        if (maxNumEntries <= 0)
            throw new AssertionError("maxNumEntries should be > 0");
        if (fpp <= 0.0 || fpp >= 1.0)
            throw new AssertionError("False positive percentage should be > 0.0 & < 1.0");

        this.fpp = fpp;
        n = maxNumEntries;
        m = optimalNumOfBits(maxNumEntries, fpp);
        k = optimalNumOfHashFunctions(maxNumEntries, m);
        bitSet = MetalBitSet.bits(m);
    }

//
//    public LongBitsetBloomFilter(List<Long> serializedBloom) {
//        this(serializedBloom.get(0), Double.longBitsToDouble(serializedBloom.get(1)));
//        List<Long> bitSet = serializedBloom.subList(2, serializedBloom.size());
//        long[] data = new long[10];
//        int count = 0;
//        for (Long aLong : bitSet) {
//            if (data.length == count) data = Arrays.copyOf(data, count * 2);
//            long l = aLong;
//            data[count++] = l;
//        }
//        data = Arrays.copyOfRange(data, 0, count);
//        this.bitSet = new MetalBitSet.LongArrayBitSet(data);
//    }

    public void clear() {
        bitSet.clear();
    }

    public static int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    public static int optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (int) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    public long sizeInBytes() {
        return bitSet instanceof LongArrayBitSet ? ((LongArrayBitSet)bitSet).bitSize() / 8 :
                4; 
    }

    public boolean test(byte[] val) {
        long hash64 = Murmur3Hash.hash64(val);
        int hash1 = (int) hash64;
        int hash2 = (int) (hash64 >>> 32);


        int k = this.k;
        int m = this.m;
        IntPredicate bits = bitSet;

        return IntStream.rangeClosed(1, k).map(i -> combineHash(hash1, hash2, i)).allMatch(combinedHash -> bits.test( /*pos*/ combinedHash % m));
    }


    public final <X> void add(X x, Function<X,byte[]> f) {
        add(f.apply(x));
    }

    public final <X> boolean test(X x, Function<X,byte[]> f) {
        return test(f.apply(x));
    }

    public final void add(byte[] val) {
        
        
        

        
        
        long hash64 = Murmur3Hash.hash64(val);
        int hash1 = (int) hash64;
        int hash2 = (int) (hash64 >>> 32);

        int k = this.k;
        int m = this.m;
        MetalBitSet bits = bitSet;

        for (int i = 1; i <= k; i++) {
            int combinedHash = combineHash(hash1, hash2, i);
            bits.set( /*pos*/ combinedHash % m );
        }
    }

    protected static int combineHash(int hash1, int hash2, int i) {
        int combinedHash = hash1 + (i * hash2);
        
        if (combinedHash < 0) {
            combinedHash = ~combinedHash;
        }
        return combinedHash;
    }

    public void addString(String val) {
        add(val.getBytes());
    }

    public void addByte(byte val) {
        add(new byte[]{val});
    }

    public void addInt(int val) {
        
        add(intToByteArrayLE(val));
    }

    public boolean addIfNotContained(int val) {
        
        byte[] bb = intToByteArrayLE(val);
        boolean b = test(bb);
        if (!b) {
            add(bb);
            return true;
        }
        return false;
    }

    public void addLong(long val) {
        
        add(longToByteArrayLE(val));
    }

    public void addFloat(float val) {
        addInt(Float.floatToIntBits(val));
    }

    public void addDouble(double val) {
        addLong(Double.doubleToLongBits(val));
    }



    public boolean testString(String val) {
        return test(val.getBytes());
    }

    public boolean testByte(byte val) {
        return test(new byte[]{val});
    }

    public boolean testInt(int val) {
        return test(intToByteArrayLE(val));
    }

    public boolean testLong(long val) {
        return test(longToByteArrayLE(val));
    }

    public boolean testFloat(float val) {
        return testInt(Float.floatToIntBits(val));
    }

    public boolean testDouble(double val) {
        return testLong(Double.doubleToLongBits(val));
    }

    public static byte[] intToByteArrayLE(int val) {
        return new byte[]{(byte) (val),
                (byte) (val >> 8),
                (byte) (val >> 16),
                (byte) (val >> 24)};
    }

    private static byte[] longToByteArrayLE(long val) {
        return new byte[]{(byte) (val),
                (byte) (val >> 8),
                (byte) (val >> 16),
                (byte) (val >> 24),
                (byte) (val >> 32),
                (byte) (val >> 40),
                (byte) (val >> 48),
                (byte) (val >> 56),};
    }

    public int getBitSize() {
        return m;
    }

    public int getNumHashFunctions() {
        return k;
    }

    public double getFalsePositivePercent() {
        return fpp;
    }

    public long getExpectedNumEntries() {
        return n;
    }

















    /**
     * Check if the specified bloom filter is compatible with the current bloom filter.
     *
     * @param that - bloom filter to check compatibility
     * @return true if compatible false otherwise
     */
    public boolean isCompatible(LongBitsetBloomFilter that) {
        return this != that &&
                getBitSize() == that.getBitSize() &&
                getNumHashFunctions() == that.getNumHashFunctions();
    }

    /**
     * Merge the specified bloom filter with current bloom filter.
     * NOTE: Merge does not check for incompatibility. Use isCompatible() before calling merge().
     *
     * @param that - bloom filter to merge
     */
    public void merge(LongBitsetBloomFilter that) {
        ((LongArrayBitSet)bitSet).putAll((LongArrayBitSet) that.bitSet);
    }

}