package jcog.math;

import jcog.Util;

/**
 * Analogous to MutableInteger, but a pair of integers in sorted order
 */
public class MutableIntRange {

    int lo;
    int hi;


    public MutableIntRange() {
        this(0,0);
    }

    public MutableIntRange(int lo, int hi) {
        set(lo, hi);
    }

    public int lo() {
        return lo;
    }

    public int hi() {
        return hi;
    }

    public void lo(int lo) {
        set(lo, hi);
    }

    @Override
    public String toString() {
        return "[" + lo + ".." + hi + ']';
    }

    public void hi(int hi) {
        set(lo, hi);
    }

    public void set(int lo, int hi) {
        if (lo <= hi) {
            this.lo = lo;
            this.hi = hi;
        }  else {
            this.lo = hi;
            this.hi = lo;
        }
    }

    public int lerp(float x) {
        if (x!=x)
            x = 0;
        if (x < 0 || x > 1)
            throw new UnsupportedOperationException("out of range");

        int l = lo;
        int h = hi;
		return l == h ? l : Util.lerpInt(x, lo, hi);
    }

}
