package jcog.decide.thompson;

import com.google.common.collect.Lists;
import jcog.random.XorShift128PlusRandom;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


public class BatchedABTestTest {
  @Test
  public void testCorrectArmChosen() {
    int correct = 0;
    int iterations = 1000;
    for (int i = 0; i<= iterations; i++) {
      Random engine = new XorShift128PlusRandom(i);
//      BanditPerformance performance = new BanditPerformance(2);
      BatchedABTest batchedBandit = new BatchedABTest();
      batchedBandit.setRandom(engine);
      BatchedBanditTester tester = new BatchedBanditTester(batchedBandit, engine);
//      if (i % 100 == 0) {
//        System.out.println("Batches complete " + i);
//      }
      correct += tester.getWinningArm();
    }
    assertTrue(correct > iterations * 0.95f);
  }

  @Test
  public void testChiSquareComputation() {
    BanditPerformance performance = new BanditPerformance(Lists.newArrayList(new ObservedArmPerformance(100L, 0L),
        new ObservedArmPerformance(0L, 100L)));
    BatchedABTest batchedABTest = new BatchedABTest();
    batchedABTest.setRequiresMinSamples(false);
    assertEquals(0, batchedABTest.getBanditStatistics(performance).victoriousArm);
    performance = new BanditPerformance(Lists.newArrayList(new ObservedArmPerformance(0L, 100L),
        new ObservedArmPerformance(100L, 0L)));
    batchedABTest = new BatchedABTest();
    batchedABTest.setRequiresMinSamples(false);
    assertEquals(1, batchedABTest.getBanditStatistics(performance).victoriousArm);
    performance = new BanditPerformance(Lists.newArrayList(new ObservedArmPerformance(5L, 5L),
        new ObservedArmPerformance(5L, 5L)));
    batchedABTest = new BatchedABTest();
    batchedABTest.setRequiresMinSamples(false);
    assertFalse(batchedABTest.getBanditStatistics(performance).victoriousArm >= 0);
  }
}
