package jcog.signal;

import jcog.Util;
import jcog.signal.wave2d.ArrayBitmap2D;

abstract public class ArraySensor extends ArrayBitmap2D /* HACK */ {
    private final boolean normalize;

    public ArraySensor(int n, boolean normalize) {
        super(n, 1);
        this.normalize = normalize;
    }

    @Override
    public void updateBitmap() {
        int w = width();
        for (int i = 0; i < w; i++)
            set(i, 0, value(i));
        if (normalize)
            normalize();
    }

    abstract protected float value(int i);

    private void normalize() {
        //Util.normalize(b[0]);
        Util.normalize(b[0], 0, width(), 0, (float)Util.sum(b[0]));
    }
}