package jcog.decide.thompson;

import com.google.common.base.Charsets;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import jcog.random.XoRoShiRo128PlusRandom;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static java.lang.String.format;

class MainTest {
  private final Random engine = new XoRoShiRo128PlusRandom(-1);

  private interface BanditCreator {
    BatchedBandit bandit();
  }

//  @Test
//  @Disabled
//  public void testAll() throws IOException {
//    ImmutableList<Double> mainArmWeights =
//        ImmutableList.<Double>builder().add(0.04, 0.05, 0.045, 0.03, 0.02, 0.035).build();
//    for (int i = 2; i <= 6; i++) {
//      List<Double> armWeights = mainArmWeights.subList(0, i);
//      BanditCreator creator = new BanditCreator() {
//        @Override
//        public BatchedBandit bandit() {
//          BatchedABTest bandit = new BatchedABTest();
//          return bandit;
//        }
//      };
//      String name = String.format("full_ab_%d", i);
//      banditTest(armWeights, creator, name);
//    }
//    for (int i = 2; i <= 6; i++) {
//      List<Double> armWeights = mainArmWeights.subList(0, i);
//      BanditCreator creator = new BanditCreator() {
//        @Override
//        public BatchedBandit bandit() {
//          PairwiseAbTest bandit = new PairwiseAbTest();
//          return bandit;
//        }
//      };
//      String name = String.format("pairwise_ab_%d", i);
//      banditTest(armWeights, creator, name);
//    }
//    for (int i = 2; i <= 6; i++) {
//      List<Double> armWeights = mainArmWeights.subList(0, i);
//      BanditCreator creator = new BanditCreator() {
//        @Override
//        public BatchedBandit bandit() {
//          BatchedThompsonSampling bandit = new BatchedThompsonSampling();
//          return bandit;
//        }
//      };
//      String name = String.format("thompson_arms_%d", i);
//      banditTest(armWeights, creator, name);
//    }
//  }

  @Test
  @Disabled
  void testFriday() throws IOException {
    ImmutableList<Double> mainArmWeights =
        ImmutableList.<Double>builder().add(0.01, 0.015).build();
    for (int i = 2; i <= 2; i++) {
      List<Double> armWeights = mainArmWeights.subList(0, i);
      BanditCreator creator = () -> {
        BatchedABTest bandit = new BatchedABTest();
        return bandit;
      };
      String name = String.format("full_ab_%d", i);
      banditTest(armWeights, creator, name);
    }
    for (int i = 2; i <= 2; i++) {
      List<Double> armWeights = mainArmWeights.subList(0, i);
      int iSafe = i;
      BanditCreator creator = () -> {
        BatchedThompsonSampling bandit = new BatchedThompsonSampling();
        return bandit;
      };
      String name = String.format("thompson_arms_%d", i);
      banditTest(armWeights, creator, name);
    }
  }

  private void banditTest(List<Double> armWeights, BanditCreator creator, String name) throws IOException {
    File file = new File(format("/tmp/bandit-results/%s.csv", name));
    Files.createParentDirs(file);
    Files.touch(file);
    List<BernouliArm> arms = FluentIterable.from(armWeights).transform(aDouble -> new BernouliArm(aDouble, engine)).toList();
    for (int i = 0; i < 10000; i++) {
      BatchedBanditTester tester = new BatchedBanditTester(creator.bandit(), engine, arms);
      String l = format("%d,%d,%f\n", tester.getWinningArm(), tester.getIterations(), tester.getCumulativeRegret());
      Files.append(l, file, Charsets.UTF_8);
    }
  }

  @Test
  void computeWeights() {
    long uniqueExitToInvite = 465;
    long uniqueControl = 242 + 214;
    long sentInviteExitToInvite = 48;
    long sentInviteControl = 16 + 18;
    ObservedArmPerformance exitToInvite = new ObservedArmPerformance(sentInviteExitToInvite,
        uniqueExitToInvite - sentInviteExitToInvite);
    ObservedArmPerformance control = new ObservedArmPerformance(sentInviteControl, uniqueControl - sentInviteControl);
    BatchedThompsonSampling bandit = new BatchedThompsonSampling();
    BanditPerformance performance = new BanditPerformance(Lists.newArrayList(exitToInvite, control));
//    System.out.println(bandit.getBanditStatistics(performance));

    uniqueExitToInvite = 474;
    uniqueControl = 243 + 218;
    sentInviteExitToInvite = 48;
    sentInviteControl = 16 + 20;
    exitToInvite = new ObservedArmPerformance(sentInviteExitToInvite,
        uniqueExitToInvite - sentInviteExitToInvite);
    control = new ObservedArmPerformance(sentInviteControl, uniqueControl - sentInviteControl);
    performance = new BanditPerformance(Lists.newArrayList(exitToInvite, control));
    bandit = new BatchedThompsonSampling();
//    System.out.println(bandit.getBanditStatistics(performance));


  }
}