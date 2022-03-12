package jcog.thing;

import jcog.Research;
import org.eclipse.collections.api.tuple.primitive.ObjectBooleanPair;

import java.util.concurrent.atomic.AtomicReference;

import static jcog.thing.Thing.ServiceState.*;
import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/**
 * component of a system.
 *
 * The generic name of 'Part' is to imply generality.
 * this class was originally derived from Guava's "Service" class and related API.
 * but Service implies some server/served activity while in reality this releationship
 * is necessarily relevant or clearly defined.  instead the simpler container/contained
 * relationship does certainly exist.  the components may 'serve' the collection,
 * and at the same time the collection may 'serve' the components.  but from the
 * system's perspective, this becomes subjective ontologically.
 *
 * @param C to whatever may be (whether temporarily or permanently):
 *          dynamically attached
 *          interfaced with
 *          integrated to
 *
 *
 *
 * they are dynamically registerable and de-registerable.
 * multiple instances of the same type of Part may be present unless explicitly restricted by singleton flag.
 *
 * this is meant to be flexible and dynamic, to support runtime metaprogramming, telemetry, performance metrics
 *   accessible to both external control and internal reasoner decision processes.
 *
 * TODO methods of accessing parts by different features:
 *      --function
 *      --class
 *      --name
 *      --etc
 *
 * @param T thing type that it composes
 */
@Research
public abstract class Part<T>  {

    final AtomicReference<Thing.ServiceState> state = new AtomicReference<>(Off);

    public final boolean isOn() {
        return state.getOpaque() == On;
    }

    public final boolean isOnOrStarting() {
        return state.getOpaque().onOrStarting;
    }

    public final boolean isOff() {
        return state.getOpaque() == Off;
    }


    @Override
    public String toString() {
        String nameString = getClass().getName();

        if (nameString.startsWith("jcog.") || nameString.startsWith("nars.")) //HACK
            nameString = getClass().getSimpleName();

        return nameString + ':' + super.toString();
    }

    protected abstract void start(T x);

    protected abstract void stop(T x);



    public Thing.ServiceState state() {
        return state.getOpaque();
    }

//    public void _state(Thing.ServiceState forced) {
//        this.state.set(forced);
//    }
    //TODO ifState(..) cmpAndSet

//    public static final Part[] EmptyArray = new Part[0];

    public final Runnable start(Thing<T, ?> c) {

        ObjectBooleanPair<Part<T>> ENABLED = pair(this, true);

        return ()-> {
            try {

                start(c.id);

                boolean nowOn = state.compareAndSet(OffToOn, On);
                if (!nowOn)
                    throw new UnsupportedOperationException("unable to set state=On");

                c.eventOnOff.accept(ENABLED/*, executor*/);

            } catch (Throwable e) {
                state.set(Off);
                c.error(this, e, "start");
            }
        };
    }
}