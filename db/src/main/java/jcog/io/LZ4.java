/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcog.io;


import org.apache.lucene.store.ByteArrayDataOutput;
import org.apache.lucene.util.packed.PackedInts;

import java.util.Arrays;

/**
 * LZ4 compression and decompression routines.
 * from Apache Lucene but modified experimentally
 * TODO add unit test
 * <p>
 * http://code.google.com/p/lz4/
 * http://fastcompression.blogspot.fr/p/lz4.html
 */
public enum LZ4 { ;


    static final int MEMORY_USAGE = 10;
//    static final int MIN_MATCH = 4; // minimum length of a match
//    static final int LAST_LITERALS = 5; // the last 5 bytes must be encoded as literals
    static final int MIN_MATCH = 3; // minimum length of a match, seem must be >=3
    static final int LAST_LITERALS = 1; // the last N bytes encoded as literals

    static final int MAX_DISTANCE = 1 << 7; // maximum distance of a reference (stored as byte so < 256 or possibly 128)
    static final int HASH_LOG_HC = 8; // log size of the dictionary for compressHC
    static final int HASH_TABLE_SIZE_HC = 1 << HASH_LOG_HC;
    static final int OPTIMAL_ML = // match length that doesn't require an additional byte
        MIN_MATCH;
        //0x0F + 4 - 1;


    private static int hash(int i, int hashBits) {
        return (i * -1640531535) >>> (32 - hashBits);
    }

    private static int hashHC(int i) {
        return hash(i, HASH_LOG_HC);
    }

    private static int readInt(byte[] buf, int i) {
        return ((buf[i] & 0xFF) << 24) | ((buf[i + 1] & 0xFF) << 16) | ((buf[i + 2] & 0xFF) << 8) | (buf[i + 3] & 0xFF);
    }

    private static boolean readIntEquals(byte[] buf, int i, int j) {
        return i == j || readInt(buf, i) == readInt(buf, j);
    }

    private static int commonBytes(byte[] b, int o1, int o2, int limit) {
        assert o1 < o2;
        // never -1 because lengths always differ
        //return FutureArrays.mismatch(b, o1, limit, b, o2, limit);
        return Arrays.mismatch(b, o1, limit, b, o2, limit);
    }

    private static int commonBytesBackward(byte[] b, int o1, int o2, int l1, int l2) {
        int count = 0;
        while (o1 > l1 && o2 > l2 && b[--o1] == b[--o2]) {
            ++count;
        }
        return count;
    }

//    /**
//     * Decompress at least <code>decompressedLen</code> bytes into
//     * <code>dest[dOff:]</code>. Please note that <code>dest</code> must be large
//     * enough to be able to hold <b>all</b> decompressed data (meaning that you
//     * need to know the total decompressed length).
//     */
//    public static int decompress(DataInput compressed, int decompressedLen, byte[] dest, int dOff) throws IOException {
//        int destEnd = dest.length;
//
//        do {
//            // literals
//            int token = compressed.readByte() & 0xFF;
//            int literalLen = token >>> 4;
//
//            if (literalLen != 0) {
//                if (literalLen == 0x0F) {
//                    byte len;
//                    while ((len = compressed.readByte()) == (byte) 0xFF) {
//                        literalLen += 0xFF;
//                    }
//                    literalLen += len & 0xFF;
//                }
//                compressed.readBytes(dest, dOff, literalLen);
//                dOff += literalLen;
//            }
//
//            if (dOff >= decompressedLen) {
//                break;
//            }
//
//            // matchs
//            int matchDec = (compressed.readByte() & 0xFF); // | ((compressed.readByte() & 0xFF) << 8);
//            assert matchDec > 0;
//
//            int matchLen = token & 0x0F;
//            if (matchLen == 0x0F) {
//                int len;
//                while ((len = compressed.readByte()) == (byte) 0xFF) {
//                    matchLen += 0xFF;
//                }
//                matchLen += len & 0xFF;
//            }
//            matchLen += MIN_MATCH;
//
//            // copying a multiple of 8 bytes can make decompression from 5% to 10% faster
//            int fastLen = (matchLen + 7) & 0xFFFFFFF8;
//            if (matchDec < matchLen || dOff + fastLen > destEnd) {
//                // overlap -> naive incremental copy
//                for (int ref = dOff - matchDec, end = dOff + matchLen; dOff < end; ++ref, ++dOff) {
//                    dest[dOff] = dest[ref];
//                }
//            } else {
//                // no overlap -> arraycopy
//                System.arraycopy(dest, dOff - matchDec, dest, dOff, fastLen);
//                dOff += matchLen;
//            }
//        } while (dOff < decompressedLen);
//
//        return dOff;
//    }

