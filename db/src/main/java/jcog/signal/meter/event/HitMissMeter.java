/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.signal.meter.event;

import jcog.signal.meter.Metered;

import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 *
 * @author me
 */
public class HitMissMeter extends Metered.MapMetrics {

    private boolean autoReset;
    public final LongAdder hit = new LongAdder();
    public final LongAdder miss = new LongAdder();

    public HitMissMeter(String id, boolean autoReset) {
        super(id);
        this.autoReset = autoReset;
        metrics(Map.of(
            "hit", p-> p.set(hit.longValue()),
            "miss", p-> p.set(miss.longValue()),
            "hit ratio",  p-> p.set(hit.longValue() / (hit.longValue() + miss.longValue()))
        ));
    }


    public long hits() {
        return hit.longValue();
    }

    public long misses() {
        return miss.longValue();
    }

    public float ratio() {
        long h = hits();
        long sum = h + misses();
        if (sum == 0) return Float.NaN;
        return h/((float)sum);
    }

    public HitMissMeter(String id) {
        this(id, true);
    }    
    
    public HitMissMeter reset() {
        hit.reset();
        miss.reset();
        return this;
    }

    public void hit() {
        hit.add(1);
    }
    public void miss() {
        miss.add(1);
    }


    public long count() {
        return hits()+misses();
    }

    /** whether to reset the hit count after the count is stored in the Metrics */
    public void setAutoReset(boolean autoReset) {
        this.autoReset = autoReset;
    }
    
    public boolean getAutoReset() { return autoReset; }
    
    
    
}
