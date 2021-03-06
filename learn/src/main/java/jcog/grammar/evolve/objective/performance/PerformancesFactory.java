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
package jcog.grammar.evolve.objective.performance;

import jcog.grammar.evolve.configuration.Configuration;
import jcog.grammar.evolve.inputs.Context;
import jcog.grammar.evolve.objective.Objective;

/**
 * Creates performance objectives with the requested configurations.
 * @author MaleLabTs
 */
public enum PerformancesFactory {
	;

	public static Objective buildObjective(Context.GrammarEvaluationPhase phase, Configuration configuration) {
        return buildObjective(phase, configuration, false);
    }
    
    public static Objective buildObjective(Context.GrammarEvaluationPhase phase, Configuration configuration, boolean isStriped){
        Context context = new Context(phase, configuration);
        context.setStripedPhase(isStriped);
        Objective objective = new PerformacesObjective();
        objective.setup(context);
        return objective;
    }
}
