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
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.CharHashSet;

import java.util.*;

/**
 * Initialize terminal set from examples (tokens, ranges) and add significant tokens to the terminal setAt.
 * It uses two separate algorithms for tokens on matches and umathces.
 * This terminalset builder always adds character classes \d \w.
 * As usual, it adds terminal sets to the predefined set in configuration file.
 * The configuration file should contain a list of constant with predefined separators.
 * Accepts these configuration population builder parameters:
 * "tokenThreashold","discardWtokens","tokenUnmatchThreashold"
 * @author MaleLabTs
 */
public class TokenizedContextTerminalSetBuilder implements TerminalSetBuilder{
    
    private static final Tokenizer tokenizer = new BasicTokenizer();
    
    
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
        TokenizedContextTerminalSetBuilder.setup(configuration, configuration.getDatasetContainer().getTrainingDataset());
    }
    
    /**
     * This setup is additive, this is not going to reset the NodeFactory configuration; 
     * There is ONE configuration per job, and the configurations have separate node factories.
     * When you need to reset the NodeFactory, you have to call configuration.initNodeFactory()
     * method and you are going to obtain a NodeFactory based only to provided constants, ranges
     * and operators.
     * @param configuration
     * @param trainingDataset
     */
    public static void setup(Configuration configuration, DataSet trainingDataset) {

        
        
        
        if (trainingDataset.getStripedDataset()!=null){
            trainingDataset = trainingDataset.getStripedDataset();
        }
        
        Map<String, String> parameters = configuration.getPopulationBuilderParameters();
        boolean DISCARD_W_TOKENS = true; 
        double TOKEN_UNMATCH_THREASHOLD = 80.0;
        double TOKEN_THREASHOLD = 80.0;
        if(parameters!=null){
            
            if(parameters.containsKey("tokenThreashold")){
                TOKEN_THREASHOLD = Double.parseDouble(parameters.get("tokenThreashold"));
            }
            if(parameters.containsKey("discardWtokens")){
                DISCARD_W_TOKENS = Boolean.parseBoolean(parameters.get("discardWtokens"));
            }
            if(parameters.containsKey("tokenUnmatchThreashold")){
                TOKEN_UNMATCH_THREASHOLD = Double.parseDouble(parameters.get("tokenUnmatchThreashold"));
            }
        }
        
        
        CharHashSet charset = new CharHashSet();

        
        NodeFactory nodeFactory = configuration.getNodeFactory();
        
        
        Set<Leaf> terminalSet = new UnifiedSet(nodeFactory.getTerminalSet());
                
        for (Example example : trainingDataset.getExamples()) {
            for (String match : example.getMatchedStrings()) {

                
                for (int i = 0; i < match.length(); i++)
                    charset.add( match.charAt(i) );

            }
        }
       
        
        Map<String, Double> winnerTokens = TokenizedContextTerminalSetBuilder.calculateWinnerMatchTokens(trainingDataset, TOKEN_THREASHOLD, DISCARD_W_TOKENS);
        Map<String, Double> winnerUnMatchTokens = TokenizedContextTerminalSetBuilder.calculateWinnerUnmatchTokens(trainingDataset, TOKEN_UNMATCH_THREASHOLD,DISCARD_W_TOKENS);
        winnerTokens.putAll(winnerUnMatchTokens);
        
        
        for (Map.Entry<String, Double> entry : winnerTokens.entrySet()) {
                String  token = entry.getKey();
                
                Leaf leaf = new Constant(Utils.escape(token));
                terminalSet.add(leaf);

        }
        
        
        
        
        Utils.generateRegexRanges(charset, terminalSet::add);
        
        
        terminalSet.add(new Constant("\\d"));
        terminalSet.add(new Constant("\\w"));
        
        
        nodeFactory.getTerminalSet().clear();
        nodeFactory.getTerminalSet().addAll(terminalSet);        
        
    }

    
    public static Map<String,Double> calculateWinnerMatchTokens(DataSet dataSet, double threashold, boolean discardWtokens){
        Map<String,Double> tokensCounter = new HashMap<>();
        for (Example example : dataSet.getExamples()) {
            for (String match : example.getMatchedStrings()) {
                List<String> tokens = tokenizer.tokenize(match);
                Set<String> tokensSet = new HashSet<>(tokens);
                for(String token : tokensSet){
                    if(matchW(token) && discardWtokens){
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
        Map<String, Double> winnerMatchTokensLocal = new HashMap<>();
        for (Map.Entry<String, Double> entry : tokensCounter.entrySet()) {
            String key = entry.getKey();
            Double double1 = entry.getValue();
            Double doublePercentange = (double1 * 100.0) / numberOfMatches;
            entry.setValue(doublePercentange); 
             if(doublePercentange >= threashold){
                winnerMatchTokensLocal.put(key,doublePercentange);
            }
        }
        return winnerMatchTokensLocal;
    }
    
    public static Map<String,Double> calculateWinnerUnmatchTokens(DataSet dataSet, double threashold, boolean discardWtokens){
        Map<String,Double> tokensCounter = new HashMap<>();
        int numberOfPositiveExamples = 0;
        for (Example example : dataSet.getExamples()) {
            if(example.getMatch().isEmpty()){
                
                continue;
            }
            numberOfPositiveExamples++;
            Set<String> exampleTokenSet = new HashSet<>();
                
            for (String unmatch : example.getUnmatchedStrings()) {
                List<String> tokens = tokenizer.tokenize(unmatch);
                exampleTokenSet.addAll(tokens);
            }
            
            
            for(String token : exampleTokenSet){
                if(matchW(token) && discardWtokens){
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

        Map<String, Double> winnerUnmatchTokensLocal = new HashMap<>();
        for (Map.Entry<String, Double> entry : tokensCounter.entrySet()) {
            String key = entry.getKey();
            Double double1 = entry.getValue();
            Double doublePercentange = (double1 * 100.0) / numberOfPositiveExamples;
            entry.setValue(doublePercentange); 
             if(doublePercentange >= threashold){
                
                winnerUnmatchTokensLocal.put(key,doublePercentange);
            }
        }
        return winnerUnmatchTokensLocal;
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
