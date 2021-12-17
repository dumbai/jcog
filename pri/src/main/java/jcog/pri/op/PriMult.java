package jcog.pri.op;

import jcog.pri.Prioritizable;

import java.util.function.Consumer;

public final class PriMult<P extends Prioritizable> implements Consumer<P> {

    public float x;

    public PriMult(float x) {
//        //TEMPORARY
//        if (factor<=0)
//            Util.nop();

        this.x = x;
    }

    @Override
    public void accept(P x) {
        x.priMul(this.x);
    }

}