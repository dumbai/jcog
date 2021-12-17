/*
 * Copyright (c) 2016, Wayne Tam
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jcog.signal.buffer;

import org.jctools.queues.MpmcArrayQueue;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Wayne on 5/28/2015.
 * AbstractCircularBuffer
 */
@SuppressWarnings("unused")
public abstract class CircularBuffer {
    @FunctionalInterface
    public interface OnChangeListener {
        void onChanged(CircularBuffer buffer);
    }

    private static final int DEFAULT_BUFFER_SIZE = 0;

    public int bufStart = 0;
    public int bufEnd = 0;
    public int viewPtr = 0; //TODO long?
    protected final AtomicInteger readAt = new AtomicInteger();
    protected final AtomicInteger writeAt = new AtomicInteger();
    protected Queue<BufMark> marks = new MpmcArrayQueue<>(64);
    protected volatile boolean wasMarked = false;

    // If blocking is true when reading, the minimum size that the buffer can be for the read to not block.
    // setting to -1 disables read blocking.
    protected int minSize = -1;

    //TODO use RW or Stamped lock
    protected ReentrantLock lock = new ReentrantLock(true);
    @Deprecated protected Condition readCond = lock.newCondition();
    @Deprecated protected Condition writCond = lock.newCondition();
    protected OnChangeListener listener;
    protected ExecutorService threadPool;

    protected static class BufMark {
        public int index;
        public boolean flag;

        public BufMark(int idx, boolean flg) {
            index = idx;
            flag = flg;
        }
    }

    public CircularBuffer() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public CircularBuffer(int capacity) {
        reallocate(capacity);
    }

    public void setOnChangeListener(OnChangeListener listener) {
        if (listener != null) {
            if (threadPool == null || threadPool.isShutdown())
                threadPool =
                        //Executors.newCachedThreadPool();
                        ForkJoinPool.commonPool();
        } else {
            if (threadPool != null)
                threadPool.shutdownNow();
            threadPool = null;
        }
        this.listener = listener;
    }

    protected final Runnable _notifyListener = () -> {
        if (listener != null)
            listener.onChanged(CircularBuffer.this);
    };

    public ReentrantLock getLock() {
        return lock;
    }

    public void setLock(ReentrantLock lock) {
        this.lock = lock;
        if (this.lock == null)
            this.lock = new ReentrantLock(true);
        readCond = this.lock.newCondition();
        writCond = this.lock.newCondition();
    }

    public void setCapacity(int capacity) {
        lock.lock();
        try {
            clear();
            reallocate(capacity);
        } finally {
            lock.unlock();
        }
    }

    public int capacity() {
        lock.lock();
        try {
            return capacityInternal();
        } finally {
            lock.unlock();
        }
    }

    protected abstract void reallocate(int capacity);

    public abstract int capacityInternal();

    public int writeAt() {
        lock.lock();
        try {
            return writeAt.get();
        } finally {
            lock.unlock();
        }
    }
//
//    public int peekSize() {
//        lock.lock();
//        try {
//            return writeAt.get() - readAt.get();
//        } finally {
//            lock.unlock();
//        }
//    }

