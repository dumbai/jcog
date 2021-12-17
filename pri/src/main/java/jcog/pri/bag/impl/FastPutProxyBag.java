//package jcog.pri.bag.impl;
//
//import jcog.Util;
//import jcog.signal.NumberX;
//import jcog.data.list.FasterList;
//import jcog.pri.Prioritizable;
//import jcog.pri.bag.Bag;
//import jcog.pri.bag.util.ProxyBag;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.locks.StampedLock;
//import java.util.function.Consumer;
//
///** TODO needs tested */
//public class FastPutProxyBag<K, X extends Prioritizable> extends ProxyBag<K, X> {
//
//
//    final StampedLock lock = new StampedLock();
//
//    final AtomicBoolean commitBusy = new AtomicBoolean(false);
//
//    //    final AtomicBoolean dirty = new AtomicBoolean(false);
//    private final BlockingQueue putQueue;
//
//    public FastPutProxyBag(Bag<K, X> bag, int backlog) {
//        super(bag);
//
//
//        putQueue = new ArrayBlockingQueue(backlog); //Util.blockingQueue(backlog);
//    }
//
//
//    /** folds simultaneous commits into one */
//    @Override public Bag<K, X> commit(@Nullable Consumer<X> update) {
//        if (commitBusy.compareAndSet(false, true)) {
//            try {
//                return super.commit(update);
//            } finally {
//                commitBusy.set(false);
//            }
//        } else {
//            //System.out.println("commit elided");
//        }
//        return this;
//    }
//
//    @Override
//    public X put(X b, NumberX overflowing) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void putAsync(X b) {
//
//        int spins = 0;
//        while (!putQueue.offer(b)) {
//            Util.pauseNextIterative(spins++);
//        }
//
//        //dirty.lazySet(true);
//
//        do {
//            long l = lock.tryWriteLock();
//            if (l != 0) {
//                try {
//                    //while (dirty.compareAndSet(true, false)) {
//                    FasterList<X> x = new FasterList(putQueue.size());
//                    putQueue.drainTo(x);
//                    x.forEach(xx -> super.put(xx, null));
////                    Object n;
////                    while ((n = putQueue.poll()) != null) {
////                        super.put((X) n, null);
////                    }
//                    //}
//                } finally {
//                    lock.unlockWrite(l);
//                }
//            } else {
//                return;
//            }
//        } while (!putQueue.isEmpty()); //check again
//    }
//}
//