package jcog.exe;

import jcog.Config;
import jcog.Log;
import jcog.exe.realtime.AdmissionQueueWheelModel;
import jcog.exe.realtime.HashedWheelTimer;
import org.slf4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


/**
 * static execution context: JVM-global dispatch, logging, profiling, etc.
 */
public enum Exe {
    ;

    /**
     * global timer
     */
    private static final HashedWheelTimer timer;
    private static final Logger logger = Log.log(Exe.class);
    public static boolean PROFILE = Config.IS("PROFILE", false);
//    static {
//        Runtime.getRuntime().addShutdownHook(new Thread(()->{
//            ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES);
//        }));
//    }
    private static Executor executor = ForkJoinPool.commonPool();

    static {
        long resolution = MILLISECONDS.toNanos(
    Config.INT("TIMER_RESOLUTION_MS",
             1
                //4
                //10
                //2
        ));

        timer = new HashedWheelTimer(

                new AdmissionQueueWheelModel(256,
                        resolution
                ),

                //            new QueueWheelModel(64,
                //                TimeUnit.MILLISECONDS.toNanos( /*1*/ /*2*/  4  /* 10 */ ), ()->
                //                        new MpscArrayQueue<>(32)
                //            ),

                HashedWheelTimer.WaitStrategy.SleepWait,
                //HashedWheelTimer.WaitStrategy.YieldingWait,
                Exe::run);
    }

    public static HashedWheelTimer timer() {
        return timer;
    }

    /**
     * soon
     */
    public static void run(Runnable r) {
        executor.execute(r);
    }

    /**
     * later
     */
    public static void runLater(Runnable r) {
        timer.submit(r);
    }

    public static Executor executor() {
        return executor;
    }

    public static synchronized void setExecutor(Executor e) {
        logger.info("global executor = {} ", e);
        executor = e;
    }

    //    /**
//     * record an already-timed event
//     */
//    public static void profiled(Object what, long start, long end) {
//        Profiler p = profiler;
//        if (p != null)
//            p.profiled(what, start, end);
//    }

//    public static void run(Object what, Runnable r) {
//        Profiler p = profiler;
//        if (p != null) {
//            p.run(what, r);
//        } else {
//            r.run();
//        }
//    }

//    private static final ThreadLocal<Boolean> singleThreaded = ThreadLocal.withInitial(()->false);
//
//    /** if set, promises that the remainder of this thread executes in a single threaded context */
//    public static void single() {
//        singleThreaded.set(true);
//    }

//    public static void multi() {
//        singleThreaded.set(false);
//    }

//    public static boolean concurrent() {
//        return !singleThreaded.get();
//    }

//    public static abstract class Profiler {
//
//        abstract public void profiled(Object what, long startNS, long endNS);
//
//        public Runnable runnable(Runnable r) {
//            return new ProfiledRunnable(r);
//        }
//
//        public final void run(Runnable r) {
//            run(r, r);
//        }
//
//        public void run(Object what, Runnable r) {
//            long start = System.nanoTime();
//            try {
//                r.run();
//            } finally {
//                long end = System.nanoTime();
//                profiled(what, start, end);
//            }
//        }
//
//        private class ProfiledRunnable implements Runnable {
//            private final Runnable r;
//
//            ProfiledRunnable(Runnable r) {
//                this.r = r;
//            }
//
//            @Override
//            public void run() {
//                long start = System.nanoTime();
//                r.run();
//                long end = System.nanoTime();
//                Profiler.this.profiled(r, start, end);
//            }
//        }
//    }

//    static Profiler profiler = null;
//
//    public static void setProfiler(@Nullable Profiler p) {
//        profiler = p;
//    }
//
//    public static class UDPeerProfiler extends Profiler {
//
//        final static org.slf4j.Logger logger = LoggerFactory.getLogger(UDPeerProfiler.class);
//
//        final UDPeer p = new UDPeer();
//
//        final AtomicBoolean busy = new AtomicBoolean();
//        final MetalConcurrentQueue<JsonNode> out = new MetalConcurrentQueue<>(2048);
//
//        /** max events per packet */
//        final static int batchSize = 16;
//
//        public UDPeerProfiler() throws IOException {
//            p.setFPS(20f);
//        }
//
//        @Override
//        public void profiled(Object what, long startNS, long endNS) {
//            if (!p.connected())
//                return;
//
//            ObjectNode n = JsonNodeFactory.instance.objectNode();
//            n.putArray("t").add(startNS).add(endNS);
//            n.put("_",
//                    Thread.currentThread().getId() + ": " + what
//            );
//
//
//            if (busy.compareAndSet(false, true)) {
//                try {
//                    int total = out.size();
//                    while (total > 0) {
//                        int s = Math.min(total, batchSize);
//                        List a = new ArrayList(s + 1);
//                        if (n != null) {
//                            a.add(n);
//                            n = null;
//                        }
//
//                        for (; s > 0; s--) {
//                            JsonNode o = out.poll();
//                            if (o == null)
//                                break;
//                            a.add(o);
//                        }
//
//                        if (!a.isEmpty()) {
//                            try {
//                                p.tellSome(a, 2, true);
//                            } catch (JsonProcessingException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                } finally {
//                    busy.set(false);
//                }
//            } else {
//                if (!out.offer(n))
//                    logger.warn("dropped profiling msg");
//            }
//        }
//    }
}