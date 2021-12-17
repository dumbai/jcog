package jcog.bloom.hash;

import java.nio.ByteBuffer;

/**
 * Created by jeff on 14/05/16.
 */
public class DefaultHasher<E> extends AbstractHasher<E> {

    @Override
    public byte[] asBytes(Object element) {
        ByteBuffer buffer = ByteBuffer
                .allocate(4)
                .putInt(element.hashCode());
        return buffer.array();
    }

}
