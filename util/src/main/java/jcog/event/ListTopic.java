package jcog.event;

import jcog.data.list.FastCoWList;
import jcog.util.CountDownThenRun;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * arraylist implementation, thread safe.  creates an array copy on each update
 * for fastest possible iteration during emitted events.
 */
public class ListTopic<V> extends FastCoWList<Consumer<V>> implements Topic<V> {

    final CountDownThenRun busy = new CountDownThenRun();

    public ListTopic() {
        this(8);
    }

    public ListTopic(int capacity) {
        super(capacity, Consumer[]::new);
    }



    @Override
    public void accept(V x) {
        forEachWith(Consumer::accept, x);
//        final Consumer[] cc = this.array();
//        for (Consumer c: cc)
//            c.accept(x);
    }



    @Override
    public void emitAsync(V x, Executor executorService) {
//        final Consumer[] cc = this.array();
//
//            for (Consumer c: cc)
//                executorService.execute(() -> c.accept(x));
        forEachWith((c,X)->executorService.execute(() -> c.accept(X)), x);
    }

    @Override
    public void emitAsyncAndWait(V x, Executor executorService) throws InterruptedException {
        Consumer[] cc = this.array();
        if (cc != null) {
            int n = cc.length;
            if (n != 0) {
                CountDownLatch l = new CountDownLatch(n);

                for (Consumer c : cc) {
                    executorService.execute(() -> {
                        try {
                            c.accept(x);
                        } finally {
                            l.countDown();
                        }

                    });
                }
                l.await();
            }
        }
    }

    @Override
    public void emitAsync(V x, Executor exe, Runnable onFinish) {
        Consumer[] cc = this.array();
        int n = cc != null ? cc.length : 0;
        switch (n) {
            case 0:
                return;
            case 1: {
                Consumer cc0 = cc[0];
                exe.execute(() -> {
                    try {
                        cc0.accept(x);
                    } finally {
                        onFinish.run();
                    }
                });
                break;
            }
            default: {
                busy.reset(n, onFinish);

                for (Consumer c: cc)
                    exe.execute(busy.run(c, x));
                break;
            }
        }
    }

    @Override
    public void start(Consumer<V> o) {
        //assert (o != null);
        add(o);
    }

    @Override
    public void stop(Consumer<V> o) {
        //assert (o != null);
        remove(o);
    }


}