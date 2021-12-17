//package jcog.mutex;
//
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//
///** simple spin lock */
//public class Spin extends AtomicBoolean {
//
//    public Spin() {
//        super(false);
//    }
//
//    public <X> X run(Supplier<X> r) {
//        start();
//
//        try {
//            return r.get();
//        } finally {
//            end();
//        }
//
//    }
//
//
//    public void run(Runnable r) {
//        start();
//
//        try {
//            r.run();
//        } finally {
//            end();
//        }
//    }
//
//    public <X> void run(Consumer<X> r, X arg) {
//
//        start();
//
//        try {
//            r.accept(arg);
//        } finally {
//            end();
//        }
//    }
//
//    private void start() {
//        while (!compareAndSet(false, true))
//            Thread.onSpinWait();
//    }
//
//    private void end() {
//        set(false);
//    }
//
//}
