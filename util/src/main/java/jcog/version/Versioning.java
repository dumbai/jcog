package jcog.version;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * versioning context that holds versioned instances
 * a maximum stack size is provided at construction and will not be exceeded
 */
public class Versioning<X> {

    protected final Versioned<X>[] items;
    protected int size = 0;
    protected int assignments = 0;

    public int ttl;

    public Versioning(int stackMax) {
        this.items = new Versioned[stackMax];
        assert (stackMax > 0);
    }

    public Versioning(int stackMax, int initialTTL) {
        this(stackMax);
        setTTL(initialTTL);
    }

    @Override
    public String toString() {
        return size + ":" + super.toString();
    }

    public final boolean revertLive(int before) {
        if (live()) {
            revert(before);
            return true;
        } else
            return false;
    }
    public final void forEach(Consumer<Versioned<X>> each) {
        int s = size;
        if (s <= 0) return;

        Versioned<X>[] i = this.items;

        while (s>0) {
            each.accept(i[--s]);
        }
    }

    /**
     * reverts/undo to previous state
     * returns whether any revert was actually applied
     */
    public final void revert(int when) {
        revert(when, null);
    }

    protected final boolean revert(int when, @Nullable Consumer each) {

        int sizePrev;
        if ((sizePrev = size) <= when)
            return false;

        int sizeNext = sizePrev;
        Versioned[] i = this.items;

        while (sizeNext > when) {
            Versioned x = i[--sizeNext];
            if (each!=null) each.accept(x);
            x.pop();
            if (x instanceof UniVersioned) assignments--;
        }

        this.size = sizeNext;

        Arrays.fill(i, when, sizePrev, null);

        if (sizeNext == 0) assignments = 0; //for safety in case of corruption

        return true;
    }

    public Versioning<X> clear() {
        revert(0);
        return this;
    }

    public final boolean add(/*@NotNull*/ Versioned<X> x) {
        //if (newItem == null)
        //    throw new NullPointerException();

        Versioned<X>[] ii = this.items;
        if (ii.length > this.size) {
            ii[this.size++] = x;
            if (x instanceof UniVersioned) assignments++;
            return true;
        }
        return false; //capacity exceeded
    }

    /**
     * whether the unifier should continue: if TTL is non-zero.
     */
    public final boolean live() {
        return ttl > 0;
    }

    /**
     * spend an amount of TTL; returns whether it is still live
     */
    public final boolean use(int cost) {
        ttl = Math.max(0, ttl - cost);
        return live();
    }

    public final void setTTL(int ttl) {
//        assert (ttl > 0);
        this.ttl = ttl;
    }

    /**
     * stack height counter
     */
    public final int size() {
        return size;
    }


    public final void pop() {
        revert(size - 1);
    }


}
