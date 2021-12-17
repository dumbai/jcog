package jcog.signal.wave2d;

public abstract class AbstractBitmap2D implements Bitmap2D {

    protected int w;
    int h;

    protected AbstractBitmap2D(int w, int h) {
        resize(w, h);
    }

    public void resize(int w, int h) {
        this.w = w; this.h = h;
    }

    @Override
    public int width() {
        return w;
    }

    @Override
    public int height() {
        return h;
    }

}
