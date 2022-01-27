/*
 * Copyright (c) 2013, SRI International
 * All rights reserved.
 * Licensed under the The BSD 3-Clause License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http:
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * Redistributions of source code must retain the above copyright
 * notice, this arrayList of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright
 * notice, this arrayList of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the aic-expresso nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jcog.data.set;

import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * A {@link Set} interface enriched with random access methods and a list iterator.
 * @author braz
 *
 * @param <X> the type of the elements.
 * from: https:
 */
public interface ArraySet<X> extends Set<X> {

	ListIterator<X> listIterator();

	ListIterator<X> listIterator(int index);

	X get(int index);



	default X first() {
		return isEmpty() ? null : get(0);
	}

	default X get(Random random) {
		int s = size();
        return switch (s) {
            case 0 -> null;
            case 1 -> get(0);
            default -> get(random.nextInt(s));
        };
	}

	X remove(RandomGenerator random);



//	default X roulette(FloatFunction<X> rank, Random rng) {
//		int s = size();
//		assert(s > 0);
//		float[] weights = new float[s];
//		for (int i = 0; i < s; i++) {
//			weights[i] = rank.floatValueOf(get(i));
//		}
//		return get(Roulette.selectRoulette(weights, rng));
//	}

	/** shuffles the list */
	void shuffle(RandomGenerator random);


}