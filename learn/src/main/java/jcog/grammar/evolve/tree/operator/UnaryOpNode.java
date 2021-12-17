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

import jcog.grammar.evolve.tree.Node;
import jcog.grammar.evolve.tree.ParentNode;
import org.eclipse.collections.impl.factory.Lists;

import java.util.ArrayList;


/**
 *
 * @author MaleLabTs
 */
public abstract class UnaryOpNode extends ParentNode {

    public UnaryOpNode() {
        super(new ArrayList(1));
    }

    public UnaryOpNode(Node child) {
        super(Lists.mutable.of(child));
        child.setParent(this);
    }

    @Override
    public final int getMinChildrenCount() {
        return 1;
    }

    @Override
    public final int getMaxChildrenCount() {
        return 1;
    }       












    @Override
    public Node cloneTree() {
        UnaryOpNode bop = buildCopy();

        if (!isEmpty()) {
            cloneChild(get(0), bop);
        }
        bop.hash = hash;
        return bop;
    }


    protected abstract UnaryOpNode buildCopy();

}