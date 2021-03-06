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
import jcog.grammar.evolve.strategy.ExecutionListenerFactory;
import jcog.grammar.evolve.strategy.RunStrategy;

import java.util.Map;

/**
 *
 * @author MaleLabTs
 */
public class SinglethreadStrategy extends AbstractExecutionStrategy {

    private Thread workThread;
    private boolean done = false;

    @Override
    public void execute(Configuration configuration, ExecutionListenerFactory listenerFactory) throws Exception {
        listenerFactory.register(this);
        workThread = Thread.currentThread();
        Map<String, String> parameters = configuration.getStrategyParameters();
        Class<? extends RunStrategy> strategyClass = getStrategy(parameters);
        long initialSeed = configuration.getInitialSeed();
        for (int i = 0; i < configuration.getJobs() && !done; i++) {
            RunStrategy job = strategyClass.getConstructor().newInstance();
            Configuration jobConf = new Configuration(configuration);
            jobConf.setJobId(i);
            jobConf.setInitialSeed(initialSeed + i);
            job.setup(jobConf, listenerFactory.getNewListener());
            try {
                job.call();
            } catch (TreeEvaluationException ev) {
                job.getExecutionListener().evolutionFailed(job, ev);
            }
        }
    }

    @Override
    public void shutdown() {
        done=true;
        workThread.interrupt();
    }
}
