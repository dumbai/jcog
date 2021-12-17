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

import jcog.grammar.evolve.tree.IDFactory;
import jcog.grammar.evolve.tree.Node;

import java.util.Arrays;

/**
 *
 * @author MaleLabTs
 */
public final class Ranking {

    private final Node tree;
    private final double[] fitness;
    private final int id;

    public Ranking(Node tree, double[] fitness) {
        this.tree = tree;
        this.fitness = fitness;
        this.id = (int)IDFactory.nextID();
    }

    @Override
    public String toString() {
        return '{' +
                tree.getDescription() +
                '=' + Arrays.toString(fitness) +
                '}';
    }


    public double[] getFitness() {
        return fitness;
    }


    public Node getNode() {
        return tree;
    }    
    
    public String getDescription(){
        StringBuilder sb = new StringBuilder();
        this.tree.describe(sb);
        return sb.toString();
    }

    @Override
    public final boolean equals(Object obj) {
        return obj == this;

        















    }

    @Override
    public final int hashCode() {
        return id;
        



    }       
    
}
