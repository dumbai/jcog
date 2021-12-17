package jcog.math.optimize.cmaes.examples;
import jcog.math.optimize.cmaes.CMAEvolutionStrategy;
import jcog.math.optimize.cmaes.CMAOptions;
import jcog.math.optimize.cmaes.CMASolution;
import jcog.math.optimize.cmaes.IObjectiveFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**  
 * Example of how to use the class CMAEvolutionStrategy including restarts with increasing 
 * population size (IPOP). Copy and modify the code to your convenience. Final termination criteria
 * are stopFitness and stopMaxFunEvals (see class {@link CMAOptions}). The remaining
 * termination criteria invoke a restart with increased population size (see incPopSizeFactor in file
 * CMAEvolutionStrategy.properties). 
 * 
 * @see CMAEvolutionStrategy
 * 
 * @author Nikolaus Hansen, released into public domain. 
 */
public class CMAExample2 {

    public static void main(String[] args) {
        int irun, nbRuns=1;  // restarts, re-read from properties file below
        double [] fitness; 
        CMASolution bestSolution = null; // initialization to allow compilation
        long counteval = 0;              // variables used for restart
        int lambda = 0;
        
        for (irun = 0; irun < nbRuns; ++irun) { // might also terminate before
        	
        	CMAEvolutionStrategy cma = new CMAEvolutionStrategy();

        	// read properties file and obtain some values for "private" use
//        	cma.readProperties(); // reads from file CMAEvolutionStrategy.properties
            cma.setDimension(22);
        	cma.setInitialX(-20, 80);
        	cma.setInitialStandardDeviation(0.3 * 100);

        	// set up fitness function
        	double nbFunc = CMAOptions.getFirstToken(cma.getProperties().getProperty("functionNumber"), 10);
        	int rotate = CMAOptions.getFirstToken(cma.getProperties().getProperty("functionRotate"), 0);
        	double axisratio = CMAOptions.getFirstToken(cma.getProperties().getProperty("functionAxisRatio"), 0.0);
            IObjectiveFunction fitfun = new FunctionCollector(nbFunc, rotate, axisratio);

            // set up restarts
            nbRuns = 1+ CMAOptions.getFirstToken(cma.getProperties().getProperty("numberOfRestarts"), 1);
            double incPopSizeFactor = CMAOptions.getFirstToken(cma.getProperties().getProperty("incPopSizeFactor"), 1.0);
             
            // initialize 
            if (irun == 0) {
            	fitness = cma.init(); // finalize setting of population size lambda, get fitness array
        		lambda = cma.param.getPopulationSize(); // retain lambda for restart
//        		cma.writeToDefaultFilesHeaders(0); // overwrite output files
        	}
        	else {
                cma.param.setPopulationSize((int)Math.ceil(lambda * Math.pow(incPopSizeFactor, irun)));
                cma.setCountEval(counteval); // somehow a hack 
                fitness = cma.init(); // provides array to assign fitness values
            }
            
            // set additional termination criterion
            if (nbRuns > 1) 
                cma.options.stopMaxIter = (long) (100 + 200*Math.pow(cma.getDimension(),2)*Math.sqrt(cma.param.getLambda()));

            // iteration loop
            double lastTime = 0, alastTime = 0; // for smarter console output
            while(cma.hasNext()) {

                // --- core iteration step ---
                double[][] pop = cma.sample(); // get a new population of solutions
                for(int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                	// a simple way to handle constraints that define a convex feasible domain  
                	// (like box constraints, i.e. variable boundaries) via "blind re-sampling" 
                	                                       // assumes that the feasible domain is convex, the optimum is  
    				while (!fitfun.test(pop[i]))     //   not located on (or very close to) the domain boundary,
                        pop[i] = cma.resampleSingle(i);    //   initialX is feasible and initialStandardDeviations are  
                                                           //   sufficiently small to prevent quasi-infinite looping here
                    // compute fitness/objective value
                	fitness[i] = fitfun.applyAsDouble(pop[i]); // fitfun.valueOf() is to be minimized
                }
                cma.updateDistribution(fitness);         // pass fitness array to update search distribution
                // --- end core iteration step ---

                // stopping conditions can be changed in file CMAEvolutionStrategy.properties 
//                cma.readProperties();

                // the remainder is output
//                cma.writeToDefaultFiles();

                // screen output
                boolean printsomething = true; // for a convenient switch to false
                if (printsomething && System.currentTimeMillis() - alastTime > 20.0e3) {
                    cma.printlnAnnotation();
                    alastTime = System.currentTimeMillis();
                }
                if (printsomething && (cma.stop.isTrue() || cma.iteration() < 4
                        || (cma.iteration() > 0 && (Math.log10(cma.iteration()) % 1) < 1.0e-11)
                        || System.currentTimeMillis() - lastTime > 2.5e3)) { // wait 2.5 seconds
                    cma.println();
                    lastTime = System.currentTimeMillis();
                }
            } // iteration loop

    		// evaluate mean value as it is the best estimator for the optimum
    		cma.setFitnessOfMeanX(fitfun.applyAsDouble(cma.getMeanX())); // updates the best ever solution

    		// retain best solution ever found 
    		if (irun == 0)
    			bestSolution = cma.getBestSolution();
    		else if (cma.getBestSolution().getFitness() < bestSolution.getFitness())
    			bestSolution = cma.getBestSolution();

            // final output for the run
//            cma.writeToDefaultFiles(1); // 1 == make sure to write final result
            cma.println("Terminated (run " + (irun+1) + ") due to");
            for (String s : cma.stop.getMessages())
                cma.println("      " + s);
    		cma.println("    best function value " + cma.getBestFunctionValue() 
    				+ " at evaluation " + cma.getBestEvaluationNumber());

            // quit restart loop if MaxFunEvals or target Fitness are reached
            boolean quit = false;
            for (String s : cma.stop.getMessages())
                if (s.startsWith("MaxFunEvals") ||
                    s.startsWith("Fitness")) 
                    quit = true;
            if (quit)
                break;
            
            counteval = cma.getCountEval();

            if (irun < nbRuns-1) // after Manual stop give time out to change stopping condition 
            	for (String s : cma.stop.getMessages())
            		if (s.startsWith("Manual")) {
            			System.out.println("incomment 'stop now' and press return to start next run");
            			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            			try { in.readLine(); }
            			catch(IOException e) { System.out.println("input not readable"); }
            		}

        } // for irun < nbRuns

        // screen output
        if (irun > 1) {
            System.out.println(" " + (irun) + " runs conducted," 
                    + " best function value " + bestSolution.getFitness() 
                    + " at evaluation " + bestSolution.getEvaluationNumber());
        }

    } // main
} // class