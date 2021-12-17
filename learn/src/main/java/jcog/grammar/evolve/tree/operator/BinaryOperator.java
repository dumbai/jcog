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
public abstract class BinaryOperator extends ParentNode {

    protected BinaryOperator() {
        super(new Lst<>(2));
    }

    protected BinaryOperator(Node a, Node b) {
        super(new Lst<>(2, new Node[] { a,b }));
        a.setParent(this);
        b.setParent(this);
    }

    @Override
    public int getMinChildrenCount() {
        return 2;
    }

    @Override
    public int getMaxChildrenCount() {
        return 2;
    }

    public final Node getLeft() {
        return get(0);
    }

    public final Node getRight() {
        return get(1);
    }

    @Override
    public Node cloneTree() {
        BinaryOperator bop = buildCopy();
        if (size() >= 2) {
            cloneChild(getLeft(), bop);
            cloneChild(getRight(), bop);
        }
        bop.hash = hash;
        return bop;
    }

    protected abstract BinaryOperator buildCopy();

}