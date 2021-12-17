package jcog.decide.thompson;

public class ObservedArmPerformance {
  public long successes() {
    return successes;
  }

  public long failures() { return failures; }

  private long successes;
  private long failures;

  public ObservedArmPerformance(long successes, long failures) {
    this.successes = successes;
    this.failures = failures;
  }

  public ObservedArmPerformance add(ObservedArmPerformance that) {
    successes += that.successes;
    failures += that.failures;
    return this;
  }

  public ObservedArmPerformance addSuccess() {
    successes += 1;
    return this;
  }

  public ObservedArmPerformance addFailure() {
    failures += 1;
    return this;
  }

  @Override
  public int hashCode() {
    return 997 * Long.hashCode(successes) ^ 991 * Long.hashCode(failures);
  }

  @Override
  public boolean equals(Object obj) {
    if(this==obj) return true;
    if (obj instanceof ObservedArmPerformance that) {
        return successes == that.successes && failures == that.failures;
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("(%d,%d)", successes, failures);
  }
}