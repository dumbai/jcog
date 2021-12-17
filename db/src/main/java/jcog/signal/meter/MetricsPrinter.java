//package jcog.signal.meter;
//
//import com.netflix.servo.Metric;
//import com.netflix.servo.publish.BaseMetricObserver;
//import com.netflix.servo.util.Clock;
//
//import java.io.PrintStream;
//import java.util.List;
//
///**
// * Writes observations to a file. The format is a basic text file with tabs
// * separating the fields.
// */
//public class MetricsPrinter extends BaseMetricObserver {
//
//    private final Clock clock;
//    private final PrintStream out;
//
//
//    /**
//     * Creates a new instance that stores files in {@code dir} with a name that
//     * is created using {@code namePattern}.
//     *
//     * @param name        name of the observer
//     * @param namePattern date format pattern used to create the file names
//     * @param dir         directory where observations will be stored
//     * @param compress    whether to compress our output
//     * @param clock       clock instance to use for getting the time used in the filename
//     */
//
//    public MetricsPrinter(String name, Clock clock, PrintStream out) {
//        super(name);
//        this.clock = clock;
//        this.out = out;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void updateImpl(List<Metric> metrics) {
//        out.println(clock.now());
//        for (Metric m : metrics) {
//            out.append(m.getConfig().getName()).append('\t')
//                    .append(m.getValue().toString()).append('\t')
//                    .append(m.getConfig().getTags().toString()).append('\n');
//        }
//    }
//}
