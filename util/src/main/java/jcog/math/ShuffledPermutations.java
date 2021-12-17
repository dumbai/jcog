package jcog.math;

import jcog.data.list.Lst;
import jcog.random.RandomBits;
import jcog.util.ArrayUtil;

import java.util.Collection;

/**
 * Created by me on 11/1/15.
 */
public class ShuffledPermutations extends Permutations {

    /** starting index of the current shuffle */
    byte[] shuffle;


    public ShuffledPermutations restart(int size, RandomBits random) {
        assert(size < 127);

        super.restart(size);

        
        byte[] shuffle = this.shuffle;
        if (shuffle == null || shuffle.length < size)
            this.shuffle = shuffle = new byte[size];

        for (int i = 0; i < size; i++)
            shuffle[i] = (byte)i;
        ArrayUtil.shuffle(shuffle, size, random);


        return this;
    }

    @Override
    public final int permute(int index) {
        return ind[shuffle[index]];
    }

    int[] nextPermute(int[] target) {
        next();
        int l = size;
        for (int i = 0; i < l; i++)
            target[i] = permute(i);
        return target;
    }


    /** TODO improve the buffering characteristics of this
     *  TODO make re-shuffling optional on each new iteration, currently this will shuffle once and it will get repeated if re-iterated
     * */
    public static <X> Iterable<X> shuffle(Iterable<X> i, RandomBits rng) {
        if (i instanceof Collection) {
            int s = ((Collection)i).size();
            if (s < 2) return i;
        }

        Lst<X> f = new Lst<>(i);
        if (f.size() <= 1)
            return i; //unchanged
        else {
            f.trimToSize();
            f.shuffleThis(rng);
            return f;
        }

    }
}
