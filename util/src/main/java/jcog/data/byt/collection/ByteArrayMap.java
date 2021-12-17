///*
// * Copyright (c) [2016] [ <ether.camp> ]
// * This file is part of the ethereumJ library.
// *
// * The ethereumJ library is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * The ethereumJ library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with the ethereumJ library. If not, see <http:
// */
//package jcog.data.byt.collection;
//
//import jcog.data.byt.RawBytes;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Created by Anton Nashatyrev on 06.10.2016.
// */
//public class ByteArrayMap<V> implements Map<byte[], V> {
//    private final Map<RawBytes, V> delegate;
//
//    public ByteArrayMap() {
//        this(new ConcurrentHashMap<RawBytes, V>());
//    }
//
//    public ByteArrayMap(Map<RawBytes, V> delegate) {
//        this.delegate = delegate;
//    }
//
//    @Override
//    public int size() {
//        return delegate.size();
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return delegate.isEmpty();
//    }
//
//    @Override
//    public boolean containsKey(Object key) {
//        return delegate.containsKey(new RawBytes((byte[]) key));
//    }
//
//    @Override
//    public boolean containsValue(Object value) {
//        return delegate.containsValue(value);
//    }
//
//    @Override
//    public V get(Object key) {
//        return delegate.get(new RawBytes((byte[]) key));
//    }
//
//    @Override
//    public V put(byte[] key, V value) {
//        return delegate.put(new RawBytes(key), value);
//    }
//
//    @Override
//    public V remove(Object key) {
//        return delegate.remove(new RawBytes((byte[]) key));
//    }
//
//    @Override
//    public void putAll(Map<? extends byte[], ? extends V> m) {
//        for (Entry<? extends byte[], ? extends V> entry : m.entrySet()) {
//            delegate.put(new RawBytes(entry.getKey()), entry.getValue());
//        }
//    }
//
//    @Override
//    public void clear() {
//        delegate.clear();
//    }
//
//    @Override
//    public Set<byte[]> keySet() {
//        throw new UnsupportedOperationException();
//
//    }
//
//    @Override
//    public Collection<V> values() {
//        return delegate.values();
//    }
//
//    @Override
//    public Set<Entry<byte[], V>> entrySet() {
//        return new MapEntrySet(delegate.entrySet());
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        return delegate.equals(o);
//    }
//
//    @Override
//    public int hashCode() {
//        return delegate.hashCode();
//    }
//
//    @Override
//    public String toString() {
//        return delegate.toString();
//    }
//
//    private class MapEntrySet implements Set<Map.Entry<byte[], V>> {
//        private final Set<Map.Entry<RawBytes, V>> delegate;
//
//        private MapEntrySet(Set<Entry<RawBytes, V>> delegate) {
//            this.delegate = delegate;
//        }
//
//        @Override
//        public int size() {
//            return delegate.size();
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return delegate.isEmpty();
//        }
//
//        @Override
//        public boolean contains(Object o) {
//            throw new RuntimeException("Not implemented");
//        }
//
//        @Override
//        public Iterator<Entry<byte[], V>> iterator() {
//            final Iterator<Entry<RawBytes, V>> it = delegate.iterator();
//            return new Iterator<Entry<byte[], V>>() {
//
//                @Override
//                public boolean hasNext() {
//                    return it.hasNext();
//                }
//
//                @Override
//                public Entry<byte[], V> next() {
//                    Entry<RawBytes, V> next = it.next();
//                    return new AbstractMap.SimpleImmutableEntry(next.getKey().arrayCompactDirect(), next.getValue());
//                }
//
//                @Override
//                public void remove() {
//                    it.remove();
//                }
//            };
//        }
//
//        @Override
//        public Object[] toArray() {
//            throw new RuntimeException("Not implemented");
//        }
//
//        @Override
//        public <T> T[] toArray(T[] a) {
//            throw new RuntimeException("Not implemented");
//        }
//
//        @Override
//        public boolean addAt(Entry<byte[], V> vEntry) {
//            throw new RuntimeException("Not implemented");
//        }
//
//        @Override
//        public boolean remove(Object o) {
//            throw new RuntimeException("Not implemented");
//        }
//
//        @Override
//        public boolean containsAll(Collection<?> c) {
//            throw new RuntimeException("Not implemented");
//        }
//
//        @Override
//        public boolean addAll(Collection<? extends Entry<byte[], V>> c) {
//            throw new RuntimeException("Not implemented");
//        }
//
//        @Override
//        public boolean retainAll(Collection<?> c) {
//            throw new RuntimeException("Not implemented");
//        }
//
//        @Override
//        public boolean removeAll(Collection<?> c) {
//            throw new RuntimeException("Not implemented");
//        }
//
//        @Override
//        public void clear() {
//            throw new RuntimeException("Not implemented");
//
//        }
//    }
//}