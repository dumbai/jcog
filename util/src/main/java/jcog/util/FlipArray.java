//package jcog.util;
//
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.Function;
//
///** double buffered array */
//public class FlipArray<X> extends AtomicInteger {
//
//    final static private int OK = 0;
//    final static private int INVALID = 1;
//    final static private int BUSY = 2;
//    private final AtomicInteger valid = new AtomicInteger(OK);
//
//
//    private volatile X[] a;
//    private volatile X[] b;
//
//    public FlipArray() {
//        this(null, null);
//    }
//
//    public FlipArray(X[] a, X[] b) {
//        super(0);
//        this.a = a;
//        this.b = b;
//    }
//
//    public X[] write() { return (getOpaque() & 1) == 0 ? a : b;   }
//
//
//    private void commit(Function<X[],X[]> writeTransform) {
//        int before = getOpaque();
//
//        X[] w = writeTransform.apply(write());
//
//        if (compareAndSet(before, before+1)) {
//            write(before, w);
//            //return w;
//        }
//    }
//
//
//    public X[] write(int commit, X[] newValue) {
//        if ((commit & 1) == 0) {
//            return this.a = newValue;
//        } else {
//            return this.b = newValue;
//        }
//    }
//
//    private X[] read() {
//        return (getOpaque() & 1) == 0 ? b : a;
//    }
//
//
//
//    public boolean ok() {
//        return valid.getOpaque()==OK;
//    }
//
////    @Nullable
////    public X[] readOK() {
////        int before = getOpaque();
////        if (ok()) {
////            int after = getOpaque();
////            X[] r = read();
////            if (before == after)
////                return r;
////        }
////        return null;
////    }
//
//    public void invalidate() {
//        valid.setAt(INVALID);
//    }
//
//    public X[] readValid(boolean waitIfBusy, Function<X[],X[]> writeTransform) {
//
//        if (valid.compareAndSet(INVALID, BUSY)) {
//            X[] x;
//            try {
//                x = commit(writeTransform);
//            } finally {
//                valid.compareAndSet(BUSY, OK);
//            }
//
//            return x;
//        }
//        if (waitIfBusy) {
//            //int waits = 0;
//            X[] result;
//            int currentVersion = getOpaque(), nextVersion;
//            //do {
//                while (valid.get() != OK && currentVersion < (nextVersion = getOpaque())) {
//                    //Util.pauseNextIterative(waits++);
//                    Thread.onSpinWait();
//                    currentVersion = nextVersion;
//                }
//                result = read();
//            //} while (getOpaque()>currentVersion && );
//            return result;
//        }
//        return read();
//    }
//
////    /** TODO optional - may affect readers in progress */
////    private void writeClear() {
////        Arrays.fill(write(), null);
////    }
//}
