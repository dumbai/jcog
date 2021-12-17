package jcog.signal.anomaly.adwin;

import java.util.Iterator;

/**
 * Datastructure for the exponential histogram used in ADWIN.
 *
 */
class AdwinHisto {

    private final int capacity;

    private final BucketContainer head;
    private BucketContainer tail;

    private int numBucketContainers;
    private int numBuckets;
    private int numElements;

    private double sum;
    private double variance;

    /**
     * Creates a new empty Histogram
     * @param capacity max number of Buckets in one bucket container. default is 5
     */
    AdwinHisto(int capacity) {
        if (capacity < 2) {
            throw new IllegalArgumentException("maxBucketsPerBucketContainer must be at least 2.");
        }

        this.head = new BucketContainer(null, null, capacity, 1);
        this.tail = this.head;
        this.numBucketContainers = 1;
        this.capacity = capacity;

    }

    /**
     * Private copy constructor. Used in the copy method.
     * @param original
     */
    private AdwinHisto(AdwinHisto original) {
        this.capacity = original.capacity;

        this.numBucketContainers = original.numBucketContainers;
        this.numBuckets = original.numBuckets;
        this.numElements = original.numElements;

        this.sum = original.sum;
        this.variance = original.variance;


        this.head = original.head.deepCopy();
        BucketContainer currentBucketContainer = this.head, next;
        while ((next = (currentBucketContainer.next())) != null) {
            currentBucketContainer = next;
        }
        // next == null;
        this.tail = currentBucketContainer;
    }

    public void add(double element) {

        head.addBucket(new Bucket(element, 0, 1));
        numBuckets++;
        numElements++;
        if (numElements > 1) {
            variance += (numElements - 1) * Math.pow(element - sum / (numElements - 1), 2) / numElements;
        }
        sum += element;
        compress();
    }

    private void addEmptyTailBucketContainer() {
        BucketContainer newTail = new BucketContainer(tail, null, capacity, tail.elemsPerBucket() * 2);
        tail.setNext(newTail);
        tail = newTail;
        numBucketContainers++;
    }

    private void compress() {
        BucketContainer pointer = head;
        while (pointer != null) {
//            BucketContainer n = pointer.next();
            if (pointer.size() == pointer.capacity()) {
                if (pointer.next() == null) {
                    addEmptyTailBucketContainer();
                }
                Bucket[] removedBuckets = pointer.removeBuckets(2);
                pointer.next().addBucket(
                    compressBuckets(removedBuckets[0], removedBuckets[1])
                );
                numBuckets -= 1;
            }
            pointer = pointer.next();
        }
    }

    private static Bucket compressBuckets(Bucket firstBucket, Bucket secondBucket) {
        assert firstBucket.size() == secondBucket.size();
        int elementsPerBucket = firstBucket.size();
        double firstSum = firstBucket.sum();
        double secondSum = secondBucket.sum();
        double newTotal = firstSum + secondSum;
        double varianceIncrease = Math.pow(firstSum - secondSum, 2) / (2 * elementsPerBucket);
        double newVariance = firstBucket.variance() + secondBucket.variance() + varianceIncrease;
        return new Bucket(newTotal, newVariance, 2 * elementsPerBucket);
    }

    public void removeBuckets(int num) {
        while (num > 0) {
            Bucket[] removedBuckets;
            int s = tail.size();
            if (num >= s) {
                num -= s;
                removedBuckets = tail.removeBuckets(s);
                tail = tail.prev();
                tail.setNext(null);
                numBucketContainers--;
            } else {
                removedBuckets = tail.removeBuckets(num);
                num = 0;
            }
            for (Bucket bucket : removedBuckets) {
                int bs = bucket.size();
                numElements -= bs;
                numBuckets--;
                sum -= bucket.sum();
                variance -= bucket.variance() + bs * numElements * Math.pow(bucket.mean() - sum / numElements, 2) / (bs + numElements);
            }
        }
    }

    public double sum() {
        return sum;
    }

    public double variance() {
        return variance;
    }

    public int size() {
        return numElements;
    }

    public Iterator<Bucket> reverseBuckets() {
        return new ReverseBucketIterator();
    }

    public Iterator<Bucket> forwardBuckets() {
        return new ForwardBucketIterator();
    }

    public int buckets() {
        return numBuckets;
    }

    /**
     * Creates a deep copy of the histogram.
     * @return new Histogram.
     */
    public AdwinHisto copy() {
        return new AdwinHisto(this);
    }

    public double mean() {
        return sum()/size();
    }

    private final class ReverseBucketIterator implements Iterator<Bucket> {

        BucketContainer currentBucketContainer = tail;
        int currentBucketIndex = tail.size();

        @Override
        public boolean hasNext() {
            return (currentBucketIndex > 0) || (currentBucketContainer.prev() != null);
        }

        @Override
        public Bucket next() {
            if (--currentBucketIndex < 0) {
                currentBucketContainer = currentBucketContainer.prev();
                currentBucketIndex = currentBucketContainer.size() - 1;
            }
            return currentBucketContainer.bucket(currentBucketIndex);
        }
    }

    private final class ForwardBucketIterator implements Iterator<Bucket> {

        BucketContainer currentBucketContainer = head;
        int currentBucketIndex = -1;

        @Override
        public boolean hasNext() {
            return (currentBucketIndex < currentBucketContainer.size() - 1) || (currentBucketContainer.next() != null);
        }

        @Override
        public Bucket next() {
            currentBucketIndex++;
            if (currentBucketIndex > currentBucketContainer.size() - 1) {
                currentBucketContainer = currentBucketContainer.next();
                currentBucketIndex = 0;
            }
            return currentBucketContainer.bucket(currentBucketIndex);
        }
    }
}