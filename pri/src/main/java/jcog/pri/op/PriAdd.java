package jcog.pri.op;

import jcog.pri.Prioritizable;

import java.util.function.Consumer;

public class PriAdd<P extends Prioritizable> implements Consumer<P> {

    public float x;

    public PriAdd(float x) {
        this.x = x;
    }

    @Override
    public void accept(P x) {
        x.priAdd(this.x);
    }
}