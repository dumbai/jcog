package jcog.decide.thompson;

import com.google.common.collect.Lists;
import jcog.data.list.Lst;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

import java.util.List;
import java.util.Random;

import static com.google.common.primitives.Doubles.max;

public class BatchedBanditTester {
    private final BatchedBandit bandit;
    private final Random Random;
    private final int winningArm;
    private final double cumulativeRegret;
    private int iteration;

    public BatchedBanditTester(BatchedBandit bandit, Random engine) {
        this(bandit, engine, Lists.newArrayList(new BernouliArm(0.01, engine), new BernouliArm(0.015, engine)));
    }

    public BatchedBanditTester(BatchedBandit bandit, Random engine, List<BernouliArm> arms) {
        this.bandit = bandit;
        this.Random = engine;
        int n = arms.size();
        DoubleArrayList armWeights = new DoubleArrayList(n);
        for (int i = 0; i < n; i++)
            armWeights.add(1.0 / n);

        BanditStatistics currentStatistics = new BanditStatistics(armWeights, -1);

        iteration = 0;
        BanditPerformance performance = new BanditPerformance(n);
        List<ObservedArmPerformance> batchPerformances = new Lst(n);
        while (currentStatistics.victoriousArm<0) {
            batchPerformances.clear();
            for (int i = 0; i < n; i++)
                batchPerformances.add(new ObservedArmPerformance(0, 0));

            for (int i = 0; i < 100; i++) {
                int arm = currentStatistics.pickArm(engine);
                ObservedArmPerformance A = batchPerformances.get(arm);
                if (arms.get(arm).draw()) A.addSuccess();
                else A.addFailure();
            }
            performance.update(batchPerformances);
            currentStatistics = bandit.getBanditStatistics(performance);
            iteration++;
        }
        winningArm = currentStatistics.victoriousArm;
        List<Double> trueConversions = new Lst(n);
        double trueWinner = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            BernouliArm A = arms.get(i);
            trueWinner = max(trueWinner, A.conversionRate);
            trueConversions.add(A.conversionRate);
        }
        cumulativeRegret = performance.cumulativeRegret(trueWinner, trueConversions);
    }

    public int getIterations() {
        return iteration;
    }

    public int getWinningArm() {
        return winningArm;
    }

    public double getCumulativeRegret() {
        return cumulativeRegret;
    }
}
