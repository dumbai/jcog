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

import jcog.grammar.evolve.tree.DescriptionContext;
import jcog.grammar.evolve.tree.Leaf;

/**
 * https:
 * @author MaleLabTs
 */
public class Backreference extends Leaf<Integer> {

    public Backreference(){
        super(1 /* TODO random? */);
    }

    public Backreference(int value){
        super(value);
    }

    public Backreference(Backreference value){
        super(value);
    }

    @Override
    public void describe(StringBuilder builder, DescriptionContext context, RegexFlavour flavour) {
        builder.append('\\');
        int v = value;
        switch (flavour) {
            case JAVA -> builder.append(v);
            default -> builder.append(v + context.getExpansionGroups());
        }
    }

    @Override
    public Leaf cloneTree() {
        return new Backreference(this);
    }



    
}
