package jcog.nn.ntm.control;

/**
 * Created by me on 7/20/15.
 */
public class UMatrix {

    public final UVector[] row;

    public UMatrix(int x, int y) {
        row = new UVector[x];
        for (int i = 0; i < x; i++)
            row[i] = new UVector(y);
    }

    public UVector row(int x) {
        return row[x];
    }

    public double value(int x, int y) {
        return row[x].value[y];
    }

    public double grad(int x, int y) {
        return row[x].grad[y];
    }

    public int width() { return row.length; }
    public int height() { return row[0].size(); }
}