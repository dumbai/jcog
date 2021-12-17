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
package jcog.bloom.hash;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Murmur3 32 and 128 bit variants.
 * 32-bit Java port of https:
 * 128-bit Java port of https:
 */
public enum Murmur3Hash {
    ;


    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;
    private static final int R1 = 31;
    private static final int R2 = 27;
    private static final int R3 = 33;
    private static final int M = 5;
    private static final int N1 = 0x52dce729;
    private static final int N2 = 0x38495ab5;

    private static final int DEFAULT_SEED = 0;
    private static final int SEED = 0x0;
    private static final int AVALANCHING_MULTIPLIER1 = 0xcc9e2d51;
    private static final int AVALANCHING_MULTIPLIER2 = 0x1b873593;
    private static final int BLOCK_OFFSET = 0xe6546b64;
    private static final int FINAL_AVALANCHING_MULTIPLIER1 = 0x85ebca6b;
    private static final int FINAL_AVALANCHING_MULTIPLIER2 = 0xc2b2ae35;


    /**
     * Murmur3 64-bit variant. This is essentially MSB 8 bytes of Murmur3 128-bit variant.
     *
     * @param data - input byte array
     * @return - hashcode
     */
    public static long hash64(byte[] data) {
        return hash64(data, data.length);
    }

    public static long hash64(byte[] data, int len) {
        return hash64(data, len, DEFAULT_SEED);
    }

    /**
     * Murmur3 64-bit variant. This is essentially MSB 8 bytes of Murmur3 128-bit variant.
     *
     * @param data   - input byte array
     * @param length - length of array
     * @param seed   - seed. (default is 0)
     * @return - hashcode
     */
    public static long hash64(byte[] data, int length, int seed) {
        long hash = seed;
        int nblocks = length >> 3;


        for (int i = 0; i < nblocks; i++) {
            int i8 = i << 3;
            long k = ((long) data[i8] & 0xff)
                    | (((long) data[i8 + 1] & 0xff) << 8)
                    | (((long) data[i8 + 2] & 0xff) << 16)
                    | (((long) data[i8 + 3] & 0xff) << 24)
                    | (((long) data[i8 + 4] & 0xff) << 32)
                    | (((long) data[i8 + 5] & 0xff) << 40)
                    | (((long) data[i8 + 6] & 0xff) << 48)
                    | (((long) data[i8 + 7] & 0xff) << 56);


            k *= C1;
            k = Long.rotateLeft(k, R1);
            k *= C2;
            hash ^= k;
            hash = Long.rotateLeft(hash, R2) * M + N1;
        }


        long k1 = 0;
        int tailStart = nblocks << 3;
        switch (length - tailStart) {
            case 7:
                k1 ^= ((long) data[tailStart + 6] & 0xff) << 48;
            case 6:
                k1 ^= ((long) data[tailStart + 5] & 0xff) << 40;
            case 5:
                k1 ^= ((long) data[tailStart + 4] & 0xff) << 32;
            case 4:
                k1 ^= ((long) data[tailStart + 3] & 0xff) << 24;
            case 3:
                k1 ^= ((long) data[tailStart + 2] & 0xff) << 16;
            case 2:
                k1 ^= ((long) data[tailStart + 1] & 0xff) << 8;
            case 1:
                k1 ^= ((long) data[tailStart] & 0xff);
                k1 *= C1;
                k1 = Long.rotateLeft(k1, R1);
                k1 *= C2;
                hash ^= k1;
        }


        hash ^= length;
        hash = fmix64(hash);

        return hash;
    }

    /**
     * Murmur3 128-bit variant. WARNING involves allocation
     *
     * @param data - input byte array
     * @return - hashcode (2 longs)
     */
    public static long[] hash128(byte[] data) {
        return hash128(data, data.length, DEFAULT_SEED);
    }

