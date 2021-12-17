package jcog.pri;

/**
 * reverse osmosis read-only budget
 */
public final class PriRO implements Prioritized {

    private final float pri;

    public PriRO(float pri) {
        this.pri = pri;
    }

    @Override
    public final float pri() {
        return pri;
    }

    @Override
    public final String toString() {
        return getBudgetString();
    }

}
