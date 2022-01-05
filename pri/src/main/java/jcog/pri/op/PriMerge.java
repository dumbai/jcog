package jcog.pri.op;

import jcog.Fuzzy;
import jcog.Util;
import jcog.pri.Prioritizable;
import jcog.pri.Prioritized;
import jcog.util.PriReturn;
import org.eclipse.collections.api.block.function.primitive.FloatFloatToFloatFunction;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Budget merge function, with input scale factor
 */

public enum PriMerge implements BiConsumer<Prioritizable, Prioritized>, FloatFloatToFloatFunction {

    plus {
        @Override
        public float valueOf(float e, float i) {
            return e + i;
        }
    },
    minus {
        @Override
        public float valueOf(float e, float i) {
            return e - i;
        }
    },
    mean {
        @Override
        public float valueOf(float e, float i) {
            return Util.mean(e, i);
        }

        @Override
        public double apply(double e, double i) {
            return Util.mean(e, i);
        }

    },
    meanGeo {
        @Override
        public float valueOf(float e, float i) {
            return Fuzzy.meanGeo(e, i);
        }
    },
    and {
        @Override
        public double apply(double e, double i) {
            return Fuzzy.and(e, i);
        }

        @Override
        protected boolean undelete() {
            return false;
        }
    },
    or {
        @Override
        public double apply(double e, double i) {
            return Fuzzy.or(e, i);
        }
    },
    min {
        @Override
        public double apply(double e, double i) {
            return Math.min(e, i);
        }
    },
    max {
        @Override
        public double apply(double e, double i) {
            return Util.max(e, i);
        }
    },
    einsteinSum {
        @Override public double apply(double e, double i) {
            return Fuzzy.einsteinSum(e,i);
        }
    },
    replace {
        @Override
        public float valueOf(float e, float i) {
            return i;
        }

        @Override
        public double apply(double e, double i) { return i; }

        @Override
        protected boolean ignoreDeletedIncoming() {
            return false;
        }
    }
    //    AVG_GEO,
    //    AVG_GEO_SLOW, //adds momentum by includnig the existing priority as a factor twice against the new value once
    //    AVG_GEO_FAST,

    ;

    /** either float or double form must be implemented */
    @Override public float valueOf(float e, float i) {
        return (float) apply(e, i);
    }

    /** either float or double form must be implemented */
    public double apply(double e, double i) {
        return valueOf((float)e, (float)i);
    }


    @Override
    public final void accept(Prioritizable existing, Prioritized incoming) {
        apply(existing, incoming.pri());
    }


    /**
     * merge 'incoming' budget (scaled by incomingScale) into 'existing'
     */
    public final float apply(Prioritizable existing, float incoming, @Nullable PriReturn mode) {
        if (mode == null) {
            //HACK
            apply(existing, incoming);
            return Float.NaN;
        }

        if (incoming != incoming && ignoreDeletedIncoming())
            return 0;

        return applyMerging(existing, incoming, mode);
    }

    public final void apply(Prioritizable existing, float incoming) {
        if (incoming == incoming || !ignoreDeletedIncoming()) {
            float x = existing.pri();
            existing.pri(this.valueOf(preFilter(x), incoming));
        }
    }

    /**
     * NaN?
     */
    private float preFilter(float x) {
        return (x == x) ? x : (undelete() ? 0 : Float.NaN);
    }

    protected boolean ignoreDeletedIncoming() {
        return true;
    }

    /**
     * if the existing value is deleted, whether to undelete (reset to zero)
     */
    protected boolean undelete() {
        return true;
    }

//    protected boolean commutative() {
//        throw new TODO();
//    }

    /**
     * merges for non-NaN 0..1.0 range
     */
    public final float mergeUnitize(float existing, float incoming) {
        if (existing != existing)
            existing = 0;
        float next = valueOf(existing, incoming);
        if (next == next) {
            if (next > 1) next = 1;
            else if (next < 0) next = 0;
        } else
            next = 0;
        return next;
    }

    public final float delta(Prioritizable p, float incomingPri) {
        return apply(p, incomingPri, PriReturn.Delta);
    }

    protected float applyMerging(Prioritizable existing, float incoming, PriReturn mode) {
        final float x = existing.pri();
        final float pBefore = preFilter(x);
        existing.pri( PriMerge.this.valueOf(pBefore, incoming) );
        return mode.apply(incoming, pBefore, existing.pri());
    }


//
//    /**
//     * sum priority
//     */
//    PriMerge<Prioritizable,Prioritized> plus = (tgt, src) -> merge(tgt, src, PLUS);
//
//    /**
//     * avg priority
//     */
//    PriMerge<Prioritizable,Prioritized> avg = (tgt, src) -> merge(tgt, src, AVG);
//
//    PriMerge<Prioritizable,Prioritized> or = (tgt, src) -> merge(tgt, src, OR);
//
//
//    PriMerge<Prioritizable,Prioritized> max = (tgt, src) -> merge(tgt, src, MAX);
//
//    /**
//     * avg priority
//     */
//    PriMerge<Prioritizable,Prioritized> replace = (tgt, src) -> tgt.pri((FloatSupplier)()-> src.pri());


//    PriMerge<Prioritizable,Prioritized> avgGeoSlow = (tgt, src) -> merge(tgt, src, AVG_GEO_SLOW);
//    PriMerge<Prioritizable,Prioritized> avgGeoFast = (tgt, src) -> merge(tgt, src, AVG_GEO_FAST);
//    PriMerge<Prioritizable,Prioritized> avgGeo = (tgt, src) -> merge(tgt, src, AVG_GEO); //geometric mean


}