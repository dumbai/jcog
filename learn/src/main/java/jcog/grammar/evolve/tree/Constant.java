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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author MaleLabTs
 */
 
public class Constant extends Leaf<String> {

    private final transient boolean charClass;
    private final transient boolean escaped;

    static final Set<String> allowedClasses = new HashSet<>(
            Arrays.asList("\\w", "\\d", ".", "\\b", "\\s")
    );

    public Constant(int value) {
        super(String.valueOf(value));
        charClass = escaped = false;
    }

    public Constant(String value) {
        super(value);
        charClass = allowedClasses.contains(value);
        escaped = value.startsWith("\\");
    }

    public Constant(Constant constant) {
        super(constant);
        charClass = constant.charClass;
        escaped = constant.escaped;
    }


    @Override
    public void describe(StringBuilder builder, DescriptionContext context, RegexFlavour flavour) {
        builder.append(value);
    }

    @Override
    public Leaf cloneTree() {
        Constant clone = new Constant(this);
        return clone;
    }





    @Override
    public boolean isCharacterClass() {
        return charClass;
    }

    @Override
    public boolean isEscaped() {
        return escaped;
    }


}
