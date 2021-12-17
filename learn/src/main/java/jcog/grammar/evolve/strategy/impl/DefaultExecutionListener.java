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

import jcog.grammar.evolve.evaluators.TreeEvaluationException;
import jcog.grammar.evolve.objective.Ranking;
import jcog.grammar.evolve.strategy.ExecutionListener;
import jcog.grammar.evolve.strategy.ExecutionListenerFactory;
import jcog.grammar.evolve.strategy.ExecutionStrategy;
import jcog.grammar.evolve.strategy.RunStrategy;
import jcog.grammar.evolve.tree.Node;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This execution listener is only a STUB, an example.
 * @author MaleLabTs
 */
public class DefaultExecutionListener implements ExecutionListener,ExecutionListenerFactory {

    private static final Logger LOG = Logger.getLogger(DefaultExecutionListener.class.getName());



    private static double getRatio(long cacheHit, long cacheMiss) {
        return cacheHit + cacheMiss == 0 ? 0 : cacheHit / ((double) cacheHit + cacheMiss);
    }

    @Override
    public void evolutionComplete(RunStrategy strategy, int generation, Collection<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();








    }

    @Override
    public void evolutionStarted(RunStrategy strategy) {
        int jobId = strategy.getConfiguration().getJobId();        
    }

    @Override
    public void logGeneration(RunStrategy strategy, int generation, Node best, double[] fitness, Collection<Ranking> population) {

    }



    @Override
    public ExecutionListener getNewListener() {
        return this;
    }

    @Override
    public void evolutionFailed(RunStrategy strategy, TreeEvaluationException cause) {
        int jobId = strategy.getConfiguration().getJobId();
        try {

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void register(ExecutionStrategy strategy) {
        
    }
    
    @Override
    public void evolutionStopped() {
        
    }
}
