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

import jcog.Util;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Wayne on 5/20/2015.
 * CircularFloatBuffer
 */
@SuppressWarnings("unused")
public class CircularFloatBuffer extends CircularBuffer {
    public float[] data;

    /** warning: starts with 0 capacity */
    public CircularFloatBuffer() {
        super();
    }

    public CircularFloatBuffer(int capacity) {
        super(capacity);
    }

    public CircularFloatBuffer(float[] data) {
        super(data.length);
        this.data = data;
    }

    @Override
    protected void reallocate(int capacity) {
        if (data == null || data.length != capacity)
            data = new float[capacity];
    }

    @Override
    public int capacityInternal() {
        return data.length;
    }

    public int write(float[] data) {
        return write(data, 0, data.length, false);
    }

    public int write(float[] data, int length) {
        return write(data, 0, length, false);
    }

    public int write(float[] data, int offset, int length, boolean blocking) {
        if (length <= 0) return 0;

        lock.lock();
        try {

            int capacity = this.data.length;
            int available = available();
            while (blocking && available < length) {
                try {
                    writCond.await();
                } catch (InterruptedException e) {
                    return -1;
                }
                available = capacity - writeAt.get();
            }
            if (available <= 0)
                return 0;


            int len = length;
            if (len > available)
                len = available;

            int tmpIdx = bufEnd + len;
            if (tmpIdx > capacity) {
                int tmpLen = capacity - bufEnd;
                System.arraycopy(data, offset, this.data, bufEnd, tmpLen);
                bufEnd = (tmpIdx) % capacity;
                System.arraycopy(data, tmpLen + offset, this.data, 0, bufEnd);
            } else {
                System.arraycopy(data, offset, this.data, bufEnd, len);
                bufEnd = (tmpIdx) % capacity;
            }
            writeAt.addAndGet(len);
            return len;



        } finally {
            readCond.signalAll();
            lock.unlock();
            if (threadPool != null)
                threadPool.submit(_notifyListener);
        }
    }


    /** signed 16-bit pairs of bytes TODO which endian */
    public int writeS16(byte[] sample, int from, int to, float gain) {
        int len = to-from;
        //TODO avoid allocation of this temporary buffer
        float[] f = new float[len/2];
        int k = from;
        for (int i= 0; i < f.length; i++) {
            byte low = sample[k++];
            byte high = sample[k++];
            f[i] = ((float)(low | (high << 8)))/Short.MAX_VALUE * gain;
        }
        return write(f);
    }

    public float[] peekLast(float[] data) {
        return peekLast(data, data.length);
    }

    public float peek(long sample) {
        return peek(Math.toIntExact(sample % data.length));
    }

    public float peek(int sample) {
        int idx = idx(sample);
        return data[idx];
    }

    public long idx(long sample) {
        while (sample < 0)
            sample += data.length; //HACK use non-iterative negative modulo

        return sample % data.length;
    }

    public int idx(int sample) {
        while (sample < 0)
            sample += data.length; //HACK use non-iterative negative modulo

        return sample % data.length;
    }

    public void skip(int len) {
        lock.lock();
        try {
//            int s = writeAt.get();
//            len = Math.min(s, len);
            bufStart-= len;
            readAt.addAndGet(len);
        } finally {
            lock.unlock();
        }
    }
    public float[] peekLast(@Nullable float[] data, int len) {
        lock.lock();
        try {

            if (data == null || data.length<len)
                data = new float[len];

            int start = Math.max(0, this.viewPtr - len);
            int tmpIdx = start + len;
            if (tmpIdx > this.data.length) {
                int tmpLen = this.data.length - start;
                System.arraycopy(this.data, start, data, 0, tmpLen);
                start = (tmpIdx) % this.data.length;
                System.arraycopy(this.data, 0, data, tmpLen, start);
            } else {
                System.arraycopy(this.data, start, data, 0, len);
                //start = (tmpIdx) % this.data.length;
            }
            return data;
        } finally {
            lock.unlock();
        }
    }


