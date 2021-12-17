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

import java.nio.ByteBuffer;
import java.util.function.ToLongFunction;

import static java.lang.Math.*;
import static jcog.Util.spreadHash;

/**
 * Count Min sketch is a probabilistic data structure for finding the frequency of events in a
 * stream of data. The data structure accepts two parameters epsilon and delta, epsilon specifies
 * the error in estimation and delta specifies the probability that the estimation is wrong (or the
 * confidence interval). The default values are 1% estimation error (epsilon) and 99% confidence
 * (1 - delta). Tuning these parameters results in increase or decrease in the size of the count
 * min sketch. The constructor also accepts width and depth parameters. The relationship between
 * width and epsilon (error) is width = Math.ceil(Math.exp(1.0)/epsilon). In simpler terms, the
 * lesser the error is, the greater is the width and hence the size of count min sketch.
 * The relationship between delta and depth is depth = Math.ceil(Math.log(1.0/delta)). In simpler
 * terms, the more the depth of the greater is the confidence.
 * The way it works is, if we need to estimate the number of times a certain key is inserted (or appeared in
 * the stream), count min sketch uses pairwise independent hash functions to map the key to
 * different locations in count min sketch and increment the counter.
 * <p/>
 * For example, if width = 10 and depth = 4, lets assume the hashcodes
 * for key "HELLO" using pairwise independent hash functions are 9812121, 6565512, 21312312, 8787008
 * respectively. Then the counter in hashcode % width locations are incremented.
 * <p/>
 * 0   1   2   3   4   5   6   7   8   9
 * --- --- --- --- --- --- --- --- --- ---
 * | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
 * --- --- --- --- --- --- --- --- --- ---
 * --- --- --- --- --- --- --- --- --- ---
 * | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
 * --- --- --- --- --- --- --- --- --- ---
 * --- --- --- --- --- --- --- --- --- ---
 * | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
 * --- --- --- --- --- --- --- --- --- ---
 * --- --- --- --- --- --- --- --- --- ---
 * | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 |
 * --- --- --- --- --- --- --- --- --- ---
 * <p/>
 * Now for a different key "WORLD", let the hashcodes be 23123123, 45354352, 8567453, 12312312.
 * As we can see below there is a collision for 2nd hashcode
 * <p/>
 * 0   1   2   3   4   5   6   7   8   9
 * --- --- --- --- --- --- --- --- --- ---
 * | 0 | 1 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 |
 * --- --- --- --- --- --- --- --- --- ---
 * --- --- --- --- --- --- --- --- --- ---
 * | 0 | 0 | 2 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
 * --- --- --- --- --- --- --- --- --- ---
 * --- --- --- --- --- --- --- --- --- ---
 * | 0 | 0 | 1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 |
 * --- --- --- --- --- --- --- --- --- ---
 * --- --- --- --- --- --- --- --- --- ---
 * | 0 | 0 | 2 | 0 | 0 | 0 | 0 | 0 | 1 | 0 |
 * --- --- --- --- --- --- --- --- --- ---
 * <p/>
 * Now, to get the estimated count for key "HELLO", same process is repeated again to find the
 * values in each position and the estimated count will be the minimum of all values (to account for
 * hash collisions).
 * <p/>
 * estimatedCount("HELLO") = min(1, 2, 1, 1)
 * <p/>
 * so even if there are multiple hash collisions, the returned value will be the best estimate
 * (upper bound) for the given key. The actual count can never be greater than this value.
 * <p>
 * http:
 */
public class CountMinSketch {

    private static final float DELTA_DEFAULT = 0.01f;
    private static final float EPSILON_DEFAULT = 0.01f;
    private static final ToLongFunction<byte[]> HASHER_DEFAULT = Murmur3Hash::hash64;

    public final int w, d;
    protected final int[][] data;
    private final ToLongFunction<byte[]> hasher;

    public CountMinSketch() {
        this(DELTA_DEFAULT, EPSILON_DEFAULT, HASHER_DEFAULT);
    }

    public CountMinSketch(float delta, float epsilon, ToLongFunction<byte[]> hasher) {
        this((int) ceil(exp(1.0) / epsilon), (int) ceil(log(1.0 / delta)), hasher);
    }

    public CountMinSketch(int width, int depth) {
        this(width, depth, HASHER_DEFAULT);
    }

    public CountMinSketch(int width, int depth, ToLongFunction<byte[]> hasher) {
        this(width, depth, new int[depth][width], hasher);
    }

    private CountMinSketch(int width, int depth, int[][] ms, ToLongFunction<byte[]> hasher) {
        this.w = width;
        this.d = depth;
        this.data = ms;
        this.hasher = hasher;
    }

