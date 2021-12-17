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
package jcog.grammar.evolve.tree;

import jcog.grammar.evolve.tree.operator.Concatenator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MaleLabTs
 */
public class NodeFactory {

    public final List<Node> functionSet;
    public final List<Leaf> terminalSet;

    public NodeFactory(NodeFactory factory){
        functionSet = new ArrayList<>(factory.getFunctionSet());
        terminalSet = new ArrayList<>(factory.getTerminalSet());
    }

    public NodeFactory() {
        functionSet = new ArrayList<>(1);
        terminalSet = new ArrayList<>(0);

        functionSet.add(new Concatenator());
        
    }

    public final List<Node> getFunctionSet() {
        return functionSet;
    }

    public final List<Leaf> getTerminalSet() {
        return terminalSet;
    }

    
}
