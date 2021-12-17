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
public abstract class AbstractNode extends Node {


    private final long id;

    protected transient int hash = 0;


    @Override
    public final long getId() {
        return id;
    }

    public AbstractNode() {
        id = IDFactory.nextID();
    }


    @Override
    public void describe(StringBuilder builder) {
        describe(builder, new DescriptionContext(), RegexFlavour.JAVA);
    }
    
    @Override
    public boolean isCharacterClass(){
        return false;
    }
    
    @Override
    public boolean isEscaped(){
        return false;
    }



}
