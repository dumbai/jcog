package jcog.signal.wave2d;

import jcog.TODO;
import jcog.func.IntIntToFloatFunction;

import java.awt.image.*;
import java.util.function.Supplier;

import static jcog.Util.intByte;
import static jcog.Util.intBytePct;

/**
 * 2D monochrome adapter to BufferedImage
 * TODO cache results in 8-bit copy
 */
public class MonoBufImgBitmap2D implements Bitmap2D, Supplier<BufferedImage> {

//    static final BufferedImage Empty = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    public static final IntIntToFloatFunction None = (x, y) -> Float.NaN;

    @Deprecated private final int[] tmp4 = new int[4];
	protected Supplier<BufferedImage> source;
	protected BufferedImage out;
	protected transient WritableRaster raster;
	private ColorMode mode = ColorMode.Gray;
	private boolean alpha;

	int W = 0, H = 0;

	private IntIntToFloatFunction value = None;

	public MonoBufImgBitmap2D() {
		this.source = null;
	}

	public MonoBufImgBitmap2D(Supplier<BufferedImage> source) {
		this.source = source;
		updateBitmap();

	}

	public MonoBufImgBitmap2D mode(ColorMode c) {
		this.mode = c;
		return this;
	}

	@Override
	public final ColorMode mode() {
		return mode;
	}

	@Override
	public float value(int x, int y, float rFactor, float gFactor, float bFactor) {
		if (raster != null) {


			if (out.getType() == BufferedImage.TYPE_BYTE_GRAY) {
				throw new TODO();
			} else {
				float sum = rFactor + gFactor + bFactor;
				if (sum < Float.MIN_NORMAL)
					return 0;

				int[] rgb = raster.getPixel(x, y, tmp4);
				float r, g, b;
				if (alpha) {
					//HACK handle either ARGB and RGBA intelligently
					ColorModel colormodel = out.getColorModel();
					r = colormodel.getRed(rgb);
					g = colormodel.getGreen(rgb);
					b = colormodel.getBlue(rgb);

					//r = rgb[1]; g = rgb[2]; b = rgb[3];
				} else {
					r = rgb[0];
					g = rgb[1];
					b = rgb[2];
				}
				return (r * rFactor + g * gFactor + b * bFactor) / (256f * sum);
			}
		}
		return Float.NaN;
	}

	@Override
	public int width() {
		return W;
	}

	@Override
	public int height() {
		return H;
	}

	@Override
	public void updateBitmap() {
		Supplier<BufferedImage> src = this.source;
        if (src!=null)
            img(src.get());
	}



	protected void img(BufferedImage nextImage) {

	    if (nextImage == null || (nextImage == this.out && W == out.getWidth() && H == out.getHeight()))
	        return;

        this.out = nextImage;
        raster = this.out.getRaster();
        W = raster.getWidth();
        H = raster.getHeight();

        ColorModel cm = out.getColorModel();
        alpha = cm.hasAlpha();
        if (alpha) {
            if (cm.getTransparency() == 3) {
                //RGBA
                //HACK pretend like its RGB since these bytes are first
                alpha = false;
            } //else: cm.getTransparency() == 0 means ARGB
        }
        if (value == None) {
            if (out.getType() == BufferedImage.TYPE_BYTE_GRAY) {
                value = (x, y) -> raster.getSampleFloat(x, y, 0);
            } else {
				value = raster.getDataBuffer() instanceof DataBufferInt ?
						new PixelBrightnessIntRGB(alpha):
						new PixelBrightnessByteRGB(alpha);
            }
        }

    }

	public MonoBufImgBitmap2D filter(ColorMode c) {
		return new MonoBufImgBitmap2D(() -> out) {
			@Override
			public int width() {
				return MonoBufImgBitmap2D.this.width();
			}

			@Override
			public int height() {
				return MonoBufImgBitmap2D.this.height();
			}

		}.mode(c);
	}

	@Override
	public final float value(int x, int y) {
		return value.value(x, y);
	}

	@Override
	public BufferedImage get() {
		return out;
	}

	private class PixelBrightnessByteRGB implements IntIntToFloatFunction {

		private final boolean alpha;
		private final byte[] data;

		PixelBrightnessByteRGB(boolean alpha) {

			this.alpha = alpha;
			data = ((DataBufferByte) raster.getDataBuffer()).getData();
		}
		@Override
		public float value(int x, int y) {

			/* HACK */

			int i = (y * W + x)*(alpha ? 4 : 3);

			return switch (mode) {
				case Red -> data[i]/256f;
				case Green -> data[i+1]/256f;
				case Blue -> data[i+2]/256f;
				case Gray -> rgb(i);
				case Hue -> hue(i);
			};
		}

		private float rgb(int i) {
			return (data[i]/256f + data[i +1]/256f + data[i +2]/256f) / (3 * 256f);
		}
		private float hue(int i) {
			return Bitmap2D.rgbToHue(data[i], data[i+1], data[i+2]);
			//return Bitmap2D.rgbToHueInfra(data[i], data[i+1], data[i+2], 0.1f);
		}
	}
	/** alpha == true -> ARGB,  alpha == false -> RGB
	 *  for single-thread use only
	 * */
	private class PixelBrightnessIntRGB implements IntIntToFloatFunction {

		/** indices of the color planes */
		final int R, G, B;

		final int[] data;

		private PixelBrightnessIntRGB(boolean alpha) {
//			this.alpha = alpha;
			R = alpha ? 1 : 0;
			G = alpha ? 2 : 1;
			B = alpha ? 3 : 2;
			data = ((DataBufferInt) raster.getDataBuffer()).getData();
		}

		@Override
		public float value(int x, int y) {

			int rgb = data[y * W + x];

			return switch (mode) {
				case Red -> intBytePct(rgb, R);
				case Green -> intBytePct(rgb, G);
				case Blue -> intBytePct(rgb, B);
				case Gray -> intByte(rgb, R) / (3 * 256f) + intByte(rgb, G) / (3 * 256f) + intByte(rgb, B) / (3 * 256f);
				case Hue ->
					Bitmap2D.rgbToHue(intByte(rgb, R), intByte(rgb, G), intByte(rgb, B));
					 //Bitmap2D.rgbToHueInfra(intByte(rgb, R), intByte(rgb, G), intByte(rgb, B), 0.1f);
			};
		}
	}
}