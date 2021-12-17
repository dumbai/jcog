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
package jcog.grammar.evolve.generations;

import jcog.data.list.Lst;
import jcog.data.map.UnifriedMap;
import jcog.grammar.evolve.configuration.Configuration;
import jcog.grammar.evolve.inputs.Context;
import jcog.grammar.evolve.inputs.DataSet;
import jcog.grammar.evolve.inputs.DataSet.Example;
import jcog.grammar.evolve.tree.Constant;
import jcog.grammar.evolve.tree.Node;
import jcog.grammar.evolve.tree.RegexRange;
import jcog.grammar.evolve.tree.operator.Concatenator;
import jcog.grammar.evolve.tree.operator.ListMatch;
import jcog.grammar.evolve.tree.operator.MatchOneOrMore;
import jcog.grammar.evolve.utils.BasicTokenizer;
import jcog.grammar.evolve.utils.Tokenizer;
import jcog.grammar.evolve.utils.Utils;

import java.util.*;

/**
 * Creates a initial population from the matches. Matches are modified in this way:
 * Significant tokens are left unchanged, other words are changed into the corresponding
 * character class (i.e. \w \d).
 * Sequences of identical classes are compacted using quantifiers.
 * @author MaleLabTs
 */
public class TokenizedPopulationBuilder implements InitialPopulationBuilder {

    private final List<Node> population = new Lst();
    private final Map<String,Double> winnerTokens = new UnifriedMap();
    
    private final Tokenizer tokenizer = new BasicTokenizer();
     
    /**
     * Initialises a population from examples by replacing charcters with "\\w"
     * and digits with "\\d"
     */
    public TokenizedPopulationBuilder() {
    }

    @Override
    public void init(List<Node> target) {
        target.addAll(population);
    }

 
    private static boolean matchW(String string){
        return (string.length()==1 && matchW(string.charAt(0)));
    }
    
    private static boolean matchW(char character){
        return Character.isAlphabetic(character) || Character.isDigit(character) || character == '_';
    }
    
    @Override
    public void setup(Configuration configuration) {
        DataSet trainingDataset = configuration.getDatasetContainer().getTrainingDataset();
        this.population.addAll(this.setup(configuration, trainingDataset));
    }
        
    private List<Node> setup(Configuration configuration, DataSet usedTrainingDataset) {

        DataSet dataSet = usedTrainingDataset;
        
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

        List<List<String>> matchTokens = new LinkedList<>();
        Map<String, Double> tokensCounter = new HashMap<>();
        for (Example example : dataSet.getExamples()) {
            for (String match : example.getMatchedStrings()) {
                List<String> tokens = tokenizer.tokenize(match);
                matchTokens.add(tokens);
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
        for (Map.Entry<String, Double> entry : tokensCounter.entrySet()) {
            String key = entry.getKey();
            Double double1 = entry.getValue();
            Double doublePercentange = (double1 * 100.0) / numberOfMatches;
            entry.setValue(doublePercentange); 
             if(doublePercentange >= TOKEN_THREASHOLD){
                winnerTokens.put(key,doublePercentange);
            }
        }
        
        int popSize = configuration.getEvolutionParameters().getPopulationSize();
   
        int counter = 0;
        List<Node> newPopulation = new LinkedList<>();
        for (List<String> tokenizedMatch : matchTokens){
            newPopulation.add(createIndividualFromString(tokenizedMatch, true, winnerTokens));
            newPopulation.add(createIndividualFromString(tokenizedMatch, false, winnerTokens));
            counter+=2;
            if (counter >= popSize) {
                break;
            }
        }
        return newPopulation;
    }

    static final RegexRange LETTERS_RANGE = new RegexRange("A-Za-z");

    private static Node createIndividualFromString(List<String> tokenizedString, boolean compact, Map<String, Double> winnerTokens) {
        Deque<Node> nodes = new LinkedList<>();

        String w = "\\w";
        String d = "\\d";

        
         
        for(String token : tokenizedString){
            if(winnerTokens.containsKey(token)){
                nodes.add(new Constant(Utils.escape(token)));
            } else {

                for (char c : token.toCharArray()) {
                    if (Character.isLetter(c)) {
                        nodes.add(new ListMatch(LETTERS_RANGE).cloneTree());
                    } else if (Character.isDigit(c)) {
                        nodes.add(new Constant(d));
                    } else {
                        nodes.add(new Constant(Utils.escape(c)));
                    }
                }
            }
        }
        
        
        
        if(compact){
            Deque<Node> newNodes = new LinkedList<>();
            
            
            while (!nodes.isEmpty()) {

                Node node = nodes.pollFirst();
                String nodeValue = node.toString();
                boolean isRepeat = false;

                while (!nodes.isEmpty()){
                    Node next = nodes.peek();
                    String nextValue = next.toString();

                    if(nodeValue.equals(nextValue)){
                        isRepeat = true;
                        
                        nodes.pollFirst();
                    } else {
                        
                        break;
                    } 
                }    
                if(isRepeat){
                    node = new MatchOneOrMore(node);
                }
                newNodes.add(node);                
            }
            nodes = newNodes;
        }


        Deque<Node> tmp = new LinkedList<>();
        while (nodes.size() > 1) {

            while (!nodes.isEmpty()) {
                Node first = nodes.pollFirst();
                Node second = nodes.pollFirst();

                if (second != null) {
                    tmp.addLast(new Concatenator(first, second));
                } else {
                    tmp.addLast(first);
                }
            }

            nodes = tmp;
            tmp = new LinkedList<>();

        }

        return nodes.getFirst();
    }

    @Override
    public List<Node> init(Context context) {
        return setup(context.getConfiguration(), context.getCurrentDataSet());
    }
}
