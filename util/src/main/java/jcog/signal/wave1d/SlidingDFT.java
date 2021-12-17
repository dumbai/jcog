/*
 *  SlidingDFT.java
 *  Eisenkraut
 *
 *  Copyright (c) 2004-2017 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */
package jcog.signal.wave1d;

import org.jetbrains.annotations.Nullable;

/**
 * For the sliding DFT algorithm, see for example
 * Bradford/Dobson/ffitch "Sliding is smoother than jumping".
 *
 *
 * The FFT provides you with amplitude and phase. The amplitude is encoded as the magnitude of the complex number (sqrt(x^2+y^2)) while the phase is encoded as the angle (atan2(y,x)). To have a strictly real result from the FFT, the incoming signal must have even symmetry (i.e. x[n]=conj(x[N-n])).
 *  If all you care about is intensity, the magnitude of the complex number is sufficient for analysis.
 * https://stackoverflow.com/questions/10304532/why-does-fft-produce-complex-numbers-instead-of-real-numbers#10304604
 */
public class SlidingDFT {
    private final int fftSize;
    private final int fftSizeP2;


    private final float[] cos;
    private final float[] sin;

    //per channel
    private final float[][] timeBuf;
    private final float[][] fftBufD;
    private final int[] timeBufIdx;


    public SlidingDFT(int fftSize, int numChannels) {
        this.fftSize = fftSize;

        fftSizeP2 = fftSize + 2;
        fftBufD = new float[numChannels][fftSizeP2];

        int bins = fftSize / 2;
        cos = new float[bins + 1];
        sin = new float[bins + 1];
        timeBuf = new float[numChannels][fftSize];
        timeBufIdx = new int[numChannels];


        double d1 = (Math.PI * 2 / fftSize);
        int binsH = bins / 2;
        for (int bin = 0, j = bins, k = binsH, m = binsH; bin < binsH; bin++, j--, k--, m++) {
            float d2 = (float) Math.cos(d1*bin);
            cos[bin] = d2;
            cos[j] = -d2;
            sin[k] = d2;
            sin[m] = d2;
        }
    }


//    public void next(float[] inBuf, int chan, float[] fftBuf) {
//        next(inBuf, 0, inBuf.length, chan, fftBuf);
//    }

    /** TODO calculate for provided interval to do aggregations */
    @Deprecated public void decode(int chan, float[] out) {

        float[] f = fftBufD[chan];
        int bands = out.length/2;
        int k = 0, p = bands;
        for (int i = 0; i < bands; ) {
            double real = f[i++];
            double imag = f[i++];
            double mag = jcog.Util.fma(real, real, imag*imag);
            if (mag!=mag)
                mag = 0; //HACK why

            out[k++] = (float)mag;

            double phase = Math.atan2(imag,real)/Math.PI + 0.5; //unpolarize/PI
            out[p++] = (float)phase;
        }

    }

    public void next(float[] inBuf, int inOff, int inLen, int chan, @Nullable float[] fftBuf) {

        if (inLen == 0 || inBuf.length == 0)
            return;

        float[] fftBufDC = fftBufD[chan];
        float[] timeBufC = timeBuf[chan];
        int timeBufIdxC = timeBufIdx[chan];


        for (int i = 0, j = inOff; i < inLen; i++, j++) {
            float f1 = inBuf[j];


            float delta = f1 - timeBufC[timeBufIdxC];

            timeBufC[timeBufIdxC] = f1;
            for (int k = 0, m = 0; m < fftSizeP2; k++) {

                float re1 = fftBufDC[m] + ((k & 1) == 0 ? +1 : -1) * delta;
                float im1 = fftBufDC[m + 1];

                float re2 = cos[k];
                float im2 = sin[k];

                float a = re1 * re2 - im1 * im2;
                float b = re1 * im2 + re2 * im1;
                fftBufDC[m++] = a;
                fftBufDC[m++] = b;
            }
            if (++timeBufIdxC == fftSize) timeBufIdxC = 0;
        }
        timeBufIdx[chan] = timeBufIdxC;

        if (fftBuf!=null)
            System.arraycopy(fftBufDC, 0, fftBuf, 0, fftBufDC.length);

    }
}