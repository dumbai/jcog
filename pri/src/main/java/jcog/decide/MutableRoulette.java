package jcog.decide;

import jcog.Is;
import jcog.TODO;
import jcog.Util;
import jcog.pri.Prioritizable;
import jcog.util.ArrayUtil;
import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;
import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;
import org.eclipse.collections.api.block.procedure.primitive.IntProcedure;

import java.util.Random;
import java.util.function.IntPredicate;

/**
 * efficient embeddable roulette decision executor.
 * takes a weight-update function applied to the current
 * choice after it has been selected in order to influence
 * its probability of being selected again.
 * <p>
 * if the update function returns zero, then the choice is
 * deactivated and can not be selected again.
 * <p>
 * when no choices remain, it exits automatically.
 */
@Is("Fitness_proportionate_selection") public class MutableRoulette {

    private static final float EPSILON = Prioritizable.EPSILON;

    private int n;
    /**
     * weights of each choice
     */
    private float[] w;
    private final Random rng;
    /**
     * weight update function applied between selections to the last selected index's weight
     */
    private final FloatToFloatFunction weightUpdate;
    /**
     * current index (roulette ball position)
     */
    private int i;
    /**
     * current weight sum
     */
    private float weightSum;
    /**
     * current # entries remaining above epsilon threshold
     */
    private int remaining;
    private boolean direction;

    /**
     * with no weight modification
     */
    public MutableRoulette(int count, IntToFloatFunction initialWeights, Random rng) {
        this(count, initialWeights, (x -> x), rng);
    }

    private MutableRoulette(int count, IntToFloatFunction initialWeights, FloatToFloatFunction weightUpdate, Random rng) {
        this(Util.floatArrayOf(initialWeights, count), count, weightUpdate, rng);
    }

    private MutableRoulette(float[] w, FloatToFloatFunction weightUpdate, Random rng) {
        this(w, w.length, weightUpdate, rng);
    }

    public MutableRoulette(FloatToFloatFunction weightUpdate, Random rng) {
        this(ArrayUtil.EMPTY_FLOAT_ARRAY, 0, weightUpdate, rng);
    }

    private MutableRoulette(float[] w, int n, FloatToFloatFunction weightUpdate, Random rng) {
        this.rng = rng;
        this.weightUpdate = weightUpdate;
        reset(w, n);
    }

    /** constructs and runs entirely in constructor */
    private MutableRoulette(float[] weights, int n, FloatToFloatFunction weightUpdate, Random rng, IntPredicate choose) {
        this(weights, n, weightUpdate, rng);
        chooseWhile(choose);
    }

    private MutableRoulette chooseWhile(IntPredicate choose) {
        while (next(choose)) { }
        return this;
    }
    public MutableRoulette chooseN(int n, IntProcedure choose) {
        for (int i = 0; i < n; i++)
            next(choose);
        return this;
    }

    public MutableRoulette reset(float[] w) {
        return reset(w, w.length);
    }

    public MutableRoulette reset(float[] w, int n) {
        this.w = w;
        this.n = n;
        reweigh();
        return this;
    }

    private static void realloc(int newSize) {
        throw new TODO();
    }

    private MutableRoulette reweigh(IntToFloatFunction initializer) {
        return reweigh(n, initializer);
    }

    public MutableRoulette reweigh(int n, IntToFloatFunction initializer) {
        assert(n>0);

        if (n!=this.n)
            realloc(n);

        float[] w = this.w;
        for (int i = 0; i < n; i++)
            w[i] = initializer.valueOf(i);

        return reweigh();
    }

    public MutableRoulette reweigh() {
        int remaining = this.n;
        float s = 0;

        int nn = remaining;
        float[] w = this.w;
        for (int i = 0; i < nn; i++) {
            float wi = w[i];
            if (wi < 0 || !Float.isFinite(wi))
                throw new RuntimeException("invalid weight: " + wi);

            if (wi < EPSILON) {
                w[i] = 0;
                remaining--;
            } else {
                s += wi;
            }
        }

        if (remaining == 0 || s < remaining * EPSILON) {
//            //flat, choose all equally
//            Arrays.fill(w, Prioritized.EPSILON);
//            s = Prioritized.EPSILON * remaining;
//            this.remaining = this.n;
            this.remaining = 0;
        } else {
            this.remaining = remaining;
        }

        this.weightSum = s;

        int n = this.n;
        if (n > 1 && remaining > 1) {
//            this.direction = rng.nextBoolean();
//            this.i = rng.nextInt(l);

            int r = rng.nextInt(); //using only one RNG call
            this.direction = r >= 0;
            this.i = (r & 0b01111111111111111111111111111111) % n;
        } else {
            this.direction = true;
            this.i = 0;
        }

        return this;
    }


    /**
     * weight array may be modified
     */
    public static void run(float[] weights, int n, Random rng, FloatToFloatFunction weightUpdate, IntPredicate choose) {
        switch (n) {
            case 0:
                return;
            case 1:
                float theWeight = weights[0];
                while (choose.test(0) && ((theWeight = weightUpdate.valueOf(theWeight)) > EPSILON)) { }
                break;
            //TODO optimized 2-ary case
            default:
                new MutableRoulette(weights, n, weightUpdate, rng, choose);
                break;
        }
    }

    private boolean next(IntPredicate select) {
        int n = next();
        return n >= 0 && select.test(n) && remaining > 0;
    }
    private boolean next(IntProcedure select) {
        int n = next();
        if (n == -1)
            return false;
        select.accept(n);
        return remaining > 0;
    }

    public final int next() {

        return switch (remaining) {
            case 0 -> -1;
            case 1 -> next1();
            default -> nextN();
        };

    }

    private int nextN() {
        float distance = rng.nextFloat() * weightSum;

        float[] w = this.w;
        int i = this.i;
        float wi;
        int count = this.n;

        int idle = count+1; //to count eextra step
        do {
            if (--idle < 0)
                return -1; //HACK emergency bailout

            wi = w[i = Util.next(i, direction, count)];
            if (wi == 0)
                continue; //skip selection, in case distance ~= 0
            distance -= wi;
        } while (distance > 0);


        float nextWeight = weightUpdate.valueOf(wi);
        if (!validWeight(nextWeight)) {
            w[i] = 0;
            weightSum -= wi;
            remaining--;
        } else if (nextWeight != wi) {
            w[i] = nextWeight;
            weightSum += nextWeight - wi;
        }

        return this.i = i;
    }

    private int next1() {
        float[] w = this.w;
        int count = n;
        for (int x = 0; x < count; x++) {
            float wx = w[x];
            if (wx >= EPSILON) {
                float nextWeight = weightUpdate.valueOf(wx);
                if (!validWeight(nextWeight)) {
                    w[x] = 0;
                    remaining = 0;
                } else {
                    w[x] = nextWeight;
                }
                return x;
            }
        }

        throw new RuntimeException();
    }

    private static boolean validWeight(float nextWeight) {
        return nextWeight==nextWeight /*!NaN*/ && nextWeight >= EPSILON;
    }

    /** weight sum */
    public float weightSum() {
        return weightSum;
    }

    public int size() {
        return n;
    }

    public void zero(int n) {
        w[n] = 0;
        reweigh();
    }
}