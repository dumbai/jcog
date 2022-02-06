package jcog.thing;

import jcog.data.map.ConcurrentFastIteratingHashSet;
import jcog.event.Off;
import jcog.event.RunThese;

import java.util.stream.Stream;

/** a part of a Thing, which also manages a collection of locally contained SubParts */
public abstract class Parts<T extends Thing> extends Part<T>  {


    protected final ConcurrentFastIteratingHashSet<SubPart> sub =
        new ConcurrentFastIteratingHashSet<>(SubPart.EmptyArray);


    /**
     * TODO rename 'nar' to 'id' to complete generic-ization
     * TODO weakref? volatile? */
    public transient T nar;

    /**
     * attached resources held until deletion (including while off)
     */
    @Deprecated
    protected final RunThese whenDeleted = new RunThese();

    //    /** register a future deactivation 'off' of an instance which has been switched 'on'. */
    protected final void on(Off... x) {
        for (Off xx : x)
            whenDeleted.add(xx);
    }

    protected final void startLocal() {
        sub.forEach(c -> c.startIn(this));
    }

    protected final void stopSubs() {
        sub.forEach(c -> c.stopIn(this));
    }

    @SafeVarargs
    public final void addAll(SubPart<Part<T>>... local) {
        for (var d : local)
            add(d);
    }

    protected final void finallyRun(Off component) {
//        if (component instanceof Parts)
//            finallyRun(component);
//        else
            whenDeleted.add(component);
    }

    @SafeVarargs
    public final void removeAll(SubPart... dd) {
        for (var d : dd)
            remove(d);
    }

    public final void add(SubPart x) {
        //if (!isOff())
//        if (isOn())
//            throw new UnsupportedOperationException(this + " is not in OFF or OFFtoON state to add sub-part " + x);

        if (this.sub.add(x)) {
            x.startIn(this);
        } else
            throw new UnsupportedOperationException("duplicate local: " + x);
    }

    public final void remove(SubPart x) {
        if (this.sub.remove(x)) {
            x.stopIn(this);
        } else
            throw new UnsupportedOperationException("unknown local: " + x + " in " + this);          //return false;
    }

    public Stream<SubPart> subs() {
        return sub.stream();
    }
}