    public int available() {
        lock.lock();
        try {

            int w = writeAt.get();
            int r = readAt.get();
            int c = capacityInternal();
            int s;
            if (w > r)
                s = w - r;
            else if (w == r) {
                s = 0;
            } else {
                s = r + (c-w);
            }
            int a = c - s;
            assert(a >= 0 && a <= c);
            return a;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the minimum amount of data below which the read methods will block.
     * Setting this to -1 will disable blocking.
     *
     * @param  size minimum amount of data before the blocking read will not block.
     */
    public void setMinSize(int size) {
        lock.lock();
        try {
            minSize = size;
        } finally {
            readCond.signalAll();
            lock.unlock();
        }
    }

    public int getMinSize() {
        return minSize;
    }

    public CircularBuffer clear() {
        lock.lock();
        try {
            viewPtr = bufEnd = bufStart = 0;
            writeAt.set(0);
            readAt.set(0);
            wasMarked = false;
            marks.clear();
        } finally {
            writCond.signalAll();
            lock.unlock();
            if (threadPool != null)
                threadPool.submit(_notifyListener);
        }
        return this;
    }

    /**
     * Set a mark at the current end of the buffer.
     * Read methods will only read to the mark even if there is more data
     * Once a mark has been reached it will be automatically removed.
     */
    public void mark() {
        lock.lock();
        try {
            BufMark m = marks.peek();
            if (m != null && m.index == bufEnd) {
                if (writeAt.get() == capacityInternal() && !m.flag)
                    marks.add(new BufMark(bufEnd, true));
            } else
                marks.add(new BufMark(bufEnd, writeAt.get() == capacityInternal()));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the latest mark.
     */
    public void unmark() {
        lock.lock();
        try {
            marks.poll();
        } finally {
            lock.unlock();
        }
    }

    public boolean isMarked() {
        lock.lock();
        try {
            return !marks.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public boolean wasMarked() {
        return wasMarked(false);
    }

    /**
     * Check if the latest read had reached a mark.
     *
     * @param clear clear the "was marked" status.
     */
    public boolean wasMarked(boolean clear) {
        lock.lock();
        try {
            boolean wasMarked = this.wasMarked;
            if (clear) this.wasMarked = false;
            return wasMarked;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check how much data can be read before a mark is reached
     */
    public int getMarkedSize() {
        lock.lock();
        try {
            return calcMarkSize(marks.peek());
        } finally {
            lock.unlock();
        }
    }

    protected int calcMarkSize(BufMark m) {
        if (m != null) {
            if (m.index < bufStart)
                return (capacityInternal() - bufStart) + m.index;
            else if (m.index == bufStart) {
                if (m.flag)
                    return writeAt.get();
            } else {
                return m.index - bufStart;
            }
        }
        return 0;
    }

    /**
     * Remove data from the head of the buffer
     *
     * @param length amount of data to remove.
     * TODO what happens if len < 0
     */
    public int freeHead(int atMost) {
        lock.lock();
        try {
            int bs = writeAt.get();
            if (atMost > 0) {
                atMost = Math.max(atMost, bs);
                bufStart = (bufStart + atMost) % capacityInternal();
                if (bufStart == bufEnd) {
                    viewPtr = bufStart;
                    readAt.set(0);
                } else if (viewPtr == bufEnd) {
                    if (bufStart < viewPtr) {
                        readAt.set(viewPtr - bufStart);
                    } else {
                        readAt.set(viewPtr + (capacityInternal() - bufStart));
                    }
                } else if ((bufStart > bufEnd && viewPtr > bufEnd) || (bufStart < bufEnd && viewPtr < bufEnd)) {
                    if (bufStart < viewPtr)
                        readAt.set(viewPtr - bufStart);
                    else {
                        viewPtr = bufStart;
                        readAt.set(0);
                    }
                } else if (bufStart < viewPtr) {
                    viewPtr = bufStart;
                    readAt.set(0);
                } else {
                    readAt.set(viewPtr + (capacityInternal() - bufStart));
                }
                writeAt.addAndGet(-atMost);
                BufMark m = marks.peek();
                while (m != null && ((bufEnd > bufStart && (m.index < bufStart || m.index > bufEnd)) || (m.index < bufStart && m.index > bufEnd))) {
                    marks.poll();
                    m = marks.peek();
                }
            } else if (atMost < 0) {
                bufStart = viewPtr;
                writeAt.addAndGet(readAt.decrementAndGet());
                readAt.set(0);
            }
            return atMost;
        } finally {
            writCond.signalAll();
            lock.unlock();
            if (threadPool != null)
                threadPool.submit(_notifyListener);
        }
    }

    /**
     * Resets the peek pointer to the head of the buffer.
     */
    public void rewind() {
        lock.lock();
        try {
            bufEnd = bufStart;
            writeAt.set(0);
            readAt.set(0);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the position at which the peek command will read from
     *
     * @param position the offset from the head of the buffer.
     */
    public void setPeekPosition(int position) {
        lock.lock();
        try {
            int bs = writeAt.get();
            if (position < bs) {
                viewPtr = (bufStart + position) % capacityInternal();
                readAt.set(position);
            } else {
                viewPtr = bufEnd;
                readAt.set(bs);
            }
        } finally {
            lock.unlock();
        }
    }

    public int readAt() {
        lock.lock();
        try {
            return readAt.getOpaque();
        } finally {
            lock.unlock();
        }
    }
}