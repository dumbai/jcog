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

import jcog.grammar.evolve.utils.Utils;

import java.util.List;

/**
 *
 * @author MaleLabTs
 */
public abstract class Node {



    public abstract Node cloneTree();

    public abstract ParentNode getParent();
    public abstract void setParent(ParentNode parent);


    public abstract int getMinChildrenCount();
    public abstract int getMaxChildrenCount();
    public abstract List<Node> children();
    public abstract long getId();

    public final String describe() {
        StringBuilder sb = new StringBuilder();
        describe(sb);
        return sb.toString();
    }

    public abstract void describe(StringBuilder builder);
    public abstract void describe(StringBuilder builder, DescriptionContext context, RegexFlavour flavour);
    public abstract boolean isValid();

    public final int toNonNegativeInteger(int ifMissing) {
        return Utils.i(toString(), ifMissing);
    }

    public String getDescription() {
        StringBuilder sb;
        describe(sb = new StringBuilder());
        return sb.toString();
    }


    public enum RegexFlavour {
        JAVA,
        CSHARP,
        JS
    }

    public abstract boolean isCharacterClass();
    public abstract boolean isEscaped();
}
