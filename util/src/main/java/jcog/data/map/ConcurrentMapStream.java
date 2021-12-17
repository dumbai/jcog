///*
// * Copyright (C) 2011 Clearspring Technologies, Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http:
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package jcog.data.map;
//
//import com.google.common.util.concurrent.AtomicDouble;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// *
// * MODIFIED FROM stream-lib's ConcurrentStreamSummary UNTESTED
// *
// * Based on the <i>Space-Saving</i> algorithm and the <i>Stream-Summary</i>
// * data structure as described in:
// * <i>Efficient Computation of Frequent and Top-k Elements in Data Streams</i>
// * by Metwally, Agrawal, and Abbadi
// * <p/>
// * Ideally used in multithreaded applications, otherwise see {@link StreamSummary}
// *
// * @param <V> type of data in the stream to be summarized
// * @author Eric Vlaanderen
// *
// *
// */
//public class ConcurrentMapStream<K, V> extends ConcurrentHashMap<K, ConcurrentMapStream.RankedItem<V>> {
//
//    private final int capacity;
//    private final AtomicReference<RankedItem<V>> minVal;
//    private final AtomicLong size;
//    private final AtomicBoolean reachCapacity;
//
//    public ConcurrentMapStream(int capacity) {
//        this.capacity = capacity;
//        this.minVal = new AtomicReference<>();
//        this.size = new AtomicLong(0);
//        this.reachCapacity = new AtomicBoolean(false);
//    }
//
//    public static class RankedItem<T> extends AtomicDouble implements Comparable<RankedItem<T>> {
//
//
//
//        public final T the;
//
//        public RankedItem(T item) {
//            super(Double.NaN);
//            this.the = item;
//
//
//        }
//
//
//        public double addAndGetCount(double delta) {
//            return this.addAndGet(delta);
//        }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        @Override
//        public int compareTo(RankedItem<T> o) {
//            return Double.compare(o.doubleValue(), doubleValue());
//        }
//
//        public String toString() {
//
//
//            String sb = String.valueOf(this.the) +
//                    ':' +
//                    this.doubleValue();
//            return sb;
//        }
//
//        public boolean isNewItem() {
//            double d = get();
//            return d!=d;
//        }
//
//
//
//
//    }
//
//
//    /** returns the existing value or null if it was inserted */
//    public V put(K key, V element, double incrementCount) {
//
//        V result;
//        RankedItem<V> value = new RankedItem<>(element);
//        RankedItem<V> oldVal = putIfAbsent(key, value);
//        if (oldVal != null) {
//            oldVal.addAndGetCount(incrementCount);
//            result = oldVal.the;
//        } else if (reachCapacity.get() || size.incrementAndGet() > capacity) {
//            reachCapacity.set(true);
//
//            RankedItem<V> oldMinVal = minVal.getAndSet(value);
//            remove(oldMinVal.the);
//
//            while (oldMinVal.isNewItem()) {
//
//
//            }
//            double count = oldMinVal.doubleValue();
//
//            value.addAndGetCount(count);
//
//            result = null;
//        } else {
//            result = element;
//        }
//
//        value.set(incrementCount);
//        minVal.set(getMinValue());
//
//        return result;
//    }
//
//    private RankedItem<V> getMinValue() {
//        RankedItem<V> minVal = null;
//        for (RankedItem<V> entry : values()) {
//            if (minVal == null || (!entry.isNewItem() && entry.doubleValue() < minVal.doubleValue())) {
//                minVal = entry;
//            }
//        }
//        return minVal;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append('[');
//        for (RankedItem entry : values()) {
//            sb.append('(').append(entry.doubleValue()).append(": ").append(entry.the).append("),");
//        }
//        sb.deleteCharAt(sb.length() - 1);
//        sb.append(']');
//        return sb.toString();
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//}
