package jcog.decision;

import com.google.common.base.Joiner;
import jcog.Str;
import jcog.data.list.Lst;

import java.io.PrintStream;
import java.util.Collection;

/**
 * table of float[]'s of uniform length, each column labeled by a unique header (H)
 */
@Deprecated public class FloatTable<H> {

    public final Collection<float[]> rows = new Lst();
    public final H[] cols;

    @SafeVarargs
    public FloatTable(H... cols) {
        this.cols = cols;
    }

    public FloatTable<H> add(float... row) {
        assert (row.length == cols.length);
        rows.add(row);
        return this;
    }

    public FloatTable<H> print(PrintStream out) {
        System.out.println(Joiner.on("\t").join(cols));
        for (float[] row : rows) {
            String s = Str.n4(row);
            out.println(s);
        }
        return this;
    }

    public int size() {
        return rows.size();
    }
}
