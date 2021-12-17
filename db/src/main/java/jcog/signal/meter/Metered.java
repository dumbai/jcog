package jcog.signal.meter;


import com.google.common.collect.Sets;
import jcog.Str;
import jcog.data.iterator.ArrayIterator;
import jcog.data.map.UnifriedMap;
import jcog.io.BinTxt;
import org.HdrHistogram.Histogram;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/** produces a menu of metrics which can be enabled to produce buffered streams of changing values (columns) */
public interface Metered  {

    Map<String,Consumer<MeterReader>> metrics();

    default String meterID() {
        return BinTxt.toString(System.identityHashCode(this));
    }

    /** optional references to sub-components that can also be probed */
    default Iterable<Metered> metricComponents() { return List.of(); }

    /** metric recorder */
    abstract class MeterReader {

        private final Set visited = Sets.newIdentityHashSet();

        protected transient String meter;
        protected transient String metric; //current probe id
        protected transient long now;

        public final void run(long now, Metered... meters) {
            run(now, ArrayIterator.iterable(meters));
        }

        public final void run(long now, Iterable<Metered> meters) {

            this.now = now;

            try {

                starting();

                meters.forEach(this::now);

                stopping();

            } finally {
                visited.clear();
            }
        }

        private void now(Metered m) {

            if (!visited.add(m))
                return;

            _now(m);

            m.metricComponents().forEach(this::now);

        }

        private void _now(Metered m) {

            meter = null;

            m.metrics().forEach((k,v) -> {
               if (meter == null)
                    meter = m.meterID();

               metric = k;
               v.accept(this);

            });

            meter = null;

        }

        public final long now() {
            return now;
        }


        /** called at start of the probe */
        protected void starting() {

        }

        /** called by meters to record the latest value.  implementations can access the transient protected fields of this class for context relating to the invocation */
        public abstract void set(Object value);

        /** called after the probe */
        protected void stopping() {

        }

    }

    class MeterLogger extends MeterReader {

        @Override
        public void set(Object value) {
            System.out.println(meter + "/" + metric + "=" + " " + value);
        }
    }

    /** simplest one metric container */
    abstract class Metric implements Metered, Serializable {
        public final String id;
        public final Consumer<MeterReader> meter;
        private final Map<String, Consumer<MeterReader>> map;

        protected Metric(String id, @Nullable Consumer<MeterReader> meter) {
            this.id = id;
            map = Map.of(id, this.meter = meter == null ? ((Consumer<MeterReader>)this) : meter);
        }

        @Override
        public String meterID() {
            return id;
        }

        @Override
        public Map<String, Consumer<MeterReader>> metrics() {
            return map;
        }
    }

    abstract class AbstractMetric extends Metric implements Consumer<MeterReader> {

        protected AbstractMetric(String id) {
            super(id, null);
        }

    }

    class MapMetrics implements Metered, Serializable {

        private Map<String, Consumer<MeterReader>> signals = Collections.EMPTY_MAP;
        private final String id;

        public MapMetrics(String id) {
            this.id = id;
        }

        public MapMetrics(String id, Map<String, Consumer<MeterReader>> signals) {
            this(id);
            metrics(signals);
        }

        protected void metrics(Map<String, Consumer<MeterReader>> signals) {
            this.signals = signals;
        }

        @Override
        public String meterID() {
            return id;
        }

        @Override
        public Map<String, Consumer<MeterReader>> metrics() {
            return signals;
        }

    }


    class FieldMetricsBuilder {
        /**
         * priority rate of Task processing attempted
         */
        private final Field[] fields;

        @Deprecated public FieldMetricsBuilder(Field[] fields) {
            this.fields = fields;
        }

        public final Metered get(String id, Object o) {
            UnifriedMap<String,Consumer<MeterReader>> m = new UnifriedMap();

            for (Field f : fields) {
                m.put(f.getName(), p->{
                    try {
                        p.set(v(f.get(o)));
                    } catch (IllegalAccessException e) {
                        p.set(e.getMessage());
                    }
                });
            }

            return new MapMetrics(id, Map.copyOf(m));
        }

        /** TODO cache the getter by returning a lambda based on field's type, not instanceof test of the value */
        private static Object v(Object o) {
            if (o instanceof LongSupplier)
                return ((LongSupplier)o).getAsLong();
            else if (o instanceof DoubleSupplier)
                return ((DoubleSupplier)o).getAsDouble();
            else if (o instanceof Object[])
                return Arrays.toString((Object[])o);
            else if (o instanceof float[])
                return Str.n4((float[])o);
            else if (o instanceof Histogram) {
                return Str.histogramString((Histogram)o, true);
            }
            return o;
        }
    }



    /** reflects the (public) fields of a class */
    static FieldMetricsBuilder fieldsOf(Class c) {
        return new FieldMetricsBuilder(
                ReflectionUtils.findFields(c, f -> true, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).stream()
                        .filter(field ->
                                        Modifier.isPublic(field.getModifiers())
                                //!Modifier.isPrivate(field.getModifiers())
                        )
                        //.sorted(Comparator.comparing(Field::getName))
                        .toArray(Field[]::new));
    }

}