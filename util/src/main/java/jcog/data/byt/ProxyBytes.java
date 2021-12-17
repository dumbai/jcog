package jcog.data.byt;

public class ProxyBytes implements ByteSequence {

    protected final ByteSequence ref;

    public ProxyBytes(ByteSequence ref) {
        this.ref = ref;
    }

    @Override
    public String toString() {
        return ref.toString();
    }

    @Override
    public int hashCode() {
        return ref.hashCode();
    }

    @Override
    public final int length() {
        return ref.length();
    }

    @Override
    public final byte at(int index) {
        return ref.at(index);
    }

    @Override
    public final ByteSequence subSequence(int start, int end) {
        return ref.subSequence(start, end);
    }
}
