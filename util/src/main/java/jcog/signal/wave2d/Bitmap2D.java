package jcog.signal.wave2d;


import jcog.Util;
import jcog.signal.Tensor;
import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;

public interface Bitmap2D extends Tensor {

    static int encodeRGB8b(float r, float g, float b) {
        return ((int)(r * 255) << 16) |
                ((int)(g * 255) << 8) |
                ((int)(b * 255));
//        return (Math.round(r * 255) << 16) |
//                (Math.round(g * 255) << 8) |
//                (Math.round(b * 255));
    }

    default ColorMode mode() {
        return ColorMode.Gray;
    }

    @Override
    default int[] shape() {
        return new int[] { width(), height() };
    }

    @Override
    default float getAt(int i) {
        int w = width();
        int y = i / w;
        int x = i % w;
        return value(x, y);
    }

    @Override
    default float get(int... cell) {
        return value(cell[0], cell[1]);
    }


    /**
     * explicit refresh update the image
     */
    default void updateBitmap() {

    }

    int width();

    int height();


    /**
     * returns a value 0..1.0 indicating the monochrome brightness (white level) at the specified pixel
     */
    float value(int x, int y);

    /**
     * RGB filtered brightness, if supported; otherwise the factors are ignored
     */
    default float value(int x, int y, float rFactor, float gFactor, float bFactor) {
        return value(x, y);
    }

    /**
     * returns a new proxy bitmap that applies per-pixel brightness function
     * TODO variation of this that takes a per-frame brightness histogram as parameter */
    default ProxyBitmap2D each(FloatToFloatFunction pixelFunc) {
        return new ProxyBitmap2D(this) {

            @Override
            public float value(int x, int y) {
                return pixelFunc.valueOf(Bitmap2D.this.get(x, y));
            }
        };
    }


    /** TODO make separate class with controls */
    @Deprecated default ProxyBitmap2D blurred() {
        return new ProxyBitmap2D(this){

            @Override
            public float value(int x, int y) {
                float c = Bitmap2D.this.value(x, y);
                float up = y > 0 ? Bitmap2D.this.value(x, y-1) : c;
                float left = x > 0 ? Bitmap2D.this.value(x-1, y) : c;
                float down = y < height()-1 ? Bitmap2D.this.value(x, y+1) : c;
                float right = x < width()-1 ? Bitmap2D.this.value(x+1, y) : c;
                return (c*8 + up + left + down + right)/12;
            }
        };
    }


//    static float rgbToMono(int r, int g, int b) {
//        return (r + g + b) / 256f / 3f;
//    }

//
//    @FunctionalInterface
//    interface EachPixelRGB {
//        void pixel(int x, int y, int aRGB);
//    }
//
//    @FunctionalInterface
//    interface EachPixelRGBf {
//        void pixel(int x, int y, float r, float g, float b, float a);
//    }

//    @FunctionalInterface
//    interface PerPixelMono {
//        void pixel(int x, int y, float whiteLevel);
//    }
//
//    @FunctionalInterface
//    interface PerIndexMono {
//        void pixel(int index, float whiteLevel);
//    }


//    default void intToFloat(EachPixelRGBf m, int x, int y, int p) {
//
//        int a = 255;
//        float r = decodeRed(p);
//        float g = decodeGreen(p);
//        float b = decodeBlue(p);
//        m.pixel(x, y, r, g, b, a/256f);
//    }


    static float decode8bRed(int p) {
        return Util.intBytePct(p, 2);
    }

    static float decode8bGreen(int p) {
        return Util.intBytePct(p, 1);
    }

    static float decode8bBlue(int p) {
        return Util.intBytePct(p, 0);
    }

    static float decode8bAlpha(int p) {
        return Util.intBytePct(p, 3);
    }

    static float rgbToHueInfra(int red, int green, int blue, float grayMargin) {

        int min = Math.min(Math.min(red, green), blue);
        int max = Math.max(Math.max(red, green), blue);

        if (max==min)
            return 1;

        if (max-min <= 256*grayMargin) {
            float v = (red+green+blue)/(256*3f);
            return (1-grayMargin) + grayMargin * v;
        }

        float hue;
        if (max == red) {
            hue =     (green - blue) / ((float)(max - min));
        } else if (max == green) {
            hue = 2 + (blue - red)   / ((float)(max - min));
        } else {
            hue = 4 + (red - green)  / ((float)(max - min));
        }


        hue /= 6;
        if (hue < 0) hue++;

        //assertUnitized(hue);
        return hue * (1-grayMargin);
    }
    static float rgbToHue(int red, int green, int blue) {

        int min = Math.min(Math.min(red, green), blue);
        int max = Math.max(Math.max(red, green), blue);

        if (min == max) {
            return 0; //green-screens the color it masks
            //return ThreadLocalRandom.current().nextFloat();
        }

        float hue;
        int shift, fade;
        if (max == red) {
            fade = (green - blue);
            shift = 0;
        } else if (max == green) {
            shift = 2;
            fade = (blue - red);
        } else {
            shift = 4;
            fade = (red - green);
        }
        hue = (shift + fade/((float)(max - min)))/6;

        if (hue < 0)
            hue++;

        //assertUnitized(hue);
        return hue;
    }




}