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


import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author MaleLabTs
 */
public abstract class Leaf<V> extends AbstractNode {


   private ParentNode parent;

   protected final V value;


   public abstract Leaf cloneTree();

   public Leaf(V value) {
      super();
      this.value = value;
      this.hash = 31 * value.hashCode() + getClass().hashCode();
   }

   public Leaf(Leaf<V> copy) {
      super();
      this.value = copy.value;
      this.hash = copy.hash;
   }

   @Override
   public void setParent(ParentNode parent) {
      this.parent = parent;
   }

   @Override
   public List<Node> children() {
      return Collections.EMPTY_LIST;
   }

   @Override
   public ParentNode getParent() {
      return parent;
   }

   @Override
   public final int getMinChildrenCount() {
      return 0;
   }

   @Override
   public final int getMaxChildrenCount() {
      return 0;
   }


   @Override
   public boolean isValid() {
      return true;
   }

   @Override
   public final String toString() {
      return value.toString();
   }

   @Override
   public final int hashCode() {
      return hash;
   }

   @Override
   public final boolean equals(Object obj) {
      if (obj == this) return true;
      return getClass() == obj.getClass() &&
              this.hash == ((Leaf)obj).hash &&
              Objects.equals(this.value, ((Leaf)obj).value);
   }

}