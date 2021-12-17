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
package jcog.grammar.evolve.utils;

import jcog.grammar.evolve.tree.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author MaleLabTs
 */
public class UniqueList<E> extends ArrayList<Node> {

    private final Set<String> hashes;

    public UniqueList(int initialCapacity) {
        super(initialCapacity);
        hashes = new HashSet<>();
    }        

    @Override
    public boolean add(Node e) {
        StringBuilder builder = new StringBuilder();
        e.describe(builder);
        String hash = builder.toString();
        if (hashes.add(hash)) {
            return super.add(e);
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Node> c) {
        boolean ret = false;
        for(Node n:c){
            ret = this.add(n) || ret;
        }
        return ret;
    }
    
    
    
}
