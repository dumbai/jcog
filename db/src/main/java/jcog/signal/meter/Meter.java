//package jcog.signal.meter;
//
//import org.eclipse.collections.impl.bag.mutable.HashBag;
//
//import java.util.function.Consumer;
//
///**
// * use this to meter Monitor fields of a class, ex:
// *
// * public final Counter deriveEval = new FastCounter("event");
//
// */
//@FunctionalInterface
//public interface Meter { ;
//
//
////    static MonitorConfig meter(String name) {
////        return MonitorConfig.builder(name).build();
////    }
////
////    default Runnable printer(PrintStream out) {
////        return printer(new FastMonitorRegistry(this), out);
////    }
////
////    default Runnable getter(Supplier<Map<String,Object>> each) {
////        return getter(new FastMonitorRegistry(this), each);
////    }
////
////
////    default Runnable printer(FastMonitorRegistry reg, PrintStream p) {
////        return new PollRunnable(
////
////                new BaseMetricPoller() {
////
////                    @Override
////                    public List<Metric> pollImpl(boolean reset) {
////                        return reg.getMetrics(clock().now());
////                    }
////                },
////                BasicMetricFilter.MATCH_ALL,
////                new MetricsPrinter(name(), clock(), p)
////        );
////    }
////    default Runnable getter(MonitorRegistry reg, Supplier<Map<String,Object>> p) {
////        return new PollRunnable(
////                new MonitorRegistryMetricPoller(reg),
////                BasicMetricFilter.MATCH_ALL,
////                new MetricsMapper(name(), clock(), p)
////        );
////    }
////
////    Clock clock();
////    String name();
//
//    class ReasonCollector implements Consumer {
//        HashBag<Object> reasons = new HashBag();
//
//        {
//         Runtime.getRuntime().addShutdownHook(new Thread(()->{
////             synchronized (Thread.class) {
//                 reasons.topOccurrences(8).forEach((x) -> System.out.println(x.getTwo() + "\t" + x.getOne()));
////             }
//         }));
//        }
//
//        @Override
//        public void accept(Object e) {
//            reasons.add(e);
//        }
//    }
//}
