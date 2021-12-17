package jcog.io;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;
import jcog.util.ArrayUtil;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * CompressHuffman is a library to compress isolated records of data in a DB using a shared huffman tree generated from all the records.
 * This library was created because compressing a single record in a DB using DEFLATE or other methods produces poor compression (5-10%) due to
 * high entropy of individual records (due to small size). Compress huffman exploits the low entropy of the entire dataset to produce a huffman tree
 * that can compress most individual records with 30-70% compression (depending on data).
 * Using the VM option -XX:+UseCompressedOops can speed things up by about 10% as long as your heap is <32GB
 * <p>
 * This library is free to use and is under the apache 2.0 licence, available @ https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Useage: feed you'r dataset (byte[record][recordData] for in memory datset or Iterable<byte[recordData]> for retreival from DB) to the Constructor
 * You can then use compress(byte[]) decompress(byte[])  after the HuffmanTree has been generated.
 * <p>
 * To Store the HuffmanTree for future use without having the generate it again use getHuffData() and store it in a file
 * Read and feed the byte array to new CompressHuffman(byte[]) at a future data then use can use  compress(byte[]) decompress(byte[])
 * <p>
 * For more info on how the library works and to report bugs visit  https://github.com/MPdaedalus/CompressHuffman
 *
 * @author daedalus
 * @version 1.1
 * from: https://github.com/MPdaedalus/CompressHuffman
 */

