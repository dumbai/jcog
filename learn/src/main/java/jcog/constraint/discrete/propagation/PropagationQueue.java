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
package jcog.constraint.discrete.propagation;

import java.util.ArrayDeque;

/**
 * PropagQueue
 */
public class PropagationQueue {

    
    
    
    private final ArrayDeque<Propagator> queue = new ArrayDeque<>();

    /**
     * Enqueues the propagator for propagation.
     * <p>
     * <p>
     * This method does nothing if the propagator is already enqueued.
     * </p>
     *
     * @param propagator the propagator to be scheduled for propagation.
     */
    public void add(Propagator propagator) {
        if (!propagator.enqueued) {
            queue.addLast(propagator);
            propagator.enqueued = true;
        }
    }

    /**
     * Propagates the pending propagators.
     * <p>
     * <p>
     * Propagate all the propagators contained in the propagation queue.
     * Propagation is likely to enqueue additional propagators while it is
     * running. The propagation stops either when the queue is empty, or if a
     * propagator failed (meaning that the problem is infeasible).
     * </p>
     * <p>
     * <p>
     * The method returns true if the propagation succeeded or false if a
     * propagator failed. The queue is empty at the end of the propagation and all
     * propagators contained in the queue have their enqueued boolean set to
     * false.
     * </p>
     *
     * @return true if propagation succeeded, false otherwise.
     */
    public boolean propagate() {
        boolean feasible = true;
        while (!queue.isEmpty()) {
            Propagator propagator = queue.removeFirst();
            
            
            
            propagator.enqueued = propagator.idempotent;
            
            feasible = feasible && propagator.propagate();
            
            propagator.enqueued = false;
        }
        return feasible;
    }
}
