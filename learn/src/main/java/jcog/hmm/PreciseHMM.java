/*
 * wiigee - accelerometerbased gesture recognition
 * Copyright (C) 2007, 2008, 2009 Benjamin Poppinga
 *
 * Developed at University of Oldenburg
 * Contact: wiigee@benjaminpoppinga.de
 *
 * This file is part of wiigee.
 *
 * wiigee is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package jcog.hmm;

import java.text.DecimalFormat;

/**
 * This is a Hidden Markov Model implementation which internally provides
 * the basic algorithms for training and recognition (forward and backward
 * algorithm). Since a regular Hidden Markov Model doesn't provide a possibility
 * to train multiple sequences, this implementation has been optimized for this
 * purposes using some state-of-the-art technologies described in several papers.
 *
 * @author Benjamin 'BePo' Poppinga
 * <p>
 * https:
 */

public class PreciseHMM {
	/**
	 * The number of states
	 */
	private final int numStates;

	/**
	 * The number of observations
	 */
	private final int sigmaSize;

	/**
	 * The initial probabilities for each state: p[state]
	 */
	public final double[] pi;

	/**
	 * The state change probability to switch from state A to
	 * state B: a[stateA][stateB]
	 */
	public double[][] a;

	/**
	 * The probability to emit symbol S in state A: b[stateA][symbolS]
	 */
	public double[][] b;

	/**
	 * Initialize the Hidden Markov Model in a left-to-right version.
	 *
	 * @param numStates Number of states
	 * @param sigmaSize Number of observations
	 */
	public PreciseHMM(int numStates, int sigmaSize) {
		this.numStates = numStates;
		this.sigmaSize = sigmaSize;
		pi = new double[numStates];
		a = new double[numStates][numStates];
		b = new double[numStates][sigmaSize];
		this.reset();
	}

	/**
	 * Reset the Hidden Markov Model to the initial left-to-right values.
	 */
	private void reset() {


		pi[0] = 1.0;
		for (int i = 1; i < numStates; i++) pi[i] = 0;


		int jumplimit = 2;
		for (int i = 0; i < numStates; i++)
			for (int j = 0; j < numStates; j++)
				if (i == numStates - 1 && j == numStates - 1) a[i][j] = 1.0;
				else if (i == numStates - 2 && j == numStates - 2) a[i][j] = 0.5;
				else if (i == numStates - 2 && j == numStates - 1) a[i][j] = 0.5;
				else if (i <= j && i > j - jumplimit - 1) a[i][j] = 1.0 / (jumplimit + 1);
				else a[i][j] = 0.0;


		for (int i = 0; i < numStates; i++)
			for (int j = 0; j < sigmaSize; j++)
				b[i][j] = 1.0 / sigmaSize;
	}

	/**
	 * Trains the Hidden Markov Model with multiple sequences.
	 * This method is normally not known to basic hidden markov
	 * models, because they usually use the Baum-Welch-Algorithm.
	 * This method is NOT the traditional Baum-Welch-Algorithm.
	 * <p>
	 * If you want to know in detail how it works please consider
	 * my Individuelles Projekt paper on the wiigee Homepage. Also
	 * there exist some english literature on the world wide web.
	 * Try to search for some papers by Rabiner or have a look at
	 * Vesa-Matti Mäntylä - "Discrete Hidden Markov Models with
	 * application to isolated user-dependent hand gesture recognition".
	 */
	public void train(Iterable<int[]> trainsequence) {

		double[][] a_new = new double[a.length][a.length];
		double[][] b_new = new double[b.length][b[0].length];

		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++) {
				double zaehler = 0;
				double nenner = 0;

				for (int[] seq : trainsequence) {

					double[] sf = this.calculateScalingFactor(seq);
					double[][] fwd = this.scaledForwardProc(seq);
					double[][] bwd = this.scaledBackwardProc(seq, sf);

					double zaehler_innersum = 0;
					double nenner_innersum = 0;

					for (int t = 0; t < seq.length - 1; t++) {
						zaehler_innersum += fwd[i][t] * a[i][j] * b[j][seq[t + 1]] * bwd[j][t + 1] * sf[t + 1];
						nenner_innersum += fwd[i][t] * bwd[i][t];
					}
					zaehler += zaehler_innersum;
					nenner += nenner_innersum;
				}

				a_new[i][j] = zaehler / nenner;
			}


