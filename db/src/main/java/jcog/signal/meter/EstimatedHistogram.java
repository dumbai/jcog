/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcog.signal.meter;

import com.google.common.base.Objects;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.stream.IntStream;


public class EstimatedHistogram {
    

    
    final AtomicLongArray buckets;
    /**
     * The series of values to which the counts in `buckets` correspond:
     * 1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 17, 20, etc.
     * Thus, a `buckets` of [0, 0, 1, 10] would mean we had seen one value of 3 and 10 values of 4.
     * <p>
     * The series starts at 1 and grows by 1.2 each time (rounding and removing duplicates). It goes from 1
     * to around 36M by default (creating 90+1 buckets), which will give us timing resolution from microseconds to
     * 36 seconds, with less precision as the numbers get larger.
     * <p>
     * Each bucket represents values from (previous bucket offset, current offset].
     */
    private final long[] bucketOffsets;






    public EstimatedHistogram(int bucketCount) {
        this(bucketCount, false);
    }

    public EstimatedHistogram(int bucketCount, boolean considerZeroes) {
        bucketOffsets = newOffsets(bucketCount, considerZeroes);
        buckets = new AtomicLongArray(bucketOffsets.length + 1);
    }

    /**
     * Create EstimatedHistogram from only bucket data.
     *
     * @param bucketData bucket data
     */
    public EstimatedHistogram(long[] bucketData) {
        assert bucketData != null && bucketData.length > 0 : "Bucket data must be an array of size more than 0";
        bucketOffsets = newOffsets(bucketData.length - 1, false);
        buckets = new AtomicLongArray(bucketData);
    }

    public EstimatedHistogram(long[] offsets, long[] bucketData) {
        assert bucketData.length == offsets.length + 1;
        bucketOffsets = offsets;
        buckets = new AtomicLongArray(bucketData);
    }

    public static long[] newOffsets(int size, boolean considerZeroes) {
        long[] result = new long[size + (considerZeroes ? 1 : 0)];
        int i = 0;
        if (considerZeroes)
            result[i++] = 0;
        long last = 1;
        result[i++] = last;
        for (; i < result.length; i++) {
            long next = Math.round(last * 1.2);
            if (next == last)
                next++;
            result[i] = next;
            last = next;
        }

        return result;
    }

    private static String nameOfRange(long[] bucketOffsets, int index) {
        StringBuilder sb = new StringBuilder();
        appendRange(sb, bucketOffsets, index);
        return sb.toString();
    }

    private static void appendRange(StringBuilder sb, long[] bucketOffsets, int index) {
        sb.append('[');
        if (index == 0)
            if (bucketOffsets[0] > 0)
                
                
                sb.append('1');
            else
                sb.append("-Inf");
        else
            sb.append(bucketOffsets[index - 1] + 1);
        sb.append("..");
        if (index == bucketOffsets.length)
            sb.append("Inf");
        else
            sb.append(bucketOffsets[index]);
        sb.append(']');
    }

    /**
     * @return the histogram values corresponding to each bucket index
     */
    long[] getBucketOffsets() {
        return bucketOffsets;
    }

    private int findIndex(long n) {
        int index = Arrays.binarySearch(bucketOffsets, n);
        if (index < 0) {
            
            index = -index - 1;
        }
        return index;
    }

    /**
     * Increments the count of the bucket closest to n, rounding UP.
     *
     * @param n
     */
    public void add(long n) {
        buckets.incrementAndGet(findIndex(n));
    }

    /**
     * Increments the count of the bucket closest to n, rounding UP by delta
     *
     * @param n
     */
    public void add(long n, long delta) {
        buckets.addAndGet(findIndex(n), delta);
    }

    /**
     * @return the count in the given bucket
     */
    long get(int bucket) {
        return buckets.get(bucket);
    }

    /**
     * @param reset zero out buckets afterwards if true
     * @return a long[] containing the current histogram buckets
     */
    long[] getBuckets(boolean reset) {
        int len = buckets.length();
        long[] rv;

        long[] arr = new long[10];
        int count = 0;
        int bound = len;
        if (reset) {
            for (int i = 0; i < bound; i++) {
                long andSet = buckets.getAndSet(i, 0L);
                if (arr.length == count) arr = Arrays.copyOf(arr, count * 2);
                arr[count++] = andSet;
            }
        }
        else {
            for (int i = 0; i < bound; i++) {
                long l = buckets.get(i);
                if (arr.length == count) arr = Arrays.copyOf(arr, count * 2);
                arr[count++] = l;
            }
        }
        arr = Arrays.copyOfRange(arr, 0, count);
        rv = arr;

        return rv;
    }

