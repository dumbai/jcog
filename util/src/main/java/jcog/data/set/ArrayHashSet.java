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

import jcog.TODO;
import jcog.Util;
import jcog.WTF;
import jcog.data.list.Lst;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

import static java.util.Collections.EMPTY_SET;

/**
 * Analogous to {@link LinkedHashSet}, but with an {@link ArrayList} instead of a {@link LinkedList},
 * offering the same advantages (random access) and disadvantages (slower addition and removal of elements),
 * but with the extra advantage of offering an iterator that is actually a {@link ListIterator}.
 *
 * @param <X> the type of the elements
 *            <p>
 *            from: https:
 * @author braz
 *
 * TODO configurable min,max capacity to determine when to free or just clear an internal collection
 */
public class ArrayHashSet<X> extends AbstractSet<X> implements ArraySet<X> {


    private static final int DEFAULT_SET_CAPACITY = 1;
    public final Lst<X> list;
    public Set<X> set = EMPTY_SET;


    public ArrayHashSet() {
        this(DEFAULT_SET_CAPACITY);
    }

    public ArrayHashSet(int capacity) {
        this(new Lst<>(capacity));
    }

    public ArrayHashSet(Lst<X> list) {
        this.list = list;
    }


    /** unordered equality via set */
    @Override public boolean equals(Object o) {
        return this == o || set.equals(((ArrayHashSet)o).set); //TODO fix for non-ArrayHashSet
    }

    /** unordered equality via set */
    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @SafeVarargs
    public final boolean addAll(Collection<? extends X>... C) {
        boolean acc = false;
        for (Collection c : C)
            acc |= addAll(c);
        return acc;

    }

    @Override
    public boolean addAll(Collection<? extends X> c) {
        //throw new TODO("optimized bulk addAll");
        boolean acc = false;
        for (X x : c)
            acc |= add(x);
        return acc;
    }

    @SafeVarargs
    public final boolean addAll(X... c) {
        //throw new TODO("optimized bulk addAll");
        boolean acc = false;
        for (X x : c)
            acc |= add(x);
        return acc;
    }

