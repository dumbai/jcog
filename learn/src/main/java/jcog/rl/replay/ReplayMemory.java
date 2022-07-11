package jcog.rl.replay;

import jcog.rl.Policy;

/** a replay experience */
public class ReplayMemory {
    /** time */
    public final long t;

    /** reward */
    public final float r;

    /** input state */
    public final double[] x;

    /** previous input state */
    public final double[] x0;

    /** previous action */
    public final double[] a;

    /**
     * https://cs.stanford.edu/people/karpathy/reinforcejs/lib/rl.js
     * learnFromTuple: function(s0, a0, r0, s1, a1)
     */
    public ReplayMemory(long t, double[] s0, double[] a, float r, double[] x) {
        this.x0 = s0;
        this.a = a;
        this.r = r;
        this.x = x;
        this.t = t;
    }

    public double[] learn(Policy policy, float alpha) {
        return policy.learn(x0, a, r, x, alpha);
    }

    @Override public int hashCode() {
        return Long.hashCode(t);
    }

//    @Override public boolean equals(Object x) {
//        return this==x;
//    }

}