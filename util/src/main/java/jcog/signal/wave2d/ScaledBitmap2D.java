package jcog.signal.wave2d;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

/**
 * Pan/Zoom filter for a BuferredImage source
 * TODO avoid dependence on Swing Image
 */
public class ScaledBitmap2D extends MonoDBufImgBitmap2D/* TODO extends ArrayBitmap2D directly, bypassing Swing */ /*implements ImageObserver*/ {

    public ScaledBitmap2D(BufferedImage source, int pw, int ph) {
        this(()->source, pw, ph);
    }

    public ScaledBitmap2D(Supplier<BufferedImage> source) {
        super(source);
    }

    public ScaledBitmap2D(Supplier<BufferedImage> source, int pw, int ph) {
        super(source, pw, ph);
    }

    @Override
    protected void render(BufferedImage in, BufferedImage out) {
        //outgfx.setColor(Color.BLACK);
        //outgfx.fillRect(0, 0, pw, ph);

        /*
            public abstract boolean drawImage(Image img,
                                      int dx1, int dy1, int dx2, int dy2,
                                      int sx1, int sy1, int sx2, int sy2,
                                      Color bgcolor,
                                      ImageObserver observer);
         */
        int sw = in.getWidth(null), sh = in.getHeight(null);

        outgfx.drawImage(in,
                0, 0, pw, ph,
                Math.round(sx1 * sw),
                Math.round(sy1 * sh),
                Math.round(sx2 * sw),
                Math.round(sy2 * sh),
                Color.BLACK, null);
    }

//    @Override
//    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
//        return true;
//    }

}