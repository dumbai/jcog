package jcog.signal.wave2d;

public class ImageFlip extends ProxyBitmap2D {
    boolean flipX, flipY;

    public ImageFlip(Bitmap2D src, boolean flipX, boolean flipY) {
        super(src);
        flip(flipX, flipY);
    }

    public final void flip(boolean flipX, boolean flipY) {
        this.flipX = flipX;
        this.flipY = flipY;
    }

    @Override
    public float value(int x, int y) {
        if (flipX) x = (width()-1)-x;
        if (flipY) y = (height()-1)-y;
        return super.value(x, y);
    }

}