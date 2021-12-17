package jcog.lab.util;

import jcog.data.list.Lst;
import jcog.lab.Lab;
import jcog.lab.Sensor;
import jcog.table.ARFF;
import jcog.table.DataTable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * the collected data associated with a subject's (X) execution of an experiment or episode
 *
 * the integration of a subject, a repeatable procedure, and measurement schema
 * <p>
 * contains:
 * -all or some of the Lab's sensors
 * -executable procedure for applying the starting conditions to the subject via
 * some or all of the variables
 * -executable schedule for recording sensor measurements, with at least
 * the start and ending state enabled by default. TODO
 */
public class Experiment<X> implements Runnable {


    /**
     * data specific to this experiment; can be merged with multi-experiment
     * data collections later
     */
    public final DataTable data;
    private final BiConsumer<X, Experiment<X>> procedure;
    /**
     * enabled sensors
     */
    private final List<Sensor<X, ?>> sensors;
    private final Supplier<X> subjectBuilder;
//    private X subject = null;

    public Experiment(Supplier<X> subjectBuilder, DataTable data, List<Sensor<X, ?>> sensors, BiConsumer<X, Experiment<X>> procedure) {
        this.subjectBuilder = subjectBuilder;
        this.procedure = procedure;
        this.data = data;
        this.sensors = sensors;
    }

    public Experiment(Supplier<X> subjectBuilder, Iterable<Sensor<X,?>> sensors, BiConsumer<X, Experiment<X>> procedure) {
        this(subjectBuilder, newData(sensors), new Lst<>(sensors), procedure);
    }


    /**
     * creates a new ARFF data with the headers appropriate for the sensors
     */
    public static <X> DataTable newData(Iterable<Sensor<X,?>> sensors) {
        DataTable data = new ARFF();
        for (Sensor<X, ?> s : sensors) s.register(data);
        return data;
    }

    public void run() {
//        long startTime = System.currentTimeMillis();

        try {
            procedure.accept(subjectBuilder.get(), this);
        } catch (RuntimeException t) {
            //sense(t.getMessage());
            t.printStackTrace(); //TODO
        }

//        long endTime = System.currentTimeMillis();

//        if (data != null) ((ARFF) data).setComment(subject + ": " + procedure +
//                "\t@" + startTime + ".." + endTime + " (" + new Date(startTime) + " .. " + new Date(endTime) + ')');
    }

    public Object[] record(X subject) {
        return Lab.record(subject, data, sensors);
    }

    public void runSerial(int iters) {
        for (int i = 0; i < iters; i++)
            run();
    }

    public void runParallel(int iters/*, int threads*/) {
        IntStream.range(0, iters).parallel().forEach(z -> run());
    }

}