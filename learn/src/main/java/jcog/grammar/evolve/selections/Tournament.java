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
package jcog.grammar.evolve.selections;

import jcog.grammar.evolve.inputs.Context;
import jcog.grammar.evolve.objective.Ranking;
import jcog.grammar.evolve.tree.Node;

/**
 *
 * @author MaleLabTs
 */
public class Tournament implements Selection {

    private final Context context;

    public Tournament(Context context) {
        this.context = context;
    }

    @Override
    public Node select(Ranking[] population) {

        int size = population.length;
        int best = size;
        for (int t = 0; t < 7; t++) {
            int index = context.getRandom().nextInt(size);
            best = Math.min(best, index);
        }

        return population[best].getNode();

    }
}
