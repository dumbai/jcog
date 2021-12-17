/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package jcog.data.map;


import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * TODO this is untested
 *
 * TODO it would be better to use a plain Array as the cached
 * "linked list" but this must be synchronized with the
 * ConcurrentHashMap's modifications..
 * which is possible but requires more care than what is done here
 */
public class ConcurrentFastIteratingHashSet<T> extends AbstractSet<T> {

    final ConcurrentFastIteratingHashMap<T,T> map;



    public ConcurrentFastIteratingHashSet(T[] emptyArray) {
         map = new ConcurrentFastIteratingHashMap<>(emptyArray);
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        map.forEachValue(action);
    }

    public <Z> void forEachWith(BiConsumer<? super T, Z> action, Z z) {
        map.forEachValueWith(action, z);
    }


    @Override
    public Iterator<T> iterator() {
        return map.valueIterator();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean add(T t) {
        return map.putIfAbsent(t, t)==null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o)!=null;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) {
            add(t);
        }
        return true; 
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            remove(o);
        }
        return true; 
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return map.removeIf(filter);
    }

    @Override
    public <U> U[] toArray(U[] a) {
        return super.toArray(a);
    }

    public List<T> asList() {
        return map.asList();
    }

    public T get(Random rng) {
        T[] a = map.valueArray();
        return a.length == 0 ? null : a[Math.abs(rng.nextInt(a.length))];
    }

}