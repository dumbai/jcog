package jcog.data.byt;

import jcog.data.pool.MetalPool;

public class RecycledDynBytes extends DynBytes implements AutoCloseable {

    private RecycledDynBytes(int bufferSize) {
        super(bufferSize);
    }

    //private transient MetalPool pool = null;

    static final int INITIAL_KEY_CAPACITY = 512;
    //final static ThreadLocal<DequePool<byte[]>> bytesPool = DequePool.threadLocal(()->new byte[MAX_KEY_CAPACITY]);
    static final ThreadLocal<MetalPool<RecycledDynBytes>> bytesPool = MetalPool.threadLocal(()->
            //new UnsafeRecycledDynBytes(
            new RecycledDynBytes(
                INITIAL_KEY_CAPACITY));

    public static RecycledDynBytes get() {
        return bytesPool.get().get();
    }

    //return new DynBytes(MAX_KEY_CAPACITY);

//    public static class UnsafeRecycledDynBytes extends RecycledDynBytes {
//
//        private UnsafeRecycledDynBytes(int bufferSize) {
//            super(bufferSize);
//        }
//
//        //overriding this method screws up itable and its called a lot
//        @Override
//        protected int ensureSized(int extra) {
//            return len;
//        }
//    }

    @Override
    public byte[] compact(byte[] forceIfSameAs, boolean force) {
        return bytes; //dont compact
    }

    @Override
    public void close() {
        //MetalPool p = pool;
        //if (p==null) throw new WTF("already closed");

        clear();
        //this.pool = null; //not necessary since threadlocal
        bytesPool.get().put(this);
    }


}
