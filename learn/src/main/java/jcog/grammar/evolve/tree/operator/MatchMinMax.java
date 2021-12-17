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

import jcog.grammar.evolve.tree.Anchor;
import jcog.grammar.evolve.tree.Constant;
import jcog.grammar.evolve.tree.DescriptionContext;
import jcog.grammar.evolve.tree.Node;

/**
 * @author MaleLabTs
 */
public class MatchMinMax extends TernaryOperator {


    @Override
    protected TernaryOperator buildCopy() {
        return new MatchMinMax();
    }

    @Override
    public void describe(StringBuilder builder, DescriptionContext context, RegexFlavour flavour) {
        StringBuilder tmp = new StringBuilder();
        Node child = getFirst();
        
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
                builder.append('{');
                int second = getSecond().toNonNegativeInteger(1);
                builder.append(second);
                int third = getThird().toNonNegativeInteger(-1);
                if (third != -1) {
                    builder.append(',').append(Math.max(third, second + 1)).append("}+");
                } else {
                    builder.append("}+");
                }
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
                builder.append('{');
                builder.append(getSecond().toNonNegativeInteger(-1));
                builder.append(',');
                builder.append(getThird().toNonNegativeInteger(-1));
                builder.append("}))\\").append(index);
                context.incExpansionGroups();
            }
        }

    }

    @Override
    public boolean isValid() {
        Node first = getFirst();
        boolean validFirst = first.isValid()
                && !(first instanceof Concatenator || first instanceof Quantifier || first instanceof MatchMinMax || first instanceof MatchMinMaxGreedy || first instanceof Anchor || first instanceof Lookaround);

        if (validFirst) {

            Node second = getSecond();
            if (!(second instanceof Constant)) return false;

            int leftValue = second.toNonNegativeInteger(-1);
            if (leftValue < 0) return false;


            Node third = getThird();
            if (!(third instanceof Constant)) return false;

            int rightValue = third.toNonNegativeInteger(-1);
            if (rightValue < 0) return false;

            return (leftValue < rightValue);

        }


        return false;
    }
}
