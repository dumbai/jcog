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
 * Manage the thread pool and instantiates one strategy per Job.
 * @author MaleLabTs
 */
public class MultithreadStrategy extends AbstractExecutionStrategy {

    public static final String THREADS_KEY = "threads";
    private static final Logger LOG = Logger.getLogger(MultithreadStrategy.class.getName());
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
        executor = Executors.newFixedThreadPool(threads);
        ExecutorCompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        long initialSeed = configuration.getInitialSeed();
        int jobs = configuration.getJobs();
        for (int i = 0; i < jobs; i++) {
            RunStrategy job = strategyClass.getConstructor().newInstance();
            Configuration jobConf = new Configuration(configuration);
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
}