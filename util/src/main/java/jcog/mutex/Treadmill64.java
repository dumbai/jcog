package jcog.mutex;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;

/** striping via 64bit (pair of 32bit codes) global exclusion locking via busy spin
 *  on a linear probed atomic array of fixed size */
public final class Treadmill64 extends AtomicInteger {

    private final int size;
    private final int offset;

    /** extra space for additional usage */
    public Treadmill64(int size, int offset) {
        this.size = size;
        this.offset = offset;
    }

    public int start(long hash, AtomicLongArray buf) {
        if (hash == 0) hash = 1; //skip 0

        int end = size + offset;

        while (true) {

            /* optimistic pre-scan determined free slot */
            int jProlly = -1;

            int now = this.getAcquire();

            for (int i = offset; i < end; i++) {
                long v = buf.getAcquire(i);
                if (v == hash)
                    break; //collision
                else if (v == 0) {
                    jProlly = i; //first empty cell candidate
                    break;
                }
            }

            if (jProlly != -1) {
                if (this.weakCompareAndSetRelease(now, now + 1)) { //TODO separate into modIn and modOut?
                    if (buf.weakCompareAndSetRelease(jProlly, 0, hash))
                        return jProlly;

                    for (int j = offset; j < end; j++)
                        if (j != jProlly && buf.weakCompareAndSetRelease(j, 0, hash))
                            return j;
                }
            }

            Thread.onSpinWait();
        }
    }


}