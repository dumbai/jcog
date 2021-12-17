package jcog.ql.dqn;

/** a replay experience */
public class ReplayMemory {
    public final long t;
    public final float r;
    public final double[] s, s0, a;

    /**
     * https://cs.stanford.edu/people/karpathy/reinforcejs/lib/rl.js
     * learnFromTuple: function(s0, a0, r0, s1, a1)
     */
    ReplayMemory(long t, double[] s0, double[] a, float r, double[] s) {
        this.s0 = s0;
        this.a = a;
        this.r = r;
        this.s = s;
        this.t = t;
    }

    public double[] learn(Policy policy, float alpha) {
        return policy.learn(s0, a, r, s, alpha);
    }

    @Override public int hashCode() {
        return Long.hashCode(t);
    }

//    @Override public boolean equals(Object x) {
//        return this==x;
//    }

}