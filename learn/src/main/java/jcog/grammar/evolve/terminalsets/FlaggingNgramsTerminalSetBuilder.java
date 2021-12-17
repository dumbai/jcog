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
package jcog.grammar.evolve.terminalsets;

import jcog.grammar.evolve.configuration.Configuration;
import jcog.grammar.evolve.inputs.Context;
import jcog.grammar.evolve.inputs.DataSet;
import jcog.grammar.evolve.inputs.DataSet.Example;
import jcog.grammar.evolve.tree.Constant;
import jcog.grammar.evolve.tree.Leaf;
import jcog.grammar.evolve.tree.NodeFactory;
import jcog.grammar.evolve.utils.Utils;
import org.eclipse.collections.impl.set.mutable.primitive.CharHashSet;

import java.util.*;

/**
 * Initialize terminalSet from examples.   
 * @author Fabiano
 */
public class FlaggingNgramsTerminalSetBuilder implements TerminalSetBuilder{
    
    
    private int NUMBER_NGRAMS = 10; 
    private boolean PENALIZE_NEGATIVES_NGRAMS = true;
      
    
    /**
     * This setup is additive, this is not going to reset the NodeFactory configuration; 
     * There is ONE configuration per job, and the configurations have separate node factories.
     * When you need to reset the NodeFactory, you have to call configuration.initNodeFactory()
     * method and you are going to obtain a NodeFactory based only to provided constants, ranges
     * and operators.
     * @param configuration
     */
    @Override
    public void setup(Configuration configuration) {
        this.setup(configuration, configuration.getDatasetContainer().getTrainingDataset());
    }
    
    
    public void setup(Configuration configuration, DataSet dataset) {
        Map<String, String> parameters = configuration.getTerminalSetBuilderParameters();
        if(configuration.getTerminalSetBuilderParameters()!=null){
            if(parameters.containsKey("cumulateScoreMultipliers")){
                NUMBER_NGRAMS = Integer.parseInt(parameters.get("numberNgrams"));
            }
            if(parameters.containsKey("penalizeNegativeNgrams")){
                PENALIZE_NEGATIVES_NGRAMS = Boolean.parseBoolean(parameters.get("penalizeNegativeNgrams"));
            }
        }
        
        
        CharHashSet charset = new CharHashSet();
       
        
        
        NodeFactory nodeFactory = configuration.getNodeFactory(); 
        Set<Leaf> terminalSet = new HashSet<>(nodeFactory.getTerminalSet());

        List<Example> examples = dataset.getExamples();
        List<Example> positiveExamples = new ArrayList<>();
        List<Example> negativeExamples = new ArrayList<>();
        for(Example example : examples){
            if(!example.getMatch().isEmpty()){
                positiveExamples.add(example);
            } else {
                negativeExamples.add(example);
            }
        }
        
        /**
         * Uses postive examples n-grams and gives a score 
         * +1 for each positive example which contains it
         */
        Map<String, Long> ngrams = new HashMap<>();
        for (Example example : positiveExamples) {
            
            for (char c : example.getString().toCharArray()) {
                    charset.add(c);
            }
            
            Set<String> subparts = Utils.subparts(example.getString());
            for (String x : subparts) {
                long v = ngrams.containsKey(x) ? ngrams.get(x) : 0;
                v++;
                ngrams.put(x, v);
            }
        }
        
       /**
         * Find out n-grams in negative examples and gives a score 
         * -1 for each negative example which contains it
         * We like to reward ngrams which are in the positives but not in the negatives
         */
        if(PENALIZE_NEGATIVES_NGRAMS){
            for (Example example : negativeExamples) {           
                Set<String> subparts = Utils.subparts(example.getString());
                for (String x : subparts) {
                    long v = ngrams.containsKey(x) ? ngrams.get(x) : 0;
                    v--;
                    ngrams.put(x, v);
                }
            }
        }
        
        ngrams = sortByValues(ngrams);
        
        long numberNgrams = 0; 
        for (Map.Entry<String, Long> entry : ngrams.entrySet()) {
                String  ngram = entry.getKey();
                Long v = entry.getValue();
                if(v <= 0){
                    
                    continue;
                }
                Leaf leaf = new Constant(Utils.escape(ngram));
                if(terminalSet.add(leaf)){
                    numberNgrams++;              
                }
                if(numberNgrams>=NUMBER_NGRAMS){
                    break;
                }
        }
        
        
        
        
        for (char c : charset.toSortedArray()) {
            terminalSet.add(new Constant(Utils.escape(c)));
        }
        
        Utils.generateRegexRanges(charset, terminalSet::add);
       
        nodeFactory.getTerminalSet().clear();
        nodeFactory.getTerminalSet().addAll(terminalSet);        
        
    }

     /**
     * Big to little score order. When score equals, bigger strings (length-wise) win. 
     * Always the return value is backed by a TreeMap.
     * @param <K>
     * @param <V>
     * @param map
     * @return Restituisce una TreeMap
     */
    static <K, V extends Comparable<V>> Map<K, V> sortByValues(Map<K, V> map) {
        Comparator<K> valueComparator = (k1, k2) -> {
            int compare = map.get(k2).compareTo(map.get(k1));
            if (compare == 0) {
                String s1 = (String) k1;
                String s2 = (String) k2;
                compare = Integer.compare(s2.length(), s1.length());
                if(compare == 0){
                    compare = s1.compareTo(s2);
                }
			}
			return compare;
		};
        Map<K, V> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    @Override
    public void setup(Context context) {
        context.getConfiguration().initNodeFactory();
        setup(context.getConfiguration(), context.getCurrentDataSet());
    }
}