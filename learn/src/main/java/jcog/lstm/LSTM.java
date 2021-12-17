package jcog.lstm;

import jcog.activation.DiffableFunction;
import jcog.activation.SigmoidActivation;
import jcog.predict.Predictor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Random;

import static jcog.Util.fma;
import static org.hipparchus.util.MathArrays.scaleInPlace;


/**
 * see: https://colah.github.io/posts/2015-08-Understanding-LSTMs/
 */
public class LSTM implements Predictor {

	public final double[][] weightsOut;
	public final double[] deltaOut;
	private final int inputs;
	private final int outputs;
	private final int cells;
	private final DiffableFunction F;
	private final DiffableFunction G;

	private final double[] context;

	public final double[][] weightsF;
	public final double[][] weightsG;
	public final double[][] dSdF;
	public final double[][] dSdG;
	public double[] out;
	public double[] in;
	public double[] full_hidden;

	public double[] sumF;
	public double[] actF;

	public double[] sumG;
	public double[] actG;

	public double[] actH;

	public double[] deltaH;
	private double alpha = 1;
	private double[] _in;

	public LSTM(int input_dimension, int outputs, int cells) {
		this.outputs = outputs;
		this.cells = cells;
		inputs = input_dimension + cells + 1;
		F = G = SigmoidActivation.the;

		context    = new double[cells];
		deltaH     = new double[cells];
		weightsF   = new double[cells][inputs];
		weightsG   = new double[cells][inputs];
		dSdF       = new double[cells][inputs];
		dSdG       = new double[cells][inputs];

		weightsOut = new double[outputs][cells + 1];
		deltaOut   = new double[outputs];
	}

	public void clear(Random r) {
		float amp = 0.5f; //TODO
		for (int j = 0; j < cells; j++) {
			for (int i = 0; i < inputs; i++) {
				weightsF[j][i] = random(amp, r);
				weightsG[j][i] = random(amp, r);
			}
		}
		for (int j = 0; j < cells + 1; j++) {
			for (int k = 0; k < outputs; k++)
				weightsOut[k][j] = random(amp, r);
		}

	}

	protected double random(float amp, Random r) {
		return
			//r.nextGaussian()
			r.nextDouble()
					* amp;
	}

	private static void updateWeights(double learningRate,
									  int length,
									  double[] in,
									  double[] out) {
		for (int i = length - 1; i >= 0; i--)
			out[i] = fma(in[i], learningRate, out[i]);
	}

	public void clear() {

		Arrays.fill(context, 0);

		for (int c = 0; c < cells; c++) {
			Arrays.fill(this.dSdG[c], 0);
			Arrays.fill(this.dSdF[c], 0);
		}
	}

	/**
	 * 0 = total forget, 1 = no forget. proportional version of the RESET operation
	 */
	public void forget(float forgetRate) {

		float f = 1f - forgetRate;

		if (f > 1)
			throw new UnsupportedOperationException();

		if (f <= Float.MIN_NORMAL) {
			clear();
			return;
		}

		scaleInPlace(f, context);
		for (double[] x : dSdG) scaleInPlace(f, x);
		for (double[] x : dSdF) scaleInPlace(f, x);
	}

	public final double[] get(double[] input) {
		return put(input, null, -1);
	}


	/** TODO make a putDelta(..) compatible method */
	@Deprecated public double[] put(double[] input, @Nullable double[] target_output, float pri) {

		pri*=alpha;

		int cells = this.cells, inputs = this.inputs;

		this._in = input;
		if ((this.in == null) || (this.in.length != inputs))
			this.in = new double[inputs];

		double[] in = this.in;

		int loc = 0;
		for (double i : input)
			in[loc++] = i;

		for (double c : context)
			in[loc++] = c;

		in[loc++] = 1.0;

		if ((sumF == null) || (sumF.length != cells)) {
			sumF = new double[cells];
			actF = new double[cells];
			sumG = new double[cells];
			actG = new double[cells];
			actH = new double[cells];
			full_hidden = new double[cells + 1];
		}
		if (out == null || out.length!=outputs) {
			out = new double[outputs];
		}

		double[] hidden = this.full_hidden;


		for (int j = 0; j < cells; j++) {
			double[] wj = weightsF[j];
			double[] wg = weightsG[j];
			double sf = 0, sg = 0;
			for (int i = 0; i < inputs; i++) {
				double ii = in[i];
				sf = fma(ii, wj[i], sf);
				sg = fma(ii, wg[i], sg);
			}
			sumF[j] = sf;
			sumG[j] = sg;
		}

		for (int j = 0; j < cells; j++) {
			double actfj = actF[j] = F.valueOf(sumF[j]);
			double actgj = actG[j] = G.valueOf(sumG[j]);
			hidden[j] = actH[j] = fma(actfj, context[j], (1 - actfj) * actgj);
		}

		hidden[cells] = 1;


		for (int k = 0; k < outputs; k++) {
			double[] wk = weightsOut[k];
			int bound = cells + 1;
			double s = 0;
			for (int j = 0; j < bound; j++)
				s = fma(wk[j], hidden[j], s);
			out[k] = s;
		}


		for (int j = 0; j < cells; j++) {

			double f = actF[j];
			double dg = G.derivative(sumG[j]);
			double udg = (1 - f) * dg;

			double g = actG[j];
			double df = F.derivative(sumF[j]);
			double h = context[j];
			double udf = (h - g) * df;

			double[] dsg = dSdG[j];
			double[] dsf = dSdF[j];

			for (int i = 0; i < inputs; i++) {
				double ii = in[i];
				dsg[i] = fma(ii, udg, f * dsg[i]);
				dsf[i] = fma(ii, udf, f * dsf[i]);
			}
		}

		if (target_output != null) {




			Arrays.fill(deltaH, 0);

			for (int k = 0; k < outputs; k++) {

				double d = target_output[k] - out[k];
				double dok = deltaOut[k] = d;

				double[] wk = weightsOut[k];

				double[] dh = this.deltaH;
				double[] ah = this.actH;
				double dokLearn = dok * pri;
				for (int j = cells - 1; j >= 0; j--) {
					dh[j] = fma(dok, wk[j], dh[j]);
					wk[j] = fma(dokLearn, ah[j], wk[j]);
				}

				wk[cells] += dok /* * 1.0 */ * pri;
			}


			for (int j = 0; j < cells; j++) {
				double dhj = deltaH[j];
				double pdhj = pri * dhj;
				updateWeights(pdhj, inputs, dSdF[j], weightsF[j]);
				updateWeights(pdhj, inputs, dSdG[j], weightsG[j]);
			}
		}


		System.arraycopy(actH, 0, context, 0, cells);

		return out;
	}

//	@Override
//	public void putDelta(double[] d, float pri) {
//		//TODO maybe this delta needs added to output for put
//		//double[] target = d.clone();
//		double[] target = MathArrays.ebeAdd(d, out);
//		put(_in, target, pri);
//		//return errorAbs();
//	}

//	public double putDelta(double[] in, double[] d, float pri) {
//		final int n = d.length;
//		assert(out.length == n);
//
//		double[] outNext = new double[n];
//		for (int i = 0; i < n; i++)
//			outNext[i] = d[i] - out[i];
//
//		put(in, outNext, pri);
//
//		return 0; //TODO error calculation
//	}


	public final LSTM alpha(double a) {
		this.alpha = a;
		return this;
	}

}