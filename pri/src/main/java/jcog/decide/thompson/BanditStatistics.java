package jcog.decide.thompson;

import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

import java.util.Random;

public class BanditStatistics {
    public final DoubleArrayList armWeights;
    public final int victoriousArm;

    /** @param victoriousArm or -1 if none */
    public BanditStatistics(DoubleArrayList armWeights, int victoriousArm) {
        this.armWeights = armWeights;
        this.victoriousArm = victoriousArm;
    }

    public int pickArm(Random engine) {
        double p = engine.nextFloat();
        double total = 0;
        int s = armWeights.size();
        int i;
        for (i = 0; i < s; i++) {
            total += armWeights.get(i);
            if (p < total)
                break;
        }
        return i;
    }

    @Override
    public String toString() {
        String weights = "weights: (" + armWeights + ")";
        String winningArm = "unknown";
        if (victoriousArm>=0) {
            winningArm += victoriousArm;
        }
        return weights + ", winner: " + winningArm;
    }
}
