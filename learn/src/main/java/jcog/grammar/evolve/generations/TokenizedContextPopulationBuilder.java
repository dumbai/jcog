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
import jcog.grammar.evolve.configuration.Configuration;
import jcog.grammar.evolve.inputs.Context;
import jcog.grammar.evolve.inputs.DataSet;
import jcog.grammar.evolve.inputs.DataSet.Example;
import jcog.grammar.evolve.terminalsets.TokenizedContextTerminalSetBuilder;
import jcog.grammar.evolve.tree.Constant;
import jcog.grammar.evolve.tree.Node;
import jcog.grammar.evolve.tree.ParentNode;
import jcog.grammar.evolve.tree.RegexRange;
import jcog.grammar.evolve.tree.operator.*;
import jcog.grammar.evolve.utils.BasicTokenizer;
import jcog.grammar.evolve.utils.Tokenizer;
import jcog.grammar.evolve.utils.Utils;

import java.util.*;

/**
 * Creates a initial population from the matches and unmatches. 
 * Matches and unmatches are modified in this way:
 * Significant tokens are left unchanged, other words(chars) are changed into the corresponding
 * character class (i.e. \w \d).
 * Sequences of identical character classes or tokens  are compacted using quantifiers.
 * Accepts these configuration population builder parameters:
 * "tokenThreashold","discardWtokens","tokenUnmatchThreashold","addNoContextIndividuals"
 * @author MaleLabTs
 */
public class TokenizedContextPopulationBuilder implements InitialPopulationBuilder {

    private final List<Node> population = new Lst();

    private final Tokenizer tokenizer = new BasicTokenizer();
     
    /**
     * Initialises a population from examples by replacing charcters with "\\w"
     * and digits with "\\d"
     */
    public TokenizedContextPopulationBuilder() {
    }


    @Override
    public void init(List<Node> target) {
        target.addAll(population);
    }
    
    @Override
    public void setup(Configuration configuration) {
        DataSet trainingDataset = configuration.getDatasetContainer().getTrainingDataset();
        this.population.addAll(this.setup(null, configuration, trainingDataset));
    }
    
    private List<Node> setup(Context context, Configuration configuration, DataSet usedTrainingDataset) {


        
        
        
        if (usedTrainingDataset.getStripedDataset()!=null){
            usedTrainingDataset = usedTrainingDataset.getStripedDataset();
        }
        
        Map<String, String> parameters = configuration.getPopulationBuilderParameters();
        boolean DISCARD_W_TOKENS = true; 
        boolean ADD_NO_CONTEXT_INDIVIDUALS = true;
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
            if(parameters.containsKey("addNoContextIndividuals")){
                ADD_NO_CONTEXT_INDIVIDUALS = Boolean.parseBoolean(parameters.get("addNoContextIndividuals"));
            }
        }
         
        List<Node> newPopulation = new LinkedList<>();


        Map<String, Double> winnerMatchTokens = TokenizedContextTerminalSetBuilder.calculateWinnerMatchTokens(usedTrainingDataset, TOKEN_THREASHOLD, DISCARD_W_TOKENS);
        Map<String, Double> winnerUnMatchTokens = TokenizedContextTerminalSetBuilder.calculateWinnerUnmatchTokens(usedTrainingDataset, TOKEN_UNMATCH_THREASHOLD, DISCARD_W_TOKENS);
        
        for(Example example : usedTrainingDataset.getExamples()){
            if(example.getMatch().isEmpty()){
                
                continue;
            }
            newPopulation.addAll(createIndividualsFromExample(example, true, winnerMatchTokens, winnerUnMatchTokens));
            newPopulation.addAll(createIndividualsFromExample(example, false, winnerMatchTokens, winnerUnMatchTokens));
            
        }
        
        
        if(ADD_NO_CONTEXT_INDIVIDUALS){
            TokenizedPopulationBuilder tokenizedPopulationBuilder = new TokenizedPopulationBuilder();
            if(context == null){
                tokenizedPopulationBuilder.setup(configuration);
                tokenizedPopulationBuilder.init(newPopulation);
            }else {
                newPopulation.addAll(tokenizedPopulationBuilder.init(context));
            }
        }
        
        int popSize = Math.min(configuration.getEvolutionParameters().getPopulationSize()/2, newPopulation.size());
        
