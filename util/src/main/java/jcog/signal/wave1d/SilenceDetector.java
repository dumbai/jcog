package jcog.signal.wave1d;

/**
 * The continuing silence detector does not break the audio processing pipeline when silence is detected.
 * from: https://github.com/JorenSix/TarsosDSP/blob/master/src/core/be/tarsos/dsp/SilenceDetector.java
 */
public class SilenceDetector  {
	
	public static final double DEFAULT_SILENCE_THRESHOLD = -70.0;//db
	
	private final double threshold;//db
	
	private final boolean breakProcessingQueueOnSilence;
	
	/**
	 * Create a new silence detector with a default threshold.
	 */
	public SilenceDetector(){
		this(DEFAULT_SILENCE_THRESHOLD,false);
	}
	
	/**
	 * Create a new silence detector with a defined threshold.
	 * 
	 * @param silenceThreshold
	 *            The threshold which defines when a buffer is silent (in dB).
	 *            Normal values are [-70.0,-30.0] dB SPL.
	 * @param breakProcessingQueueOnSilence 
	 */
	public SilenceDetector(double silenceThreshold, boolean breakProcessingQueueOnSilence){
		this.threshold = silenceThreshold;
		this.breakProcessingQueueOnSilence = breakProcessingQueueOnSilence;
	}

	/**
	 * Calculates the local (linear) energy of an audio buffer.
	 * 
	 * @param buffer
	 *            The audio buffer.
	 * @return The local (linear) energy of an audio buffer.
	 */
	private static double localEnergy(float[] buffer) {
		double power = 0.0D;
		for (float element : buffer) {
			power += element * element;
		}
		return power;
	}

	/**
	 * Returns the dBSPL for a buffer.
	 * 
	 * @param buffer
	 *            The buffer with audio information.
	 * @return The dBSPL level for the buffer.
	 */
	private static double soundPressureLevel(float[] buffer) {
		double value = Math.pow(localEnergy(buffer), 0.5);
        value /= buffer.length;
		return linearToDecibel(value);
	}

	/**
	 * Converts a linear to a dB value.
	 * 
	 * @param value
	 *            The value to convert.
	 * @return The converted value.
	 */
	private static double linearToDecibel(double value) {
		return 20.0 * Math.log10(value);
	}
	
	double currentSPL = 0;
	public double currentSPL(){
		return currentSPL;
	}

	/**
	 * Checks if the dBSPL level in the buffer falls below a certain threshold.
	 * 
	 * @param buffer
	 *            The buffer with audio information.
	 * @param silenceThreshold
	 *            The threshold in dBSPL
	 * @return True if the audio information in buffer corresponds with silence,
	 *         false otherwise.
	 */
	public boolean isSilence(float[] buffer, double silenceThreshold) {
		currentSPL = soundPressureLevel(buffer);
		return currentSPL < silenceThreshold;
	}

	public boolean isSilence(float[] buffer) {
		return isSilence(buffer, threshold);
	}


	public boolean process(float[] buffer) {
		boolean isSilence = isSilence(buffer);
		//break processing chain on silence?
		//break if silent
		//never break the chain
		return !breakProcessingQueueOnSilence || !isSilence;
	}


}