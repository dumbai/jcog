package jcog.memoize.byt;

import jcog.data.byt.DynBytes;
import jcog.pri.PriProxy;

import java.util.Arrays;

public class ByteKeyExternal implements ByteKey {

    private static final ThreadLocal<DynBytes> buffer = ThreadLocal.withInitial(()->new DynBytes(1024));

    public transient DynBytes key;

    protected int hash;

    final int start;
    private int end = -1;
    transient byte[] k;

    public ByteKeyExternal() {
		this(
		        buffer.get()
//                RecycledDynBytes.get()
        );
    }

    public ByteKeyExternal(DynBytes key) {
        super();
        this.key = key;
        this.start = key.length();
    }

    public void internedNew(PriProxy/*ByteKeyInternal*/ i) {

    }

    protected final void commit() {
        //TODO optional compression
        this.end = key.length();
        this.hash = key.hashCode(start, end);
//        this.hash = key.hashCode();
    }

    public void close() {
        key.rewind(end-start);
//        key.close();
        key = null;
        k = null;
    }


    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public final boolean equals(Object obj) {
        return ByteKey.equals(this, (ByteKey) obj);
    }

    @Override
    public final int length() {
        return end-start;
    }

//    @Override
//    public byte at(int i) {
//        return key.at(start + i);
//        //return key.at(i);
//    }

    @Override
    public byte[] array() {
        if (k==null)
            k = Arrays.copyOfRange(key.arrayDirect(), start, end);
        return k;
    }

    @Override
    public boolean equals(ByteKeyExternal y, int at, int len) {
        throw new UnsupportedOperationException();
    }

}