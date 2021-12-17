//package thompsonsampling;
//
//import com.google.common.base.Function;
//import com.google.common.collect.FluentIterable;
//import com.google.common.collect.Lists;
//import jcog.random.XoRoShiRo128PlusRandom;
//import org.hipparchus.random.MersenneTwister;
//import org.junit.jupiter.api.Test;
//import thompsonsampling.BatchedBanditTester;
//import thompsonsampling.BernouliArm;
//import thompsonsampling.PairwiseAbTest;
//
//import java.util.List;
//import java.util.Random;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//
//public class PairwiseAbTestTest {
//  @Test
//  public void testCorrectness() {
//    List<Double> weights = Lists.newArrayList(0.04, 0.05, 0.045, 0.03, 0.02, 0.035);
//    final Random engine = new XoRoShiRo128PlusRandom(-1);
//    List<BernouliArm> armWeights = FluentIterable.from(weights).transform(aDouble -> new BernouliArm(aDouble, engine)).toList();
//    BatchedBanditTester tester = new BatchedBanditTester(new PairwiseAbTest(), engine, armWeights);
//    assertEquals(1, tester.getWinningArm());
//    weights = Lists.newArrayList(0.04, 0.02, 0.045, 0.03, 0.05, 0.035);
//    armWeights = FluentIterable.from(weights).transform(aDouble -> new BernouliArm(aDouble, engine)).toList();
//    tester = new BatchedBanditTester(new PairwiseAbTest(), engine, armWeights);
//    assertEquals(4, tester.getWinningArm());
//  }
//}
