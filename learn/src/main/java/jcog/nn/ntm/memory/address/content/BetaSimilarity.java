package jcog.nn.ntm.memory.address.content;

import jcog.Util;
import jcog.nn.ntm.control.Unit;


public class BetaSimilarity extends Unit {
    public final Unit beta;

    public final Similarity measure;

    /**
     * Key strength beta
     */
    private final double B;

    public BetaSimilarity(Unit beta, Similarity m) {
        super();
        B = Math.exp((this.beta = beta).value);
        measure = m;
        value = (m != null) ? (B * m.uv.value) : 0.0;
    }

    public BetaSimilarity() {
        this(new Unit(), null);
    }

    public static BetaSimilarity[][] tensor2(int x, int y) {
        return Util.arrayOf(i ->
                Util.arrayOf(j -> new BetaSimilarity(), new BetaSimilarity[y]),
            new BetaSimilarity[x][]);
    }


    public void backward() {

        Unit sim = measure.uv;

        double betaGradient = grad;
        beta.grad += sim.value * B * betaGradient;
        sim.grad += B * betaGradient;

        measure.backwardErrorPropagation();
    }

}