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
import jcog.grammar.evolve.evaluators.CachedEvaluator;
import jcog.grammar.evolve.evaluators.TreeEvaluationException;
import jcog.grammar.evolve.inputs.Context;
import jcog.grammar.evolve.objective.Objective;
import jcog.grammar.evolve.objective.Ranking;
import jcog.grammar.evolve.objective.performance.PerformacesObjective;
import jcog.grammar.evolve.objective.performance.PerformancesFactory;
import jcog.grammar.evolve.outputs.FinalSolution;
import jcog.grammar.evolve.outputs.JobEvolutionTrace;
import jcog.grammar.evolve.outputs.Results;
import jcog.grammar.evolve.strategy.ExecutionListener;
import jcog.grammar.evolve.strategy.ExecutionListenerFactory;
import jcog.grammar.evolve.strategy.ExecutionStrategy;
import jcog.grammar.evolve.strategy.RunStrategy;
import jcog.grammar.evolve.tree.Node;
import jcog.grammar.evolve.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A textual interface that works only on Unix systems. Uses the ANSI escape
 * sequence 0x1B+"[2J" to clear the screen. Handy for experiments that take a
 * long time.
 *
 * @author MaleLabTs
 */
public class CoolTextualExecutionListener implements ExecutionListener, ExecutionListenerFactory {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final Logger LOG = Logger.getLogger(DefaultExecutionListener.class.getName());
    private final Map<Integer, String> screen = new TreeMap<>();
    private final Map<Integer, Long> jobStartTimes = new ConcurrentHashMap<>();
    private final NavigableSet<Integer> remove = new TreeSet<>();
    private final String header;
    private int jobDone = 0;
    private int jobTotal = 0;
    private int overallDone = 0;
    private int overallTotal = 0;
    private final long startTime = System.currentTimeMillis();
    private String eta;
    private FinalSolution best;
    private final Results results;
    private boolean isEvaluatorCached = false;
    private final boolean isFlagging;

    public CoolTextualExecutionListener(String message, Configuration configuration, Results results) throws IOException {
        File outputFolder = configuration.getOutputFolder();
        if (outputFolder == null)
            outputFolder = Files.createTempDirectory("regexgen").toFile();

        this.header = ((message!=null)? message + "\n" : "") + "Output folder: " + outputFolder.getName();
        this.jobTotal = configuration.getJobs();
        this.overallTotal = configuration.getEvolutionParameters().getGenerations() * jobTotal;
        this.results = results;
        if (configuration.getEvaluator() instanceof CachedEvaluator) {
            this.isEvaluatorCached = true;
        }
        this.isFlagging = configuration.isIsFlagging();
    }

    private synchronized void print() {
        char esc = 27;
        String clear = esc + "[2J";
        System.out.print(clear);

        int doneAll = 20 * overallDone / overallTotal;
        double percAll = Math.round(1000 * overallDone / (double) overallTotal) / 10.0;

        System.out.println(header);
        if (isEvaluatorCached) {
            CachedEvaluator evaluator = (CachedEvaluator) this.results.getConfiguration().getEvaluator();
            System.out.printf("[%s] %.2f%%  | %d/%d | ETA: %s | CR: %.2f\n", progress(doneAll), percAll, jobDone, jobTotal, eta, evaluator.getRatio());
        } else {
            System.out.printf("[%s] %.2f%%  | %d/%d | ETA: %s\n", progress(doneAll), percAll, jobDone, jobTotal, eta);
        }
        for (Map.Entry<Integer, String> entry : screen.entrySet()) {
            String color = "";
            if (remove.contains(entry.getKey())) {
                color = ANSI_GREEN;
            }
            System.out.println(color + entry.getValue() + ANSI_RESET);
        }

        System.out.println("Best: " + ANSI_GREEN + printRegex(best.getSolution()) + ANSI_RESET);

    }

    @Override
    public void evolutionStarted(RunStrategy strategy) {
        int jobId = strategy.getConfiguration().getJobId();

        synchronized (screen) {
            String print = "[                     ] 0% Gen --> 0 job: " + jobId;
            screen.put(jobId, print);
        }

        this.jobStartTimes.put(jobId, System.currentTimeMillis());
    }