    /**
     * this manipulates some cursor position variables, TODO make that optional and combine with peekLast API to select any part of the buffer
     */
    public int peek(float[] data, int length) {
        lock.lock();
        try {
            int remSize = writeAt.get() - readAt.get();
            if (length > 0 && remSize > 0) {
                int len = length;
                if (len > remSize)
                    len = remSize;
                int tmpIdx = viewPtr + len;
                if (tmpIdx > this.data.length) {
                    int tmpLen = this.data.length - viewPtr;
                    System.arraycopy(this.data, viewPtr, data, 0, tmpLen);
                    viewPtr = (tmpIdx) % this.data.length;
                    System.arraycopy(this.data, 0, data, tmpLen, viewPtr);
                } else {
                    System.arraycopy(this.data, viewPtr, data, 0, len);
                    viewPtr = (tmpIdx) % this.data.length;
                }
                readAt.addAndGet(len);
                return len;
            }
            return 0;
        } finally {
            lock.unlock();
        }
    }

    public int readFully(float[] data, int offset, int length) {
        lock.lock();
        try {
            if (length > 0) {
                int minSize = Math.max(this.minSize, 0);
                while (writeAt.get() - minSize < length) {
                    try {
                        readCond.await();
                    } catch (InterruptedException e) {
                        wasMarked = false;
                        return -1;
                    }
                }
                return read(data, offset, length, false);
            }
            return 0;
        } finally {
            writCond.signalAll();
            lock.unlock();
        }
    }

    public int read(float[] data, int length, boolean blocking) {
        return read(data, 0, length, blocking);
    }

    public int read(float[] data, int offset, int length, boolean blocking) {
        lock.lock();
        try {
            wasMarked = false;
            if (length > 0) {
                int bs = writeAt.get();
                while (blocking && minSize > -1 && bs <= minSize) {
                    try {
                        readCond.await();
                    } catch (InterruptedException e) {
                        return -1;
                    }
                }
                int minSize = Math.max(this.minSize, 0);
                if (bs > 0) {
                    int len = length;
                    if (len > bs - minSize)
                        len = bs - minSize;
                    int tmpLen;
                    BufMark m = marks.peek();
                    if (m != null) {
                        tmpLen = calcMarkSize(m);
                        if (tmpLen <= len) {
                            marks.poll();
                            len = tmpLen;
                            wasMarked = true;
                        }
                    }
                    if (len > 0) {
                        int tmpIdx = bufStart + len;
                        if (tmpIdx > this.data.length) {
                            tmpLen = this.data.length - bufStart;
                            System.arraycopy(this.data, bufStart, data, offset, tmpLen);
                            bufStart = (tmpIdx) % this.data.length;
                            System.arraycopy(this.data, 0, data, offset + tmpLen, bufStart);
                        } else {
                            System.arraycopy(this.data, bufStart, data, offset, len);
                            bufStart = (tmpIdx) % this.data.length;
                        }
                        if (tmpIdx < viewPtr)
                            readAt.set(viewPtr - bufStart);
                        else {
                            viewPtr = bufStart;
                            readAt.set(0);
                        }
                        writeAt.addAndGet(-len);
                    }
                    return len;
                }
            }
            return 0;
        } finally {
            writCond.signalAll();
            lock.unlock();
            if (threadPool != null)
                threadPool.submit(_notifyListener);
        }
    }


    /**
     * from inclusive, to exclusive
     */
    public float sum(int from, int to) {
        float s = 0;
        //TODO optimize cursor
        for (int i = from; i < to; i++)
            s += peek(i);
        return s;
    }

    /**
     * interpolates fractional areas
     * from inclusive, to exclusive (next integer ceiling)
     * needs tested
     */
    public double sum(double sStart, double sEnd) {
        return Util.interpMean((i)->data[i], capacity(), sStart, sEnd, true);
    }

    public double mean(double sStart, double sEnd) {
        double sum = sum(sStart, sEnd);
        return sum / (1+(sEnd - sStart));
    }



}