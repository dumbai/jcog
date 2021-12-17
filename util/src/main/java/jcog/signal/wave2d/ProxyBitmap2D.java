package jcog.signal.wave2d;

import jcog.TODO;
import org.jetbrains.annotations.Nullable;

/**
 * exposes a buffered image as a camera video source
 * TODO integrate with Tensor API
 */
public class ProxyBitmap2D implements Bitmap2D {

    private Bitmap2D src;
    protected int w, h;

    public ProxyBitmap2D(@Nullable Bitmap2D src) {
        set(src);
    }

    public <P extends ProxyBitmap2D> P set(@Nullable Bitmap2D src) {
        this.src = src;
        if (src!=null)
            updateBitmap();
        return (P) this;
    }

    @Override
    public ColorMode mode() {
        return src.mode();
    }

    @Override
    public void updateBitmap() {
        synchronized(this) {
            Bitmap2D s = this.src;
            if (s != null) {
                s.updateBitmap();
                w = s.width();
                h = s.height();
            } else {
                w = h = 0;
            }
        }
    }

    @Override
    public final int width() {
        return w;
    }

    @Override
    public final int height() {
        return h;
    }

    @Override
    public float value(int x, int y) {
        return srcValue(x, y);
    }

    public final float srcValue(int x, int y) {
        return src.value(x, y);
    }

    public final float srcMean(int x1, int y1, int x2, int y2) {
        throw new TODO();
    }

}