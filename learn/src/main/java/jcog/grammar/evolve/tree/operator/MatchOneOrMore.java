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
import jcog.grammar.evolve.tree.Node;

/**
 *
 * @author MaleLabTs
 */
public class MatchOneOrMore extends Quantifier {

    public MatchOneOrMore() {
        super();
    }

    public MatchOneOrMore(Node node) {
        super(node);
    }

    @Override
    public void describe(StringBuilder builder, DescriptionContext context, RegexFlavour flavour) {
        StringBuilder tmp = new StringBuilder();
        Node child = get(0);
        
        int index = context.incGroups();
        child.describe(tmp, context, flavour);
        int l = child.isEscaped() ? tmp.length() - 1 : tmp.length();
        boolean group = l > 1 && !child.isCharacterClass() && !(child instanceof Group) && !(child instanceof NonCapturingGroup);
        switch (flavour) {
            case JAVA -> {
                if (group) {
                    builder.append("(?:");
                    builder.append(tmp);
                    builder.append(')');
                } else {
                    builder.append(tmp);
                }
                builder.append("++");
            }
            default -> {
                builder.append("(?=(");
                if (group) {
                    builder.append("(?:");
                    builder.append(tmp);
                    builder.append(')');
                } else {
                    builder.append(tmp);
                }
                builder.append("+))\\").append(index);
                context.incExpansionGroups();
            }
        }
    }

    @Override
    protected UnaryOpNode buildCopy() {
        return new MatchOneOrMore();
    }
     

}