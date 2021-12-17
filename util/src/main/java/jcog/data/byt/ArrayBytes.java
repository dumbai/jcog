package jcog.data.byt;

import jcog.Util;
import jcog.util.ArrayUtil;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by me on 12/20/16.
 */
public class ArrayBytes implements ByteSequence, Serializable, Comparable<ArrayBytes> /*implements CharSequence*/ {

    public final byte[] bytes;

    public ArrayBytes(byte... bytes) {
        this.bytes = bytes;
    }

    public ArrayBytes(byte[] bytes, int start, int end) {
        this(ArrayUtil.subarray(bytes, start, end));
    }

    @Override
    public int hashCode() {
        return Util.hash(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        return Arrays.equals(bytes, ((ArrayBytes)obj).bytes);
    }

    @Override
    public void toArray(byte[] c, int offset) {
        System.arraycopy(bytes, 0, c, offset, length());
    }

    @Override
    public byte[] arrayCompactDirect() {
        return bytes;
    }

    @Override
    public int length() {
        return bytes.length;
    }

    @Override
    public byte at(int index) {
        return this.bytes[index];
    }

    @Override
    public ByteSequence subSequence(int start, int end) {
        return subSeq(start, end);
    }

    public ByteSequence subSeq(int start, int end) {
        if (end - start == 1)
            return new OneByteSeq(at(start));

        if (start == 0 && end == length())
            return this; 

        return new WindowBytes(this, start, end);
    }

    public String toString() {
        return new String(bytes);
    }

    @Override
    public int compareTo(ArrayBytes o) {
        return Arrays.compare(bytes, o.bytes);
    }
}