    private static void encodeLen(int l, ByteArrayDataOutput out) {
        while (l >= 0xFF) {
            out.writeByte((byte) 0xFF);
            l -= 0xFF;
        }
        out.writeByte((byte) l);
    }

    private static void encodeLiterals(byte[] bytes, int token, int anchor, int literalLen, ByteArrayDataOutput out) {
        out.writeByte((byte) token);

        // encode literal length
        if (literalLen >= 0x0F) {
            encodeLen(literalLen - 0x0F, out);
        }

        // encode literals
        out.writeBytes(bytes, anchor, literalLen);
    }

    private static void encodeLastLiterals(byte[] bytes, int anchor, int literalLen, ByteArrayDataOutput out) {
        int token = Math.min(literalLen, 0x0F) << 4;
        encodeLiterals(bytes, token, anchor, literalLen, out);
    }

    private static void encodeSequence(byte[] bytes, int anchor, int matchRef, int matchOff, int matchLen, ByteArrayDataOutput out) {
        int literalLen = matchOff - anchor;
//        assert matchLen >= MIN_MATCH;
        // encode token
        int token = (Math.min(literalLen, 0x0F) << 4) | Math.min(matchLen - MIN_MATCH, 0x0F);
        encodeLiterals(bytes, token, anchor, literalLen, out);

        // encode match dec
        int matchDec = matchOff - matchRef;
        assert matchDec > 0 && matchDec < 1 << 16;
        out.writeByte((byte) matchDec);
//        out.writeByte((byte) (matchDec >>> 8));

        // encode match len
        if (matchLen >= MIN_MATCH + 0x0F) {
            encodeLen(matchLen - 0x0F - MIN_MATCH, out);
        }
    }

    public static final class LZ4Table {
        private int hashLog;
        private PackedInts.Mutable hashTable;

        void reset(int len) {
            int bitsPerOffset = PackedInts.bitsRequired(len - LAST_LITERALS);
            int bitsPerOffsetLog = 32 - Integer.numberOfLeadingZeros(bitsPerOffset - 1);
            hashLog = MEMORY_USAGE + 3 - bitsPerOffsetLog;
            if (hashTable == null || hashTable.size() < 1 << hashLog || hashTable.getBitsPerValue() < bitsPerOffset) {
                hashTable = PackedInts.getMutable(1 << hashLog, bitsPerOffset, PackedInts.DEFAULT);
            } else {
                hashTable.clear();
            }
        }

    }

    /**
     * Compress <code>bytes[off:off+len]</code> into <code>out</code> using
     * at most 16KB of memory. <code>ht</code> shouldn't be shared across threads
     * but can safely be reused.
     */
    public static void compress(byte[] bytes, int off, int len, ByteArrayDataOutput out, LZ4Table ht) {

        int base = off;
        int end = off + len;

        int anchor = off++;

        if (len > LAST_LITERALS + MIN_MATCH) {

            ht.reset(len);
            int hashLog = ht.hashLog;
            PackedInts.Mutable hashTable = ht.hashTable;

            int limit = end - LAST_LITERALS;
            int matchLimit = limit - MIN_MATCH;
            main:
            while (off <= limit) {
                // find a match
                int ref;
                while (true) {
                    if (off >= matchLimit) {
                        break main;
                    }
                    int v = readInt(bytes, off);
                    int h = hash(v, hashLog);
                    ref = base + (int) hashTable.get(h);
                    //assert PackedInts.bitsRequired(off - base) <= hashTable.getBitsPerValue();
                    hashTable.set(h, off - base);
                    if (off - ref < MAX_DISTANCE && readInt(bytes, ref) == v) {
                        break;
                    }
                    ++off;
                }

                // compute match length
                int matchLen = MIN_MATCH + commonBytes(bytes, ref + MIN_MATCH, off + MIN_MATCH, limit);

                encodeSequence(bytes, anchor, ref, off, matchLen, out);
                off += matchLen;
                anchor = off;
            }
        }

        // last literals
        int literalLen = end - anchor;
        assert literalLen >= LAST_LITERALS || literalLen == len;
        encodeLastLiterals(bytes, anchor, end - anchor, out);
    }

    private static final class Match {
        int start;
        int ref;
        int len;

        void fix(int correction) {
            start += correction;
            ref += correction;
            len -= correction;
        }

        int end() {
            return start + len;
        }
    }

    private static void copyTo(Match m1, Match m2) {
        m2.len = m1.len;
        m2.start = m1.start;
        m2.ref = m1.ref;
    }

