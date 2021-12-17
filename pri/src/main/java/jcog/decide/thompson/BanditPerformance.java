package jcog.decide.thompson;

import com.google.common.collect.Lists;

import java.util.List;

public class BanditPerformance {
  private final List<ObservedArmPerformance> performances;

  public BanditPerformance(List<ObservedArmPerformance> performances) {
    this.performances = performances;
  }

  public BanditPerformance(int numberOfArms) {
    this(Lists.newArrayListWithCapacity(numberOfArms));
    for (int i = 0; i < numberOfArms; i++) {
      performances.add(new ObservedArmPerformance(0, 0));
    }
  }

  public List<ObservedArmPerformance> getPerformances() {
    return performances;
  }

  public void update(List<ObservedArmPerformance> newPerformances) {
    int s = newPerformances.size();
    if (s != performances.size()) {
      throw new IllegalArgumentException(String.format("Wrong number of arms given: expected %d.",
          performances.size()));
    }
    for (int i = 0; i < s; i++) {
      performances.set(i, performances.get(i).add(newPerformances.get(i)));
    }
  }

  public double cumulativeRegret(double bestArmPerformance, List<Double> allPerformances) {
    int arms = allPerformances.size();
    double cumulativeRegret = 0.0;
    for (int j = 0; j < arms; j++) {
      long samples = performances.get(j).failures() + performances.get(j).successes();
      double armLoss = samples * (bestArmPerformance - allPerformances.get(j));
      cumulativeRegret += armLoss;
    }
    return cumulativeRegret;
  }

  public List<Double> getExpectedConversions() {
    List<Double> results = Lists.newArrayList();
    for (ObservedArmPerformance performance : performances) {
      double alpha = performance.successes() + 1.0;
      double beta = performance.failures() + 1.0;
      results.add(alpha / (alpha + beta));
    }
    return results;
  }

}
