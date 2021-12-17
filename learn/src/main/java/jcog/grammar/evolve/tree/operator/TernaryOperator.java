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
package jcog.grammar.evolve.tree.operator;

import jcog.data.list.Lst;
import jcog.grammar.evolve.tree.Node;
import jcog.grammar.evolve.tree.ParentNode;

/**
 *
 * @author MaleLabTs
 */
public abstract class TernaryOperator extends ParentNode {

    public TernaryOperator() {
        super(new Lst());
    }













    @Override
    public Node cloneTree() {
        TernaryOperator bop = buildCopy();
        if (size() >= 3) {
            cloneChild(get(0), bop);
            cloneChild(get(1), bop);
            cloneChild(get(2), bop);
        }
        bop.hash = hash;
        return bop;
    }

    @Override
    public final int getMinChildrenCount() {
        return 3;
    }

    @Override
    public final int getMaxChildrenCount() {
        return 3;
    }
    
    public final Node getFirst() {
        return get(0);
    }

    public final Node getSecond() {
        return get(1);
    } 
    
    public final Node getThird() {
        return get(2);
    }
    
    protected abstract  TernaryOperator buildCopy();
    
}
