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

/**
 *
 * @author MaleLabTs
 */
public class RegexRange extends Leaf<String> {





    public RegexRange(String r) {
        super(r);
    }
    public RegexRange(RegexRange r) {
        super(r);
    }

    @Override
    public void describe(StringBuilder builder, DescriptionContext context, RegexFlavour flavour) {
        builder.append(value);
    }

    @Override
    public Leaf cloneTree() {
        RegexRange clone = new RegexRange(this); 
        return clone;
    }


    /**
     * Valido solo se sono all'interno di ListMatch o ListNotMatch
     * @return
     */
    @Override
    public boolean isValid() {
        return false;
    }


    @Override
    public boolean isCharacterClass() {
        return true;
    }


    
}
