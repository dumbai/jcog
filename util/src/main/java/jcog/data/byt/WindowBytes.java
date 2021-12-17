package jcog.data.byt;

import java.util.Arrays;

/**
 * Created by me on 4/17/17.
 */
public class WindowBytes extends ArrayBytes /*implements CharSequence*/ {
    final int start;
    final int end;

    public WindowBytes(ArrayBytes a, int start, int end) {
        this(a.bytes, start, end);
    }

    protected WindowBytes(byte[] bytes, int start, int end) {
        super(bytes);
        if (start < 0) {
            throw new IllegalArgumentException("start " + start + " < 0");
        } else if (end > bytes.length) {
            throw new IllegalArgumentException("end " + end + " > length " + bytes.length);
        } else if (end < start) {
            throw new IllegalArgumentException("end " + end + " < start " + start);
        } else if (start == 0 && end == bytes.length) {
            throw new IllegalArgumentException("window unnecessary");
        }

        this.start = start;
        this.end = end;

    }



    @Override
    public int hashCode() {
        return hashCode(start, end);
    }

    @Override
    public final void toArray(byte[] c, int offset) {
        System.arraycopy(bytes, start, c, offset, length());
    }

    @Override
    public byte[] arrayCompactDirect() {
        return Arrays.copyOfRange(bytes, start, end);
    }

    @Override
    public int length() {
        return this.end - this.start;
    }

    @Override
    public byte at(int index) {
        return this.bytes[index + this.start];
    }

    @Override
    public ByteSequence subSequence(int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start " + start + " < 0");
        } else if (end > this.length()) {
            throw new IllegalArgumentException("end " + end + " > length " + this.length());
        } else if (end < start) {
            throw new IllegalArgumentException("end " + end + " < start " + start);
        } else {
            return new WindowBytes(this, this.start + start, this.start + end);
        }
    }

    public String toString() {
        return new String(arrayCompactDirect());
    }


}
