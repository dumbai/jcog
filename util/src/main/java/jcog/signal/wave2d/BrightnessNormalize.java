package jcog.signal.wave2d;

import jcog.TODO;
import jcog.random.SplitMix64Random;

import static jcog.Util.unitize;

/** TODO extends MonoBufImgBitmap2D */
public class BrightnessNormalize implements Bitmap2D {

    private final Bitmap2D src;
//    BufferedImage store = null;

    static final float epsilon = Float.MIN_NORMAL;

    private transient float[] s;
    int W;
    int H;

    /** for noise */
    private final SplitMix64Random rng = new SplitMix64Random(1);

    public BrightnessNormalize(Bitmap2D src) {
        this.src = src;
        updateBitmap(); //initialize
    }

    @Override
    public synchronized void updateBitmap() {

        src.updateBitmap();

//        BufferedImage from = src.get();
//        if (from == null) {
//            W = H = 0;
//            store = null;
//            return;
//        }


        W = src.width();
        H = src.height();
//        if (store == null || store.getWidth()!= W || store.getHeight()!= H)
//            store = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_GRAY);
//
//        s = ((DataBufferByte) store.getRaster().getDataBuffer()).getData();
        if (s == null || s.length!=W*H)
            s = new float[W * H];

        int i = 0;

        double brightSum = 0;
        float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
//                int rgb = from.getRGB(x,y);
//                float r = Bitmap2D.decode8bRed(rgb);
//                float g = Bitmap2D.decode8bGreen(rgb);
//                float b = Bitmap2D.decode8bBlue(rgb);
//                float bri = (r + g + b)/3f;
                float bri = src.value(x, y);
                if (bri == bri) {
                    if (bri < min) min = bri;
                    if (bri > max) max = bri;
                    brightSum += bri;
                } else {
                    bri = Float.NaN;
                }

                s[i++] = bri;
            }
        }
//        float mean = (float) (brightSum / (W * H));
        float range = max-min;
        if (range < epsilon)
            return; //no change, just pass-thru

        //center around the mean value
        //TODO refine
//        if (max - mean < mean - min)
//            max = Util.min(1, (mean - min) + mean);
//        else
//            min = Util.max(0, mean - (max - mean));

        for (i = 0; i < s.length; i++) {
            float b = s[i];
            if (b==b) {
                s[i] = unitize((b-min)/range );
            }
        }


    }

//    @Override
//    public BufferedImage get() {
//        update();
//        return store;
//    }

    @Override
    public int width() {
        return W;
    }

    @Override
    public int height() {
        return H;
    }

    @Override
    public float value(int xx, int y) {
        if (s == null)
            return rng.nextFloat();

        float x = s[y * W + xx];
        if (x != x)
            return rng.nextFloat();
        return x;
    }

    @Override
    public float value(int x, int y, float rFactor, float gFactor, float bFactor) {
        throw new TODO();
    }
}