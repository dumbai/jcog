package jcog.nn.ntm.memory.address.content;

import jcog.Util;
import jcog.nn.ntm.control.Unit;

import static jcog.Util.fma;

public class CosineSimilarityFunction implements ISimilarityFunction {

    double uv, uNorm, vNorm;

    @Override
    public Unit apply(Unit[] u, Unit[] v) {
        double uv = 0;
        double normalizedU = 0, normalizedV = 0;

        for (int i = 0; i < u.length; i++) {
            double uV = u[i].value, vV = v[i].value;
            uv = fma(uV, vV, uv);
            normalizedU = fma(uV, uV, normalizedU);
            normalizedV = fma(vV, vV, normalizedV);
        }
        normalizedU = Math.sqrt(normalizedU);
        normalizedV = Math.sqrt(normalizedV);

        double denom = normalizedU * normalizedV;

        double value;
        value = uv / denom;
        if (!Double.isFinite(value))
            value = Util.equals(uv, denom, Float.MIN_NORMAL) ? 1 : 0; //TODO
//        if (Math.abs(denom) < Double.MIN_NORMAL)
//            value = 0;
//        else {
//
//
////                throw new NumberException("Cosine Similarity NaN", value);
//        }

        this.uNorm = normalizedU;
        this.vNorm = normalizedV;
        this.uv = uv;

        return new Unit(value);
    }

    @Override
    public void differentiate(Unit similarity, Unit[] uVector, Unit[] vVector) {
        double uvuu = uv / Util.sqr(uNorm);
        double uvvv = uv / Util.sqr(vNorm);
        double uvg = similarity.grad / (uNorm * vNorm);
        int n = uVector.length;
        for (int i = 0; i < n; i++) {
            Unit ui = uVector[i], vi = vVector[i];
            double u = ui.value, v = vi.value;
            ui.grad = fma((v - (u * uvuu)), uvg, ui.grad);
            vi.grad = fma((u - (v * uvvv)), uvg, vi.grad);
        }
    }

}