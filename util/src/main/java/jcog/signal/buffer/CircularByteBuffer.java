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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("UnusedDeclaration")
public class CircularByteBuffer extends CircularBuffer {

    private byte[] _circBuffer;

    public CircularByteBuffer() {
        super();
    }

    public CircularByteBuffer(int capacity) {
        super(capacity);
    }

    @Override
    protected void reallocate(int capacity) {
        _circBuffer = new byte[capacity];
    }

    @Override
    public int capacityInternal() {
        return _circBuffer.length;
    }

    public InputStream getInputStream() {
        return new ReadStream();
    }

    public OutputStream getOutputStream() {
        return new WriteStream();
    }

    private class ReadStream extends InputStream {

        private final byte[] singleByteBuf = new byte[1];
        private boolean isClosed = false;
        private boolean isEOS = false;

        @Override
        public int available() {
            return CircularByteBuffer.this.writeAt();
        }

        @Override
        public int read() throws IOException {
            if(isClosed)
                throw new IOException("Stream was closed");
            if(isEOS)
                return -1;
            lock.lock();
            try {
                if (wasMarked) {
                    wasMarked = false;
                    isEOS = true;
                    return -1;
                }
                int result = CircularByteBuffer.this.readFully(singleByteBuf, 0, 1);
                if (result < 1)
                    return -1;
                return singleByteBuf[0] & 0x000000FF;
            } finally {
                lock.unlock();
            }
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public int read(byte[] buf, int offset, int len) throws IOException {
            if(isClosed)
                throw new IOException("Stream was closed");
            if(isEOS)
                return -1;
            lock.lock();
            try {
                if (wasMarked) {
                    wasMarked = false;
                    isEOS = true;
                    return -1;
                }
                int result = CircularByteBuffer.this.read(buf, offset, len, true);
                if (result < 0) {
                    return -1;
                }
                return result;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void close() throws IOException {
            isClosed = true;
            lock.lock();
            try {
                wasMarked = false;
            } finally {
                lock.unlock();
            }
            super.close();
        }
    }

    private class WriteStream extends OutputStream {

        private final byte[] buf = new byte[1];

        @SuppressWarnings("NullableProblems")
        @Override
        public void write(byte[] buf, int offset, int len) {
            CircularByteBuffer.this.write(buf, offset, len, true);
        }

        @Override
        public void write(int data) {
            buf[0] = (byte) (data & 0x000000ff);
            CircularByteBuffer.this.write(buf, 0, 1, true);
        }

    }

    public int write(byte[] data) {
        return write(data, 0, data.length, false);
    }

    public int write(byte[] data, int length) {
        return write(data, 0, length, false);
    }

    public int write(byte[] data, int offset, int length, boolean blocking) {
        lock.lock();
        try {
            if (length > 0) {
                int emptySize = _circBuffer.length - writeAt.get();
                while (blocking && emptySize < length) {
                    try {
                        writCond.await();
                    } catch (InterruptedException e) {
                        return -1;
                    }
                    emptySize = _circBuffer.length - writeAt.get();
                }
                if (emptySize > 0) {
                    int len = length;
                    if (len > emptySize)
                        len = emptySize;

                    int tmpIdx = bufEnd + len;
                    if (tmpIdx > _circBuffer.length) {
                        int tmpLen = _circBuffer.length - bufEnd;
                        System.arraycopy(data, offset, _circBuffer, bufEnd, tmpLen);
                        bufEnd = (tmpIdx) % _circBuffer.length;
                        System.arraycopy(data, tmpLen + offset, _circBuffer, 0, bufEnd);
                    } else {
                        System.arraycopy(data, offset, _circBuffer, bufEnd, len);
                        bufEnd = (tmpIdx) % _circBuffer.length;
                    }
                    writeAt.addAndGet(len);
                    return len;
                }
            }
            return 0;
        } finally {
            readCond.signalAll();
            lock.unlock();
            if (threadPool != null)
                threadPool.submit(_notifyListener);
        }
    }

    /**
     * Read data from the buffer without removing it.
     *
     * @param data the output array
     * @param length amount of data to read
     * @return the amount of data read
     */
    public int peek(byte[] data, int length) {
        lock.lock();
        try {
            int remSize = writeAt.get() - readAt.get();
            if (length > 0 && remSize > 0) {
                int len = length;
                if (len > remSize)
                    len = remSize;
                int tmpIdx = viewPtr + len;
                if (tmpIdx > _circBuffer.length) {
                    int tmpLen = _circBuffer.length - viewPtr;
                    System.arraycopy(_circBuffer, viewPtr, data, 0, tmpLen);
                    viewPtr = (tmpIdx) % _circBuffer.length;
                    System.arraycopy(_circBuffer, 0, data, tmpLen, viewPtr);
                } else {
                    System.arraycopy(_circBuffer, viewPtr, data, 0, len);
                    viewPtr = (tmpIdx) % _circBuffer.length;
                }
                readAt.addAndGet(len);
                return len;
            }
            return 0;
        } finally {
            lock.unlock();
        }
    }

    public int readFully(byte[] data, int offset, int length) {
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

    public int read(byte[] data, int length, boolean blocking) {
        return read(data, 0, length, blocking);
    }

    public int read(byte[] data, int offset, int length, boolean blocking) {
        lock.lock();
        try {
            wasMarked = false;
            if (length > 0) {
                while (blocking && minSize > -1 && writeAt.get() <= minSize) {
                    try {
                        readCond.await();
                    } catch (InterruptedException e) {
                        return -1;
                    }
                }
                int minSize = Math.max(this.minSize, 0);
                if (writeAt.get() > 0) {
                    int len = length;
                    if (len > writeAt.get() - minSize)
                        len = writeAt.get() - minSize;
                    int tmpLen;
                    CircularBuffer.BufMark m = marks.peek();
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
                        if (tmpIdx > _circBuffer.length) {
                            tmpLen = _circBuffer.length - bufStart;
                            System.arraycopy(_circBuffer, bufStart, data, offset, tmpLen);
                            bufStart = (tmpIdx) % _circBuffer.length;
                            System.arraycopy(_circBuffer, 0, data, offset + tmpLen, bufStart);
                        } else {
                            System.arraycopy(_circBuffer, bufStart, data, offset, len);
                            bufStart = (tmpIdx) % _circBuffer.length;
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
}
