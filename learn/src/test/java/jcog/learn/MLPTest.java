package jcog.learn;

import jcog.WTF;
import jcog.activation.ReluActivation;
import jcog.activation.SigmoidActivation;
import jcog.data.DistanceFunction;
import jcog.nn.MLP;
import jcog.nn.layer.DenseLayer;
import jcog.nn.optimizer.AdamOptimizer;
import jcog.nn.optimizer.RMSPropOptimizer;
import jcog.nn.optimizer.SGDOptimizer;
import jcog.nn.optimizer.WeightUpdater;
import jcog.predict.Predictor;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.signal.anomaly.ewma.Ewma;
import jcog.table.ARFF;
import jcog.table.DataTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MLPTest {

	private static final boolean trace = false;

	@Test void XOR2_sgd() {
		mlpXor2Test(new SGDOptimizer(0));
	}
	@Test void XOR2_sgd_minibatch() {
		mlpXor2Test(new SGDOptimizer(0).minibatches(2));
	}
	@Test void IRIS_in_2_out_1_sgd() {
		mlpIrisTest(new SGDOptimizer(0));
	}

	@Test void IRIS_in_2_out_1_sgd_dropout() {
		mlpIrisTest(new SGDOptimizer(0), 0.1f);
	}
	@Test void IRIS_in_2_out_1_sgd_minibatch() {
		mlpIrisTest(new SGDOptimizer(0).minibatches(4));
	}
	@Test void IRIS_in_2_out_1_sgd_momentum() {
		mlpIrisTest(new SGDOptimizer(0.9f));
	}
	@Test void IRIS_adam() {
		mlpIrisTest(new AdamOptimizer());
	}

	@Test void IRIS_adam_dropout() {
		mlpIrisTest(new AdamOptimizer(), 0.1f);
	}
	@Test void IRIS_adam_minibatch8() {
		mlpIrisTest(new AdamOptimizer().minibatches(8));
	}

	@Test void XOR2_sgd_momentum_half() {
		mlpXor2Test(new SGDOptimizer(0.5f));
	}

	@Test void XOR2_sgd_momentum_full() {
		mlpXor2Test(new SGDOptimizer(0.99f));
	}

	@Test void XOR2_rmsprop() {
		mlpXor2Test(new RMSPropOptimizer());
	}

	@Test void XOR2_adam() {
		mlpXor2Test(new AdamOptimizer());
	}
	@Test void XOR2_adam_minibatch2() {
		mlpXor2Test(new AdamOptimizer().minibatches(2));
	}

	private static void mlpXor2Test(WeightUpdater u) {
		MLP predictor = new MLP(2,
				new MLP.Dense(2, SigmoidActivation.the),
				new MLP.Output(1)
		).optimizer(u);

		xor2Test(predictor);
	}

	private static void mlpIrisTest(WeightUpdater u) {
		mlpIrisTest(u, 0);
	}
	private static void mlpIrisTest(WeightUpdater u, float dropOut) {
		MLP predictor = new MLP(4,
				new MLP.Dense(8, ReluActivation.the),
					new MLP.Dense(3, SigmoidActivation.the)
		).optimizer(u);

		((DenseLayer)predictor.layers[1]).dropout = dropOut;

		irisTest(predictor);
	}

	static void xor2Test(Predictor predictor) {
		predictorTest(predictor, MLPTest.xor2ARFF, 0, 2, 2, 3);
	}

	static void irisTest(Predictor predictor) {
		predictorTest(predictor, MLPTest.irisARFF, 0, 4, 4, 7);
	}

	private static void predictorTest(Predictor predictor, String data, int is, int ie, int os, int oe) {
		try {
			predictorTest(predictor, new ARFF(data), is, ie, os, oe);
		} catch (IOException | ARFF.ARFFParseError e) {
			throw new WTF(e);
		}
	}

	private static void predictorTest(Predictor p, DataTable d, int is, int ie, int os, int oe) {
		int cases = d.rowCount();

		int seed = 1;

		Random r = new XoRoShiRo128PlusRandom(seed);

		p.clear(r);

		float alpha = 0.04f;

		int maxIter = 8000*d.rowCount();
		int minIterations = cases * 2;
		double errGoal = 0.05;

		Ewma errMean = new Ewma().period(minIterations);
		int t;
		for (t = 0; t < maxIter; t++) {

			var row = d.row(r.nextInt(cases));

			double[]
					I = DataTable.toDouble(row, is, ie),
					O = DataTable.toDouble(row, os, oe);

			double[] oActual = p.put(I, O, alpha);

			errMean.accept(DistanceFunction.distanceManhattan(O, oActual));

//			if (trace && (t + 1) % 10 == 0) {
//				for (int c = 0; c < cases; c++) {
//					double[]
//							Ic = DataTable.toDouble(d.row(c), is, ie),
//							Oc = DataTable.toDouble(d.row(c), os, oe);
//					System.out.println(t + "\t" + n4(Ic) + " -> " + n4(p.get(Ic)));
//				}
//				System.out.println();
//			}

			double err = errMean.mean();

			if (trace && (t + 1) % 10 == 0)
				System.out.println("err=" + err);

			if (t > minIterations && err < errGoal)
				break;
		}
		System.out.println(errMean.mean() + " " + p + " " + t);
		assertTrue(t < maxIter-cases-1);
	}


	/** https://github.com/renatopp/arff-datasets/blob/master/boolean/xor.arff */
	private static final String xor2ARFF = "%\n" +
			"% XOR\n" +
			"%\n" +
			'\n' +
			"@RELATION XOR\n" +
			'\n' +
			"@ATTRIBUTE input1 REAL\n" +
			"@ATTRIBUTE input2 REAL\n" +
			"@ATTRIBUTE y REAL\n" +
			'\n' +
			"@DATA\n" +
			"0.0,0.0,0.0\n" +
			"0.0,1.0,1.0\n" +
			"1.0,0.0,1.0\n" +
			"1.0,1.0,0.0\n";

	private static final String irisARFF = "% 1. Title: Iris Plants Database\n" +
			"% \n" +
			"% 2. Sources:\n" +
			"%      (a) Creator: R.A. Fisher\n" +
			"%      (b) Donor: Michael Marshall (MARSHALL%PLU@io.arc.nasa.gov)\n" +
			"%      (c) Date: July, 1988\n" +
			"% \n" +
			"% 3. Past Usage:\n" +
			"%    - Publications: too many to mention!!!  Here are a few.\n" +
			"%    1. Fisher,R.A. \"The use of multiple measurements in taxonomic problems\"\n" +
			"%       Annual Eugenics, 7, Part II, 179-188 (1936); also in \"Contributions\n" +
			"%       to Mathematical Statistics\" (John Wiley, NY, 1950).\n" +
			"%    2. Duda,R.O., & Hart,P.E. (1973) Pattern Classification and Scene Analysis.\n" +
			"%       (Q327.D83) John Wiley & Sons.  ISBN 0-471-22361-1.  See page 218.\n" +
			"%    3. Dasarathy, B.V. (1980) \"Nosing Around the Neighborhood: A New System\n" +
			"%       Structure and Classification Rule for Recognition in Partially Exposed\n" +
			"%       Environments\".  IEEE Transactions on Pattern Analysis and Machine\n" +
			"%       Intelligence, Vol. PAMI-2, No. 1, 67-71.\n" +
			"%       -- Results:\n" +
			"%          -- very low misclassification rates (0% for the setosa class)\n" +
			"%    4. Gates, G.W. (1972) \"The Reduced Nearest Neighbor Rule\".  IEEE \n" +
			"%       Transactions on Information Theory, May 1972, 431-433.\n" +
			"%       -- Results:\n" +
			"%          -- very low misclassification rates again\n" +
			"%    5. See also: 1988 MLC Proceedings, 54-64.  Cheeseman et al's AUTOCLASS II\n" +
			"%       conceptual clustering system finds 3 classes in the data.\n" +
			"% \n" +
			"% 4. Relevant Information:\n" +
			"%    --- This is perhaps the best known database to be found in the pattern\n" +
			"%        recognition literature.  Fisher's paper is a classic in the field\n" +
			"%        and is referenced frequently to this day.  (See Duda & Hart, for\n" +
			"%        example.)  The data setAt contains 3 classes of 50 instances each,\n" +
			"%        where each class refers to a type of iris plant.  One class is\n" +
			"%        linearly separable from the other 2; the latter are NOT linearly\n" +
			"%        separable from each other.\n" +
			"%    --- Predicted attribute: class of iris plant.\n" +
			"%    --- This is an exceedingly simple domain.\n" +
			"% \n" +
			"% 5. Number of Instances: 150 (50 in each of three classes)\n" +
			"% \n" +
			"% 6. Number of Attributes: 4 numeric, predictive attributes and the class\n" +
			"% \n" +
			"% 7. Attribute Information:\n" +
			"%    1. sepal length in cm\n" +
			"%    2. sepal width in cm\n" +
			"%    3. petal length in cm\n" +
			"%    4. petal width in cm\n" +
			"%    5. class: \n" +
			"%       -- Iris Setosa\n" +
			"%       -- Iris Versicolour\n" +
			"%       -- Iris Virginica\n" +
			"% \n" +
			"% 8. Missing Attribute Values: None\n" +
			"% \n" +
			"% Summary Statistics:\n" +
			"%  \t           Min  Max   Mean    SD   Class Correlation\n" +
			"%    sepal length: 4.3  7.9   5.84  0.83    0.7826   \n" +
			"%     sepal width: 2.0  4.4   3.05  0.43   -0.4194\n" +
			"%    petal length: 1.0  6.9   3.76  1.76    0.9490  (high!)\n" +
			"%     petal width: 0.1  2.5   1.20  0.76    0.9565  (high!)\n" +
			"% \n" +
			"% 9. Class Distribution: 33.3% for each of 3 classes.\n" +
			'\n' +
			"@RELATION iris\n" +
			'\n' +
			"@ATTRIBUTE \"(sepal,length)\"\tREAL\n" +
			"@ATTRIBUTE \"(sepal,width)\"\tREAL\n" +
			"@ATTRIBUTE \"(petal,length)\"\tREAL\n" +
			"@ATTRIBUTE \"(petal,width)\"\tREAL\n" +
			"@ATTRIBUTE \"(a)\"\tREAL\n" +
			"@ATTRIBUTE \"(b)\"\tREAL\n" +
			"@ATTRIBUTE \"(c)\"\tREAL\n" +
			'\n' +
			"@DATA\n" +
			"5.1,3.5,1.4,0.2,1,0,0\n" +
			"4.9,3.0,1.4,0.2,1,0,0\n" +
			"4.7,3.2,1.3,0.2,1,0,0\n" +
			"4.6,3.1,1.5,0.2,1,0,0\n" +
			"5.0,3.6,1.4,0.2,1,0,0\n" +
			"5.4,3.9,1.7,0.4,1,0,0\n" +
			"4.6,3.4,1.4,0.3,1,0,0\n" +
			"5.0,3.4,1.5,0.2,1,0,0\n" +
			"4.4,2.9,1.4,0.2,1,0,0\n" +
			"4.9,3.1,1.5,0.1,1,0,0\n" +
			"5.4,3.7,1.5,0.2,1,0,0\n" +
			"4.8,3.4,1.6,0.2,1,0,0\n" +
			"4.8,3.0,1.4,0.1,1,0,0\n" +
			"4.3,3.0,1.1,0.1,1,0,0\n" +
			"5.8,4.0,1.2,0.2,1,0,0\n" +
			"5.7,4.4,1.5,0.4,1,0,0\n" +
			"5.4,3.9,1.3,0.4,1,0,0\n" +
			"5.1,3.5,1.4,0.3,1,0,0\n" +
			"5.7,3.8,1.7,0.3,1,0,0\n" +
			"5.1,3.8,1.5,0.3,1,0,0\n" +
			"5.4,3.4,1.7,0.2,1,0,0\n" +
			"5.1,3.7,1.5,0.4,1,0,0\n" +
			"4.6,3.6,1.0,0.2,1,0,0\n" +
			"5.1,3.3,1.7,0.5,1,0,0\n" +
			"4.8,3.4,1.9,0.2,1,0,0\n" +
			"5.0,3.0,1.6,0.2,1,0,0\n" +
			"5.0,3.4,1.6,0.4,1,0,0\n" +
			"5.2,3.5,1.5,0.2,1,0,0\n" +
			"5.2,3.4,1.4,0.2,1,0,0\n" +
			"4.7,3.2,1.6,0.2,1,0,0\n" +
			"4.8,3.1,1.6,0.2,1,0,0\n" +
			"5.4,3.4,1.5,0.4,1,0,0\n" +
			"5.2,4.1,1.5,0.1,1,0,0\n" +
			"5.5,4.2,1.4,0.2,1,0,0\n" +
			"4.9,3.1,1.5,0.1,1,0,0\n" +
			"5.0,3.2,1.2,0.2,1,0,0\n" +
			"5.5,3.5,1.3,0.2,1,0,0\n" +
			"4.9,3.1,1.5,0.1,1,0,0\n" +
			"4.4,3.0,1.3,0.2,1,0,0\n" +
			"5.1,3.4,1.5,0.2,1,0,0\n" +
			"5.0,3.5,1.3,0.3,1,0,0\n" +
			"4.5,2.3,1.3,0.3,1,0,0\n" +
			"4.4,3.2,1.3,0.2,1,0,0\n" +
			"5.0,3.5,1.6,0.6,1,0,0\n" +
			"5.1,3.8,1.9,0.4,1,0,0\n" +
			"4.8,3.0,1.4,0.3,1,0,0\n" +
			"5.1,3.8,1.6,0.2,1,0,0\n" +
			"4.6,3.2,1.4,0.2,1,0,0\n" +
			"5.3,3.7,1.5,0.2,1,0,0\n" +
			"5.0,3.3,1.4,0.2,1,0,0\n" +
			"7.0,3.2,4.7,1.4,0,1,0\n" +
			"6.4,3.2,4.5,1.5,0,1,0\n" +
			"6.9,3.1,4.9,1.5,0,1,0\n" +
			"5.5,2.3,4.0,1.3,0,1,0\n" +
			"6.5,2.8,4.6,1.5,0,1,0\n" +
			"5.7,2.8,4.5,1.3,0,1,0\n" +
			"6.3,3.3,4.7,1.6,0,1,0\n" +
			"4.9,2.4,3.3,1.0,0,1,0\n" +
			"6.6,2.9,4.6,1.3,0,1,0\n" +
			"5.2,2.7,3.9,1.4,0,1,0\n" +
			"5.0,2.0,3.5,1.0,0,1,0\n" +
			"5.9,3.0,4.2,1.5,0,1,0\n" +
			"6.0,2.2,4.0,1.0,0,1,0\n" +
			"6.1,2.9,4.7,1.4,0,1,0\n" +
			"5.6,2.9,3.6,1.3,0,1,0\n" +
			"6.7,3.1,4.4,1.4,0,1,0\n" +
			"5.6,3.0,4.5,1.5,0,1,0\n" +
			"5.8,2.7,4.1,1.0,0,1,0\n" +
			"6.2,2.2,4.5,1.5,0,1,0\n" +
			"5.6,2.5,3.9,1.1,0,1,0\n" +
			"5.9,3.2,4.8,1.8,0,1,0\n" +
			"6.1,2.8,4.0,1.3,0,1,0\n" +
			"6.3,2.5,4.9,1.5,0,1,0\n" +
			"6.1,2.8,4.7,1.2,0,1,0\n" +
			"6.4,2.9,4.3,1.3,0,1,0\n" +
			"6.6,3.0,4.4,1.4,0,1,0\n" +
			"6.8,2.8,4.8,1.4,0,1,0\n" +
			"6.7,3.0,5.0,1.7,0,1,0\n" +
			"6.0,2.9,4.5,1.5,0,1,0\n" +
			"5.7,2.6,3.5,1.0,0,1,0\n" +
			"5.5,2.4,3.8,1.1,0,1,0\n" +
			"5.5,2.4,3.7,1.0,0,1,0\n" +
			"5.8,2.7,3.9,1.2,0,1,0\n" +
			"6.0,2.7,5.1,1.6,0,1,0\n" +
			"5.4,3.0,4.5,1.5,0,1,0\n" +
			"6.0,3.4,4.5,1.6,0,1,0\n" +
			"6.7,3.1,4.7,1.5,0,1,0\n" +
			"6.3,2.3,4.4,1.3,0,1,0\n" +
			"5.6,3.0,4.1,1.3,0,1,0\n" +
			"5.5,2.5,4.0,1.3,0,1,0\n" +
			"5.5,2.6,4.4,1.2,0,1,0\n" +
			"6.1,3.0,4.6,1.4,0,1,0\n" +
			"5.8,2.6,4.0,1.2,0,1,0\n" +
			"5.0,2.3,3.3,1.0,0,1,0\n" +
			"5.6,2.7,4.2,1.3,0,1,0\n" +
			"5.7,3.0,4.2,1.2,0,1,0\n" +
			"5.7,2.9,4.2,1.3,0,1,0\n" +
			"6.2,2.9,4.3,1.3,0,1,0\n" +
			"5.1,2.5,3.0,1.1,0,1,0\n" +
			"5.7,2.8,4.1,1.3,0,1,0\n" +
			"6.3,3.3,6.0,2.5,0,0,1\n" +
			"5.8,2.7,5.1,1.9,0,0,1\n" +
			"7.1,3.0,5.9,2.1,0,0,1\n" +
			"6.3,2.9,5.6,1.8,0,0,1\n" +
			"6.5,3.0,5.8,2.2,0,0,1\n" +
			"7.6,3.0,6.6,2.1,0,0,1\n" +
			"4.9,2.5,4.5,1.7,0,0,1\n" +
			"7.3,2.9,6.3,1.8,0,0,1\n" +
			"6.7,2.5,5.8,1.8,0,0,1\n" +
			"7.2,3.6,6.1,2.5,0,0,1\n" +
			"6.5,3.2,5.1,2.0,0,0,1\n" +
			"6.4,2.7,5.3,1.9,0,0,1\n" +
			"6.8,3.0,5.5,2.1,0,0,1\n" +
			"5.7,2.5,5.0,2.0,0,0,1\n" +
			"5.8,2.8,5.1,2.4,0,0,1\n" +
			"6.4,3.2,5.3,2.3,0,0,1\n" +
			"6.5,3.0,5.5,1.8,0,0,1\n" +
			"7.7,3.8,6.7,2.2,0,0,1\n" +
			"7.7,2.6,6.9,2.3,0,0,1\n" +
			"6.0,2.2,5.0,1.5,0,0,1\n" +
			"6.9,3.2,5.7,2.3,0,0,1\n" +
			"5.6,2.8,4.9,2.0,0,0,1\n" +
			"7.7,2.8,6.7,2.0,0,0,1\n" +
			"6.3,2.7,4.9,1.8,0,0,1\n" +
			"6.7,3.3,5.7,2.1,0,0,1\n" +
			"7.2,3.2,6.0,1.8,0,0,1\n" +
			"6.2,2.8,4.8,1.8,0,0,1\n" +
			"6.1,3.0,4.9,1.8,0,0,1\n" +
			"6.4,2.8,5.6,2.1,0,0,1\n" +
			"7.2,3.0,5.8,1.6,0,0,1\n" +
			"7.4,2.8,6.1,1.9,0,0,1\n" +
			"7.9,3.8,6.4,2.0,0,0,1\n" +
			"6.4,2.8,5.6,2.2,0,0,1\n" +
			"6.3,2.8,5.1,1.5,0,0,1\n" +
			"6.1,2.6,5.6,1.4,0,0,1\n" +
			"7.7,3.0,6.1,2.3,0,0,1\n" +
			"6.3,3.4,5.6,2.4,0,0,1\n" +
			"6.4,3.1,5.5,1.8,0,0,1\n" +
			"6.0,3.0,4.8,1.8,0,0,1\n" +
			"6.9,3.1,5.4,2.1,0,0,1\n" +
			"6.7,3.1,5.6,2.4,0,0,1\n" +
			"6.9,3.1,5.1,2.3,0,0,1\n" +
			"5.8,2.7,5.1,1.9,0,0,1\n" +
			"6.8,3.2,5.9,2.3,0,0,1\n" +
			"6.7,3.3,5.7,2.5,0,0,1\n" +
			"6.7,3.0,5.2,2.3,0,0,1\n" +
			"6.3,2.5,5.0,1.9,0,0,1\n" +
			"6.5,3.0,5.2,2.0,0,0,1\n" +
			"6.2,3.4,5.4,2.3,0,0,1\n" +
			"5.9,3.0,5.1,1.8,0,0,1\n";

}