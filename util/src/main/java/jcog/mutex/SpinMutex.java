package jcog.mutex;

import java.util.function.Supplier;

public interface SpinMutex {

    /** returns ticket to exit */
    default int start(int context, int key) {
        long hash = (((long)context) << 32) | key;
        if (hash == 0) hash = 1; 
        return start(hash);
    }

    /** returns ticket to exit (slot id) */
    int start(long hash);

    void end(int ticketToExit);

    default void run(int context, int key, Runnable what) {
        int ticket = start(context, key);
        try {
            what.run();
        } finally {
            end(ticket);
        }
    }

//    default <X> void runWith(int context, int key, Consumer<X> what, X arg) {
//        int ticket = start(context, key);
//        try {
//            what.accept(arg);
//        } finally {
//            end(ticket);
//        }
//    }

    default <X> X run(int context, int key, Supplier<X> what) {
        int ticket = start(context, key);
        try {
            return what.get();
        } finally {
            end(ticket);
        }
    }


}