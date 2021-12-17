package jcog.lstm;

import java.util.List;

public abstract class AgentSupervised {
	public static final class NonResetInteraction {
		public double[] observation;
		public double[] target_output;
	}

	public abstract void clear();
	public abstract double[] learn(double[] input, double[] target_output, float learningRate) ;
	public abstract double[] learnBatch(List<NonResetInteraction> interactions, boolean requireOutput) ;
	public abstract double[] predict(double[] input) ;
}
