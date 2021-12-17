package jcog.exe;

/**
 * like an Iterator, but for unbounded iterations,
 * with both synchronous and asynchronous execution methods
 *
 * calls the Runnable.run() method each cycle/iteration.
 */
public interface Cycled extends Runnable {

    /**
     * run asynchronously with specified delay (milliseconds)
     */
    Loop startPeriodMS(int periodMS);

//    /**
//     * runs until the returned AtomicBoolean is false
//     */
//    default AtomicBoolean runUntil() {
//        AtomicBoolean kontinue = new AtomicBoolean(true);
//        while (kontinue.get())
//            run();
//        return kontinue;
//    }

    /**
     * runs a specified number of cycles
     */
    default void run(int cycles) {
        for (; cycles > 0; cycles--)
            run();
    }

    default void run(int cycles, Runnable afterEachCycle) {
        for (; cycles > 0; cycles--) {
            run();
            afterEachCycle.run();
        }
    }

//    /**
//     * run while the supplied predicate returns true
//     */
//    default void runWhile(BooleanSupplier test) {
//        while (test.getAsBoolean()) {
//            run();
//        }
//    }

    /**
     * run asynchronously with no delay
     */
    default Loop start() {
        return startPeriodMS(0);
    }

    /**
     * run asynchronously at specified FPS
     */
    default Loop startFPS(float initialFPS) {
        assert (initialFPS >= 0);

        float millisecPerFrame = initialFPS > 0 ? 1000.0f / initialFPS : 0 /* infinite speed */;
        return startPeriodMS(Math.round(millisecPerFrame));
    }


    /*
      public static DurLoop onWhile(NAR nar, Predicate<NAR> r) {
        return new DurLoop(nar) {
            @Override
            protected void run(NAR n, long dt) {
                if (!r.test(n)) {
                    off();
                }
            }

            @Override
            public String toString() {
                return r.toString();
            }
        };
    }
     */

}
