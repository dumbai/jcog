package jcog.decide.thompson;


import com.google.common.collect.Lists;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.random.XorShift128PlusRandom;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;


public class BatchedThompsonSamplingTest {
    @Test
    public void testUpdate() {
        BanditPerformance performance = new BanditPerformance(2);
//        BatchedThompsonSampling bandit = new BatchedThompsonSampling();
        performance.update(Lists.newArrayList(new ObservedArmPerformance(1, 2), new ObservedArmPerformance(3, 4)));
        assertEquals(Lists.newArrayList(new ObservedArmPerformance(1, 2), new ObservedArmPerformance(3, 4)), performance.getPerformances());
        performance.update(Lists.newArrayList(new ObservedArmPerformance(1, 2), new ObservedArmPerformance(3, 4)));
        assertEquals(Lists.newArrayList(new ObservedArmPerformance(2, 4), new ObservedArmPerformance(6, 8)), performance.getPerformances());
        try {
            performance.update(Lists.newArrayList(new ObservedArmPerformance(1, 2), new ObservedArmPerformance(3, 4), new ObservedArmPerformance(5, 6)));
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    @Disabled
    public void testCorrectArmChosen() {
        int correct = 0;
        for (int i = 0; i < 10000; i++) {
            Random rng = new XorShift128PlusRandom(i);
            BatchedThompsonSampling batchedBandit = new BatchedThompsonSampling();
            batchedBandit.setRandom(rng);
            BatchedBanditTester tester = new BatchedBanditTester(batchedBandit, rng);
            if (i % 100 == 0) {
                System.out.println("Batches complete " + i);
            }
            correct += tester.getWinningArm();
        }
        System.out.println(correct);
        assertTrue(correct > 9500);
    }

    @Test
    public void testPerformance() {
        int maxBanditIterations = 0;
        double maxBanditRegret = 0.0;
        for (int i = 51; i <= 60; i++) {
            Random engine = new XoRoShiRo128PlusRandom(i);
            BanditPerformance performance = new BanditPerformance(2);
            BatchedThompsonSampling batchedBandit = new BatchedThompsonSampling();
            batchedBandit.setRandom(engine);
            BatchedBanditTester tester = new BatchedBanditTester(batchedBandit, engine);
            double regret = performance.cumulativeRegret(0.015, Lists.newArrayList(0.01, 0.015));
            maxBanditIterations = max(maxBanditIterations, tester.getIterations());
            maxBanditRegret = max(maxBanditRegret, regret);
            assertEquals(1, tester.getWinningArm());
        }
        int minAbIterations = Integer.MAX_VALUE;
        double minAbRegret = Double.MAX_VALUE;
        for (int i = 51; i <= 60; i++) {
            Random engine = new XoRoShiRo128PlusRandom(i);
            BanditPerformance performance = new BanditPerformance(2);
            BatchedABTest batchedBandit = new BatchedABTest();
            batchedBandit.setRandom(engine);
            BatchedBanditTester tester = new BatchedBanditTester(batchedBandit, engine);
            double regret = performance.cumulativeRegret(0.015, Lists.newArrayList(0.01, 0.015));
            minAbIterations = min(minAbIterations, tester.getIterations());
            minAbRegret = min(minAbRegret, regret);
            assertEquals(1, tester.getWinningArm());
        }
    }

    @Test
    public void testPerformance2() {
        int maxBanditIterations = 0;
        double maxBanditRegret = 0.0;
        for (int i = 51; i <= 60; i++) {
            Random engine = new XoRoShiRo128PlusRandom(i);
            BanditPerformance performance = new BanditPerformance(6);
            BatchedThompsonSampling batchedBandit = new BatchedThompsonSampling();
            batchedBandit.setRandom(engine);
            BatchedBanditTester tester = new BatchedBanditTester(batchedBandit, engine,
                    List.of(new BernouliArm(0.04, engine),
                            new BernouliArm(0.05, engine),
                            new BernouliArm(0.045, engine),
                            new BernouliArm(0.03, engine),
                            new BernouliArm(0.02, engine),
                            new BernouliArm(0.035, engine)));
            double regret = performance.cumulativeRegret(0.05, Lists.newArrayList(0.04, 0.05, 0.045, 0.03, 0.02, 0.035));
            maxBanditIterations = max(maxBanditIterations, tester.getIterations());
            maxBanditRegret = max(maxBanditRegret, regret);
        }
        int minAbIterations = Integer.MAX_VALUE;
        double minAbRegret = Double.MAX_VALUE;
        for (int i = 51; i <= 60; i++) {
            Random engine = new XoRoShiRo128PlusRandom(i);
            BanditPerformance performance = new BanditPerformance(6);
            BatchedABTest batchedBandit = new BatchedABTest();
            batchedBandit.setRandom(engine);
            BatchedBanditTester tester = new BatchedBanditTester(batchedBandit, engine,
                    List.of(new BernouliArm(0.04, engine),
                            new BernouliArm(0.05, engine),
                            new BernouliArm(0.045, engine),
                            new BernouliArm(0.03, engine),
                            new BernouliArm(0.02, engine),
                            new BernouliArm(0.035, engine)));
            double regret = performance.cumulativeRegret(0.05, Lists.newArrayList(0.04, 0.05, 0.045, 0.03, 0.02, 0.35));
            minAbIterations = min(minAbIterations, tester.getIterations());
            minAbRegret = min(minAbRegret, regret);
        }
        System.out.println("Min A/B regret: " + minAbRegret);
        System.out.println("Max Bandit regret: " + maxBanditRegret);
        System.out.println("Min A/B # batches (batch size = 100 samples): " + minAbIterations);
        System.out.println("Max Bandit # batches (batch size = 100 samples): " + maxBanditIterations);
    }
}
