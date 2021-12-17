package jcog.signal.wave2d;

import java.awt.image.BufferedImage;

public class RGBBufImgBitmap2D extends PlanarBitmap2D {
    public final BufferedImage image;

    public RGBBufImgBitmap2D(BufferedImage i) {
        super(i.getWidth(), i.getHeight(), i.getRaster().getNumBands());
        this.image = i;
    }


    @Override
    public float get(int... cell) {
        int x = cell[0];
        int y = cell[1];

        int p = image.getRGB(x, y);

        int plane = cell[2];
        return switch (plane) {
            case 0 -> Bitmap2D.decode8bRed(p);
            case 1 -> Bitmap2D.decode8bGreen(p);
            case 2 -> Bitmap2D.decode8bBlue(p);
            case 3 -> Bitmap2D.decode8bAlpha(p);
            default -> throw new UnsupportedOperationException();
        };
    }


}