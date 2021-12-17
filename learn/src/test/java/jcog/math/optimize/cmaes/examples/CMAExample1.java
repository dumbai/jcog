package jcog.math.optimize.cmaes.examples;

import jcog.math.optimize.cmaes.CMAEvolutionStrategy;
import jcog.math.optimize.cmaes.IObjectiveFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The very well-known Rosenbrock objective function to be minimized.
 */
class Rosenbrock implements IObjectiveFunction { // meaning implements methods valueOf and isFeasible
    public double applyAsDouble(double[] x) {
        double res = 0;
        for (int i = 0; i < x.length - 1; ++i)
            res += 100 * (x[i] * x[i] - x[i + 1]) * (x[i] * x[i] - x[i + 1]) +
                    (x[i] - 1.0) * (x[i] - 1.0);
        return res;
    }

}

/**
 * A very short example program how to use the class CMAEvolutionStrategy.  The code is given below, see also the code snippet in the documentation of class {@link CMAEvolutionStrategy}.
 * For implementation of restarts see {@link CMAExample2}.
 * <pre>
 * public class CMAExample1 {
 * public static void main(String[] args) {
 * IObjectiveFunction fitfun = new Rosenbrock();
 *
 * // new a CMA-ES and set some initial values
 * CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
 * cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
 * cma.setDimension(22); // overwrite some loaded properties
 * cma.setInitialX(0.5); // in each dimension, also setTypicalX can be used
 * cma.setInitialStandardDeviation(0.2); // also a mandatory setting
 * cma.options.stopFitness = 1e-9;       // optional setting
 *
 * // initialize cma and get fitness array to fill in later
 * double[] fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];
 *
 * // initial output to files
 * cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files
 *
 * // iteration loop
 * while(cma.stopConditions.getNumber() == 0) {
 *
 * // core iteration step
 * double[][] pop = cma.samplePopulation(); // get a new population of solutions
 * for(int i = 0; i < pop.length; ++i) {    // for each candidate solution i
 * while (!fitfun.isFeasible(pop[i]))   //    test whether solution is feasible,
 * pop[i] = cma.resampleSingle(i);  //       re-sample solution until it is feasible
 * fitness[i] = fitfun.valueOf(pop[i]); //    compute fitness value, where fitfun
 * }	                                     //    is the function to be minimized
 * cma.updateDistribution(fitness);         // pass fitness array to update search distribution
 *
 * // output to console and files
 * cma.writeToDefaultFiles();
 * int outmod = 150;
 * if (cma.getCountIter() % (15*outmod) == 1)
 * cma.printlnAnnotation(); // might write file as well
 * if (cma.getCountIter() % outmod == 1)
 * cma.println();
 * }
 * // evaluate mean value as it is the best estimator for the optimum
 * cma.setFitnessOfMeanX(fitfun.valueOf(cma.getMeanX())); // updates the best ever solution
 *
 * // final output
 * cma.writeToDefaultFiles(1);
 * cma.println();
 * cma.println("Terminated due to");
 * for (String s : cma.stopConditions.getMessages())
 * cma.println("  " + s);
 * cma.println("best function value " + cma.getBestFunctionValue()
 * + " at evaluation " + cma.getBestEvaluationNumber());
 *
 * // we might return cma.getBestSolution() or cma.getBestX()
 *
 * } // main
 * } // class
 * </pre>
 *
 * @author Nikolaus Hansen, released into public domain.
 * @see CMAEvolutionStrategy
 */
public class CMAExample1 {
    static int outmod = 150;

    @Test
    void test1() {
        IObjectiveFunction f = new Rosenbrock();

        // new a CMA-ES and set some initial values
        CMAEvolutionStrategy c = new CMAEvolutionStrategy(10);
//		cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
        c.setInitialX(0.05); // in each dimension, also setTypicalX can be used
        c.setInitialStandardDeviation(0.2); // also a mandatory setting
        c.options.stopFitness = 1.0e-14;       // optional setting

        // initialize cma and get fitness array to fill in later
        double[] fitness = c.init();  // new double[cma.parameters.getPopulationSize()];

        // initial output to files
//		cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files

        // iteration loop
        while (c.hasNext()) {

            double[][] pop = c.sample(); // get a new population of solutions
            for (int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                // a simple way to handle constraints that define a convex feasible domain
                // (like box constraints, i.e. variable boundaries) via "blind re-sampling"
                // assumes that the feasible domain is convex, the optimum is
                while (!f.test(pop[i]))     //   not located on (or very close to) the domain boundary,
                    pop[i] = c.resampleSingle(i);    //   initialX is feasible and initialStandardDeviations are
                //   sufficiently small to prevent quasi-infinite looping here
                // compute fitness/objective value	
                fitness[i] = f.applyAsDouble(pop[i]); // fitfun.valueOf() is to be minimized
            }
            c.updateDistribution(fitness);         // pass fitness array to update search distribution

            //TODO
            //c.next();

            // output to files and console
//			cma.writeToDefaultFiles();
            if (c.iteration() % (15 * outmod) == 1)
                c.printlnAnnotation(); // might write file as well
            if (c.iteration() % outmod == 1)
                c.println();
        }
        // evaluate mean value as it is the best estimator for the optimum
        c.setFitnessOfMeanX(f.applyAsDouble(c.getMeanX())); // updates the best ever solution

        // final output
//		cma.writeToDefaultFiles(1);
        c.println();
        c.println("Terminated due to");
        for (String s : c.stop.getMessages())
            c.println("  " + s);
        c.println("best function value " + c.getBestFunctionValue()
                + " at evaluation " + c.getBestEvaluationNumber());

        assertTrue(c.iteration() < 8000);
        // we might return cma.getBestSolution() or cma.getBestX()

    } // main
} // class