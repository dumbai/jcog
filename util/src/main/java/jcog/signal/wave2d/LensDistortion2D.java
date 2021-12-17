package jcog.signal.wave2d;

import jcog.Util;

import static jcog.Util.sqr;

/**
 * https://stackoverflow.com/questions/4978039/fisheye-picture-effect-barrel-distortion-algorithm-with-java
 *
 * TODO parameterize
 */
public class LensDistortion2D extends ProxyBitmap2D {
    float xscale = 0.5f, yscale = 0.5f, xshift = 0, yshift = 0;
    float k = 0.03f;
    float cx, cy;

    public LensDistortion2D(Bitmap2D src) {
        super(src);
        int W = src.width(); int H = src.height();
        cx = W /2f;
        cy = H /2f;
        xshift = W/4f; //TODO calculate from zoom
        yshift = H/4f; //TODO calculate from zoom
    }

    @Override
    public float value(int x, int y) {
        float xx = x * xscale + xshift;
        float yy = y * yscale + yshift;
        float dx = xx - cx;
        float dy = yy - cy;
        float mag = sqr(dx) + sqr(dy);
        return srcValue(
            Util.clamp(Math.round(xx + Math.signum(dx) * k * mag), 0, this.w),
            Util.clamp(Math.round(yy + Math.signum(dy) * k * mag), 0, this.h)
        );
    }


}