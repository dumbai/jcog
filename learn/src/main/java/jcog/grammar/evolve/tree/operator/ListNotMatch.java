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

import jcog.grammar.evolve.tree.Constant;
import jcog.grammar.evolve.tree.DescriptionContext;
import jcog.grammar.evolve.tree.Node;
import jcog.grammar.evolve.tree.RegexRange;

/**
 *
 * @author MaleLabTs
 */
public class ListNotMatch extends UnaryOpNode {
    
    @Override
    protected UnaryOpNode buildCopy() {
        return new ListNotMatch();
    }

    @Override
    public void describe(StringBuilder builder, DescriptionContext context, RegexFlavour flavour) {
        Node child = get(0);
        builder.append("[^");
        child.describe(builder, context, flavour);
        builder.append(']');
    }
    
    @Override
    public boolean isValid() {
        return checkValid(get(0));
    }

    private static boolean checkValid(Node root){

        if(!(root instanceof Constant || root instanceof RegexRange || root instanceof Concatenator)){
            return false;
        }

        return root.children().stream().allMatch(ListNotMatch::checkValid);
    }  
    
    @Override
    public boolean isCharacterClass() {
        return true;
    }
}