    public static final class LZ4HCTable {
        static final int MAX_ATTEMPTS = 256;
        static final int MASK = MAX_DISTANCE - 1;
        int nextToUpdate;
        private int base;
        private final int[] hashTable;
        private final short[] chainTable;

        public LZ4HCTable() {
            hashTable = new int[HASH_TABLE_SIZE_HC];
            chainTable = new short[MAX_DISTANCE];
        }

        private void reset(int base) {
            this.base = base;
            nextToUpdate = base;
            Arrays.fill(hashTable, -1);
            Arrays.fill(chainTable, (short) 0);
        }

        private int hashPointer(byte[] bytes, int off) {
            int v = readInt(bytes, off);
            int h = hashHC(v);
            return hashTable[h];
        }

        private int next(int off) {
            return off - (chainTable[off & MASK] & 0xFFFF);
        }

        private void addHash(byte[] bytes, int off) {
            int v = readInt(bytes, off);
            int h = hashHC(v);
            int delta = off - hashTable[h];
            assert delta > 0 : delta;
            if (delta >= MAX_DISTANCE) {
                delta = MAX_DISTANCE - 1;
            }
            chainTable[off & MASK] = (short) delta;
            hashTable[h] = off;
        }

        void insert(int off, byte[] bytes) {
            for (; nextToUpdate < off; ++nextToUpdate) {
                addHash(bytes, nextToUpdate);
            }
        }

        boolean insertAndFindBestMatch(byte[] buf, int off, int matchLimit, Match match) {
            match.start = off;
            match.len = 0;

            insert(off, buf);

            int ref = hashPointer(buf, off);

            int repl = 0;
            int delta = 0;
            if (ref >= off - 4 && ref <= off && ref >= base) { // potential repetition
                if (readIntEquals(buf, ref, off)) { // confirmed
                    delta = off - ref;
                    repl = match.len = MIN_MATCH + commonBytes(buf, ref + MIN_MATCH, off + MIN_MATCH, matchLimit);
                    match.ref = ref;
                }
                ref = next(ref);
            }

            for (int i = 0; i < MAX_ATTEMPTS; ++i) {
                if (ref < Math.max(base, off - MAX_DISTANCE + 1) || ref > off) {
                    break;
                }
                if (buf[ref + match.len] == buf[off + match.len] && readIntEquals(buf, ref, off)) {
                    int matchLen = MIN_MATCH + commonBytes(buf, ref + MIN_MATCH, off + MIN_MATCH, matchLimit);
                    if (matchLen > match.len) {
                        match.ref = ref;
                        match.len = matchLen;
                    }
                }
                ref = next(ref);
            }

            if (repl != 0) {
                int ptr = off;
                int end = off + repl - (MIN_MATCH - 1);
                while (ptr < end - delta) {
                    chainTable[ptr & MASK] = (short) delta; // pre load
                    ++ptr;
                }
                do {
                    chainTable[ptr & MASK] = (short) delta;
                    hashTable[hashHC(readInt(buf, ptr))] = ptr;
                    ++ptr;
                } while (ptr < end);
                nextToUpdate = end;
            }

            return match.len != 0;
        }

        boolean insertAndFindWiderMatch(byte[] buf, int off, int startLimit, int matchLimit, int minLen, Match match) {
            match.len = minLen;

            insert(off, buf);

            int delta = off - startLimit;
            int ref = hashPointer(buf, off);
            for (int i = 0; i < MAX_ATTEMPTS; ++i) {
                if (ref < Math.max(base, off - MAX_DISTANCE + 1) || ref > off) {
                    break;
                }
                if (buf[ref - delta + match.len] == buf[startLimit + match.len]
                        && readIntEquals(buf, ref, off)) {
                    int matchLenForward = MIN_MATCH + commonBytes(buf, ref + MIN_MATCH, off + MIN_MATCH, matchLimit);
                    int matchLenBackward = commonBytesBackward(buf, ref, off, base, startLimit);
                    int matchLen = matchLenBackward + matchLenForward;
                    if (matchLen > match.len) {
                        match.len = matchLen;
                        match.ref = ref - matchLenBackward;
                        match.start = off - matchLenBackward;
                    }
                }
                ref = next(ref);
            }

            return match.len > minLen;
        }

    }

