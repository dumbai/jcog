package jcog.bloom.hash;

import java.util.function.Function;

public class BytesHasher<E> extends AbstractHasher<E> {

    final Function<E,byte[]> bytes;

    public BytesHasher(Function<E, byte[]> bytes) {
        this.bytes = bytes;
    }

    @Override
    public byte[] asBytes(E element) {
        return bytes.apply(element);
    }
}