    /**
     * Murmur3 128-bit variant. WARNING involves allocation
     *
     * @param data   - input byte array
     * @param length - length of array
     * @param seed   - seed. (default is 0)
     * @return - hashcode (2 longs)
     */
    public static long[] hash128(byte[] data, int length, int seed) {
        long h1 = seed;
        long h2 = seed;
        int nblocks = length >> 4;


        for (int i = 0; i < nblocks; i++) {
            int i16 = i << 4;
            long k1 = ((long) data[i16] & 0xff)
                    | (((long) data[i16 + 1] & 0xff) << 8)
                    | (((long) data[i16 + 2] & 0xff) << 16)
                    | (((long) data[i16 + 3] & 0xff) << 24)
                    | (((long) data[i16 + 4] & 0xff) << 32)
                    | (((long) data[i16 + 5] & 0xff) << 40)
                    | (((long) data[i16 + 6] & 0xff) << 48)
                    | (((long) data[i16 + 7] & 0xff) << 56);

            long k2 = ((long) data[i16 + 8] & 0xff)
                    | (((long) data[i16 + 9] & 0xff) << 8)
                    | (((long) data[i16 + 10] & 0xff) << 16)
                    | (((long) data[i16 + 11] & 0xff) << 24)
                    | (((long) data[i16 + 12] & 0xff) << 32)
                    | (((long) data[i16 + 13] & 0xff) << 40)
                    | (((long) data[i16 + 14] & 0xff) << 48)
                    | (((long) data[i16 + 15] & 0xff) << 56);


            k1 *= C1;
            k1 = Long.rotateLeft(k1, R1);
            k1 *= C2;
            h1 ^= k1;
            h1 = Long.rotateLeft(h1, R2);
            h1 += h2;
            h1 = h1 * M + N1;


            k2 *= C2;
            k2 = Long.rotateLeft(k2, R3);
            k2 *= C1;
            h2 ^= k2;
            h2 = Long.rotateLeft(h2, R1);
            h2 += h1;
            h2 = h2 * M + N2;
        }


        long k1 = 0;
        long k2 = 0;
        int tailStart = nblocks << 4;
        switch (length - tailStart) {
            case 15:
                k2 ^= (long) (data[tailStart + 14] & 0xff) << 48;
            case 14:
                k2 ^= (long) (data[tailStart + 13] & 0xff) << 40;
            case 13:
                k2 ^= (long) (data[tailStart + 12] & 0xff) << 32;
            case 12:
                k2 ^= (long) (data[tailStart + 11] & 0xff) << 24;
            case 11:
                k2 ^= (long) (data[tailStart + 10] & 0xff) << 16;
            case 10:
                k2 ^= (long) (data[tailStart + 9] & 0xff) << 8;
            case 9:
                k2 ^= (data[tailStart + 8] & 0xff);
                k2 *= C2;
                k2 = Long.rotateLeft(k2, R3);
                k2 *= C1;
                h2 ^= k2;

            case 8:
                k1 ^= (long) (data[tailStart + 7] & 0xff) << 56;
            case 7:
                k1 ^= (long) (data[tailStart + 6] & 0xff) << 48;
            case 6:
                k1 ^= (long) (data[tailStart + 5] & 0xff) << 40;
            case 5:
                k1 ^= (long) (data[tailStart + 4] & 0xff) << 32;
            case 4:
                k1 ^= (long) (data[tailStart + 3] & 0xff) << 24;
            case 3:
                k1 ^= (long) (data[tailStart + 2] & 0xff) << 16;
            case 2:
                k1 ^= (long) (data[tailStart + 1] & 0xff) << 8;
            case 1:
                k1 ^= (data[tailStart] & 0xff);
                k1 *= C1;
                k1 = Long.rotateLeft(k1, R1);
                k1 *= C2;
                h1 ^= k1;
        }


        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return new long[]{h1, h2};
    }

    private static long fmix64(long h) {
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= (h >>> 33);
        return h;
    }

    public static int hash(byte[] data) {
        ByteBuffer buffer = ByteBuffer
                .wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN);


        int length = data.length;
        int hash = SEED;


        int numberOfBlocks = length / 4;
        int ints = numberOfBlocks * 4;
        for (int i = 0; i < ints; i += 4) {
            int block = buffer.getInt(i);

            block *= AVALANCHING_MULTIPLIER1;
            block = Integer.rotateLeft(block, 15);
            block *= AVALANCHING_MULTIPLIER2;

            hash ^= block;
            hash = Integer.rotateLeft(hash, 13);
            hash = 5 * hash + BLOCK_OFFSET;
        }


        int leftOverLength = length % 4;
        int block = 0;
        switch (leftOverLength) {
            case 3:
                block ^= buffer.get(length - 3) << 16;
            case 2:
                block ^= buffer.get(length - 2) << 8;
            case 1:
                block ^= buffer.get(length - 1);
                block *= AVALANCHING_MULTIPLIER1;
                block = Integer.rotateLeft(block, 15);
                block *= AVALANCHING_MULTIPLIER2;
                hash ^= block;
        }


        hash ^= length;
        hash ^= hash >>> 16;
        hash *= FINAL_AVALANCHING_MULTIPLIER1;
        hash ^= hash >>> 13;
        hash *= FINAL_AVALANCHING_MULTIPLIER2;
        hash ^= hash >>> 16;

        return hash;
    }
}