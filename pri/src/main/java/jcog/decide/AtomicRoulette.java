//package jcog.decide;
//
//import com.google.common.base.Joiner;
//import jcog.TODO;
//import jcog.Util;
//import jcog.data.list.FastCoWList;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Random;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicIntegerArray;
//import java.util.function.IntFunction;
//import java.util.function.IntPredicate;
//import java.util.stream.IntStream;
//
//public class AtomicRoulette<X> {
//
//    /**
//     * TODO this can be smaller per cause, ex: byte
//     */
//    private final AtomicIntegerArray pri;
//
//    public final FastCoWList<X> choice;
//
//    private final BlockingQueue<X> onQueue = Util.blockingQueue(512);
//    private final BlockingQueue<X> offQueue = Util.blockingQueue(512);
//
//    private final AtomicBoolean busy = new AtomicBoolean(false);
//    private final AtomicInteger priTotal = new AtomicInteger(0);
//
//    public AtomicRoulette(int capacity, IntFunction<X[]> arrayBuilder) {
//        this.choice = new FastCoWList<>(capacity, arrayBuilder);
//        this.pri = new AtomicIntegerArray(capacity);
//    }
//
//    public void add(X c) {
//        onQueue.add(c);
//    }
//
//    protected void remove(X c) {
//        offQueue.add(c);
//    }
//
//    @Override
//    public String toString() {
//
//        if (choice.isEmpty())
//            return "empty";
//        else
//            return Joiner.on("\n").join(IntStream.range(0, choice.size()).mapToObj(
//                    x -> pri.get(x) + "=" + choice.get(x)
//            ).iterator());
//    }
//
//    public boolean commit(@Nullable Runnable r) {
//        if (!busy.compareAndSet(false, true))
//            return false;
//
//        try {
//            if (!onQueue.isEmpty()) {
//                onQueue.removeIf((adding) -> {
//
//                    int slot = findSlot(adding);
//                    if (slot != -1) {
//                        choice.set(slot, adding);
//                        onAdd(adding, slot);
//                    }
//                    return true;
//                });
//            }
//            if (!offQueue.isEmpty()) {
//                throw new TODO();
//            }
//
//            if (r != null)
//                r.run();
//
//
//        } finally {
//            busy.set(false);
//        }
//
//        return true;
//
//    }
//
//    private int findSlot(X x) {
//        int i;
//        int s = choice.size();
//        for (i = 0; i < s; i++) {
//            X ci = choice.get(i);
//            if (ci == null)
//                return i;
//            else if (ci == x)
//                return -1;
//        }
//
//        if (i < pri.length())
//            return i;
//
//        throw new RuntimeException("overload");
//    }
//
//    protected void onAdd(X x, int slot) {
//
//    }
//
//    /**
//     * priGetAndSet..
//     */
//    public int priGetAndSet(int i, int y) {
//        int x = pri.getAndSet(i, y);
//        if (y != x) {
//            int t = priTotal.addAndGet(y - x);
//
//        }
//        return x;
//    }
//
//
//    public boolean priGetAndSetIfEquals(int i, int x0, int y) {
//        if (pri.compareAndSet(i, x0, y)) {
//            int t = priTotal.addAndGet(y - x0);
//
//            return true;
//        }
//        return false;
//    }
//
//    public int pri(int i) {
//        return pri.get(i);
//    }
//
//    public void decide(Random rng, IntPredicate kontinue) {
//
//        int i = 0;
//
//        boolean kontinued;
//        restart: do {
//
//            int priTotal = this.priTotal.get();
//            if (priTotal == 0)
//                kontinued = kontinue.test(-1);
//            else {
//                int count = choice.size();
//
//                int distance = (int) (rng.nextFloat() * priTotal);
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//                int pp;
//                int start = i;
//                while (((pp = pri.get(i)) == 0) || ((distance = distance - pp) > 0)) {
//                    if (++i == count) i = 0;
//                    if (i == start) {
//                        kontinued = kontinue.test(-1);
//                        continue restart;
//                    }
//                }
//
//                kontinued = kontinue.test(i);
//            }
//
//        } while (kontinued);
//
//    }
//
//
//}
