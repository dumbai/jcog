package jcog.nn.ntm.memory.address.content;

import jcog.nn.ntm.control.Unit;

/** a similarity measurement */
class Similarity {
    private final ISimilarityFunction sim;
    public final Unit uv;
    private final Unit[] u, v;

    Similarity(ISimilarityFunction sim, Unit[] u, Unit[] v) {
        this.sim = sim;
        this.u = u;
        this.v = v;
        uv = sim.apply(u, v);
    }

    public void backwardErrorPropagation() {
        sim.differentiate(uv, u, v);
    }

}