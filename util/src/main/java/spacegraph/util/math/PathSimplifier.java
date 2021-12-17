package spacegraph.util.math;

import jcog.math.v2;

import java.util.function.Consumer;

/** 2d path simplifier that can transduce an arbitrary object stream */
public abstract class PathSimplifier<X> implements Consumer<X> {

    X n, p, c;
    protected v2 nv;
    protected v2 pv;
    protected v2 cv;

    /**
     * should call, ex: add(n, x(n), y(n))
     */
    public abstract void accept(X n);

    public void add(X n, float x, float y) {
        this.n = n;
        this.nv = new v2(x, y);

        X c = this.c;
        if (c!=null) {
            v2 cv = this.cv;
            accept(c, pv != null && cv != null ? collinear(pv, cv, nv) : 0);

            this.p = c;
            this.pv = cv;
        }

        this.c = n;
        this.cv = nv;
    }

    protected double collinear(v2 p, v2 c, v2 n) {
        return Simplify2D.collinearity(p, c, n);
        //TODO other impl or heuristics
    }

    /**
     * called when an item is included
     */
    protected abstract void accept(X n, double collinearity);

}