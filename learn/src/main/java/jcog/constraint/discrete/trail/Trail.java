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
package jcog.constraint.discrete.trail;

import jcog.data.list.Lst;
import org.eclipse.collections.impl.stack.mutable.primitive.IntArrayStack;

/**
 * {@code Trail} contains the chronological sequences of changes to undo.
 */
public class Trail {

    /**
     * undo's
     * <p>
     * <p>
     * A {@code Change} represents any kind of undoable operation that affects the
     * state of the solver, its variables, or its propagators. A {@code Change} is
     * typically trailed and undone when a backtrack occurs.
     * </p>
     */
    private final Lst<Runnable> changes = new Lst<>();

    private final IntArrayStack levels = new IntArrayStack();
    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void store(Runnable change) {
        changes.add(change);
    }

    public void newLevel() {
        levels.push(changes.size());
        timestamp++;
    }

    public void undoLevel() {
        if (!levels.isEmpty())
            undoUntil(levels.pop());
        timestamp++;
    }

    public void undoAll() {
        while (!levels.isEmpty()) {
            undoUntil(levels.pop());
        }
        timestamp++;
    }

    private void undoUntil(int size) {
        while (changes.size() > size) {
            changes.removeLast().run();
        }
    }
}