		for (int i = 0; i < b.length; i++)
			for (int j = 0; j < b[i].length; j++) {
				double zaehler = 0;
				double nenner = 0;

				for (int[] seq : trainsequence) {

					double[] sf = this.calculateScalingFactor(seq);
					double[][] fwd = this.scaledForwardProc(seq);
					double[][] bwd = this.scaledBackwardProc(seq, sf);

					double zaehler_innersum = 0;
					double nenner_innersum = 0;

					for (int t = 0; t < seq.length - 1; t++) {
						if (seq[t] == j) zaehler_innersum += fwd[i][t] * bwd[i][t] * sf[t];
						nenner_innersum += fwd[i][t] * bwd[i][t] * sf[t];
					}
					zaehler += zaehler_innersum;
					nenner += nenner_innersum;
				}

				b_new[i][j] = zaehler / nenner;
			}

		this.a = a_new;
		this.b = b_new;
	}


	private double[] calculateScalingFactor(int[] sequence) {

		double[][] fwd = this.forwardProc(sequence);
		double[][] help = new double[fwd.length][fwd[0].length];
		double[][] scaled = new double[fwd.length][fwd[0].length];
		double[] sf = new double[sequence.length];


		for (int i = 0; i < help.length; i++) help[i][0] = fwd[i][0];


		double sum0 = 0.0;
		for (double[] doubles1 : help)
			sum0 += doubles1[0];

		for (int i = 0; i < scaled.length; i++) scaled[i][0] = help[i][0] / sum0;


		sf[0] = 1 / sum0;


		for (int t = 1; t < sequence.length; t++) {

			for (int i = 0; i < help.length; i++)
				for (int j = 0; j < this.numStates; j++) help[i][t] += scaled[j][t - 1] * a[j][i] * b[i][sequence[t]];

			double sum = 0;
			for (double[] doubles : help) sum += doubles[t];

			for (int i = 0; i < scaled.length; i++) scaled[i][t] = help[i][t] / sum;


			sf[t] = 1 / sum;

		}

		return sf;
	}

	/***
	 * Returns the scaled Forward variable.
	 * TODO: Maybe try out if the other precalculated method is faster.
	 * @param sequence
	 * @return
	 */
	private double[][] scaledForwardProc(int[] sequence) {
		double[][] fwd = this.forwardProc(sequence);
		double[][] out = new double[fwd.length][fwd[0].length];
		for (int i = 0; i < fwd.length; i++)
			for (int t = 0; t < sequence.length; t++) {
				double sum = 0;
				for (double[] doubles : fwd) sum += doubles[t];
				out[i][t] = fwd[i][t] / sum;
			}
		return out;
	}

	private double[][] scaledBackwardProc(int[] sequence, double[] sf) {
		double[][] bwd = this.backwardProc(sequence);
		double[][] out = new double[bwd.length][bwd[0].length];
		for (int i = 0; i < bwd.length; i++)
			for (int t = 0; t < sequence.length; t++) {
				out[i][t] = 1;
				for (int r = t + 1; r < sequence.length; r++) out[i][t] *= sf[r] * bwd[i][t];
			}
		return out;
	}

	/**
	 * Returns the probability that a observation sequence O belongs
	 * to this Hidden Markov Model without using the bayes classifier.
	 * Internally the well known forward algorithm is used.
	 *
	 * @param o observation sequence
	 * @return probability that sequence o belongs to this hmm
	 */
	public double getProbability(int[] o) {
		return scaledViterbi(o);
		
		/*double prob = 0.0;
		double[][] forward = this.forwardProc(o);
		
		for (int i = 0; i < forward.length; i++) { 
			prob += forward[i][forward[i].length - 1];
		}
		return prob;*/
	}

	public double sProbability(int[] o) {
		double prod = 1.0;
		double[][] fwd = this.scaledForwardProc(o);
		for (int t = 0; t < o.length; t++) {
			double sum = 0.0;
			for (int i = 0; i < this.numStates; i++) sum += fwd[i][t];
			sum = 1 / sum;
			prod *= sum;
		}
		return 1 / prod;
	}

	public double scaledViterbi(int[] o) {
		double[][] phi = new double[this.numStates][o.length];

		for (int i = 0; i < this.numStates; i++) phi[i][0] = Math.log(pi[i]) + Math.log(b[i][o[0]]);

		for (int t = 1; t < o.length; t++)
			for (int j = 0; j < this.numStates; j++) {
				double max = Double.NEGATIVE_INFINITY;
				for (int i = 0; i < this.numStates; i++) {
					double val = phi[i][t - 1] + Math.log(this.a[i][j]);
					if (val > max) max = val;
				}

				phi[j][t] = max + Math.log(this.b[j][o[t]]);
			}

		boolean seen = false;
		double best = 0;
		for (int i = 0; i < this.numStates; i++) {
			double v = phi[i][o.length - 1];
			if (v >= Double.NEGATIVE_INFINITY) if (!seen || Double.compare(v, best) > 0) {
				seen = true;
				best = v;
			}
		}
		double lp = seen ? best : Double.NEGATIVE_INFINITY;


		System.out.println("prob = " + Math.exp(lp));
		return Math.exp(lp);

	}

	/**
	 * Traditional Forward Algorithm.
	 *
	 * @param o the observationsequence O
	 * @return Array[State][Time]
	 */
	private double[][] forwardProc(int[] o) {
		double[][] f = new double[numStates][o.length];
		for (int l = 0; l < f.length; l++) f[l][0] = pi[l] * b[l][o[0]];
		for (int i = 1; i < o.length; i++)
			for (int k = 0; k < f.length; k++) {
				double sum = 0;
				for (int l = 0; l < numStates; l++) sum += f[l][i - 1] * a[l][k];
				f[k][i] = sum * b[k][o[i]];
			}
		return f;
	}

	/**
	 * Backward algorithm.
	 *
	 * @param o observation sequence o
	 * @return Array[State][Time]
	 */
	private double[][] backwardProc(int[] o) {
		int T = o.length;
		double[][] bwd = new double[numStates][T];
		/* Basisfall */
		for (int i = 0; i < numStates; i++)
			bwd[i][T - 1] = 1;
		/* Induktion */
		for (int t = T - 2; t >= 0; t--)
			for (int i = 0; i < numStates; i++) {
				bwd[i][t] = 0;
				for (int j = 0; j < numStates; j++)
					bwd[i][t] += (bwd[j][t + 1] * a[i][j] * b[j][o[t + 1]]);
			}
		return bwd;
	}


	/**
	 * Prints everything about this model, including
	 * all values. For debug purposes or if you want
	 * to comprehend what happend to the model.
	 */
	public void print() {
		DecimalFormat fmt = new DecimalFormat();
		fmt.setMinimumFractionDigits(10);
		fmt.setMaximumFractionDigits(10);
		for (int i = 0; i < numStates; i++)
			System.out.println("pi(" + i + ") = " + fmt.format(pi[i]));
		System.out.println();
		for (int i = 0; i < numStates; i++) {
			for (int j = 0; j < numStates; j++)
				System.out.println("a(" + i + ',' + j + ") = "
					+ fmt.format(a[i][j]) + ' ');
			System.out.println();
		}
		System.out.println();
		for (int i = 0; i < numStates; i++) {
			for (int k = 0; k < sigmaSize; k++)
				System.out.println("b(" + i + ',' + k + ") = "
					+ fmt.format(b[i][k]) + ' ');
			System.out.println();
		}
	}

	public double[][] getA() {
		return this.a;
	}

	public void setA(double[][] a) {
		this.a = a;
	}

	public double[][] getB() {
		return this.b;
	}

	public void setB(double[][] b) {
		this.b = b;
	}
}