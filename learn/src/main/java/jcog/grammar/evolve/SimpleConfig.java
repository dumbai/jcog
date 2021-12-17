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
package jcog.grammar.evolve;

import jcog.grammar.evolve.configuration.Configuration;
import jcog.grammar.evolve.configuration.DatasetContainer;
import jcog.grammar.evolve.generations.FlaggingNaivePopulationBuilder;
import jcog.grammar.evolve.inputs.DataSet;
import jcog.grammar.evolve.objective.FlaggingAccuracyPrecisionLengthObjective;
import jcog.grammar.evolve.selections.best.BasicFlaggingLearningBestSelector;
import jcog.grammar.evolve.strategy.impl.MultithreadStrategy;
import jcog.grammar.evolve.terminalsets.FlaggingNgramsTerminalSetBuilder;

import java.util.logging.Logger;


/**
 *
 * @author MaleLabTs
 */
public class SimpleConfig {


    public int numberThreads = 4;
    public int numberOfJobs = 1;
    public int generations;
    public int populationSize;
    public DataSet dataset;
    public boolean populateOptionalFields = true;
    public boolean isStriped = true;
    public boolean isFlagging = false;
    
    public transient String datasetName;
    public transient String outputFolder;

    /**
     * Percentange [0,100] of the number of the generations used for the Spared termination
     * criteria. 
     */
    public double termination = 20.0;
    public String comment;

    public SimpleConfig() {

    }

    public SimpleConfig(DataSet d, int populationSize, int generations) {
        this.populationSize = populationSize;
        this.generations = generations;
        this.dataset = d;
    }

    public Configuration buildConfiguration(){
        assert !(isFlagging&&isStriped);
        
        
        Configuration configuration = new Configuration();
        configuration.setConfigName("Console config");
        configuration.getEvolutionParameters().setGenerations(generations);
        configuration.getEvolutionParameters().setPopulationSize(populationSize);
        configuration.setJobs(numberOfJobs);
        configuration.getStrategyParameters().put(
                MultithreadStrategy.THREADS_KEY, String.valueOf(numberThreads)
        );
        
        int terminationGenerations = (int)(termination * configuration.getEvolutionParameters().getGenerations() / 100.0);
        if(termination==100.0){
            configuration.getStrategyParameters().put("terminationCriteria","false");  
        } else {
            configuration.getStrategyParameters().put("terminationCriteria","true");
        }
        configuration.getStrategyParameters().put("terminationCriteriaGenerations", String.valueOf(terminationGenerations));
        
        configuration.getStrategyParameters().put("terminationCriteria2","false");
        
        if(dataset == null){
            throw new IllegalArgumentException("You must define a dataset");
        }
        dataset.populateUnmatchesFromMatches();
        DatasetContainer datasetContainer = new DatasetContainer(dataset);
        datasetContainer.createDefaultRanges((int) configuration.getInitialSeed());
        
        dataset.updateStats();
        if(isStriped){
            Logger.getLogger(this.getClass().getName()).info("Enabled striping.");
            datasetContainer.setDataSetsStriped(true);
            double STRIPING_DEFAULT_MARGIN_SIZE = 5;
            datasetContainer.setDatasetStripeMarginSize(STRIPING_DEFAULT_MARGIN_SIZE);
            datasetContainer.setProposedNormalDatasetInterval(100);
        }
        configuration.setDatasetContainer(datasetContainer); 
        
        
        
        
        configuration.setIsFlagging(isFlagging);
        if(this.isFlagging){
            configuration.setStrategy(new MultithreadStrategy());
            configuration.setBestSelector(new BasicFlaggingLearningBestSelector());
            configuration.setObjective(new FlaggingAccuracyPrecisionLengthObjective());
            configuration.setPopulationBuilder(new FlaggingNaivePopulationBuilder()); 
            configuration.setTerminalSetBuilder(new FlaggingNgramsTerminalSetBuilder()); 
            
            configuration.getTerminalSetBuilderParameters().put("discardWtokens", "false");
            configuration.getStrategyParameters().put("isFlagging", "true"); 





        }
        
        
        
        configuration.setup(); 
        
        return configuration;
    }
}
