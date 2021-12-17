package jcog.predict;

import jcog.Util;
import jcog.random.XoRoShiRo128PlusRandom;

import java.util.Random;

public class RandomPredictor extends DeltaPredictor {
    final int i, o;

    public RandomPredictor(int i, int o) {
        this.i = i;
        this.o = o;
    }

    @Override
    public void putDelta(double[] d, float pri) {
        //return 0;
    }

    @Override
    public double[] get(double[] x) {
        Random rng = new XoRoShiRo128PlusRandom();
        return Util.arrayOf(i -> rng.nextFloat(), new double[o]);
    }

    @Override
    public void clear(Random rng) {

    }
}