    /**
     * @return the smallest value that could have been added to this histogram
     */
    public long min() {
        int n = buckets.length();
        for (int i = 0; i < n; i++) {
            if (buckets.get(i) > 0)
                return i == 0 ? 0 : 1 + bucketOffsets[i - 1];
        }
        return 0;
    }

    /**
     * @return the largest value that could have been added to this histogram.  If the histogram
     * overflowed, returns Long.MAX_VALUE.
     */
    public long max() {
        int lastBucket = buckets.length() - 1;
        if (buckets.get(lastBucket) > 0)
            return Long.MAX_VALUE;

        for (int i = lastBucket - 1; i >= 0; i--) {
            if (buckets.get(i) > 0)
                return bucketOffsets[i];
        }
        return 0;
    }

    /**
     * @param percentile
     * @return estimated value at given percentile
     */
    public long percentile(double percentile) {
        assert percentile >= 0 && percentile <= 1.0;
        int lastBucket = buckets.length() - 1;
        if (buckets.get(lastBucket) > 0)
            throw new IllegalStateException("Unable to compute when histogram overflowed");

        long pcount = (long) Math.ceil(count() * percentile);
        if (pcount == 0)
            return 0;

        long elements = 0;
        for (int i = 0; i < lastBucket; i++) {
            elements += buckets.get(i);
            if (elements >= pcount)
                return bucketOffsets[i];
        }
        return 0;
    }

    /**
     * @return the ceil of mean histogram value (average of bucket offsets, weighted by count)
     * @throws IllegalStateException if any values were greater than the largest bucket threshold
     */
    public long mean() {
        return (long) Math.ceil(rawMean());
    }

    /**
     * @return the mean histogram value (average of bucket offsets, weighted by count)
     * @throws IllegalStateException if any values were greater than the largest bucket threshold
     */
    public double rawMean() {
        int lastBucket = buckets.length() - 1;
        if (buckets.get(lastBucket) > 0)
            throw new IllegalStateException("Unable to compute ceiling for max when histogram overflowed");

        long elements = 0;
        long sum = 0;
        for (int i = 0; i < lastBucket; i++) {
            long bCount = buckets.get(i);
            elements += bCount;
            sum += bCount * bucketOffsets[i];
        }

        return (double) sum / elements;
    }

    /**
     * @return the total number of non-zero values
     */
    public long count() {
        int bound = buckets.length();
        long sum = IntStream.range(0, bound).mapToLong(buckets::get).sum();
        return sum;
    }

    /**
     * @return the largest bucket offset
     */
    public long getLargestBucketOffset() {
        return bucketOffsets[bucketOffsets.length - 1];
    }

    /**
     * @return true if this histogram has overflowed -- that is, a value larger than our largest bucket could bound was added
     */
    public boolean isOverflowed() {
        return buckets.get(buckets.length() - 1) > 0;
    }

//    /**
//     * log.debug() every record in the histogram
//     *
//     * @param log
//     */
//    public void log(Logger log) {
//
//        int nameCount;
//		nameCount = buckets.get(buckets.length() - 1) == 0 ? buckets.length() - 1 : buckets.length();
//        String[] names = new String[nameCount];
//
//        int maxNameLength = 0;
//        for (int i = 0; i < nameCount; i++) {
//            names[i] = nameOfRange(bucketOffsets, i);
//            maxNameLength = Math.max(maxNameLength, names[i].length());
//        }
//
//
//        String formatstr = "%" + maxNameLength + "s: %d";
//        for (int i = 0; i < nameCount; i++) {
//            long count = buckets.get(i);
//
//
//
//            if (i == 0 && count == 0)
//                continue;
//            log.debug(String.format(formatstr, names[i], count));
//        }
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof EstimatedHistogram that))
            return false;

        return Arrays.equals(getBucketOffsets(), that.getBucketOffsets()) &&
                Arrays.equals(getBuckets(false), that.getBuckets(false));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getBucketOffsets(), getBuckets(false));
    }












































}