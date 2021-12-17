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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import jcog.grammar.evolve.configuration.Configuration;
import jcog.grammar.evolve.configuration.Configurator;
import jcog.grammar.evolve.outputs.Results;
import jcog.grammar.evolve.strategy.ExecutionStrategy;
import jcog.grammar.evolve.strategy.impl.CoolTextualExecutionListener;
import jcog.grammar.evolve.utils.Utils;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * This is the GP engine project, this is not intended to be executed directly from this class.
 * It accepts the filename of a serialized(JSON) configuration file.
 * This is used for experiments and by GP engine developers.
 * @author MaleLabTs
 */
public enum EvolveGrammar {
	;

	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            System.exit(0);
        }


        Configuration configuration = Configurator.configureFile(args[0]);
        


        Logger.getLogger("").addHandler(new FileHandler(new File(configuration.getOutputFolder(), "log.xml").getCanonicalPath()));

        run(args[0], configuration);

    }

    public static Results run(Configuration configuration) throws Exception {
        return run(null, configuration);
    }

    public static Results run(String message, Configuration configuration) throws Exception {


        Results results = new Results(configuration);
        results.setMachineHardwareSpecifications(Utils.cpuInfo());

        ExecutionStrategy strategy = configuration.getStrategy();
        long startTime = System.currentTimeMillis();

        strategy.execute(configuration,
                new CoolTextualExecutionListener(message, configuration, results));

        if (configuration.getPostProcessor() != null) {
            startTime = System.currentTimeMillis() - startTime;
            configuration.getPostProcessor().elaborate(configuration, results, startTime);
        }

        return results;
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar \"Random_Regex_Turtle.jar\" configFileName [startGui]");
    }
}
