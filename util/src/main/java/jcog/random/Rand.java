package jcog.random;

/** simplified interface for Random Number Generators that dont necessarily extend java.util.Random and its overhead */
public interface Rand {

    float nextFloat();

    long nextLong();

    default int nextInt() { return nextInt(Integer.MAX_VALUE); }

    int nextInt(int i);

    void setSeed(long s);
}
