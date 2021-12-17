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
import jcog.grammar.evolve.utils.BasicTokenizer;
import jcog.grammar.evolve.utils.Tokenizer;
import jcog.grammar.evolve.utils.Utils;
import org.eclipse.collections.impl.set.mutable.primitive.CharHashSet;

import java.util.*;

/**
 * Initialize terminal set from examples (tokens, ranges) and add significant tokens to the terminal setAt.
 * This terminal set builder always adds character classes \d \w.
 * As usual, it adds terminal sets to the predefined set in configuration file.
 * The configuration file should contain a list of constant with predefined separators.
 * @author MaleLabTs
 */
public class TokenizedTerminalSetBuilder implements TerminalSetBuilder{
    
    private final Tokenizer tokenizer = new BasicTokenizer();
    
    
    private static boolean matchW(String string){
        return (string.length()==1 && matchW(string.charAt(0)));
    }
    
    private static boolean matchW(char character){
        return Character.isAlphabetic(character) || Character.isDigit(character) || character == '_';
    }
    
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
    
    /**
     * This setup is additive, this is not going to reset the NodeFactory configuration; 
     * There is ONE configuration per job, and the configurations have separate node factories.
     * When you need to reset the NodeFactory, you have to call configuration.initNodeFactory()
     * method and you are going to obtain a NodeFactory based only to provided constants, ranges
     * and operators.
     * @param configuration
     */
    private void setup(Configuration configuration, DataSet dataSet) {
        
        
        double TOKEN_THREASHOLD = 80.0;
        boolean DISCARD_W_TOKENS = true; 
        Map<String, String> parameters = configuration.getPopulationBuilderParameters();
        if(parameters!=null){
            
            if(parameters.containsKey("tokenThreashold")){
                TOKEN_THREASHOLD = Double.parseDouble(parameters.get("tokenThreashold"));
            }
            if(parameters.containsKey("discardWtokens")){
                DISCARD_W_TOKENS = Boolean.parseBoolean(parameters.get("discardWtokens"));
            }
        }
        
        
        CharHashSet charset = new CharHashSet();
       
        NodeFactory nodeFactory = configuration.getNodeFactory(); 
        
        Set<Leaf> terminalSet = new HashSet<>(nodeFactory.getTerminalSet());
        
        
        Map<String,Double> tokensCounter = new HashMap<>();


        for (Example example : dataSet.getExamples()) {
            for (String match : example.getMatchedStrings()) {
                
                
                for(char c : match.toCharArray()){
                    charset.add(c);
                }
                
                List<String> tokens = tokenizer.tokenize(match);
                Set<String> tokensSet = new HashSet<>(tokens);
                for(String token : tokensSet){
                    if(matchW(token) && DISCARD_W_TOKENS){
                        continue;
                    }
                    if(tokensCounter.containsKey(token)){
                        Double value = tokensCounter.get(token);
                        value++;
                        tokensCounter.put(token, value);
                    } else {
                        tokensCounter.put(token, 1.0);
                    }
                }
            }
        }
        
        int numberOfMatches = dataSet.getNumberMatches();
        Map<String, Double> winnerTokens = new HashMap<>();
        for (Map.Entry<String, Double> entry : tokensCounter.entrySet()) {
            String key = entry.getKey();
            Double double1 = entry.getValue();
            Double doublePercentange = (double1 * 100.0) / numberOfMatches;
            entry.setValue(doublePercentange); 
             if(doublePercentange >= TOKEN_THREASHOLD){
                winnerTokens.put(key,doublePercentange);
            }
        }
        
        
        for (Map.Entry<String, Double> entry : winnerTokens.entrySet()) {
                String  token = entry.getKey();
                double v = entry.getValue();
                Leaf leaf = new Constant(Utils.escape(token));
                terminalSet.add(leaf);
        }
        
        
        
        Utils.generateRegexRanges(charset, terminalSet::add);
        
        
        terminalSet.add(new Constant("\\d"));
        terminalSet.add(new Constant("\\w"));
        
        
        nodeFactory.getTerminalSet().clear();
        nodeFactory.getTerminalSet().addAll(terminalSet);        
        
    }

    /**
     * This setup also resets the NodeFactory configuration inside current configuration; 
     * There is ONE configuration per job, and the configurations have separate node factories.
     * Calling this method is useful when you have to change the NodeFactory configuration
     * (--i.e. SeparateAndConquer strategies)
     * @param context
     */
    @Override
    public void setup(Context context) {
        
        
        
        context.getConfiguration().initNodeFactory();
        setup(context.getConfiguration(), context.getCurrentDataSet());
    }
    
}
