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
import jcog.grammar.evolve.objective.Objective;
import jcog.grammar.evolve.objective.Ranking;
import jcog.grammar.evolve.objective.performance.PerformacesObjective;
import jcog.grammar.evolve.tree.Node;
import jcog.grammar.evolve.tree.operator.Or;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.*;

/**
 * Optional accepted parameters: "terminationCriteria", Boolean, then True the
 * termination criteria is always enabled
 * "terminationCriteriaGenerations", Integer, number of generations for the
 * termination criteria.Default value: 200
 * "convertToUnmatch", boolean, when true extracted matches are converted to unmatches
 * "isFlagging", boolean, when true the evolution is a flagging problem; default is false (text extraction) 
 * when dividing the dataset. When false the extracted matches are converted to
 * unannotated ranges when dividing the dataset.
 * @author MaleLabTs
 */
public class SeparateAndConquerStrategy extends DiversityElitarismStrategy{

    public static final Comparator<Ranking> comparator = (o1, o2) -> {
        double[] fitness1 = o1.getFitness();
        double[] fitness2 = o2.getFitness();
        int compare = 0;
        for (int i = 0; i < fitness1.length; i++) {
            compare = Double.compare(fitness1[i], fitness2[i]);
            if (compare != 0) {
                return compare;
            }
        }
        return -o1.getDescription().compareTo(o2.getDescription());
    };
    private boolean convertToUnmatch = true;
    private boolean isFlagging = false;
    private double dividePrecisionThreashold =1.0;
    
    @Override
    protected void readParameters(Configuration configuration) {
        super.readParameters(configuration); 
        Map<String, String> parameters = configuration.getStrategyParameters();
        if (parameters != null) {
            if (parameters.containsKey("convertToUnmatch")) {
                convertToUnmatch = Boolean.parseBoolean(parameters.get("convertToUnmatch"));
            }
            if (parameters.containsKey("isFlagging")) {
                isFlagging = Boolean.parseBoolean(parameters.get("isFlagging"));
            }
            if (parameters.containsKey("dividePrecisionThreashold")) {
                dividePrecisionThreashold = Double.parseDouble(parameters.get("dividePrecisionThreashold"));
            }

        }
    }


    private static void initialize() {
        throw new RuntimeException("share the impl from DiversityElitismStrategy");
















    }

    @Override
    public Void call() throws TreeEvaluationException {
        try {
            listener.evolutionStarted(this);
            initialize();

            context.setSeparateAndConquerEnabled(true);

            int terminationCriteriaGenerationsCounter = 0;
            String oldGenerationBestValue = null;
            int generation;
            Set<Node> bests = new UnifiedSet<>();
            for (generation = 0; generation < param.getGenerations(); generation++) {
                context.setStripedPhase(context.getDataSetContainer().isDataSetStriped() && ((generation % context.getDataSetContainer().getProposedNormalDatasetInterval()) != 0));

                evolve();
                Ranking best = rankings.first();

                
                List<Node> tmpBests = new LinkedList<>(bests);
                 
                
                tmpBests.add(best.getNode());
                
                Node joinedBest = joinSolutions(tmpBests);
                context.setSeparateAndConquerEnabled(false);
                double[] fitnessOfJoined = objective.fitness(joinedBest);
                context.setSeparateAndConquerEnabled(true);
                
                
                if (listener != null) {
                    
                    
                    
                    listener.logGeneration(this, generation + 1, joinedBest, fitnessOfJoined, this.rankings);
                }
                boolean allPerfect = Arrays.stream(this.rankings.first().getFitness()).noneMatch(fitness -> Math.round(fitness * 10000) != 0);
                if (allPerfect) {
                    break;
                }

                Objective trainingObjective = new PerformacesObjective();
                trainingObjective.setup(context);
                double[] trainingPerformace = trainingObjective.fitness(best.getNode());
                Map<String, Double> performancesMap = new HashMap<>();
                PerformacesObjective.populatePerformancesMap(trainingPerformace, performancesMap, isFlagging);

                double pr = isFlagging ? performancesMap.get("flag precision") : performancesMap.get("match precision");
                
                String newBestValue = best.getDescription();
                if (newBestValue.equals(oldGenerationBestValue)) {
                    terminationCriteriaGenerationsCounter++;
                } else {
                    terminationCriteriaGenerationsCounter = 0;
                }
                oldGenerationBestValue = newBestValue;

                if (terminationCriteriaGenerationsCounter >= terminationCriteriaGenerations && pr >= dividePrecisionThreashold && generation < (param.getGenerations() - 1)) {
                    terminationCriteriaGenerationsCounter = 0;
                    bests.add(best.getNode());
                    
                    StringBuilder builder = new StringBuilder();
                    best.getNode().describe(builder);
                    context.getTrainingDataset().addSeparateAndConquerLevel(builder.toString(), (int) context.getSeed(), convertToUnmatch, isFlagging);

                    
                    if (context.getCurrentDataSet().getNumberMatches() == 0) {
                        context.getTrainingDataset().removeSeparateAndConquerLevel((int) context.getSeed());
                        break;
                    }
                    
                    initialize();
                    
                }

                if (Thread.interrupted()) {
                    break;
                }

            }

            Ranking best = rankings.first();
            bests.add(best.getNode());

             
             
            
            if (listener != null) {
                List<Node> dividedPopulation = new ArrayList<>(population.size());
                List<Node> tmpBests = new LinkedList<>(bests);
                for (Ranking r : rankings) {
                    tmpBests.set(tmpBests.size() - 1, r.getNode());
                    dividedPopulation.add(joinSolutions(tmpBests));
                }


                context.setSeparateAndConquerEnabled(false);
                TreeSet<Ranking> tmp = new TreeSet();
                sortRankings(dividedPopulation, objective, tmp);


                listener.evolutionComplete(this, generation - 1, tmp);
            }
            return null;
        } catch (RuntimeException x) {
            throw new TreeEvaluationException("Error during evaluation of a tree", x, this);
        }
    }

    /**
     * Overrides base sortByFirst and implements a lexicographic order, for fitnesses.
     * @param front
     */
    @Override
    protected void sortByFirst(List<Ranking> front) {
        front.sort(comparator);
    }

    private static Node joinSolutions(List<Node> bests) {
        Deque<Node> nodes = new LinkedList<>(bests);
        Deque<Node> tmp = new LinkedList<>();
        while (nodes.size() > 1) {

            while (!nodes.isEmpty()) {
                Node first = nodes.pollFirst();
                Node second = nodes.pollFirst();

                if (second != null) {
                    tmp.addLast(new Or(first, second));
                } else {
                    tmp.addLast(first);
                }
            }

            nodes = tmp;
            tmp = new LinkedList<>();

        }
        return nodes.getFirst();
    }
}
