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
package jcog.grammar.evolve.objective;

import jcog.grammar.evolve.evaluators.TreeEvaluationException;
import jcog.grammar.evolve.evaluators.TreeEvaluator;
import jcog.grammar.evolve.inputs.Context;
import jcog.grammar.evolve.inputs.DataSet;
import jcog.grammar.evolve.inputs.DataSet.Bounds;
import jcog.grammar.evolve.inputs.DataSet.Example;
import jcog.grammar.evolve.tree.Node;
import jcog.grammar.evolve.utils.BasicStats;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Flagging objective, accuracy, precision, length
 *
 * @author MaleLabTs
 */
public class FlaggingAccuracyPrecisionLengthObjective implements Objective {

    private Context context;
    
    @Override
    public void setup(Context context) {
        this.context = context;


    }

    @Override
    public double[] fitness(Node individual) {
         
        DataSet dataSetView = this.context.getCurrentDataSet();
        TreeEvaluator evaluator = context.getConfiguration().getEvaluator();
        double[] fitness = new double[3];

        double fitnessLenght;

        List<Bounds[]> evaluate;
        try {
            evaluate = evaluator.evaluate(individual, context);
            StringBuilder builder = new StringBuilder();
            individual.describe(builder);
            fitnessLenght = builder.length();
        } catch (TreeEvaluationException ex) {
            Logger.getLogger(FlaggingAccuracyPrecisionLengthObjective.class.getName()).log(Level.SEVERE, null, ex);
            Arrays.fill(fitness, Double.POSITIVE_INFINITY);
            return fitness;
        }

        

       BasicStats statsOverall = new BasicStats();

        for (int i = 0; i < evaluate.size(); i++) {
            Bounds[] result = evaluate.get(i);

            Example example = dataSetView.getExample(i);
            
            if (isUnannotated(example)){
                continue;
            }
            
            
            BasicStats stats = new BasicStats();

            stats.tp = isTruePositive(result, example.match) ? 1 : 0;
            stats.fp = isFalsePositive(result, example.unmatch) ? 1 : 0;
            stats.fn = isFalseNegative(result, example.match) ? 1 : 0;
            stats.tn = isTrueNegative(result, example.unmatch) ? 1 : 0;
            
            statsOverall.add(stats);
        }

        fitness[0] = 1 - statsOverall.accuracy();
        fitness[1] = 1 - statsOverall.precision();
        fitness[2] = fitnessLenght;

        return fitness;
    }

    public static boolean isUnannotated(Example ex){
        List<Bounds> mm = ex.match;
        return mm.isEmpty() && ex.unmatch.isEmpty();
    }
     
    public static boolean isTruePositive(Bounds[] individualMatches, List<Bounds> expectedMatches){
        return individualMatches.length > 0 && !expectedMatches.isEmpty();
    }

    public static boolean isFalsePositive(Bounds[] individualMatches, List<Bounds> expectedUnmatches){
        return individualMatches.length > 0 && !expectedUnmatches.isEmpty();
    }
    
    public static boolean isFalseNegative(Bounds[] individualMatches, List<Bounds> expectedMatches){
        return individualMatches.length == 0 && !expectedMatches.isEmpty();
    }
    
    public static boolean isTrueNegative(Bounds[] individualMatches, List<Bounds> expectedUnmatches){
        return individualMatches.length == 0 && !expectedUnmatches.isEmpty();
    }
    
    @Override
    public TreeEvaluator getTreeEvaluator() {
        return context.getConfiguration().getEvaluator();
    }

    @Override
    public Objective cloneObjective() {
        return new FlaggingAccuracyPrecisionLengthObjective();
    }
}
