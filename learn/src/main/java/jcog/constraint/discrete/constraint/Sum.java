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
package jcog.constraint.discrete.constraint;

import jcog.constraint.discrete.IntVar;
import jcog.constraint.discrete.propagation.Propagator;
import jcog.constraint.discrete.trail.TrailedInt;

public class Sum extends Propagator {

    private final IntVar sum;
    private final IntVar[] assigned;
    private final TrailedInt nAssignedT;
    private final TrailedInt sumAssignedT;

    public Sum(IntVar[] variables, IntVar sum, int offset) {
        this.sum = sum;
        this.assigned = variables.clone();
        this.nAssignedT = new TrailedInt(sum.trail(), 0);
        this.sumAssignedT = new TrailedInt(sum.trail(), offset);
    }

    @Override
    public boolean setup() {
        sum.watchBounds(this);
        for (int i = 0; i < assigned.length; i++) {
            assigned[i].watchBounds(this);
        }
        return propagate();
    }

    @Override
    public boolean propagate() {
        
        int nAssigned = nAssignedT.getValue();
        int sumAssigned = sumAssignedT.getValue();

        
        boolean reduce = true;
        while (reduce) {
            reduce = false;

            int sumTermsMin = sumAssigned;
            int sumTermsMax = sumAssigned;
            int maxDiff = 0;

            
            
            for (int i = nAssigned; i < assigned.length; i++) {
                IntVar term = assigned[i];
                int min = term.min();
                int max = term.max();
                sumTermsMin += min;
                sumTermsMax += max;
                int diff = max - min;
                if (diff == 0) {
                    sumAssigned += min;
                    
                    
                    assigned[i] = assigned[nAssigned];
                    assigned[nAssigned] = term;
                    nAssigned++;
                    continue;
                }
                maxDiff = Math.max(maxDiff, diff);
            }

            
            
            if (!sum.updateMin(sumTermsMin))
                return false;
            if (!sum.updateMax(sumTermsMax))
                return false;

            
            
            int sumMax = sum.max();
            int sumMin = sum.min();

            if (sumTermsMax - maxDiff < sumMin) {
                for (int i = nAssigned; i < assigned.length; i++) {
                    IntVar term = assigned[i];
                    int oldMin = term.min();
                    int newMin = sumMin - sumTermsMax + term.max();
                    if (newMin > oldMin) {
                        if (!term.updateMin(newMin))
                            return false;
                        reduce |= newMin != term.min();
                    }
                }
            }

            if (sumTermsMin - maxDiff > sumMax) {
                for (int i = nAssigned; i < assigned.length; i++) {
                    IntVar term = assigned[i];
                    int oldMax = term.max();
                    int newMax = sumMax - sumTermsMin + term.min();
                    if (newMax < oldMax) {
                        if (!term.updateMax(newMax))
                            return false;
                        reduce |= newMax != term.max();
                    }
                }
            }
        }

        
        nAssignedT.setValue(nAssigned);
        sumAssignedT.setValue(sumAssigned);
        return true;
    }
}
