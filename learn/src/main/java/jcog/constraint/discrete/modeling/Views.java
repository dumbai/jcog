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
package jcog.constraint.discrete.modeling;

import jcog.constraint.discrete.IntVar;
import jcog.constraint.discrete.IntVarOffset;
import jcog.constraint.discrete.IntVarOpposite;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import java.util.HashMap;

public enum Views {
	;


	private static final HashMap<IntVar, IntVar> oppositeViews = new HashMap<>();

    
    private static final HashMap<Pair<IntVar, Integer>, IntVar> offsetViews = new HashMap<>();

    public static IntVar opposite(IntVar x) {
        IntVar view = oppositeViews.get(x);
        if (view == null) {
            view = new IntVarOpposite(x);
            oppositeViews.put(x, view);
            oppositeViews.put(view, x);
        }
        return view;
    }

    public static IntVar offset(IntVar x, int k) {
        Pair<IntVar, Integer> t = Tuples.pair(x, k);
        IntVar view = offsetViews.get(t);
        if (view == null) {
            view = new IntVarOffset(x, k);
            offsetViews.put(t, view);
            t = Tuples.pair(view, -k);
            offsetViews.put(t, x);
        }
        return view;
    }
}
