package jcog.memoize.byt;

import java.util.function.Function;

/** simple 2-level with constant threshold and capacity ratio
 *  TODO test
 * */
public class ByteDualHijackMemoize<X extends ByteKeyExternal,Y> extends ByteMultiHijackMemoize<X,Y> {

    private final int reprobes;
    private final int lengthThesh;
    private final int cap0;
    private final int cap1;

    public ByteDualHijackMemoize(Function<X, Y> f, int capacity, int reprobes, boolean soft, int lengthThesh, float ratio) {
        super(f, capacity, soft, 2);
        this.lengthThesh = lengthThesh;
        this.cap0 = (int) (ratio * capacity);
        this.cap1 = (int) ((1f-ratio) * capacity);
        this.reprobes = reprobes;
    }

    @Override
    int capacity(int level) {
        return level==0 ? cap0 : cap1;
    }

    @Override
    int reprobes(int level) {
        return reprobes;
    }

    @Override
    int level(ByteKey key) {
        return key.length() < lengthThesh ? 0 : 1;
    }
}
