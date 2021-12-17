package jcog.signal.wave2d;

import jcog.math.v2i;

public abstract class WrappedBitmap2D extends ProxyBitmap2D {

    public final v2i center = new v2i(-1, -1);

    public WrappedBitmap2D(Bitmap2D src) {
        super(null);
        set(src);
    }

    public abstract void updateCenter();

    protected void setCenter(float x, float y) {
        center.setFloor(x, y);
    }

    @Override
    public void updateBitmap() {
        updateCenter();
        super.updateBitmap();
    }

    @Override
    public float value(int x, int y) {
        x += w / 2 - center.x;
        y += h / 2 - center.y;
        if (x < 0) x += w; else if (x >= w) x -= w;
        if (y < 0) y += h; else if (y >= h) y -= h;
        return super.value(x, y);
    }

}