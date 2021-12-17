package jcog.data.byt;

import java.util.Objects;

public class ConcatBytes implements ByteSequence {
    final ByteSequence a;
    final ByteSequence b;


    public ConcatBytes(ByteSequence a, ByteSequence b) {
        this.a = a;
        this.b = b;
    }


    @Override
    public int hashCode() {
        return Objects.hash(this, 0, length());
    }

    @Override
    public int length() {
        return a.length() + b.length();
    }

    @Override
    public byte at(int index) {
        int al = a.length();
        return at(index, al);
    }

    protected byte at(int index, int al) {
        ByteSequence ab;
        if ( index < al) {
            ab = a;
        } else {
            index -= al;
            ab = b;
        }
        return ab.at(index);
    }

    @Override
    public ByteSequence subSequence(int start, int end) {
        byte[] x = new byte[end-start];
        int al = a.length();
        for (int i = start, j = 0; i < end; i++) {
            x[j++] = at(i, al);
        }
        return new HashedArrayBytes(x);
    }
}