    /**
     * Serialize the count min sketch to byte array. The format of serialization is width followed by
     * depth followed by integers in multiset from row1, row2 and so on..
     *
     * @return serialized byte array
     */
    public static byte[] serialize(CountMinSketch cms) {
        long serializedSize = cms.byteSize();
        ByteBuffer b = ByteBuffer.allocate((int) serializedSize);
        b.putInt(cms.w);
        b.putInt(cms.depth());
        for (int i = 0; i < cms.depth(); i++) {
            int[] row = cms.data[i];
            for (int j = 0; j < cms.w; j++)
                b.putInt(row[j]);
        }
        b.flip();
        return b.array();
    }

    public static CountMinSketch deserialize(byte[] serialized) {
        return deserialize(serialized, HASHER_DEFAULT);
    }

    /**
     * Deserialize the serialized count min sketch.
     *
     * @param serialized - serialized count min sketch
     * @return deserialized count min sketch object
     */
    public static CountMinSketch deserialize(byte[] serialized, @Deprecated ToLongFunction<byte[]> hasher) {
        ByteBuffer bb = ByteBuffer.allocate(serialized.length);
        bb.put(serialized);
        bb.flip();
        int width = bb.getInt();
        int depth = bb.getInt();
        int[][] multiset = new int[depth][width];
        for (int i = 0; i < depth; i++) {
            int[] row = multiset[i];
            for (int j = 0; j < width; j++)
                row[j] = bb.getInt();
        }
        return new CountMinSketch(width, depth, multiset, hasher);
    }

    public int depth() {
        return d;
    }

    /**
     * Returns the size in bytes after serialization.
     *
     * @return serialized size in bytes
     */
    public long byteSize() {
        return ((w * d) + 2) * (Integer.SIZE / 8);
    }

    public void add(byte[] key) {
        add(hash(key));
    }

    public void add(int hash1, int hash2) {
        for (int i = 0; i < d; i++) {
            int combinedHash = hash1 + ((i - 1) * hash2);

            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }
            int pos = combinedHash % w;
            data[i][pos] += 1;
        }
    }

    protected final long hash(byte[] key) {
        return hasher.applyAsLong(key);
    }


    public void add(byte val) {
        add(spreadHash(val & 0xf), spreadHash(val >>> 4));
    }
    public int count(byte val) {
        return count(spreadHash(val & 0xf), spreadHash(val >>> 4));
    }


    public void add(short val) {
        add(spreadHash(val & 0xff), spreadHash(val >>> 8));
    }
    public int count(short val) {
        return count(spreadHash(val & 0xff), spreadHash(val >>> 8));
    }

    public void add(int val) {
        add(spreadHash(val & 0xffff), spreadHash(val >>> 16));
    }

    public int count(int val) {
        return count(spreadHash(val & 0xffff), spreadHash(val >>> 16));
    }

    public void add(String val) {
        add(val.getBytes());
    }


    public void add(long x) { add((int) x, (int) (x >>> 32)); }
    public int count(long hash64) {
        return count((int) hash64, (int) (hash64 >>> 32));
    }

    public void add(float val) {
        add(Float.floatToIntBits(val));
    }
    public int count(float val) {
        return count(Float.floatToIntBits(val));
    }


    public void add(double val) {
        add(Double.doubleToLongBits(val));
    }
    public int count(double val) {
        return count(Double.doubleToLongBits(val));
    }

    private int count(int hash1, int hash2) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < d; i++) {
            //int combinedHash = hash1 + ((i - 1) * hash2);

            int combinedHash = (int) ((hash1 + ((i-1) * ((long)hash2))));
            if (combinedHash < 0)
                combinedHash = ~combinedHash;

            min = min(min, data[i][combinedHash % w]);
        }

        return min;
    }

    public int count(String val) {
        return count(val.getBytes());
    }
    public int count(byte[] b) {
        return count(hasher.applyAsLong(b));
    }


    /**
     * Merge the give count min sketch with current one. Merge will throw RuntimeException if the
     * provided CountMinSketch is not compatible with current one.
     *
     * @param that - the one to be merged
     */
    public CountMinSketch merge(CountMinSketch that) {
        if (that == null || that == this) {
            return this;
        }

        if (this.w != that.w) {
            throw new RuntimeException("Merge failed! Width of count min sketch do not match!" +
                    "this.width: " + w + " that.width: " + that.w);
        }

        if (this.d != that.d) {
            throw new RuntimeException("Merge failed! Depth of count min sketch do not match!" +
                    "this.depth: " + this.depth() + " that.depth: " + that.depth());
        }

        for (int i = 0; i < d; i++) {
            int[] xi = this.data[i];
            int[] yi = that.data[i];
            for (int j = 0; j < w; j++) {
                xi[j] += yi[j];
            }
        }

        return this;
    }
}