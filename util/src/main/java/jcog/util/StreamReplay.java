package jcog.util;

import jcog.data.list.Lst;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

/** https://github.com/javasync/streamemo/blob/master/src/main/java/org/javasync/streams/Replayer.java */
public enum StreamReplay {
	;

	public static <T> Supplier<Stream<T>> replay(Stream<T> data) {
        return replay(() -> data);
    }

    public static <T> Supplier<Stream<T>> replay(Supplier<Stream<T>> dataSrc) {
        Recorder<T> rec = new Recorder<>(dataSrc);
        return () -> {
            // MemoizeIter starts on index 0 and reads data from srcIter or
            // from an internal mem replay Recorder.
            Spliterator<T> iter = rec.memIterator();
            return stream(iter, false);
        };
    }

    static class Recorder<T> {
        private final Supplier<Stream<T>> dataSrc;
        private Spliterator<T> srcIter;
        private long estimateSize;
        private boolean hasNext = true;
        private List<T> mem;

        Recorder(Supplier<Stream<T>> dataSrc) {
            this.dataSrc= dataSrc;
        }

        synchronized Spliterator<T> getSrcIter() {
            if(srcIter == null) {
                srcIter = dataSrc.get().spliterator();
                estimateSize = srcIter.estimateSize();
                if((srcIter.characteristics() & Spliterator.SIZED) == 0)
                    mem = new Lst<>(); // Unknown size!!!
                else {
                    if(estimateSize > Integer.MAX_VALUE)
                        throw new IllegalStateException("Replay unsupported for estimated size bigger than Integer.MAX_VALUE!");
                    mem = new Lst<>((int) estimateSize);
                }
            }
            return srcIter;
        }

        public synchronized boolean getOrAdvance(
                int index,
                Consumer<? super T> cons) {
            if (index < mem.size()) {
                // If it is in mem then just get if from the corresponding index.
                cons.accept(mem.get(index));
                return true;
            } else if (hasNext)
                // If not in mem then advance the srcIter iterator
                hasNext = getSrcIter().tryAdvance(item -> {
                    mem.add(item);
                    cons.accept(item);
                });
            return hasNext;
        }

        public Spliterator<T> memIterator() {
            // Fast-path when all items are already saved in mem!
            return hasNext ? new MemoizeIter() : new RandomAccessSpliterator();
        }

        class MemoizeIter extends Spliterators.AbstractSpliterator<T>  {
            int index = 0;
            MemoizeIter(){
                super(estimateSize, getSrcIter().characteristics());
            }
            public boolean tryAdvance(Consumer<? super T> cons) {
                return getOrAdvance(index++, cons);
            }
            public Comparator<? super T> getComparator() {
                return getSrcIter().getComparator();
            }
        }

        /**
         * An index-based split-by-two, lazily initialized Spliterator covering
         * a List that access elements via {@link List#get}.
         *
         * There are no concurrent modifications to the underlying list.
         * That list is the mem field of Recorder and this iterator is just used
         * when the list is completely filled.
         *
         * Based on AbstractList.RandomAccessSpliterator
         */
        class RandomAccessSpliterator implements Spliterator<T> {

            private int index; // current index, modified on advance/split
            private int fence; // -1 until used; then one past last index

            RandomAccessSpliterator() {
                this.index = 0;
                this.fence = -1;
            }

            /**
             * Create new spliterator covering the given  range
             */
            private RandomAccessSpliterator(RandomAccessSpliterator parent,
                                            int origin, int fence) {
                this.index = origin;
                this.fence = fence;
            }

            private int getFence() { // initialize fence to size on first use
                int hi;
                List<T> lst = mem;
                if ((hi = fence) < 0) {
                    hi = fence = lst.size();
                }
                return hi;
            }

            public Spliterator<T> trySplit() {
                int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
                return (lo >= mid) ? null : // divide range in half unless too small
                        new RandomAccessSpliterator(this, lo, index = mid);
            }

            public boolean tryAdvance(Consumer<? super T> action) {
                if (action == null)
                    throw new NullPointerException();
                int hi = getFence(), i = index;
                if (i < hi) {
                    index = i + 1;
                    action.accept(mem.get(i));
                    return true;
                }
                return false;
            }

            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);
//                List<T> lst = mem;
                int hi = getFence();
                int i = index;
                index = hi;
                for (; i < hi; i++)
                    action.accept(mem.get(i));
            }

            public long estimateSize() {
                return (getFence() - index);
            }

            public int characteristics() {
                return Spliterator.ORDERED
                        | Spliterator.SIZED
                        | Spliterator.SUBSIZED
                        | getSrcIter().characteristics();
            }
            public Comparator<? super T> getComparator() {
                return getSrcIter().getComparator();
            }
        }
    }
}