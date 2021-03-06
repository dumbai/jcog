/*
 * Copyright (C) 2015 Machine Learning Lab - University of Trieste, 
 * Italy (http:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:
 */
package jcog.grammar.evolve.strategy.impl;

import jcog.grammar.evolve.configuration.Configuration;
import jcog.grammar.evolve.evaluators.TreeEvaluationException;
import jcog.grammar.evolve.strategy.ExecutionListener;
import jcog.grammar.evolve.strategy.ExecutionListenerFactory;
import jcog.grammar.evolve.strategy.RunStrategy;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage the thread pool and instantiates two strategies, one per Job.
 * Executes two Strategy, one in the first half of jobs and another in the last part.
 * The runStrategy2 property contains the name of the alternative strategy.
 * Please note that strategies properties should not "collide", they need different properties or the same values in the properties.
 * Multiple configurations are not supported at the moment
 * @author MaleLabTs
 */
public class CombinedMultithreadStrategy extends AbstractExecutionStrategy {

    public static final String RUN_ALT_STRATEGY_KEY = "runStrategy2";
    public static final String RUN_ALT_FITNESS_KEY = "objective2";
    public static final String RUN_ALT_TERMINATION_CRITERIA_KEY = "terminationCriteria2";
    
    
    public static final String THREADS_KEY = "threads";
    private static final Logger LOG = Logger.getLogger(CombinedMultithreadStrategy.class.getName());
    ExecutorService executor;
    private volatile Thread workingThread;
    private volatile boolean terminated = false;

    private static int countThreads(Map<String, String> parameters) {
        String paramValue = parameters.get(THREADS_KEY);
        int threads;
        try {
            threads = Integer.parseInt(paramValue);
        } catch (NumberFormatException x) {
            threads = Runtime.getRuntime().availableProcessors();
            LOG.log(Level.WARNING, "Falling back to default threads count: {0}", threads);
        }
        return threads;
    }

    @Override
    public void execute(Configuration configuration, ExecutionListenerFactory listenerFactory) throws Exception {
        workingThread = Thread.currentThread();
        listenerFactory.register(this);
        Map<String, String> parameters = configuration.getStrategyParameters();
        int threads = countThreads(parameters);
        Class<? extends RunStrategy> strategyClass = getStrategy(parameters);
        Class<? extends RunStrategy> altStrategyClass = strategyClass;

        executor = Executors.newFixedThreadPool(Math.max(1, threads));
        ExecutorCompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        long initialSeed = configuration.getInitialSeed();
        int jobs = configuration.getJobs();
        
        int changejobs = jobs+1;
        
        if(parameters.containsKey(RUN_ALT_STRATEGY_KEY)){
            altStrategyClass = CombinedMultithreadStrategy.getAlternativeStrategy(parameters);
            changejobs = jobs / 2;
        }

        String altFitnessClassName = null;
        if(parameters.containsKey(RUN_ALT_FITNESS_KEY)){
            altFitnessClassName = CombinedMultithreadStrategy.getAlternativeFitness(parameters);
        }
        
        for (int i = 0; i < jobs; i++) {
            RunStrategy job;
            Configuration jobConf = new Configuration(configuration);
            if(i<changejobs){
                 job = strategyClass.getConstructor().newInstance();
            } else {
                
                 CombinedMultithreadStrategy.activeAlternativeParameter(RUN_ALT_TERMINATION_CRITERIA_KEY, jobConf.getStrategyParameters());
                 if(altFitnessClassName != null){
                     jobConf.updateObjective(altFitnessClassName);
                 }
                 job = altStrategyClass.getConstructor().newInstance();
            }
            
            jobConf.setJobId(i);
            jobConf.setInitialSeed(initialSeed + i);
            job.setup(jobConf, listenerFactory.getNewListener());
            completionService.submit(job);
        }
        executor.shutdown();
        
        ExecutionListener listener = listenerFactory.getNewListener();               
        for (int i = 0; i < jobs; i++) {
            Future<Void> result = null;
            try {
                if(terminated) {
                    if (listener != null) {
                        listener.evolutionStopped();
                    }
                    return;
                }
                result = completionService.take();
            } catch (InterruptedException ex) {
                
                if (listener != null) {
                        listener.evolutionStopped();
                }
                return;
            }
            try {
                result.get();
            } catch (ExecutionException x) {
                if (x.getCause() instanceof TreeEvaluationException ex) {
                    RunStrategy strategy = ex.getAssociatedStrategy();
                    LOG.log(Level.SEVERE, "Job " + strategy.getConfiguration().getJobId() + " failed with exception", ex.getCause());
                    if (listener != null) {
                        listener.evolutionFailed(strategy, ex);
                    }
                }
            }
        }
    }

    @Override
    public void shutdown() {
        executor.shutdownNow();
        if(workingThread!=null){
            terminated = true;
            workingThread.interrupt();
        }
    }
    
    protected static Class<? extends RunStrategy> getAlternativeStrategy(Map<String, String> parameters) {
        String paramValue = parameters.get(RUN_ALT_STRATEGY_KEY);
        Class<? extends RunStrategy> strategyClass;
        try{
            strategyClass = Class.forName(paramValue).asSubclass(RunStrategy.class);
        }catch(Exception x){
            LOG.warning("Falling back to default RunStrategy");
            strategyClass = DefaultStrategy.class;
        }
        return strategyClass;
    }

    
    private static String getAlternativeFitness(Map<String, String> parameters) {
        String paramValue = parameters.get(RUN_ALT_FITNESS_KEY);
        return paramValue;
    }
    
    private static void activeAlternativeParameter(String parameterAlternativeName, Map<String, String> parametersMap) {
        if(!parametersMap.containsKey(parameterAlternativeName)){
            LOG.warning("Invalid parameterAlternativeName provided to activeAlternaveParameter method");
            return;
        }
        String alternativeValue = parametersMap.get(parameterAlternativeName);
        String parameterOriginalName = parameterAlternativeName.substring(0,parameterAlternativeName.length()-1);
        parametersMap.put(parameterOriginalName, alternativeValue);
    }
}