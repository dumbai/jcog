package jcog.signal.wave2d;

import jcog.signal.tensor.ArrayTensor;


/** stores multiple parallel channels in 3rd dimension of tensor, ie. RGB */
public abstract class PlanarBitmap2D extends ArrayTensor implements Bitmap2D {
//    protected int[] shape;

    public PlanarBitmap2D() {
        this(0, 0, 0);
    }

    public PlanarBitmap2D(int w, int h, int channels) {
        super(new int[] { w, h, channels });
        //resize(w, h, bitplanes); //TODO ResizeableArrayTensor
    }

//    protected void resize(int w, int h, int bitplanes) {
//        if (shape != null) {
//            if (shape.length == 3 && shape[0] == w && shape[1] == h && shape[2] == bitplanes)
//                return; //no change
//        }
//        this.shape = new int[]{w, h, bitplanes};
//    }


    public int width() {
        return shape[0];
    }

    public int height() {
        return shape[1];
    }

    @Override
    public float value(int x, int y) {
        //TODO handle alpha channel correctly
        int planes = shape[2];
        float sum = 0;
        for (int p = 0; p < planes; p++) {
            sum += get(x, y, p);
        }
        return sum / planes;
    }


}