    @Override
    public void logGeneration(RunStrategy strategy, int generation, Node best, double[] fitness, Collection<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();
        int done = 20 * generation / strategy.getConfiguration().getEvolutionParameters().getGenerations();
        double perc = Math.round(1000 * generation / (double) strategy.getConfiguration().getEvolutionParameters().getGenerations()) / 10f;

        overallDone++;

        long timeTakenPerGen = (System.currentTimeMillis() - startTime) / overallDone;
        long elapsedMillis = (overallTotal - overallDone) * timeTakenPerGen;

        eta = String.format("%d h, %d m, %d s",
                TimeUnit.MILLISECONDS.toHours(elapsedMillis),
                TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMillis)),
                TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)));


        
        Ranking bestRanking = new Ranking(best, fitness);
        FinalSolution generationBestSolution = new FinalSolution(bestRanking);
         
 
        
        Objective learningObjective = PerformancesFactory.buildObjective(Context.GrammarEvaluationPhase.LEARNING, strategy.getConfiguration());

        Ranking best2 = population.iterator().next();

        double[] learningPerformance = learningObjective.fitness(best2.getNode());

        PerformacesObjective.populatePerformancesMap(learningPerformance, generationBestSolution.getLearningPerformances(), isFlagging);
        
        this.updateBest(generationBestSolution);
        
        

        String print = String.format("[%s] %.2f%% g: %d j: %d f: %s d: %.2f%% ", progress(done), perc, generation, jobId, printArray(fitness), Utils.diversity(population));
        synchronized (screen) {
            screen.put(jobId, print);
            print();
        }
        
        
        results.addCharachterEvaluated(strategy.getContext().getCurrentDataSet().getNumberOfChars() * population.size());
    }

    @Override
    public void evolutionComplete(RunStrategy strategy, int generation, Collection<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();
        long executionTime = System.currentTimeMillis() - this.jobStartTimes.remove(jobId);

        
        int jumpedGenerations = strategy.getConfiguration().getEvolutionParameters().getGenerations() - generation;
        overallDone+=jumpedGenerations;
        
        synchronized (screen) {
            remove.add(jobId);

            if (screen.size() > 10) {
                screen.remove(remove.pollFirst());
            }
        }

        jobDone++;

        if (jobDone >= strategy.getConfiguration().getJobs()) {
            print();
        }
        JobEvolutionTrace jobTrace = this.results.getJobTrace(jobId);
        jobTrace.setExecutionTime(executionTime);
        /*
         Populate Job final population with FinalSolution(s). The final population has the same order as fitness ranking but can contain fitness and performance info
         The performance are propulated here:
         */
        Objective trainingObjective = PerformancesFactory.buildObjective(Context.GrammarEvaluationPhase.TRAINING, strategy.getConfiguration());
        Objective validationObjective = PerformancesFactory.buildObjective(Context.GrammarEvaluationPhase.VALIDATION, strategy.getConfiguration());
        Objective learningObjective = PerformancesFactory.buildObjective(Context.GrammarEvaluationPhase.LEARNING, strategy.getConfiguration());

        int i = 0;
        for (Ranking individual : population) {
            FinalSolution finalSolution = new FinalSolution(individual);
            
            if(i++==0){
                double[] trainingPerformace = trainingObjective.fitness(individual.getNode());
                double[] validationPerformance = validationObjective.fitness(individual.getNode());
                double[] learningPerformance = learningObjective.fitness(individual.getNode());
                PerformacesObjective.populatePerformancesMap(trainingPerformace, finalSolution.getTrainingPerformances(), isFlagging);
                PerformacesObjective.populatePerformancesMap(validationPerformance, finalSolution.getValidationPerformances(), isFlagging);
                PerformacesObjective.populatePerformancesMap(learningPerformance, finalSolution.getLearningPerformances(), isFlagging);
            }
            jobTrace.getFinalGeneration().add(finalSolution);
        }
    }

    @Override
    public void evolutionFailed(RunStrategy strategy, TreeEvaluationException cause) {
        int jobId = strategy.getConfiguration().getJobId();
        try {
            
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static String progress(int done) {
        StringBuilder builder = new StringBuilder();

        builder.append("=".repeat(Math.max(0, done)));

        if (done < 20) {
            builder.append('>');
            builder.append(" ".repeat(Math.max(0, 19 - done)));
        }

        return builder.toString();
    }

    @Override
    public void register(ExecutionStrategy strategy) {
        
    }

    @Override
    public ExecutionListener getNewListener() {
        return this;
    }

    private static String printRegex(String regex) {
        if (regex.length() > 65) {
            return regex.substring(0, 64) + " [..]";
        }
        return regex;
    }

    private static String printArray(double[] fitness) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (double v : fitness) {
            String s = String.valueOf(Math.round(v * 100) / 100f);
            joiner.add(s);
        }
        String sb = joiner.toString();
        return sb;
    }

    
     /**
     * Checks the candidate parameter to be better than previous recorded best.
     * When the candidate is better than the best, candidate becames the new best.
     * The fitness arrays are compared in this way:
     * integer i=0
     * when f(i) > g(i) fitness g is better than fitness f
     * when f(i) < g(i) fitness f is better than fitness g
     * when f(i) = g(i) we have to raise the i; i=i+1 and check again
     * @param candidate
     */
     public synchronized void updateBest(FinalSolution candidate){
        if(this.best == null){
            this.best = candidate;
            return;
        }
        int index = 0;

        double[] ff = this.best.getFitness();

        for(double value : ff){
            double f = candidate.getFitness()[index++];
            if(value > f){
                this.best = candidate;
                return;
            }
            if(value < f){
                return;
            }
            

        }
    }
   
    @Override
    public void evolutionStopped() {
        
    }
}