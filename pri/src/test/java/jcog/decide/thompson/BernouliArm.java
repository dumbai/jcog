package jcog.decide.thompson;


import java.util.Random;

class BernouliArm {
  public final double conversionRate;
  private final Random random;

  BernouliArm(double conversionRate, Random Random) {
    this.conversionRate = conversionRate;
    this.random = Random;
  }

  public boolean draw() {
    return !(random.nextFloat() > conversionRate);
  }

}