        Collections.shuffle(newPopulation,new Random(0));
        newPopulation = new LinkedList<>(newPopulation.subList(0, popSize));
        return newPopulation;
    }
    
    private List<Node> createIndividualsFromExample(Example example,boolean compact, Map<String,Double> winnerMatchTokens, Map<String,Double> winnerUnMatchTokens){
        if(example.getNumberMatches()==0){
            return Collections.emptyList();
        }
        List<Node> individualsForExample = new LinkedList<>();
        Set<String> matchSet = new HashSet<>(example.getMatchedStrings());
        Set<String> unmatchSet = new HashSet<>(example.getUnmatchedStrings());
        List<String> orderedAnnotatedStrings = example.getOrderedAnnotatedStrings();
         
        
        String pre=null,match=null,post=null;
        for (int i = 0; i < orderedAnnotatedStrings.size(); i++) {
            if(matchSet.contains(orderedAnnotatedStrings.get(i))){
                match =  orderedAnnotatedStrings.get(i);
                if(i>0){
                    if(unmatchSet.contains(orderedAnnotatedStrings.get(i-1))){
                        pre = orderedAnnotatedStrings.get(i-1);
                    }
                }
                if((i+1)<orderedAnnotatedStrings.size()){
                    if(unmatchSet.contains(orderedAnnotatedStrings.get(i+1))){
                        post = orderedAnnotatedStrings.get(i+1);
                    }
                }
                individualsForExample.add(createIndividualFromStrings(pre, match, post, compact, winnerMatchTokens, winnerUnMatchTokens));
            }            
        }
        return individualsForExample;
    }

    
    
    
    private Node createIndividualFromStrings(String preUnmatchString, String matchString, String postUnmatchString, boolean compact, 
                                                                    Map<String,Double> winnerMatchTokens, Map<String,Double> winnerUnmatchTokens) {
        
        Node preUnmatchNode = null;

        if(preUnmatchString != null){
            List<String> preUnmatchStringTokens = tokenizer.tokenize(preUnmatchString);
            preUnmatchNode = createIndividualFromTokenizedString(preUnmatchStringTokens, winnerUnmatchTokens, compact, true);
        }
        Node matchNode = null;
        if(matchString != null){
            List<String> matchStringTokens = tokenizer.tokenize(matchString);
            matchNode = createIndividualFromTokenizedString(matchStringTokens, winnerMatchTokens, compact, false);
        }
        Node postUnmatchNode = null;
        if(postUnmatchString != null){
            List<String> postUnmatchStringTokens = tokenizer.tokenize(postUnmatchString);        
            postUnmatchNode = createIndividualFromTokenizedString(postUnmatchStringTokens, winnerUnmatchTokens, compact, false);
        }
        
        Node finalIndividual = matchNode;
        if(postUnmatchNode!=null){
            ParentNode finalIndividualTemp = new Concatenator();
            finalIndividualTemp.add(matchNode);
            ParentNode positiveLookAhead = new PositiveLookahead();
            positiveLookAhead.add(postUnmatchNode);
            finalIndividualTemp.add(positiveLookAhead);
            finalIndividual = finalIndividualTemp;
        }
        if(preUnmatchNode!=null){
            ParentNode finalIndividualTemp = new Concatenator();
            ParentNode positiveLookBehind = new PositiveLookbehind();
            positiveLookBehind.add(preUnmatchNode);
            finalIndividualTemp.add(positiveLookBehind);
            finalIndividualTemp.add(finalIndividual);
            finalIndividual = finalIndividualTemp;
        }
        return finalIndividual;
    }

    static final String w = "\\w";
    static final String d = "\\d";
    
    
    private static Node createIndividualFromTokenizedString(List<String> tokenizedString, Map<String, Double> winnerTokens, boolean compact, boolean useMinMaxQuantifier){


        ParentNode letters = new ListMatch();
        letters.add(new RegexRange("A-Za-z"));

        Deque<Node> nodes = new LinkedList<>();
        for(String token : tokenizedString){
            if(winnerTokens.containsKey(token)){
                nodes.add(new Constant(Utils.escape(token)));
            } else {
                for (char c : token.toCharArray()) {
                    if (Character.isLetter(c)) {
                        nodes.add(letters.cloneTree());
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
                int repetitions = 1;
                while (!nodes.isEmpty()){
                    Node next = nodes.peek();
                    String nextValue = next.toString();

                    if(nodeValue.equals(nextValue)){
                        repetitions++;
                        isRepeat = true;
                        
                        nodes.pollFirst();
                    } else {
                        
                        break;
                    } 
                }    
                if(isRepeat){
                    ParentNode finalNode;
                    if(useMinMaxQuantifier){
                        finalNode = new MatchMinMax();
                        finalNode.add(node, new Constant(1), new Constant(repetitions));
                    } else {
                        finalNode = new MatchOneOrMore();
                        finalNode.add(node);
                        
                    }
                    node = finalNode;
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
        return setup(context, context.getConfiguration(), context.getCurrentDataSet());
    }
}
