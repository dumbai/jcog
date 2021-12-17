package jcog.exe;

import jcog.Log;
import jcog.WTF;
import jcog.data.list.FastCoWList;
import net.openhft.affinity.AffinityLock;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * uses affinity locking to pin new threads to their own unique, stable CPU core/hyperthread etc
 * TODO use ThreadGroup
 */
public class AffinityExecutor implements Executor {

    //static final AtomicInteger serial = new AtomicInteger(0);
    //id + '_' + serial.getAndIncrement()

    private static final Logger logger = Log.log(AffinityExecutor.class);
    public final FastCoWList<Thread> threads = new FastCoWList<>(Thread[]::new);
    public final Semaphore running;
    public final String id;
    public final int maxThreads;
    private boolean tryPin;
    private Supplier<? extends Runnable> workerBuilder;

    /** whether to respawn thread on uncaught (top-level) exception */
    public volatile boolean exceptionRespawn = true;
    private volatile boolean shutdown = false;

    public AffinityExecutor(/* int maxThreads */) {
        this(Runtime.getRuntime().availableProcessors());
    }

    public AffinityExecutor(int maxThreads) {
        this(Thread.currentThread().getThreadGroup().getName(), maxThreads);
    }

    public AffinityExecutor(String id, int maxThreads) {
        this.id = id;

        this.maxThreads = maxThreads;
        this.running = new Semaphore(maxThreads);
    }

    protected void kill(Thread thread) {


//        Runnable t = ((AffinityRunnable)thread).run;
//        if (t instanceof AutoCloseable) {
//
//            try {
//                ((AutoCloseable) t).close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }

        //if (thread.isAlive())
        //if (Thread.currentThread()!=thread) {
            running.release(1);
            try {
                thread.interrupt();
                //thread.stop();
            } catch (Throwable t) {
                //..
            }
        //}
    }

    private void die(Thread t, boolean replace) {
        if (threads.remove(t))
            running.release(1);

        if (replace)
            add();
    }

    /** ensures that all worker threads are running */
    public void runAll() {
        int p = running.availablePermits();
        for (int i = 0; i < p; i++)
            add();
    }

    @Override
    public final void execute(Runnable command) {
        execute(command);
    }

    public final void shutdownNow() {
        shutdown = true;
        stop();
    }

    public int size() {
        return maxThreads - running.availablePermits();
    }

    public int running() {
        return threads.size();
    }

    public void stop() {

        synchronized (this) {
            threads.removeIf(t -> {
                kill(t);
                return true;
            });

            if (!threads.isEmpty())
                throw new WTF();
//            if (running.availablePermits() != maxThreads);
//                throw new WTF();
        }

    }


    public final void set(Supplier<? extends Runnable> workerBuilder, boolean tryPin) {
        this.workerBuilder = workerBuilder;
        this.tryPin = tryPin;
    }

    private void add() {

        boolean ready = running.tryAcquire();
        if (!ready)
            throw new RuntimeException("too many threads");

        Thread at = new Thread(this::run);

        at.setUncaughtExceptionHandler(this::uncaughtException);

        at.start();
    }

    private String dumpThreadInfo() {
        StringBuilder sb = new StringBuilder();

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        for (Thread t : threads) {
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(t.getId());
            sb.append('{');
            sb.append("name=").append(t.getName()).append(',');
            sb.append("id=").append(t.getId()).append(',');
            sb.append("state=").append(threadInfo.getThreadState()).append(',');
            sb.append("lockInfo=").append(threadInfo.getLockInfo());
            sb.append('}');
        }

        return sb.toString();
    }

    private void uncaughtException(Thread tr, Throwable e) {
        if (e.getCause() != null)
            e = e.getCause();
        if (!(e instanceof InterruptedException))
            e.printStackTrace();
        die(tr, exceptionRespawn && !(e instanceof InterruptedException));
    }

    private void run() {

        if (shutdown)
            return;

        Runnable w = workerBuilder.get();

        threads.add(Thread.currentThread());

        (tryPin ? new AffinityRunnable(w) : w).run();

        die(Thread.currentThread(), false);

    }

//    public void remove(int i) {
//        kill(threads.get(i));
//    }

//    public long[] threadIDs() {
//        return threads.stream().mapToLong(Thread::getId).toArray();
//    }

    private static final class AffinityRunnable implements Runnable {

        final Runnable run;

        AffinityRunnable(Runnable run) {
            this.run = run;
        }

        @Override
        public void run() {
            try (AffinityLock lock = AffinityLock.acquireCore()) {
                run.run();
            } catch (Exception e) {
                logger.warn("Could not acquire affinity lock; executing normally: {} ", e.getMessage());
                run.run();
            }
        }
    }

//    private final class PlainRunnable implements Runnable {
//        private final Runnable w;
//
//        public PlainRunnable(Runnable w) {
//            this.w = w;
//        }
//
//        @Override
//        public void run() {
//
//                w.run();
//
//        }
//    }
}