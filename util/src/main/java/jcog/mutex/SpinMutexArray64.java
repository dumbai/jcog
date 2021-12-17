package jcog.mutex;


import jcog.Util;

import java.util.concurrent.atomic.AtomicLongArray;

/** contains 2 sub-treadmills
 * TODO parameterize the bit which it checks adjustable so these can be chained arbitrarily
 * TODO make SpinMutexArray32 with AtomicIntegerArray instead of AtomicLongArray
 * */
public final class SpinMutexArray64 implements SpinMutex {

    private final Treadmill64[] mutex;
    private final AtomicLongArray buf;

    public SpinMutexArray64(int stripes, int stripeWidth) {
        stripes = Util.largestPowerOf2NoGreaterThan(stripes)*2;
        assert(stripes < (1 << 15));
        assert(stripeWidth < (1 << 15));


        buf = new AtomicLongArray(stripes * stripeWidth);

        mutex = new Treadmill64[stripes];
        for (int i  = 0; i < stripes; i++)
            mutex[i] = new Treadmill64(stripeWidth, i*stripeWidth);
    }


    @Override
    public int start(long hash) {
        int s = (Long.hashCode(hash) & (~(1 << 31))) % mutex.length;
        return mutex[s].start(hash, buf) | (s << 16);
    }

    @Override
    public void end(int slot) {
        buf.setRelease(slot & 0xffff, 0);
    }
}