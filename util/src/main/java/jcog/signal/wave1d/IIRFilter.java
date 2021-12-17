package jcog.signal.wave1d;

import java.util.stream.IntStream;

/**
 * https://github.com/JorenSix/TarsosDSP/blob/master/src/core/be/tarsos/dsp/filters/IIRFilter.java
 * An Infinite Impulse Response, or IIR, filter is a filter that uses a set of
 * coefficients and previous filtered values to filter a stream of audio. It is
 * an efficient way to do digital filtering. IIRFilter is a general IIRFilter
 * that simply applies the filter designated by the filter coefficients so that
 * sub-classes only have to dictate what the values of those coefficients are by
 * defining the <code>calcCoeff()</code> function. When filling the coefficient
 * arrays, be aware that <code>b[0]</code> corresponds to
 * <code>b<sub>1</sub></code>.
 * 
 * @author Damien Di Fede
 * @author Joren Six
 * 
 */
public abstract class IIRFilter {
	
	/** The b coefficients. */
	protected float[] b;

	/** The a coefficients. */
	protected float[] a;

	/**
	 * The input values to the left of the output value currently being
	 * calculated.
	 */
	protected final float[] in;
	
	/** The previous output values. */
	protected final float[] out;

	private float frequency;
	
	private final float sampleRate;


	/**
	 * Constructs an IIRFilter with the given cutoff frequency that will be used
	 * to filter audio recorded at <code>sampleRate</code>.
	 * 
	 * @param freq
	 *            the cutoff frequency
	 * @param sampleRate
	 *            the sample rate of audio to be filtered
	 */
	public IIRFilter(float freq, float sampleRate) {
		this.sampleRate = sampleRate;
		this.frequency = Math.max(1, freq);
		update();
		in = new float[a.length];
		out = new float[b.length];
	}
	
	public void setFrequency(float freq){
		if (freq!=this.frequency) {
			this.frequency = freq;
			update();
		}
	}

	/**
	 * Returns the cutoff frequency (in Hz).
	 * 
	 * @return the current cutoff frequency (in Hz).
	 */
	protected final float getFrequency() {
		return frequency;
	}
	
	protected final float getSampleRate(){
		return sampleRate;
	}

	/**
	 * Calculates the coefficients of the filter using the current cutoff
	 * frequency. To make your own IIRFilters, you must extend IIRFilter and
	 * implement this function. The frequency is expressed as a fraction of the
	 * sample rate. When filling the coefficient arrays, be aware that
	 * <code>b[0]</code> corresponds to the coefficient
	 * <code>b<sub>1</sub></code>.
	 * 
	 */
	protected abstract void update() ;

	

	public boolean process(float[] audioFloatBuffer, int start, int end) {

		
		for (int i = start; i < end; i++) {
			//shift the in array
			//TODO use ring buffer to avoid these copies
			System.arraycopy(in, 0, in, 1, in.length - 1);
			in[0] = audioFloatBuffer[i];
	
			//calculate y based on a and b coefficients
			//and in and out.
            int bound1 = a.length;
            double y = IntStream.range(0, bound1).mapToDouble(j1 -> a[j1] * in[j1]).sum();
            int bound = b.length;
            double sum = IntStream.range(0, bound).mapToDouble(j -> b[j] * out[j]).sum();
            y += sum;
			//shift the out array
			//TODO use ring buffer to avoid these copies
			System.arraycopy(out, 0, out, 1, out.length - 1);

			audioFloatBuffer[i] = out[0] = (float)y;
		} 
		return true;
	}

	/**
	 * A High pass IIR filter. Frequency defines the cutoff.
	 * @author Joren Six
	 */
	public static class HighPass extends IIRFilter {

		public HighPass(float freq, float sampleRate) {
			super(freq, sampleRate);
		}

		protected void update()
		{
			float fracFreq = getFrequency()/getSampleRate();
			double x = Math.exp(-2 * Math.PI * fracFreq);
			a = new float[] {(float) ((1+x)/2), (float) (-(1+x)/2)};
			b = new float[] {(float) x};
		}

	}

	/**
	 * Single pass low pass filter.
	 * @author Joren Six
	 */
	public static class LowPassSP extends IIRFilter {

		public LowPassSP(float freq, float sampleRate) {
			super(freq, sampleRate);
		}

		@Override
		protected void update() {
			float fracFreq = getFrequency() / getSampleRate();
			float x = (float) Math.exp(-2 * Math.PI * fracFreq);
			a = new float[] { 1 - x };
			b = new float[] { x };
		}

	}

	/**
	 * Four stage low pass filter.
	 *
	 */
	public static class LowPassFS extends IIRFilter{

		public LowPassFS(float freq, float sampleRate) {
			super(freq, sampleRate);
		}

		@Override
		protected void update() {
			float freqFrac = getFrequency() / getSampleRate();
			float x = (float) Math.exp(-14.445 * freqFrac);
			a = new float[] { (float) Math.pow(1 - x, 4) };
			b = new float[] { 4 * x, -6 * x * x, 4 * x * x * x, -x * x * x * x };
		}


	}

	/**
	 * A band pass filter is a filter that filters out all frequencies except for
	 * those in a band centered on the current frequency of the filter.
	 *
	 * @author Damien Di Fede
	 *
	 */
	public static class BandPass extends IIRFilter
	{
		private float bw;

		/**
		 * Constructs a band pass filter with the requested center frequency,
		 * bandwidth and sample rate.
		 *
		 * @param freq
		 *          the center frequency of the band to pass (in Hz)
		 * @param bandWidth
		 *          the width of the band to pass (in Hz)
		 * @param sampleRate
		 *          the sample rate of audio that will be filtered by this filter
		 */
		public BandPass(float freq, float bandWidth, float sampleRate)
		{
			super(freq, sampleRate);
			setBandWidth(bandWidth);
		}

		/**
		 * Sets the band width of the filter. Doing this will cause the coefficients
		 * to be recalculated.
		 *
		 * @param bandWidth
		 *          the band width (in Hz)
		 */
		public void setBandWidth(float bandWidth)
		{
			bw = bandWidth / getSampleRate();
			update();
		}

		/**
		 * Returns the band width of this filter.
		 *
		 * @return the band width (in Hz)
		 */
		public float getBandWidth()
		{
			return bw * getSampleRate();
		}

		protected void update()
		{
			float R = 1 - 3 * bw;
			float fracFreq = getFrequency() / getSampleRate();
			float T = 2 * (float) Math.cos(2 * Math.PI * fracFreq);
			float K = (1 - R * T + R * R) / (2 - T);
			a = new float[] { 1 - K, (K - R) * T, R * R - K };
			b = new float[] { R * T, -R * R };
		}
	}
}