    /**
     * Compress <code>bytes[off:off+len]</code> into <code>out</code>. Compared to
     * {@link LZ4#compress(byte[], int, int, ByteArrayDataOutput, LZ4Table)}, this method
     * is slower and uses more memory (~ 256KB per thread) but should provide
     * better compression ratios (especially on large inputs) because it chooses
     * the best match among up to 256 candidates and then performs trade-offs to
     * fix overlapping matches. <code>ht</code> shouldn't be shared across threads
     * but can safely be reused.
     */
    public static void compressHC(byte[] src, int srcOff, int srcLen, ByteArrayDataOutput out, LZ4HCTable ht) {

        int srcEnd = srcOff + srcLen;

        int sOff = srcOff;
        int anchor = sOff++;

        ht.reset(srcOff);
        Match match0 = new Match();
        Match match1 = new Match();
        Match match2 = new Match();
        Match match3 = new Match();

        int matchLimit = srcEnd - LAST_LITERALS;
        int mfLimit = matchLimit - MIN_MATCH;
        main:
        while (sOff <= mfLimit) {
            if (!ht.insertAndFindBestMatch(src, sOff, matchLimit, match1)) {
                ++sOff;
                continue;
            }

            // saved, in case we would skip too much
            copyTo(match1, match0);

            search2:
            while (true) {
                assert match1.start >= anchor;
                if (match1.end() >= mfLimit
                        || !ht.insertAndFindWiderMatch(src, match1.end() - 2, match1.start + 1, matchLimit, match1.len, match2)) {
                    // no better match
                    encodeSequence(src, anchor, match1.ref, match1.start, match1.len, out);
                    anchor = sOff = match1.end();
                    continue main;
                }

                if (match0.start < match1.start) {
                    if (match2.start < match1.start + match0.len) { // empirical
                        copyTo(match0, match1);
                    }
                }
                assert match2.start > match1.start;

                if (match2.start - match1.start < 3) { // First Match too small : removed
                    copyTo(match2, match1);
                    continue search2;
                }

                search3:
                while (true) {
                    if (match2.start - match1.start < OPTIMAL_ML) {
                        int newMatchLen = match1.len;
                        if (newMatchLen > OPTIMAL_ML) {
                            newMatchLen = OPTIMAL_ML;
                        }
                        if (match1.start + newMatchLen > match2.end() - MIN_MATCH) {
                            newMatchLen = match2.start - match1.start + match2.len - MIN_MATCH;
                        }
                        int correction = newMatchLen - (match2.start - match1.start);
                        if (correction > 0) {
                            match2.fix(correction);
                        }
                    }

                    if (match2.start + match2.len >= mfLimit
                            || !ht.insertAndFindWiderMatch(src, match2.end() - 3, match2.start, matchLimit, match2.len, match3)) {
                        // no better match -> 2 sequences to encode
                        if (match2.start < match1.end()) {
                            match1.len = match2.start - match1.start;
                        }
                        // encode seq 1
                        encodeSequence(src, anchor, match1.ref, match1.start, match1.len, out);
                        anchor = /*sOff = */match1.end();
                        // encode seq 2
                        encodeSequence(src, anchor, match2.ref, match2.start, match2.len, out);
                        anchor = sOff = match2.end();
                        continue main;
                    }

                    if (match3.start < match1.end() + 3) { // Not enough space for match 2 : remove it
                        if (match3.start >= match1.end()) { // // can write Seq1 immediately ==> Seq2 is removed, so Seq3 becomes Seq1
                            if (match2.start < match1.end()) {
                                int correction = match1.end() - match2.start;
                                match2.fix(correction);
                                if (match2.len < MIN_MATCH) {
                                    copyTo(match3, match2);
                                }
                            }

                            encodeSequence(src, anchor, match1.ref, match1.start, match1.len, out);
                            anchor = /*sOff = */match1.end();

                            copyTo(match3, match1);
                            copyTo(match2, match0);

                            continue search2;
                        }

                        copyTo(match3, match2);
                        continue search3;
                    }

                    // OK, now we have 3 ascending matches; let's write at least the first one
                    if (match2.start < match1.end()) {
                        if (match2.start - match1.start < 0x0F) {
                            if (match1.len > OPTIMAL_ML) {
                                match1.len = OPTIMAL_ML;
                            }
                            if (match1.end() > match2.end() - MIN_MATCH) {
                                match1.len = match2.end() - match1.start - MIN_MATCH;
                            }
                            int correction = match1.end() - match2.start;
                            match2.fix(correction);
                        } else {
                            match1.len = match2.start - match1.start;
                        }
                    }

                    encodeSequence(src, anchor, match1.ref, match1.start, match1.len, out);
                    anchor = /*sOff = */match1.end();

                    copyTo(match2, match1);
                    copyTo(match3, match2);

                    //continue search3;
                }

            }

        }

        encodeLastLiterals(src, anchor, srcEnd - anchor, out);
    }

}