public class Huffman {
    //	public long eightyPCMemory;
    private static final String[] charsByFreq = {" ", "e", "t", "a", "o", "i", "n", "s", "r", "h", "l", "d", "c", "u", "m", "f", "p", "g", "w", "y", "b", "v", "k", "x", "j", "q", "z", "E", "T", "A", "O", "I", "N", "S", "R", "H", "L", "D", "C", "U", "M", "F", "P", "G", "W", "Y", "B", "V", "K", "X", "J", "Q", "Z", ",", ".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "'", "\"", ";", ")", "(", ":", "!", "?", "/", "&", "-", "%", "@", "$", "_", "\\", "*", "=", "[", "]", "+", ">", "<", "^", "`", "|", "~", "{", "}", "¢", "£", "©", "®", "°", "±", "²", "³", "µ", "¼", "½", "¾", "÷"};
    //hash stuff
    private static final long PRIME64_1 = -7046029288634856825L; //11400714785074694791
    private static final long PRIME64_2 = -4417276706812531889L; //14029467366897019727
    private static final long PRIME64_3 = 1609587929392839161L;
    private static final long PRIME64_4 = -8796714831421723037L; //9650029242287828579
    private static final long PRIME64_5 = 2870177450012600261L;
    private final Weigher<ByteAry, Integer> memoryUsageWeigher = (key, value) -> key.ary.length + 32;
    private int nodeId;
    private int symbl2CodLstIdx;
    private int maxSymbolLength;
    private int defsymbl2CodLstIdx;
    private int altCodeIdx;
    private int altCodeBytes;
    private int highestLevel;
    private int totalSymbols;
    private byte[][] codeValues;
    private byte[][] symbol2Code;
    private byte[][] defsymbol2Code;
    private byte[][] defCodeValues;
    private byte[][] tmpsymbol2Code;
    private byte[][] tmpCodeValues;
    private byte[][][] codeIdx2Symbols;
    private byte[][][] defCodeIdx2Symbols;
    private byte[][][] tmpCodeIdx2Symbols;
    private boolean useAltOnly;
    private HuffConfig config;
    //	String symbolFile;
    private PriorityQueue<Weight> trees;
    private Cache<ByteAry, Integer> freqList;

    /**
     * Construct Huffman Tree using maxSymbol Size of 9, max number of symbols 100000, max Tree depth of 20
     * These settings are a good tradeoff between comp/decompress speed and compression level
     * Est. Decompression speed 40-45MB/Sec per core , Est. Compression 55%
     * Est. HuffTree build speed=2-3MB/Sec (of dataSet) on quad core CPU,  Est. Final HuffData size=1-2MB
     *
     * @param data The data Set
     */
    public Huffman(Stream<byte[]> data) {
        this(data, config(9, 100000, Runtime.getRuntime().freeMemory() / 2 < 2000000000 ? Runtime.getRuntime().freeMemory() / 2 : 2000000000, false, 20));
    }

    /**
     * @param data The data Set
     * @param hc   Config File (see config() method docs)
     */
    public Huffman(Stream<byte[]> data, HuffConfig hc) {
        code(data, hc);
    }

    /**
     * Construct Huffman Tree using maxSymbol Size of 9, max number of symbols 100000, max Tree depth of 20
     * These settings are a good tradeoff between comp/decompress speed and compression level
     * Est. Decompression speed 40-45MB/Sec per core , Est. Compression 55%
     * Est. HuffTree build speed=2-3MB/Sec (of dataSet) on quad core CPU, Est. Final HuffData size=1-2MB
     *
     * @param data The data Set
     */
    public Huffman(Iterable<byte[]> data) {
        this(data, config(9, 100000, Runtime.getRuntime().freeMemory() / 2 < 2000000000 ? Runtime.getRuntime().freeMemory() / 2 : 2000000000, false, 20));
    }

    /**
     * @param data The data Set
     * @param hc   Config File (see config() method docs)
     */
    public Huffman(Iterable<byte[]> data, HuffConfig hc) {
        this(StreamSupport.stream(data.spliterator(), false), hc);
    }

    /**
     * @param in Stored HuffData from getHuffData()
     */
    public Huffman(byte[] in) {
        Inflater inflater = new Inflater();
        inflater.setInput(in);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(in.length * 2);
        int count;
        try {
            byte[] buffer = new byte[8192];
            while (!inflater.finished()) {
                count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            in = outputStream.toByteArray();
        } catch (Exception e) {
            System.err.println("ERROR: unzipping CompressHuffman data failed!");
            e.printStackTrace();
        }
        totalSymbols = byteArrayToInt(in, 0);
        symbl2CodLstIdx = byteArrayToInt(in, 4);
        maxSymbolLength = byteArrayToInt(in, 8);
        altCodeIdx = byteArrayToInt(in, 12);
        useAltOnly = in[16] == 1;
        highestLevel = byteArrayToInt(in, 17);
        altCodeBytes = byteArrayToInt(in, 21);
        codeIdx2Symbols = new byte[byteArrayToInt(in, 25)][][];
        symbol2Code = new byte[symbl2CodLstIdx + 1][];
        codeValues = new byte[symbol2Code.length][];
        int inIdx = 29;
        for (int i = 1; i < codeIdx2Symbols.length; i++) {
            count = byteArrayToInt(in, inIdx);
            inIdx += 4;
            codeIdx2Symbols[i] = new byte[1 << i][];
            for (int w = 0; w < count; w++) {
                byte[] symbol = new byte[in[inIdx++]];
                inIdx = bytesFromByteArray(in, symbol, inIdx);
                int codeIdx = byteArrayToInt(in, inIdx);
                inIdx += 4;
                codeIdx2Symbols[i][codeIdx] = symbol;
                int hashIdx = byteArrayToInt(in, inIdx);
                inIdx += 4;
                symbol2Code[hashIdx] = symbol;
                codeValues[hashIdx] = createCode((byte) i, codeIdx);
            }
        }
        switchFields(true);
        trees = new PriorityQueue<>();
        buildAltTree(false);
    }

    private static int aryHash(byte[] ary) {
        if (ary.length == 0) return 1;
        return longHash(hash(ary, 0, ary.length, -1640531527));
    }

    /**
     * Use max symbolLength of 8, max symbols 10000
     * Est. Decompression speed 50-55MB/Sec per core , Est. Compression 45%
     * This Setting also produces the lowest memory usage for the Compressor/Decompressor as well as shortest huffTree build time
     * Est. HuffTree build speed=3MB/Sec (of dataSet) on quad core CPU, ,  Est. Final HuffData size=100KB-1MB
     */
    public static HuffConfig fastestCompDecompTime() {
        return config(8, 10000, 2000000000, false, 20);
    }

    /**
     * Use max symbolLength of 10, max symbols 1 million
     * Est. Decompression speed 30-35MB/Sec per core , Est. Compression 60%
     * This Setting also produces the highest memory usage and longest comp/decompress time for the Compressor/Decompressor
     * as well as longest huffTree build time
     * Est. HuffTree build speed=2MB/Sec (of dataSet) on quad core CPU,  Est. Final HuffData size=10-20MB
     */
    public static HuffConfig smallestFileSize() {
        return config(10, 1000000, 2000000000, false, 26);
    }

    /**
     * With maxSymbolLength and maxSymbols higher values may give better compression at expense of longer compress/decompress time
     * and much longer (many times) hufftree generation time.
     *
     * @param maxSymbolLength    default=9,  Diminishing returns beyond 8.
     * @param maxSymbols         default=100000, Diminishing returns beyond 1 million
     * @param maxSymbolCacheSize default=2GB, will use half of free memory if less than 2GB available. Bigger symbol cache does not always improve compression beyond 2GB
     * @param twoPass            default=false, whether to generate dummy hufftree and do dummy compress to eliminate unused symbols, improves compression by a few %
     *                           but hufftree generation takes twice as long or more, has little effect to comp/decomp time
     * @param maxTreeDepth       default=20, shorter treeDepth reduces memory needed for huffman Tree but will reduce compression
     *                           Do not go below indexOf(highest set bit(2 x number of records)) or above 31, or you may get error.
     */
    private static HuffConfig config(int maxSymbolLength, int maxSymbols, long maxSymbolCacheSize, boolean twoPass, int maxTreeDepth) {
        HuffConfig hc = new HuffConfig();
        hc.maxSymbolLength = maxSymbolLength;
        hc.maxSymbols = maxSymbols;
        hc.maxSymbolCacheSize = maxSymbolCacheSize;
        hc.twoPass = twoPass;
        hc.maxTreeDepth = maxTreeDepth;
        return hc;
    }

    private static byte[] addSymbolToCodes(byte[] codes, int codesIdx, byte[] aCode) {
        int numCodeBts = aCode[0];
        int bitCount = 0;
        int codeByteIdx = 1;
        for (int q = 0; q < numCodeBts; q++) {
            if (((aCode[codeByteIdx] >> bitCount++) & 1) == 1) codes[codesIdx / 8] |= 1 << (codesIdx & 7);
            codesIdx++;
            if (bitCount == 8) {
                bitCount = 0;
                codeByteIdx++;
            }
        }
        return codes;
    }

    // (this method takes up majority of compression time)
    private static int findSymblIdx(int startIdx, int endIdx, byte[] data, int symbLastIdx, byte[][] symKeySet) {
        int hash = 1;
        int curMatchIdx = -1;
        for (int i = startIdx; i < endIdx; i++) {
            hash = longHash(hash(data, startIdx, (i - startIdx) + 1, -1640531527));
            int symbolIdx = hash & symbLastIdx;
            byte[] aKey;
            probe:
            while ((aKey = symKeySet[symbolIdx]) != null) {
                int tmpIdx = symbolIdx;
                if (++symbolIdx == symKeySet.length) symbolIdx = 0;
                if (aKey.length == (i - startIdx) + 1) {
                    for (int w = 0; w < aKey.length; w++)
                        if (startIdx + w == endIdx || aKey[w] != data[startIdx + w]) continue probe;
                } else {
                    continue;
                }
                curMatchIdx = tmpIdx;
            }
        }
        return curMatchIdx;
    }

    private static int nextPO2(int v) {
        v--;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v++;
        return v;
    }

    public static int numBitsForNumber(long x) {
        return 63 - Long.numberOfLeadingZeros(x);
    }

    private static byte[] expand(byte[] ary, int nextCodeLen) {
        byte[] newAry = new byte[(ary.length * 2) + nextCodeLen];
        System.arraycopy(ary, 0, newAry, 0, ary.length);
        return newAry;
    }

    private static byte[] createCode(byte numBits, int index) {
        byte[] out = new byte[((numBits + 7) / 8) + 1];
        out[0] = numBits;
        int bits = 0;
        for (int i = 1; i < out.length; i++) {
            out[i] = (byte) (index >>> bits);
            bits += 8;
        }
        return out;
    }

    private static int byteArrayToInt(byte[] b, int startIdx) {
        return (b[startIdx] << 24) + ((b[startIdx + 1] & 0xFF) << 16) + ((b[startIdx + 2] & 0xFF) << 8) + (b[startIdx + 3] & 0xFF);
    }

    private static int intToByteArray(int value, byte[] array, int startIdx) {
        array[startIdx] = (byte) (value >>> 24);
        array[startIdx + 1] = (byte) (value >>> 16);
        array[startIdx + 2] = (byte) (value >>> 8);
        array[startIdx + 3] = (byte) value;
        return startIdx + 4;
    }

    private static int bytesToByteArray(byte[] values, byte[] array, int startIdx) {
        System.arraycopy(values, 0, array, startIdx, values.length);
        return startIdx + values.length;
    }

    private static int bytesFromByteArray(byte[] src, byte[] dest, int idx) {
        System.arraycopy(src, idx, dest, 0, dest.length);
        return idx + dest.length;
    }

    /**
     * <p>
     * Calculates XXHash64 from given {@code byte[]} buffer.
     * </p><p>
     * This code comes from <a href="https://github.com/jpountz/lz4-java">LZ4-Java</a> created
     * by Adrien Grand.
     * </p>
     *
     * @param buf  to calculate hash from
     * @param off  offset to start calculation from
     * @param len  length of data to calculate hash
     * @param seed hash seed
     * @return XXHash.
     */
    private static long hash(byte[] buf, int off, int len, long seed) {
        if (len < 0) {
            throw new IllegalArgumentException("lengths must be >= 0");
        }
        if (off < 0 || off >= buf.length || off + len < 0 || off + len > buf.length) {
            throw new IndexOutOfBoundsException();
        }

        int end = off + len;
        long h64;

        if (len >= 32) {
            int limit = end - 32;
            long v1 = seed + PRIME64_1 + PRIME64_2;
            long v2 = seed + PRIME64_2;
            long v3 = seed;
            long v4 = seed - PRIME64_1;
            do {
                v1 += readLongLE(buf, off) * PRIME64_2;
                v1 = Long.rotateLeft(v1, 31);
                v1 *= PRIME64_1;
                off += 8;

                v2 += readLongLE(buf, off) * PRIME64_2;
                v2 = Long.rotateLeft(v2, 31);
                v2 *= PRIME64_1;
                off += 8;

                v3 += readLongLE(buf, off) * PRIME64_2;
                v3 = Long.rotateLeft(v3, 31);
                v3 *= PRIME64_1;
                off += 8;

                v4 += readLongLE(buf, off) * PRIME64_2;
                v4 = Long.rotateLeft(v4, 31);
                v4 *= PRIME64_1;
                off += 8;
            } while (off <= limit);

            h64 = Long.rotateLeft(v1, 1) + Long.rotateLeft(v2, 7) + Long.rotateLeft(v3, 12) + Long.rotateLeft(v4, 18);

            v1 *= PRIME64_2;
            v1 = Long.rotateLeft(v1, 31);
            v1 *= PRIME64_1;
            h64 ^= v1;
            h64 = h64 * PRIME64_1 + PRIME64_4;

            v2 *= PRIME64_2;
            v2 = Long.rotateLeft(v2, 31);
            v2 *= PRIME64_1;
            h64 ^= v2;
            h64 = h64 * PRIME64_1 + PRIME64_4;

            v3 *= PRIME64_2;
            v3 = Long.rotateLeft(v3, 31);
            v3 *= PRIME64_1;
            h64 ^= v3;
            h64 = h64 * PRIME64_1 + PRIME64_4;

            v4 *= PRIME64_2;
            v4 = Long.rotateLeft(v4, 31);
            v4 *= PRIME64_1;
            h64 ^= v4;
            h64 = h64 * PRIME64_1 + PRIME64_4;
        } else {
            h64 = seed + PRIME64_5;
        }

        h64 += len;

        while (off <= end - 8) {
            long k1 = readLongLE(buf, off);
            k1 *= PRIME64_2;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= PRIME64_1;
            h64 ^= k1;
            h64 = Long.rotateLeft(h64, 27) * PRIME64_1 + PRIME64_4;
            off += 8;
        }

        if (off <= end - 4) {
            h64 ^= (readIntLE(buf, off) & 0xFFFFFFFFL) * PRIME64_1;
            h64 = Long.rotateLeft(h64, 23) * PRIME64_2 + PRIME64_3;
            off += 4;
        }

        while (off < end) {
            h64 ^= (buf[off] & 0xFF) * PRIME64_5;
            h64 = Long.rotateLeft(h64, 11) * PRIME64_1;
            ++off;
        }

        h64 ^= h64 >>> 33;
        h64 *= PRIME64_2;
        h64 ^= h64 >>> 29;
        h64 *= PRIME64_3;
        h64 ^= h64 >>> 32;

        return h64;
    }

    private static long readLongLE(byte[] buf, int i) {
        return (buf[i] & 0xFFL) | ((buf[i + 1] & 0xFFL) << 8) | ((buf[i + 2] & 0xFFL) << 16) | ((buf[i + 3] & 0xFFL) << 24)
                | ((buf[i + 4] & 0xFFL) << 32) | ((buf[i + 5] & 0xFFL) << 40) | ((buf[i + 6] & 0xFFL) << 48) | ((buf[i + 7] & 0xFFL) << 56);
    }

    private static int readIntLE(byte[] buf, int i) {
        return (buf[i] & 0xFF) | ((buf[i + 1] & 0xFF) << 8) | ((buf[i + 2] & 0xFF) << 16) | ((buf[i + 3] & 0xFF) << 24);
    }

    private static int longHash(long h) {
        //$DELAY$
        h *= -7046029254386353131L;
        h ^= h >> 32;
        return (int) (h ^ h >> 16);
    }

    //Note: allocating big dataBuffer rather then checking and resizing buffer is faster as long as buffer limit not reached (will cause outofbounds  error)
    public byte[] decompress(byte[] codes) {
        if (codes.length == 0) return ArrayUtil.EMPTY_BYTE_ARRAY;
        byte[] data = new byte[codes.length * 20];
        int dataIdx = 0;
        byte unCompSymb = 0;
        int codeIdx = 3;
        int symbolIdx = 0;
        int codesLen = (codes.length * 8) - (byte) (codes[0] & 0b111);
        while (codeIdx < codesLen) {
            symbolIdx = 0;
            byte[] symbol = null;
            int bitLen = 0;
            //add codes to symbolIdx bit by bit until symbol is found, &7 == % 8, this loop takes up the majority of the total decompress time
            while (symbol == null && codeIdx < codesLen) {
                symbolIdx |= ((byte) ((codes[codeIdx / 8]) >>> (codeIdx++ & 7)) & (byte) 1) << bitLen;
                symbol = codeIdx2Symbols[++bitLen][symbolIdx];
            }
            if (symbol == null) break;
            int symbolLen = symbol.length;
            if (symbolLen == 0) {
                if (((codes[codeIdx / 8] >> (codeIdx & 7)) & 1) == 1) {
                    unCompSymb = 0;
                    for (int i = 0; i < 8; i++) {
                        codeIdx++;
                        if (((codes[codeIdx / 8] >> (codeIdx & 7)) & 1) == 1) unCompSymb |= 1 << i;
                    }
                    //if(dataIdx+1 >=data.length) data = expand(data,1);
                    data[dataIdx++] = unCompSymb;
                    codeIdx++;
                } else {
                    codeIdx++;
                    symbolIdx = 0;
                    bitLen = 0;
                    symbol = null;
                    //add codes to symbolIdx bit by bit until symbol is found
                    while (symbol == null && codeIdx < codesLen) {
                        symbolIdx |= ((byte) ((codes[codeIdx / 8]) >>> (codeIdx++ & 7)) & (byte) 1) << bitLen;
                        symbol = defCodeIdx2Symbols[++bitLen][symbolIdx];
                    }
                    if (symbol == null) {
                        while ((symbol = defCodeIdx2Symbols[bitLen++][symbolIdx = (symbolIdx << 1)]) == null) ;
                        symbolLen = symbol.length;
                        //if(dataIdx + symbolLen >= data.length) data = expand(data,symbolLen);
                        System.arraycopy(symbol, 0, data, dataIdx, symbolLen);
                        dataIdx += symbolLen;
                        continue;
                    }
                    symbolLen = symbol.length;
                    //if(dataIdx + symbolLen >= data.length) data = expand(data,symbolLen);
                    System.arraycopy(symbol, 0, data, dataIdx, symbolLen);
                    dataIdx += symbolLen;
                }
            } else {
                System.arraycopy(symbol, 0, data, dataIdx, symbolLen);
                dataIdx += symbolLen;
            }
        }
        if (dataIdx != data.length) {
            byte[] finalData = Arrays.copyOfRange(data, 0, dataIdx);
            return finalData;
        }
        return data;
    }

    public byte[] compress(byte[] data) {
        if (data.length == 0) return ArrayUtil.EMPTY_BYTE_ARRAY;
        byte[] codes = new byte[data.length];
        int codesIdx = 3;
        int startIdx = 0;
        int endIdx = maxSymbolLength;
        int dataLen = data.length;
        int curMatchIdx = -1;
        while (true) {
            if (endIdx > dataLen) endIdx = dataLen;
            curMatchIdx = findSymblIdx(startIdx, endIdx, data, symbl2CodLstIdx, symbol2Code);
            if (curMatchIdx == -1) curMatchIdx = altCodeIdx;
            byte[] aCode = codeValues[curMatchIdx];
            if (curMatchIdx != altCodeIdx && (aCode[0] + 7) / 8 > symbol2Code[curMatchIdx].length + altCodeBytes) {
                aCode = codeValues[altCodeIdx];
                curMatchIdx = altCodeIdx;
            }
            if (((codesIdx + 7) / 8) + ((aCode[0] + 7) / 8) > codes.length) codes = expand(codes, (aCode[0] + 7) / 8);
            codes = addSymbolToCodes(codes, codesIdx, aCode);
            codesIdx += aCode[0];
            //if symbol not in main tree consult default tree, if not in default tree then just add uncompressed
            if (curMatchIdx == altCodeIdx) {
                if (useAltOnly) {
                    if (((codesIdx + 16) / 8) > codes.length) codes = expand(codes, 2);
                    //set raw bit to true and add byte without compression
                    codes[codesIdx / 8] |= 1 << (codesIdx & 7);
                    codesIdx++;
                    for (int q = 0; q < 8; q++) {
                        if (((data[startIdx] >> q) & 1) == 1) codes[codesIdx / 8] |= 1 << (codesIdx & 7);
                        codesIdx++;
                    }
                    startIdx++;
                } else {
                    curMatchIdx = findSymblIdx(startIdx, endIdx, data, defsymbl2CodLstIdx, defsymbol2Code);
                    if (curMatchIdx != -1) {
                        codesIdx++;
                        aCode = defCodeValues[curMatchIdx];
                        if (((codesIdx + 7) / 8) + ((aCode[0] + 7) / 8) > codes.length)
                            codes = expand(codes, (aCode[0] + 7) / 8);
                        codes = addSymbolToCodes(codes, codesIdx, aCode);
                        codesIdx += aCode[0];
                        startIdx += defsymbol2Code[curMatchIdx].length;
                    } else {
                        if (((codesIdx + 16) / 8) > codes.length) codes = expand(codes, 2);
                        //set raw bit to true and add byte without compression
                        codes[codesIdx / 8] |= 1 << (codesIdx & 7);
                        codesIdx++;
                        for (int q = 0; q < 8; q++) {
                            if (((data[startIdx] >> q) & 1) == 1) codes[codesIdx / 8] |= 1 << (codesIdx & 7);
                            codesIdx++;
                        }
                        startIdx++;
                    }
                }
            } else {
                startIdx += symbol2Code[curMatchIdx].length;
            }
            if (startIdx >= data.length) break;
            endIdx = startIdx + maxSymbolLength;
        }
        int actualBytes = (codesIdx + 7) / 8;
        codes = Arrays.copyOf(codes, actualBytes);
        byte leftOverBits = (byte) ((actualBytes * 8) - codesIdx);
        codes[0] |= leftOverBits;
        return codes;
    }

    /**
     * generate codec
     */
    private void code(Stream<byte[]> data, HuffConfig hc) {
        //System.out.println("Compress Huffman: Building hufftree");
        config = hc;
        this.maxSymbolLength = Math.min(hc.maxSymbolLength, 30);
        freqList = Caffeine.newBuilder().maximumWeight(config.maxSymbolCacheSize).weigher(memoryUsageWeigher)
                //.initialCapacity(config.maxSymbolCacheSize > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) config.maxSymbolCacheSize)
                .build();
        useAltOnly = false;
        if (hc.twoPass) {
            //System.out.println("Compress Huffman: compiling symbol freq, Stage one of four");
            generateSymbolFreqs(data, false);
            //System.out.println("Compress Huffman: building dummy huffTree, Stage two of four");
            freqToTree(1, false);
            buildHuffTree(false, false);
            freqList.invalidateAll();
            //System.out.println("Compress Huffman: dummy compress to find unused symbols, Stage three of four");
            generateSymbolFreqs(data, true);
            //System.out.println("Compress Huffman: build final huffTree, Stage four of four");
            //System.out.println("Compress Huffman: Done building HuffTree! Use getHuffData() to store tree");
        } else {
            //System.out.println("Compress Huffman: compiling symbol freq, Stage one of two");
            generateSymbolFreqs(data, false);
            //System.out.println("Compress Huffman: building huffTree Stage two of two");
            //System.out.println("Compress Huffman: Done building HuffTree! Use getHuffData() to store tree");
        }
        freqToTree(1, true);
        buildHuffTree(false, true);
        altCodeBytes = (codeValues[1][0] + 7) / 8;
        switchFields(true);
        buildAltTree(false);
    }

    //convert freqList hashmap to priorty queue ordered by frequency (lowest first)
    private void freqToTree(int freqDivide, boolean addAltNode) {
        int maxSymbols = config.maxSymbols;
        trees = new PriorityQueue<>();
        Entry<ByteAry, Integer> ent;
        int removedBytes = 1;
        if (freqDivide < 2) {
            for (Entry<ByteAry, Integer> byteAryIntegerEntry: freqList.asMap().entrySet()) {
                ent = byteAryIntegerEntry;
                if (ent.getValue() > 3) {
                    trees.add(new TmpNode(ent.getValue() * ent.getKey().ary.length, ent.getKey().ary));
                } else {
                    removedBytes += ent.getValue() * ent.getKey().ary.length;
                }
            }
            while (trees.size() > maxSymbols) trees.poll();
            if (addAltNode) trees.add(new TmpNode(removedBytes, ArrayUtil.EMPTY_BYTE_ARRAY));
        } else {
            for (Entry<ByteAry, Integer> byteAryIntegerEntry: freqList.asMap().entrySet()) {
                ent = byteAryIntegerEntry;
                if (ent.getValue() > 3) {
                    int freq = (ent.getValue() / freqDivide) * ent.getKey().ary.length;
                    if (freq == 0) freq = 1;
                    trees.add(new TmpNode(freq, ent.getKey().ary));
                } else {
                    removedBytes += ent.getValue() * ent.getKey().ary.length;
                }
            }
            while (trees.size() > maxSymbols) trees.poll();
            if (addAltNode)
                trees.add(new TmpNode(removedBytes / freqDivide == 0 ? 1 : removedBytes / freqDivide, ArrayUtil.EMPTY_BYTE_ARRAY));
        }
    }

    private void buildAltTree(boolean useDefault) {
        if (useDefault || trees.isEmpty()) {
            trees = new PriorityQueue<>();
            for (int i = 0; i < charsByFreq.length; i++) {
                trees.add(new TmpNode((charsByFreq.length * 100) - (i * 90), charsByFreq[i].getBytes()));
            }
        }
        buildHuffTree(true, false);
        switchFields(false);
    }

    private void buildHuffTree(boolean isAlt, boolean isFinal) {
        // remove the two trees with least frequency then put them into a new node and insert into the queue
        totalSymbols = trees.size();
        //System.out.println("total symbols=" + totalSymbols);
        int maxTreeDepth;
		maxTreeDepth = isAlt ? 26 : Math.min(config.maxTreeDepth, 31);
        int freqDivide = 1;
        HuffmanTree objectTree;
        while (true) {
            nodeId = 0;
            int longTreeId = 0;
            while (trees.size() > 1) {
                HuffmanTree hf1 = nextTree();
                HuffmanTree hf2 = nextTree();
                HuffmanTree hn = new HuffmanNode(hf1, hf2, nextNodeID());
                trees.add(hn);
                if (longTreeId == 0 || hf1.id == longTreeId || hf2.id == longTreeId) {
                    longTreeId = hn.id;
                }
            }
            objectTree = (HuffmanTree) trees.poll();
            highestLevel = 0;
            findMaxDepth(((HuffmanNode) objectTree).left, 1);
            findMaxDepth(((HuffmanNode) objectTree).right, 1);
            if (highestLevel <= maxTreeDepth) break;
            freqDivide *= 2;
            freqToTree(freqDivide, true);
        }
        codeIdx2Symbols = new byte[highestLevel + 1][][];
        for (int i = 1; i < codeIdx2Symbols.length; i++) codeIdx2Symbols[i] = new byte[1 << i][];
        //make sure size power of 2 for  symbl2CodLstIdx & hash
        symbol2Code = new byte[(totalSymbols * 2 & (totalSymbols * 2 - 1)) == 0 ? totalSymbols * 2 : nextPO2(totalSymbols * 2)][];
        codeValues = new byte[symbol2Code.length][];
        symbl2CodLstIdx = symbol2Code.length - 1;
        populateLUTNCodes(((HuffmanNode) objectTree).left, new byte[]{1, 0});
        populateLUTNCodes(((HuffmanNode) objectTree).right, new byte[]{1, 1});
        if (!isAlt && isFinal || freqList == null) {
            int symbolIdx = (aryHash(ArrayUtil.EMPTY_BYTE_ARRAY) & symbl2CodLstIdx) - 1;
            if (symbolIdx + 1 == symbol2Code.length) symbolIdx = -1;
            byte[] aKey;
            while ((aKey = symbol2Code[++symbolIdx]) != null) {
                if (aKey.length == 0) {
                    altCodeIdx = symbolIdx;
                    break;
                }
            }
        }
    }

    private HuffmanTree nextTree() {
        HuffmanTree hf1;
        Weight w1 = trees.poll();
		hf1 = w1 instanceof TmpNode ? new HuffmanLeaf(w1.getWeight(), ((TmpNode) w1).key, nextNodeID()) : (HuffmanTree) w1;
        return hf1;
    }

    private int nextNodeID() {
        return nodeId += 2;
    }

    private void findMaxDepth(HuffmanTree objectTree, int level) {
        if (objectTree instanceof HuffmanNode) {
            findMaxDepth(((HuffmanNode) objectTree).left, level + 1);
            findMaxDepth(((HuffmanNode) objectTree).right, level + 1);
        } else {
            if (level > highestLevel) highestLevel = level;
        }
    }

    //curCode first byte= num of bits used so far
    private void populateLUTNCodes(HuffmanTree objectTree, byte[] curCode) {
        if (objectTree instanceof HuffmanNode) {
            byte[] leftCode;
            if (++curCode[0] > (curCode.length - 1) * 8) {
                leftCode = new byte[curCode.length + 1];
                System.arraycopy(curCode, 0, leftCode, 0, curCode.length);
            } else {
                leftCode = curCode;
            }
            curCode = null;
            byte[] rightCode = Arrays.copyOf(leftCode, leftCode.length);
            int bitIdx;
			bitIdx = (rightCode[0] & 7) == 0 ? 7 : (rightCode[0] & 7) - 1;
            rightCode[rightCode.length - 1] |= 1 << bitIdx;
            populateLUTNCodes(((HuffmanNode) objectTree).left, leftCode);
            populateLUTNCodes(((HuffmanNode) objectTree).right, rightCode);
        } else {
            byte[] symbol = ((HuffmanLeaf) objectTree).val;
            int bitCount = 0;
            int codeByteIdx = 1;
            int codeV = 0;
            for (int i = 0, len = curCode[0]; i < len; i++) {
                if (((curCode[codeByteIdx] >> bitCount++) & 1) == 1) codeV |= 1 << i;
                if (bitCount == 8) {
                    bitCount = 0;
                    codeByteIdx++;
                }
            }
            codeIdx2Symbols[curCode[0]][codeV] = symbol;
            int hashIdx = aryHash(symbol) & symbl2CodLstIdx;
            while (symbol2Code[hashIdx] != null) if (++hashIdx == symbol2Code.length) hashIdx = 0;
            symbol2Code[hashIdx] = symbol;
            codeValues[hashIdx] = curCode;
        }
    }

    //setup multithreading
    private void generateSymbolFreqs(Stream<byte[]> data, boolean isFindUsed) {
//        int cores = Runtime.getRuntime().availableProcessors();
        //workQueue = new ArrayBlockingQueue<>(cores*5);
//        Thread[] workers = new Thread[cores];
        @SuppressWarnings("unchecked")
//        ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue(64);
//        for (int i = 0; i < cores; i++) {
//            queues[i] = new ArrayBlockingQueue<>(50);
                Runnable task;
		task = isFindUsed ? new FUGenerator(data) : new SFGenerator(data);
        task.run();

////            workers[i].start();
////        }
//        try {
//            long n = 0, counter = 0;
//            data.forEach(ba -> {
//                if ((counter++ & 131071) == 0) //System.out.println("CompressHuffman: Processed Records=" + counter);
//                if (ba != null) while (!queues[(int) (n++ % cores)].offer(ba)) ;
//            });
//            queue.offer(ba)
//            while (true) {
//                cores = 0;
//                for (Thread t: workers) if (t.getState() == State.WAITING) cores++;
//                if (cores == workers.length) break;
//                Thread.sleep(20);
//            }
//            //System.out.println("CompressHuffman: Finished Stage, Processed Records=" + counter);
//            for (Thread t: workers) t.interrupt();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    private void switchFields(boolean toTmp) {
        if (toTmp) {
            tmpsymbol2Code = symbol2Code;
            tmpCodeIdx2Symbols = codeIdx2Symbols;
            tmpCodeValues = codeValues;
        } else {
            defsymbol2Code = symbol2Code;
            defCodeValues = codeValues;
            defCodeIdx2Symbols = codeIdx2Symbols;
            defsymbl2CodLstIdx = codeIdx2Symbols.length - 1;
            symbol2Code = tmpsymbol2Code;
            codeIdx2Symbols = tmpCodeIdx2Symbols;
            codeValues = tmpCodeValues;
            symbl2CodLstIdx = tmpsymbol2Code.length - 1;
            tmpsymbol2Code = null;
            tmpCodeIdx2Symbols = null;
            tmpCodeValues = null;
        }
    }

    /**
     * Retrives the HuffmanTree data for offline storage
     * Feed the returned byte array into new CompressHuffman(byte[]) when you need to compress/decompress after program exit
     * Don't call this method before the HuffTree has been generated
     *
     * @return The HuffmanTree and other internal datastructures
     */
    public byte[] codec() {
        byte[] out = new byte[(totalSymbols * (maxSymbolLength * 10)) * 500];
        intToByteArray(totalSymbols, out, 0);
        intToByteArray(symbl2CodLstIdx, out, 4);
        intToByteArray(maxSymbolLength, out, 8);
        intToByteArray(altCodeIdx, out, 12);
        out[16] = useAltOnly ? (byte) 1 : 0;
        intToByteArray(highestLevel, out, 17);
        intToByteArray(altCodeBytes, out, 21);
        intToByteArray(codeIdx2Symbols.length, out, 25);
        int idx = 29;
        int count = 0;
        for (int i = 1; i < codeIdx2Symbols.length; i++) {
            count = 0;
            for (int w = 0; w < codeIdx2Symbols[i].length; w++) if (codeIdx2Symbols[i][w] != null) count++;
            idx = intToByteArray(count, out, idx);
            main:
            for (int w = 0; w < codeIdx2Symbols[i].length; w++) {
                if (codeIdx2Symbols[i][w] != null) {
                    if (out.length - idx < 1000) out = expand(out, 1);
                    out[idx++] = (byte) codeIdx2Symbols[i][w].length;
                    idx = bytesToByteArray(codeIdx2Symbols[i][w], out, idx);
                    idx = intToByteArray(w, out, idx);
                    byte[] symbol = codeIdx2Symbols[i][w];
                    int hashIdx = aryHash(symbol) & symbl2CodLstIdx;
                    byte[] aKey;
                    while ((aKey = symbol2Code[hashIdx]) != null) {
                        if (Arrays.equals(aKey, symbol)) {
                            idx = intToByteArray(hashIdx, out, idx);
                            continue main;
                        }
                        if (++hashIdx == symbol2Code.length) hashIdx = 0;
                    }
                    System.err.println("CompressHuffman: a symbol not found when gettingHuffData");
                }
            }
        }
        out = Arrays.copyOf(out, idx);
        Deflater deflater = new Deflater();
        deflater.setInput(out);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(out.length);
        byte[] buffer = new byte[8192];
        while (!deflater.finished()) {
            count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        return outputStream.toByteArray();
    }

    @FunctionalInterface
    interface Weight {
        long getWeight();
    }

    static class ByteAry {
        final byte[] ary;

        ByteAry(byte[] ary) {
            this.ary = ary;
        }

        @Override
        public int hashCode() {
            return longHash(hash(ary, 0, ary.length, -1640531527));
        }

        @Override
        public boolean equals(Object obj) {
            return Arrays.equals(ary, ((ByteAry) obj).ary);
        }
    }

    static class TmpNode implements Comparable<Weight>, Weight {

        final long wt;
        final byte[] key;

        TmpNode(long weight, byte[] key) {
            this.wt = weight;
            this.key = key;
        }

        @Override
        public int compareTo(Weight o) {
            int c = Long.compare(wt, o.getWeight());
            if (c == 0) return 1;
            return c;
        }

        @Override
        public int hashCode() {
            return longHash(hash(key, 0, key.length, -1640531527));
//			int hash = 1;
//			for(byte b : key) hash = (257 * hash + b);
//			return hash;
        }

        @Override
        public long getWeight() {
            return wt;
        }
    }

    static class HuffConfig {
        int maxSymbolLength;
        int maxSymbols;
        int maxTreeDepth;
        long maxSymbolCacheSize;
        boolean twoPass;
    }

//    static class ByteAryLst {
//        byte[] ary;
//        int len = 0;
//        int capacity;
//
//        public ByteAryLst(int size) {
//            ary = new byte[size];
//            capacity = ary.length;
//        }
//
//        public void add(byte e) {
//            ary[len] = e;
//            if (++len == capacity) {
//                ary = Arrays.copyOf(ary, ary.length * 2);
//                capacity = ary.length;
//            }
//        }
//
//        public byte get(int idx) {
//            return ary[idx];
//        }
//
//        public byte[] toAry() {
//            return Arrays.copyOf(ary, len);
//        }
//    }

    //generate symbols and record their frequency in dataset
    class SFGenerator implements Runnable {


        private final Stream<byte[]> workQueue;

        SFGenerator(Stream<byte[]> workQueue) {
            this.workQueue = workQueue;
        }

        @Override
        public void run() {

            workQueue.forEach(dV -> {
                int VLen = dV.length;
                for (int i = 0; i < VLen; i += maxSymbolLength) {
                    for (int j = i; j < VLen && j < i + maxSymbolLength; j++) {
                        byte[] symbol = Arrays.copyOfRange(dV, i, j + 1);
                        ByteAry ba = new ByteAry(symbol);
                        while (true) {
                            Integer oldWeight = freqList.getIfPresent(ba);
                            int weight;
                            if (oldWeight != null) {
                                weight = oldWeight + symbol.length;
                                if (freqList.asMap().replace(ba, oldWeight, weight)) break;
                            } else {
                                weight = symbol.length;
                                if (freqList.asMap().putIfAbsent(ba, weight) == null) break;
                            }
                        }
                    }

                }
            });
        }
    }

    //find out what symbols are really used during compression and add to freqList
    class FUGenerator implements Runnable {
        final Stream<byte[]> workQueue;
        int startIdx;
        int endIdx;
        int symbIdx;
        byte[] symbol;
        ByteAry bAry;
        Integer weight;
        Integer oldWeight;

        FUGenerator(Stream<byte[]> workQueue) {
            this.workQueue = workQueue;
        }

        @Override
        public void run() {
            workQueue.forEach(ba -> {
                        if (ba.length == 0) return;
                        startIdx = 0;
                        endIdx = Math.min(ba.length, maxSymbolLength);
                        while (true) {
                            symbIdx = findSymblIdx(startIdx, endIdx, ba, symbl2CodLstIdx, symbol2Code);
                            if (symbIdx != -1) {
                                //symbUsed[symbIdx] = true;
                                symbol = symbol2Code[symbIdx];
                                bAry = new ByteAry(symbol);
                                while (true) {
                                    oldWeight = freqList.getIfPresent(bAry);
                                    if (oldWeight != null) {
                                        weight = oldWeight + symbol.length;
                                        if (freqList.asMap().replace(bAry, oldWeight, weight)) break;
                                    } else {
                                        weight = symbol.length;
                                        if (freqList.asMap().putIfAbsent(bAry, weight) == null) break;
                                    }
                                }
                                startIdx += symbol.length;
                            } else {
                                startIdx += endIdx - startIdx;
                            }
                            if (startIdx >= ba.length) break;
                            endIdx = startIdx + maxSymbolLength;
                            if (endIdx > ba.length) endIdx = ba.length;
                        }
                    }
            );

        }
    }

    abstract static class HuffmanTree implements Comparable<Weight>, Weight {
        final long frequency;
        final int id;

        HuffmanTree(long freq, int nodeID) {
            frequency = freq;
            id = nodeID;
        }

        public int compareTo(Weight w) {
            return Long.compare(frequency, w.getWeight());
        }

        public long getWeight() {
            return frequency;
        }
    }

    static class HuffmanLeaf extends HuffmanTree {
        final byte[] val;

        HuffmanLeaf(long weight, byte[] dat, int nodeID) {
            super(weight, nodeID);
            val = dat;
        }

    }

    static class HuffmanNode extends HuffmanTree {
        final HuffmanTree left;
        final HuffmanTree right;

        HuffmanNode(HuffmanTree lef, HuffmanTree rit, int nodeID) {
            super(lef.frequency + rit.frequency, nodeID);
            left = lef;
            right = rit;
        }
    }
}

