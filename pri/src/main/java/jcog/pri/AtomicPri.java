package jcog.pri;

import jcog.Util;
import jcog.signal.MutableFloat;

public abstract class AtomicPri extends MutableFloat implements Prioritizable {
////    private static final AtomicFloatFieldUpdater<AtomicPri> PRI =
////            new MutableFloatFieldUpdater(AtomicPri.class, "pri");
//    private static final VarHandle PRI = Util.VAR(AtomicPri.class, "pri", float.class);


    /** initialized to zero */
    public AtomicPri() {

    }

    public AtomicPri(float p) {
        setPlain(p);
    }

    public AtomicPri(Prioritized x) {
        this(x.pri());
    }

    @Override
    public String toString() {
        return String.valueOf(pri());
    }

    @Override
    public final float priElse(float valueIfDeleted) {
        float f = pri();
        return f == f ? f : valueIfDeleted;
    }

    @Override
    public final float pri() {
        return asFloat();
    }

    @Override protected final float post(float x) {
        return Float.isFinite(x) ? postFinite(x) : Float.NaN;
    }

    private float postFinite(float x) {
        return unit() ? Util.unitizeSafe(x) : Math.max(0, x);
    }

    /** set */
    @Override public final void pri(float p) {
        set(p);
    }

    @Override
    public void priMul(float x) {
        if (x == 0)
            set(0);
        else if (x != 1)
            mul(x);
    }

    @Override
    public void priAdd(float a) {
        if (a!=0)
            add(a);
    }

    /** override and return true if the implementation clamps values to 0..+1 (unit) */
    protected boolean unit() {
        return false;
    }

    @Override
    public boolean delete() {
        return wasAndSetNaN();
        //return wasAndSetReleaseNaN();
    }

}