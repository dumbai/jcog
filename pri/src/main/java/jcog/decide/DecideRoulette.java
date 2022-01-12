package jcog.decide;

import jcog.Is;
import jcog.math.FloatSupplier;

import java.util.Random;


@Is("Fitness_proportionate_selection")
public class DecideRoulette implements Decide {

    private final FloatSupplier rngFloat;

    public DecideRoulette(Random rng) {
        this.rngFloat = rng::nextFloat;
    }

    @Override
    public int applyAsInt(float[] value) {
        return Roulette.selectRoulette(value, rngFloat);
    }

}