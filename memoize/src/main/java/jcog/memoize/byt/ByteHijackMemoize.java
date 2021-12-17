package jcog.memoize.byt;

import jcog.Str;
import jcog.io.Huffman;
import jcog.memoize.HijackMemoize;
import jcog.pri.PLink;
import jcog.pri.PriProxy;
import jcog.signal.meter.SafeAutoCloseable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Function;

public class ByteHijackMemoize<X extends ByteKeyExternal,Y> extends HijackMemoize<X,Y> {

    public ByteHijackMemoize(Function<X, Y> f, int capacity, int reprobes, boolean soft) {
        super(f, capacity, reprobes, soft);
    }

    @Override
    public final PriProxy computation(X x, Y y) {
        return new ByteKeyInternal(x, y, value(x, y));
    }

    @Override
    public final PriProxy<X, Y> put(X x, Y y) {
        PriProxy<X, Y> xy = super.put(x, y);
        x.close();
        return xy;
    }

    @Override
    public final @Nullable Y apply(X x) {
        Y y = super.apply(x);
        x.close();
        return y;
    }


    public Huffman buildCodec() {
        return buildCodec(new Huffman(bag.stream().map(b -> bag.key(b).array()),
                Huffman.fastestCompDecompTime()));
    }

    public static Huffman buildCodec(Huffman h) {
        //TODO add incremental codec building from multiple ByteHijackMemoize's
        return h;
    }

//    @Override
//    protected void boost(PriProxy<X, Y> p) {
//        p.priAdd(/*valueBase(p.x()) * */
//            CACHE_HIT_BOOST);
//    }
//
//    @Override
//    protected void cut(PriProxy<X, Y> p) {
//        p.priSub(
//            //valueBase(p.x()) *
//            CACHE_SURVIVE_COST);
//    }
//
//    @Override
//    public float value(X x, Y y) {
//        return DEFAULT_VALUE;
//    }

    @Override
    protected final void removed(PriProxy<X, Y> value) {
        //internalPut((ByteKeyInternal)value);

        Object r = ((ByteKeyInternal) value).id;
        if (r instanceof SafeAutoCloseable) {
            ((SafeAutoCloseable)r).close();
        }
    }

    @Override
    protected void internedNew(X x, PriProxy<X, Y> input) {
        x.internedNew(input);
    }

    @Override
    protected final void rejected(PriProxy<X, Y> value) {
        removed(value);
    }

    //private static final ThreadLocal<MetalPool<ByteKeyInternal>> internals = MetalPool.threadLocal(()->new ByteKeyInternal());
    //static final MpmcArrayQueue<ByteKeyInternal> internalsShared = new MpmcArrayQueue<>(16*1024 /* TODO tune */);


    //private void internalPut(ByteKeyInternal old) {
        //old.clear();
        //internals.get().put(k);
        //internalsShared.relaxedOffer(k);
    //}

    private static final class ByteKeyInternal<Y> extends PLink<Y> implements ByteKey, PriProxy<ByteKey,Y> {

        //TODO decide if these need to be volatile


        /*@Stable*/ byte[] key;
        int hash;

        ByteKeyInternal(ByteKeyExternal x, Y y, float value) {
            super(y, value);
            this.hash = x.hash;
            this.key = x.array();
        }

//        @Override
//        public byte at(int i) {
//            return key[i];
//        }

        @Override
        public final int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(ByteKeyExternal y, int at, int len) {
            int ys = y.start;
            return
                Arrays.equals(key, at, len, y.key.arrayDirect(), ys + at, ys + len);
                //ArrayUtil.equals(key, at, len, y.key.arrayDirect(), ys + at, ys + len);
        }

        @Override
        public final boolean equals(Object obj) {
            return ByteKey.equals(this, (ByteKey) obj);
        }

        @Override
        public final ByteKeyInternal<Y> x() {
            return this;
        }

        @Override
        public Y y() {
            return id;
        }

        @Override
        public final byte[] array() {
            return key;
        }

        @Override
        public final int length() {
            return key.length;
        }

        @Override
        public String toString() {
            return id + " = $" + Str.n4(pri()) + ' ' + Arrays.toString(key);
        }

    }


//    private static float valueBase(ByteKey x) {
//        return 1;
//        //return 1/((1+x.length()));
//        //return (float) (1 /(Math.log(1+x.length())));
//        //return 1 /((1+sqr(x.length())));
//        //return 1f/(bag.reprobes * (1+ Util.sqr(x.length())));
//    }
}