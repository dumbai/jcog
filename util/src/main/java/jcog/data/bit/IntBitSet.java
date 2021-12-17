package jcog.data.bit;

public final class IntBitSet extends MetalBitSet {

    public int x;

    public int intValue() {
        return x;
    }

    @Override
    public void setAll(boolean b) {
        x = b ? ~0 : 0;
    }

    @Override
    public void orThis(MetalBitSet y) {
        if (!(y instanceof IntBitSet)) throw new UnsupportedOperationException();
        x |= ((IntBitSet)y).x;
    }

    @Override
    public void andThis(MetalBitSet y) {
        if (!(y instanceof IntBitSet)) throw new UnsupportedOperationException();
        x &= ((IntBitSet)y).x;
    }

    @Override
    public MetalBitSet clone() {
        IntBitSet i = new IntBitSet();
        i.x = x;
        return i;
    }

    @Override
    public int first(boolean what) {
        int i = Integer.numberOfTrailingZeros(what ? x : ~x);
        return i < 32 ? i : -1; //all filled?
    }

    public int capacity() {
        return 32;
    }

    private static void assertValidBit(int i) {
        if (i < 0 || i > 31)
            throw new ArrayIndexOutOfBoundsException("out-of-bounds bit access");
        //throw new ArrayIndexOutOfBoundsException("out-of-bounds bit access " + i + " in " + IntBitSet.class);
    }

    @Override
    public String toString() {
        return Integer.toBinaryString(x);
    }

    @Override
    public boolean isEmpty() {
        return x == 0;
    }

    @Override
    public boolean test(int i) {
        assertValidBit(i);
        return testFast(i);
    }

    public boolean testFast(int i) {
        return (x & (1 << i)) != 0;
    }

    @Override
    public void set(int i, boolean v) {
        assertValidBit(i);
        int m = 1 << i;
        if (v)
            x |= m;
        else
            x &= ~m;
    }

    @Override
    public void clear() {
        x = 0;
    }

    @Override
    public int cardinality() {
        return Integer.bitCount(x);
    }

    @Override
    public MetalBitSet negateThis() {
        this.x = ~this.x;
        return this;
    }

}