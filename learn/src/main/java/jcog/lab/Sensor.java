package jcog.lab;

import jcog.table.DataTable;

import java.util.function.Function;

/** data source.
 *  produces an instantaneous observation instance detected in the experiment
 */
public abstract class Sensor<E, S> implements Function<E,S>, Comparable<Sensor> {
    /** sensor ID */
    public final String id;

    protected Sensor(String id) {
        this.id = id;
    }

    /** absolute */
    public static final NumberSensor unixtime = new NumberSensor("time_ms") {
            @Override
            public Long apply(Object o) {
                return System.currentTimeMillis();
            }
        };

    /** relative to start */
    public static NumberSensor nanotime() {
        return new NumberSensor("time_ns") {
            final long start = System.nanoTime();

            @Override
            public Long apply(Object o) {
                return System.nanoTime() - start;
            }
        };
    }

    public static <X> LabelSensor<X> label(String id) {
        return new LabelSensor<>(id);
    }

//        @Nullable
//        abstract S apply(E x);

    public abstract void register(DataTable data);

    @Override
    public final int compareTo(Sensor s) {
        return id.compareTo(s.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }



}