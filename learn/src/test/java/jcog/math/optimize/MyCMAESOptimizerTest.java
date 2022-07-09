package jcog.math.optimize;

import jcog.random.XoRoShiRo128PlusRandom;
import org.hipparchus.analysis.MultivariateFunction;
import org.hipparchus.optim.InitialGuess;
import org.hipparchus.optim.MaxEval;
import org.hipparchus.optim.PointValuePair;
import org.hipparchus.optim.SimpleBounds;
import org.hipparchus.optim.nonlinear.scalar.GoalType;
import org.hipparchus.optim.nonlinear.scalar.ObjectiveFunction;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class MyCMAESOptimizerTest {

	private static final int DIM = 13;
	private static final int LAMBDA = 4 + (int) (3. * Math.log(DIM));

	@Test
	void testRosen() {
		double[] startPoint = point(DIM, 0.1);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 1.0), 0.0);
		doTest(new Rosen(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
		doTest(new Rosen(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
	}

	@Test void testRosenAsync() {
		double[] start = point(DIM, 0.1);
		double[] sigma = point(DIM, 0.1);
		int pop = LAMBDA;
		int iters = 1800;

		Rosen rosen = new Rosen();

		var o = new MyAsyncCMAESOptimizer(pop, sigma) {

			@Override
			protected boolean apply(double[][] X) {
				//ASYNC start:
				double[] y = new double[X.length];
				for (int i = 0; i < X.length; i++)
					y[i] = rosen.value(X[i]);

				//System.out.println(n2(X[0]) + "->" + n2(y));

				//ASYNC end:
				commit(y);
				return true;
			}
		};

		var iter = o.iterator(GoalType.MINIMIZE, start);
		for (int i = 0; i < iters; i++)
			iter.iterate();

		double[] best = o.best();  //System.out.println(n2(o.best()));

		/* 1,1,1,1,... */
		for (int i = 0; i < pop; i++)  assertEquals(best[i], 1, 1.0e-12);
	}

	@Test
	void testMaximize() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 1.0);
		doTest(new MinusElli(), startPoint, insigma, boundaries,
			GoalType.MAXIMIZE, LAMBDA, true, 0, 1.0 - 1e-13,
			2e-10, 5e-6, 100000, expected);
		doTest(new MinusElli(), startPoint, insigma, boundaries,
			GoalType.MAXIMIZE, LAMBDA, false, 0, 1.0 - 1e-13,
			2e-10, 5e-6, 100000, expected);
		boundaries = boundaries(DIM, -0.3, 0.3);
		startPoint = point(DIM, 0.1);
		doTest(new MinusElli(), startPoint, insigma, boundaries,
			GoalType.MAXIMIZE, LAMBDA, true, 0, 1.0 - 1e-13,
			2e-10, 5e-6, 200000, expected);
	}

	@Test
	void testEllipse() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new Elli(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
		doTest(new Elli(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
	}

	@Test
	void testElliRotated() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new ElliRotated(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
		doTest(new ElliRotated(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
	}

	@Test
	void testCigar() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new Cigar(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
			1e-13, 1e-6, 200000, expected);
		doTest(new Cigar(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
	}

	@Test
	void testCigarWithBoundaries() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = boundaries(DIM, -1e100, Double.POSITIVE_INFINITY);
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new Cigar(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
			1e-13, 1e-6, 200000, expected);
		doTest(new Cigar(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
	}

	@Test
	void testTwoAxes() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new TwoAxes(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, 2 * LAMBDA, true, 0, 1e-13,
			1e-13, 1e-6, 200000, expected);
		doTest(new TwoAxes(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, 2 * LAMBDA, false, 0, 1e-13,
			1e-8, 1e-3, 200000, expected);
	}

	@Test
	void testCigTab() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.3);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new CigTab(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
			1e-13, 5e-5, 100000, expected);
		doTest(new CigTab(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
			1e-13, 5e-5, 100000, expected);
	}

	@Test
	void testSphere() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new Sphere(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
		doTest(new Sphere(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
	}

	@Test
	void testTablet() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new Tablet(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
		doTest(new Tablet(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
	}

	@Test
	void testDiffPow() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new DiffPow(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, 10, true, 0, 1e-13,
			1e-8, 1e-1, 100000, expected);
		doTest(new DiffPow(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, 10, false, 0, 1e-13,
			1e-8, 2e-1, 100000, expected);
	}

	@Test
	void testSsDiffPow() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new SsDiffPow(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, 10, true, 0, 1e-13,
			1e-4, 1e-1, 200000, expected);
		doTest(new SsDiffPow(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, 10, false, 0, 1e-13,
			1e-4, 1e-1, 200000, expected);
	}

	@Test
	void testAckley() {
		double[] startPoint = point(DIM, 1.0);
		double[] insigma = point(DIM, 1.0);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new Ackley(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, 2 * LAMBDA, true, 0, 1e-13,
			1e-9, 1e-5, 100000, expected);
		doTest(new Ackley(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, 2 * LAMBDA, false, 0, 1e-13,
			1e-9, 1e-5, 100000, expected);
	}

	@Test
	void                                                                                  testRastrigin() {
		double[] startPoint = point(DIM, 0.1);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 0.0), 0.0);
		doTest(new Rastrigin(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, (int) (200 * Math.sqrt(DIM)), true, 0, 1e-13,
			1e-13, 1e-6, 200000, expected);
		doTest(new Rastrigin(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, (int) (200 * Math.sqrt(DIM)), false, 0, 1e-13,
			1e-13, 1e-6, 200000, expected);
	}

	@Test
	void testConstrainedRosen() {
		double[] startPoint = point(DIM, 0.1);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = boundaries(DIM, -1, 2);
		PointValuePair expected =
			new PointValuePair(point(DIM, 1.0), 0.0);
		doTest(new Rosen(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, 2 * LAMBDA, true, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
		doTest(new Rosen(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, 2 * LAMBDA, false, 0, 1e-13,
			1e-13, 1e-6, 100000, expected);
	}

	@Test
	void testDiagonalRosen() {
		double[] startPoint = point(DIM, 0.1);
		double[] insigma = point(DIM, 0.1);
		double[][] boundaries = null;
		PointValuePair expected =
			new PointValuePair(point(DIM, 1.0), 0.0);
		doTest(new Rosen(), startPoint, insigma, boundaries,
			GoalType.MINIMIZE, LAMBDA, false, 1, 1e-13,
			1e-10, 1e-4, 1000000, expected);
	}

	@Test
	void testMath864() {
		double[] sigma = {1e-1};
		MyCMAESOptimizer optimizer
			= new MyCMAESOptimizer(30000, 0, true, 10,
			0, rng(), false, null, 5, sigma);
		MultivariateFunction fitnessFunction = parameters -> {
            final double target = 1;
            double error = target - parameters[0];
            return error * error;
        };

		double[] start = {0};
		double[] lower = {-1e6};
		double[] upper = {1.5};
		double[] result = optimizer.optimize(new MaxEval(10000),
			new ObjectiveFunction(fitnessFunction),
			GoalType.MINIMIZE,
			new InitialGuess(start),
			new SimpleBounds(lower, upper)).getPoint();
		assertTrue(
			result[0] <= upper[0], () -> "Out of bounds (" + result[0] + " > " + upper[0] + ')');
	}

	/**
	 * Cf. MATH-867
	 */
	@Test
	void testFitAccuracyDependsOnBoundary() {

		double[] sigma1 = {1e-1};
		MyCMAESOptimizer optimizer
			= new MyCMAESOptimizer(30000, 0, true, 10,
			0, rng(), false, null, 5, sigma1);
		MultivariateFunction fitnessFunction = parameters -> {
            final double target = 11.1;
            double error = target - parameters[0];
            return error * error;
        };

		double[] start = {1};


		PointValuePair result = optimizer.optimize(new MaxEval(100000),
			new ObjectiveFunction(fitnessFunction),
			GoalType.MINIMIZE,
			SimpleBounds.unbounded(1),
			new InitialGuess(start));
		double resNoBound = result.getPoint()[0];


        double[] sigma2 = {10};
		optimizer
			= new MyCMAESOptimizer(30000, 0, true, 10,
			0, rng(), false, null, 5, sigma2);

        double[] upper = {5e16};
        double[] lower = {-20};
        result = optimizer.optimize(new MaxEval(100000),
			new ObjectiveFunction(fitnessFunction),
			GoalType.MINIMIZE,
			new InitialGuess(start),
			new SimpleBounds(lower, upper));
		double resNearLo = result.getPoint()[0];


		lower[0] = -5e16;
		upper[0] = 20;
		result = optimizer.optimize(new MaxEval(100000),
			new ObjectiveFunction(fitnessFunction),
			GoalType.MINIMIZE,
			new InitialGuess(start),
			new SimpleBounds(lower, upper));
		double resNearHi = result.getPoint()[0];


		assertEquals(resNoBound, resNearLo, 1e-3);
		assertEquals(resNoBound, resNearHi, 1e-3);
	}

	/**
	 * @param func           Function to optimize.
	 * @param startPoint     Starting point.
	 * @param inSigma        Individual input sigma.
	 * @param boundaries     Upper / lower point limit.
	 * @param goal           Minimization or maximization.
	 * @param lambda         Population size used for offspring.
	 * @param isActive       Covariance update mechanism.
	 * @param diagonalOnly   Simplified covariance update.
	 * @param stopValue      Termination criteria for optimization.
	 * @param fTol           Tolerance relative error on the objective function.
	 * @param pointTol       Tolerance for checking that the optimum is correct.
	 * @param maxEvaluations Maximum number of evaluations.
	 * @param expected       Expected point / value.
	 */
	private static void doTest(MultivariateFunction func,
							   double[] startPoint,
							   double[] inSigma,
							   double[][] boundaries,
							   GoalType goal,
							   int lambda,
							   boolean isActive,
							   int diagonalOnly,
							   double stopValue,
							   double fTol,
							   double pointTol,
							   int maxEvaluations,
							   PointValuePair expected) {
		int dim = startPoint.length;

		MyCMAESOptimizer optim = new MyCMAESOptimizer(10000, stopValue, isActive, diagonalOnly,
			0, rng(), false, null, lambda, inSigma);
		PointValuePair result = boundaries == null ?
			optim.optimize(
				new MaxEval(maxEvaluations),
				new ObjectiveFunction(func),
				goal,
				new InitialGuess(startPoint),
				SimpleBounds.unbounded(dim)) :
			optim.optimize(new MaxEval(maxEvaluations),
				new ObjectiveFunction(func),
				goal,
				new SimpleBounds(boundaries[0], boundaries[1]),
				new InitialGuess(startPoint));


		assertEquals(expected.getValue(), result.getValue(), fTol);
		for (int i = 0; i < dim; i++) {
			assertEquals(expected.getPoint()[i], result.getPoint()[i], pointTol);
		}

		assertTrue(optim.getIterations() > 0);
	}

	private static Random rng() {
		return new XoRoShiRo128PlusRandom(1);
//		return new RandomAdaptor(
//			new MersenneTwister(1)
//		);
	}

	private static double[] point(int n, double value) {
		double[] ds = new double[n];
		Arrays.fill(ds, value);
		return ds;
	}

	private static double[][] boundaries(int dim,
										 double lower, double upper) {
		double[][] boundaries = new double[2][dim];
		for (int i = 0; i < dim; i++) {
			boundaries[0][i] = lower;
		}
		for (int i = 0; i < dim; i++) {
			boundaries[1][i] = upper;
		}
		return boundaries;
	}

	private static class Sphere implements MultivariateFunction {

		@Override
		public double value(double[] x) {
			double f = Arrays.stream(x).map(v -> v * v).sum();
			return f;
		}
	}

	private static class Cigar implements MultivariateFunction {
		private double factor;

		Cigar() {
			this(1e3);
		}

		Cigar(double axisratio) {
			factor = axisratio * axisratio;
		}

		@Override
		public double value(double[] x) {
			double f = x[0] * x[0];
			double sum = IntStream.range(1, x.length).mapToDouble(i -> factor * x[i] * x[i]).sum();
			f += sum;
			return f;
		}
	}

	private static class Tablet implements MultivariateFunction {
		private double factor;

		Tablet() {
			this(1e3);
		}

		Tablet(double axisratio) {
			factor = axisratio * axisratio;
		}

		@Override
		public double value(double[] x) {
			double f = factor * x[0] * x[0];
			double sum = IntStream.range(1, x.length).mapToDouble(i -> x[i] * x[i]).sum();
			f += sum;
			return f;
		}
	}

	private static class CigTab implements MultivariateFunction {
		private double factor;

		CigTab() {
			this(1e4);
		}

		CigTab(double axisratio) {
			factor = axisratio;
		}

		@Override
		public double value(double[] x) {
			int end = x.length - 1;
			double f = x[0] * x[0] / factor + factor * x[end] * x[end];
			double sum = IntStream.range(1, end).mapToDouble(i -> x[i] * x[i]).sum();
			f += sum;
			return f;
		}
	}

	private static class TwoAxes implements MultivariateFunction {

		private double factor;

		TwoAxes() {
			this(1e6);
		}

		TwoAxes(double axisratio) {
			factor = axisratio * axisratio;
		}

		@Override
		public double value(double[] x) {
			double f = IntStream.range(0, x.length).mapToDouble(i -> (i < x.length / 2 ? factor : 1) * x[i] * x[i]).sum();
			return f;
		}
	}

	private static class ElliRotated implements MultivariateFunction {
		private Basis B = new Basis();
		private double factor;

		ElliRotated() {
			this(1e3);
		}

		ElliRotated(double axisratio) {
			factor = axisratio * axisratio;
		}

		@Override
		public double value(double[] x) {
            x = B.Rotate(x);
            double f = 0;
            for (int i = 0; i < x.length; ++i) {
				f += Math.pow(factor, i / (x.length - 1.)) * x[i] * x[i];
			}
			return f;
		}
	}

	private static class Elli implements MultivariateFunction {

		private double factor;

		Elli() {
			this(1e3);
		}

		Elli(double axisratio) {
			factor = axisratio * axisratio;
		}

		@Override
		public double value(double[] x) {
			double f = IntStream.range(0, x.length).mapToDouble(i -> Math.pow(factor, i / (x.length - 1.)) * x[i] * x[i]).sum();
			return f;
		}
	}

	private static class MinusElli implements MultivariateFunction {

		@Override
		public double value(double[] x) {
			return 1.0 - (new Elli().value(x));
		}
	}

	private static class DiffPow implements MultivariateFunction {

		@Override
		public double value(double[] x) {
			double f = IntStream.range(0, x.length).mapToDouble(i -> Math.pow(Math.abs(x[i]), 2. + 10 * (double) i
					/ (x.length - 1.))).sum();
			return f;
		}
	}

	private static class SsDiffPow implements MultivariateFunction {

		@Override
		public double value(double[] x) {
			double f = Math.pow(new DiffPow().value(x), 0.25);
			return f;
		}
	}

	private static class Rosen implements MultivariateFunction {

		@Override
		public double value(double[] x) {
            int bound = x.length - 1;
			double f = 0.0;
			for (int i = 0; i < bound; i++) {
				double xi = x[i];
				double a = xi * xi - x[i + 1];
				double b = xi - 1.;
				f += 1e2 * a * a + b * b;
			}
			return f;
		}
	}

	private static class Ackley implements MultivariateFunction {
		private double axisratio;

		Ackley(double axra) {
			axisratio = axra;
		}

		Ackley() {
			this(1);
		}

		@Override
		public double value(double[] x) {
			double f = 0;
			double res2 = 0;
			double fac = 0;
			int n = x.length;
			for (int i = 0; i < n; ++i) {
				fac = Math.pow(axisratio, (i - 1.) / (n - 1.));
				double xi = x[i];
				f += fac * fac * xi * xi;
				res2 += Math.cos(2. * Math.PI * fac * xi);
			}
			f = (20. - 20. * Math.exp(-0.2 * Math.sqrt(f / n))
				+ Math.exp(1.) - Math.exp(res2 / n));
			return f;
		}
	}

	private static class Rastrigin implements MultivariateFunction {

		private double axisratio;
		private double amplitude;

		Rastrigin() {
			this(1, 10);
		}

		Rastrigin(double axisratio, double amplitude) {
			this.axisratio = axisratio;
			this.amplitude = amplitude;
		}

		@Override
		public double value(double[] x) {
			double f = 0;
			int n = x.length;
			for (int i = 0; i < n; ++i) {
                double fac = Math.pow(axisratio, (i - 1.) / (n - 1.));
				double xi = x[i];
				if (i == 0 && xi < 0) {
					fac *= 1.;
				}
				f += fac * fac * xi * xi + amplitude * (1. - Math.cos(2. * Math.PI * fac * xi));
			}
			return f;
		}
	}

	private static class Basis {
		double[][] basis;
		Random rand = new Random(2);

		double[] Rotate(double[] x) {
			GenBasis(x.length);
			double[] y = new double[x.length];
			for (int i = 0; i < x.length; ++i) {
				y[i] = 0;
				for (int j = 0; j < x.length; ++j) {
					y[i] += basis[i][j] * x[j];
				}
			}
			return y;
		}

		void GenBasis(int DIM) {
			if (basis != null && basis.length == DIM) {
				return;
			}

            /* generate orthogonal basis */
			basis = new double[DIM][DIM];
			for (int i = 0; i < DIM; ++i) {
				/* sample components gaussian */
                int j;
                for (j = 0; j < DIM; ++j) {
					basis[i][j] = rand.nextGaussian();
				}
				/* substract projection of previous vectors */
                int k;
                double sp;
                for (j = i - 1; j >= 0; --j) {
					for (sp = 0., k = 0; k < DIM; ++k) {
						sp += basis[i][k] * basis[j][k]; /* scalar product */
					}
					for (k = 0; k < DIM; ++k) {
						basis[i][k] -= sp * basis[j][k]; /* substract */
					}
				}
				/* normalize */
				for (sp = 0., k = 0; k < DIM; ++k) {
					sp += basis[i][k] * basis[i][k]; /* squared norm */
				}
				for (k = 0; k < DIM; ++k) {
					basis[i][k] /= Math.sqrt(sp);
				}
			}
		}
	}
}