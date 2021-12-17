package jcog.signal.wave2d;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import static java.awt.RenderingHints.*;

public abstract class MonoDBufImgBitmap2D extends MonoBufImgBitmap2D {
    /**
     * output pixel width / height
     */
    public int pw;
    public int ph;
    protected Graphics2D outgfx;

    float sx1 = 0;
    float sy1 = 0;
    float sx2 = 1;
    float sy2 = 1;

    public MonoDBufImgBitmap2D(int pw, int ph) {
        super();
        this.pw = pw;
        this.ph = ph;
    }

    public MonoDBufImgBitmap2D(Supplier<BufferedImage> source) {
        this(source, source.get().getWidth(), source.get().getHeight());
    }

    public MonoDBufImgBitmap2D(Supplier<BufferedImage> source, int pw, int ph) {
        this(pw, ph);
        this.source = source;
    }

    public MonoDBufImgBitmap2D crop(float sx1, float sy1, float sx2, float sy2) {
        this.sx1 = sx1;
        this.sy1 = sy1;
        this.sx2 = sx2;
        this.sy2 = sy2;
        return this;
    }

    @Override
    public int width() {
        return pw;
    }

    @Override
    public int height() {
        return ph;
    }

    @Override
    public void updateBitmap() {

        if (source == null)
            return;

        if (source instanceof Bitmap2D)
            ((Bitmap2D) source).updateBitmap();

        BufferedImage in = source.get();
        if (in == null)
            return;


        if (outgfx == null || raster.getWidth() != pw || raster.getHeight() != ph) {

            if (outgfx != null)
                outgfx.dispose();

            out = new BufferedImage(pw, ph, in.getType());
            outgfx = out.createGraphics();

            //if (mode()!=ColorMode.Hue) {
            outgfx.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            outgfx.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
            outgfx.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
            outgfx.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
            //}

            img(out);
        }


        render(in, out);

    }

    protected abstract void render(BufferedImage in, BufferedImage out);
}