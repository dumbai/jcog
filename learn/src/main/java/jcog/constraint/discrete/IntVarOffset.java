/*
 * Copyright 2016, Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcog.constraint.discrete;


import jcog.constraint.discrete.propagation.PropagationQueue;
import jcog.constraint.discrete.propagation.Propagator;
import jcog.constraint.discrete.trail.Trail;

/** */
public class IntVarOffset extends IntVar {

    private final IntVar variable;
    private final int offset;

    public IntVarOffset(IntVar variable, int offset) {
        this.variable = variable;
        this.offset = offset;
    }

    @Override
    public PropagationQueue propagQueue() {
        return variable.propagQueue();
    }

    @Override
    public Trail trail() {
        return variable.trail();
    }

    @Override
    public int min() {
        return variable.min() + offset;
    }

    @Override
    public int max() {
        return variable.max() + offset;
    }

    @Override
    public int size() {
        return variable.size();
    }

    @Override
    public boolean isAssigned() {
        return variable.isAssigned();
    }

    @Override
    public boolean contains(int value) {
        return variable.contains(value - offset);
    }

    @Override
    public boolean assign(int value) {
        return variable.assign(value - offset);
    }

    @Override
    public boolean remove(int value) {
        return variable.remove(value - offset);
    }

    @Override
    public boolean updateMin(int value) {
        return variable.updateMin(value - offset);
    }

    @Override
    public boolean updateMax(int value) {
        return variable.updateMax(value - offset);
    }

    @Override
    public int copyDomain(int[] array) {
        int size = variable.copyDomain(array);
        for (int i = 0; i < size; i++)
            array[i] += offset;
        return size;
    }

    @Override
    public void watchChange(Propagator propagator) {
        variable.watchChange(propagator);
    }

    @Override
    public void watchAssign(Propagator propagator) {
        variable.watchAssign(propagator);
    }

    @Override
    public void watchBounds(Propagator propagator) {
        variable.watchBounds(propagator);
    }
}
