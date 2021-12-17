package jcog.time;

import jcog.Str;
import jcog.data.list.Lst;
import jcog.util.HashCachedPair;
import org.HdrHistogram.AtomicHistogram;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UsageNS<X> {

    public final Map<X, AtomicHistogram> usage = new ConcurrentHashMap<>();

    {
        Runtime.getRuntime().addShutdownHook(new Thread(this::print));
    }

    protected void print() {
        print(System.out);
    }

    protected void print(PrintStream out) {
        //FasterList<Pair<X, AtomicHistogram>> l = usage.entrySet().stream().map(e -> Tuples.pair(e.getKey(), e.getValue())).collect(toList());
        Lst<HashCachedPair<X, AtomicHistogram>> fl = new Lst();
        for (Map.Entry<X, AtomicHistogram> entry : usage.entrySet()) {
            X x = entry.getKey();
            AtomicHistogram h = entry.getValue();
            if (h.getTotalCount() > 0) {
                fl.add(new HashCachedPair(x, h.copy()));
            }
        }
        //descending
        fl.sort((Comparator<? super HashCachedPair<X, AtomicHistogram>>) (a, b) -> {
            if (a == b) return 0;
            AtomicHistogram aa = a.getTwo();
            double am = aa.getTotalCount() * aa.getMean();
            AtomicHistogram bb = b.getTwo();
            double bm = bb.getTotalCount() * bb.getMean();
            int abm = Double.compare(bm, am); //descending
            return abm != 0 ? abm : Integer.compare(System.identityHashCode(a), System.identityHashCode(b));
        });
        fl.forEach((xh) -> {

            //out.println(xh.getOne());
            out.println(xh.getTwo().getTotalCount() + "\t*" + Str.n4(xh.getTwo().getMean()) + "\t" + xh.getOne() );
//            AtomicHistogram h = xh.getTwo();
//            if (h.getTotalCount() > 0) {
//                Texts.histogramPrint(h.copy(), out);
//            } else {
//                out.println("none");
//            }
//           out.println();

        });
    }

    public AtomicHistogram the(X x) {
        return usage.computeIfAbsent(x, a ->
                new AtomicHistogram(1,1_000_000_000L, 2));
    }
}