    @Override
    public boolean removeAll(Collection c) {
        //throw new TODO("optimized bulk addAt");
        boolean rem = false;
        for (Object x : c)
            rem |= remove(x);
        return rem;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super X> filter) {
        switch(size()) {
            case 0:
                return false;
            case 1: {
                if (filter.test(get(0))) {
                    clear();
                    return true;
                } else
                    return false;
            }
            default: {
                return list.removeIf(x -> {
                    if (filter.test(x)) {
                        boolean setRemoved = set.remove(x); assert(setRemoved);
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new TODO();
    }

    @Override
    public void forEach(Consumer<? super X> action) {
        list.forEach(action);
    }

    @Override
    public final Stream<X> stream() {
        return list.stream();
    }

    @Override
    public ListIterator<X> listIterator() {
        return size()==0 ? Collections.emptyListIterator() : new ArrayHashSetIterator();
    }

    @Override
    public ListIterator<X> listIterator(int index) {
        return new ArrayHashSetIterator(index);
    }

    public boolean OR(org.eclipse.collections.api.block.predicate.Predicate<? super X> test) {
        return Util.or(test, list);
    }

    int setSize() {
        return set.size();
    }

    public boolean AND(org.eclipse.collections.api.block.predicate.Predicate<? super X> test) {
        return Util.and(test, list);
    }


    @Override
    public X get(int index) {
        return list.get(index);
    }

    @Override
    public boolean add(X x) {
        if (x == null)
            throw new NullPointerException();

        if (list.isEmpty()) {
            if (set instanceof UnifiedSet) {
                //assert(set.isEmpty());
            } else {
                set = newSet(DEFAULT_SET_CAPACITY /*list.capacity()*/);
            }
        }

        if (set.add(x)) {
            addedUnique(x);
            return true;
        } else {
            return false;
        }
    }

    void addedUnique(X x) {
        list.add(x);
    }

    protected Set<X> newSet(int cap) {
        return new UnifiedSet<>(cap);
        //return new HashSet(cap, 0.99f);
    }

    private void clearSet() {
        Set<X> s = this.set;
        this.set = EMPTY_SET;
        s.clear();
    }

    @Override
    public final Iterator<X> iterator() {
        return size() == 0 ? Collections.emptyListIterator() : listIterator();
    }

//    /** use if remove() not needed */
//    public final Iterator<X> iteratorReadOnly() {
//        int s = size();
//        return switch (s) {
//            case 0 -> Collections.emptyListIterator();
//            case 1 -> new SingletonIterator(get(0));
//            default -> ArrayIterator.iterateN(list.array(), s);
//        };
//    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        int s = size();
        if (s == 0) return false;
        boolean removed = set.remove(o);
        if (removed) {
            if (s == 1) {
                set = EMPTY_SET;
                list.clear();
            } else {
                list.remove(o);
            }
        }

        return removed;
    }


    @Override
    public final void clear() {
        if (list.clearIfChanged())
            clearSet();
    }


    @Override
    public X remove(RandomGenerator random) {
        int s = size();
        if (s == 0) return null;
        int index = s == 1 ? 0 : random.nextInt(s);
        X removed;
        removed = list.remove(index);
        set.remove(removed);
        return removed;
    }

    @Override
    public void shuffle(RandomGenerator random) {
        list.shuffleThis(random);
        //Collections.shuffle(list, random);
    }


    /** removes the last item in the list, or null if empty */
    public @Nullable X poll() {
        X x = list.poll();
        if (x != null)
            set.remove(x);
        return x;
    }

    public void replace(int i, X y) {
        set.remove(list.get(i));
        list.setFast(i, y);
        set.add(y);
    }

    private final class ArrayHashSetIterator implements ListIterator<X> {

        private final ListIterator<X> i;
        private X current;

        ArrayHashSetIterator() {
            this(-1);
        }

        ArrayHashSetIterator(int index) {
            this.i = index == -1 ? list.listIterator() : list.listIterator(index);
        }

        @Override
        public boolean hasNext() {
            return i.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return i.hasPrevious();
        }

        @Override
        public X next() {
            return current = i.next();
        }

        @Override
        public X previous() {
            return current = i.previous();
        }

        @Override
        public int nextIndex() {
            return i.nextIndex();
        }

        @Override
        public int previousIndex() {
            return i.previousIndex();
        }

        @Override
        public void add(X element) {
            if (set == null)
                throw new TODO();

            if (set.add(element))
                i.add(element);
        }

        @Override
        public void remove() {

            i.remove();
            X lastElementProvided = current;

            boolean removed = set.remove(lastElementProvided);
            if (!removed)
                throw new WTF();


            if (set.isEmpty()) {
                set = EMPTY_SET;
            }

            current = null;


        }

        @Override
        public void set(X next) {
            if (current == next)
                return;

            i.remove();
            X prev = current;

            if (!set.remove(prev))
                throw new IllegalArgumentException("Cannot set already-present element in a different position in ArrayHashSet.");

            if (set.add(next))
                i.add(next);
        }

    }

//    protected boolean consistent() {
//        return new UnifiedSet(list).equals(set);
//    }

    //
//    public static final ArrayHashSet EMPTY = new ArrayHashSet(0) {
//        @Override
//        public boolean add(Object x) {
//            throw new UnsupportedOperationException();
//        }
//
//        @Override
//        public Object first() {
//            return null;
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return true;
//        }
//
//        @Override
//        public int size() {
//            return 0;
//        }
//
//        @Override
//        public ListIterator listIterator() {
//            return Collections.emptyListIterator();
//        }
//
//        @Override
//        public ListIterator listIterator(int index) {
//            assert (index == 0);
//            return Collections.emptyListIterator();
//        }
//    };
}