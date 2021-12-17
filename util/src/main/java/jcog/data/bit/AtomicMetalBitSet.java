package jcog.data.bit;

import jcog.TODO;
import jcog.data.atomic.MetalAtomicIntegerFieldUpdater;

/** atomic integer metal bitset, cap = 32 */
public class AtomicMetalBitSet extends MetalBitSet {

    private static final MetalAtomicIntegerFieldUpdater<AtomicMetalBitSet> X =
            new MetalAtomicIntegerFieldUpdater<>(AtomicMetalBitSet.class, "x");

    private volatile int x;

    @Override
    public int capacity() {
        return 32;
    }

    @Override
    public MetalBitSet negateThis() {
        throw new TODO();
    }

    @Override
    public void setAll(boolean b) {
        setDirect(b ? ~0 : 0);
    }

    @Override
    public boolean test(int i) {
        assert(i < 32);
        return (X.get(this) & (1 << i)) != 0;
    }

    public boolean compareAndSet(int i, boolean expect, boolean set) {
        int mask = 1 << i;
        boolean[] got = {false};
        X.accumulateAndGet(this, mask, (v,m)->{
            if (((v & m) != 0)==expect) {
                got[0] = true;
                return set ? v|m : v&(~m);
            } else {
                return v;
            }
        });
        return got[0];
    }

    @Override
    public MetalBitSet clone() {
        AtomicMetalBitSet a = new AtomicMetalBitSet();
        a.x = x;
        return a;
    }

    @Override
    public void set(int i, boolean v) {
        assert(i < 32);
        int mask = 1<<i;
        if (v)
            X.accumulateAndGet(this, mask, (k, m) -> k | m);
        else
            X.accumulateAndGet(this, ~mask, (k, m) -> k & m );
    }

    public boolean getAndSet(int i) {
        assert(i < 32);
        int mask = 1<<i;
        return ((X.getAndAccumulate(this, mask, (v,m) -> v|m)) & mask) != 0;
        //return (X.getAndUpdate(this, v-> v|(mask) ) & mask) != 0;
    }


    public boolean getAndClear(int i) {
        assert(i < 32);
        int mask = (1<<i);
        int antimask = ~mask;
        return (X.accumulateAndGet(this, antimask, (v,am)-> v&(am) ) & mask) != 0;
    }

    @Override
    public void clear() {
         setDirect(0);
    }

    @Override
    public int cardinality() {
        return Integer.bitCount(X.get(this));
    }

    public void copyFrom(AtomicMetalBitSet copyFrom) {
        setDirect(copyFrom.intDirect());
    }

    public void setDirect(int bitmask) {
        X.set(this, bitmask);
    }

    public int intDirect() {
        return X.get(this);
    }

    public String toBitString() {
        return Integer.toBinaryString(intDirect());
    }

    public boolean getAndSet(short b, boolean pressed) {
        return pressed ? getAndSet(b) : getAndClear(b);
    }

}