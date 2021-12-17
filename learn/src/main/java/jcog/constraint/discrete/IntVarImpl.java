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
import jcog.constraint.discrete.trail.TrailedInt;
import jcog.data.list.Lst;

/**
 * A sparse set based implementation of IntVar
 * <p>
 * <p>
 * This class implements {@code IntVar} with a sparse set representation.
 * <p>
 * Reference:
 * -
 * -
 * </p>
 */
public class IntVarImpl extends IntVar {

    private final PropagationQueue pQueue;
    private final Trail trail;

    private final int initMin;
    private final int initMax;

    private final TrailedInt minT;
    private final TrailedInt maxT;
    private final TrailedInt sizeT;

    private final int[] values;
    private final int[] positions;

    private final Lst<Propagator> changeWatchers = new Lst<>();
    private final Lst<Propagator> assignWatchers = new Lst<>();
    private final Lst<Propagator> boundsWatchers = new Lst<>();

    public IntVarImpl(PropagationQueue pQueue, Trail trail, int initMin, int initMax) {
        this.pQueue = pQueue;
        this.trail = trail;
        this.initMin = initMin;
        this.initMax = initMax;
        this.minT = new TrailedInt(trail, initMin);
        this.maxT = new TrailedInt(trail, initMax);
        int size = initMax - initMin + 1;
        this.sizeT = new TrailedInt(trail, size);
        this.values = makeInt(size, i -> i + initMin);
        this.positions = makeInt(size, i -> i);
    }

    public IntVarImpl(PropagationQueue pQueue, Trail trail, int[] values) {
        this.pQueue = pQueue;
        this.trail = trail;
        this.values = values.clone();
        this.sizeT = new TrailedInt(trail, values.length);

        
        int min = MAX_VALUE;
        int max = MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            min = Math.min(min, values[i]);
            max = Math.max(max, values[i]);
        }
        this.initMin = min;
        this.initMax = max;
        this.minT = new TrailedInt(trail, initMin);
        this.maxT = new TrailedInt(trail, initMax);

        
        int range = max - min + 1;
        this.positions = makeInt(range, i -> range);
        for (int i = 0; i < values.length; i++) {
            this.positions[values[i] - initMin] = i;
        }
    }

    @Override
    public PropagationQueue propagQueue() {
        return pQueue;
    }

    @Override
    public Trail trail() {
        return trail;
    }

    @Override
    public int min() {
        return minT.getValue();
    }

    @Override
    public int max() {
        return maxT.getValue();
    }

    @Override
    public int size() {
        return sizeT.getValue();
    }

    @Override
    public boolean isAssigned() {
        return sizeT.getValue() == 1;
    }

    @Override
    public boolean contains(int value) {
        if (value < initMin || value > initMax) {
            return false;
        }
        return positions[value - initMin] < sizeT.getValue();
    }

    private void swap(int pos1, int pos2) {
        int v1 = values[pos1];
        int v2 = values[pos2];
        values[pos1] = v2;
        values[pos2] = v1;
        int id1 = v1 - initMin;
        positions[id1] = pos2;
        int id2 = v2 - initMin;
        positions[id2] = pos1;
    }

    @Override
    public boolean assign(int value) {
        if (value < minT.getValue() || value > maxT.getValue()) {
            return false;
        }
        int size = sizeT.getValue();

        
        if (size == 1) {
            return true;
        }

        
        int position = positions[value - initMin];
        if (position >= size) {
            return false;
        }

        
        swap(position, 0);
        minT.setValue(value);
        maxT.setValue(value);
        sizeT.setValue(1);
        awakeAssign();
        awakeBounds();
        awakeChange();
        return true;
    }

    @Override
    public boolean remove(int value) {
        
        
        int min = minT.getValue();
        int max = maxT.getValue();
        if (value < min || value > max) {
            return true;
        }

        
        
        int size = sizeT.getValue();
        if (size == 1) {
            return false;
        }

        
        int position = positions[value - initMin];
        if (position >= size) {
            return true;
        }

        
        
        size--;
        swap(position, size);
        sizeT.setValue(size);

        
        
        if (size == 1) {
            
            if (value == min) {
                minT.setValue(max);
            } else {
                maxT.setValue(min);
            }
            awakeAssign();
            awakeBounds();
        } else if (min == value) {
            
            int i = min - initMin + 1;
            while (positions[i] >= size) {
                i++;
            }
            minT.setValue(i + initMin);
            awakeBounds();
        } else if (max == value) {
            
            int i = max - initMin - 1;
            while (positions[i] >= size) {
                i--;
            }
            maxT.setValue(i + initMin);
            awakeBounds();
        }
        awakeChange();
        return true;
    }

    @Override
    public boolean updateMin(int value) {
        int max = maxT.getValue();
        if (value == max) {
            return assign(value);
        }
        if (max < value) {
            return false;
        }
        int min = minT.getValue();
        if (value <= min) {
            return true;
        }
        
        int i = min - initMin;
        int size = sizeT.getValue();
        while (i < value - initMin) {
            int position = positions[i];
            if (position < size) {
                swap(position, --size);
            }
            i++;
        }
        
        while (size <= positions[i]) {
            i++;
        }

        
        minT.setValue(i + initMin);
        sizeT.setValue(size);

        
        if (size == 1) {
            awakeAssign();
        }
        awakeBounds();
        awakeChange();
        return true;
    }

    @Override
    public boolean updateMax(int value) {
        int min = minT.getValue();
        if (value == min) {
            return assign(value);
        }
        if (min > value) {
            return false;
        }
        int max = maxT.getValue();
        if (value >= max) {
            return true;
        }
        
        int i = max - initMin;
        int size = sizeT.getValue();
        while (i > value - initMin) {
            int position = positions[i];
            if (position < size) {
                swap(position, --size);
            }
            i--;
        }
        
        while (size <= positions[i]) {
            i--;
        }

        
        maxT.setValue(i + initMin);
        sizeT.setValue(size);

        
        if (size == 1) {
            awakeAssign();
        }
        awakeBounds();
        awakeChange();
        return true;
    }

    @Override
    public int copyDomain(int[] array) {
        int size = sizeT.getValue();
        System.arraycopy(values, 0, array, 0, size);
        return size;
    }

    @Override
    public void watchChange(Propagator propagator) {
        changeWatchers.add(propagator);
    }

    @Override
    public void watchAssign(Propagator propagator) {
        boundsWatchers.add(propagator);
    }

    @Override
    public void watchBounds(Propagator propagator) {
        changeWatchers.add(propagator);
    }

    private void awakeAssign() {
        assignWatchers.forEach(pQueue::add);
    }

    private void awakeBounds() {
        boundsWatchers.forEach(pQueue::add);
    }

    private void awakeChange() {
        changeWatchers.forEach(pQueue::add);
    }
}
