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
package jcog.grammar.evolve.evaluators;

import jcog.grammar.evolve.inputs.Context;
import jcog.grammar.evolve.inputs.DataSet.Bounds;
import jcog.grammar.evolve.tree.Node;

import java.util.List;
import java.util.Map;

/**
 *
 * @author MaleLabTs
 */
public interface TreeEvaluator {

    void setup(Map<String, String> parameters);
    List<Bounds[]> evaluate(Node root, Context context) throws TreeEvaluationException;
    
}
