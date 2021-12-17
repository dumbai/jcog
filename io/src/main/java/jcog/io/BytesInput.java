package jcog.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * from: jdk DataInputWrapper.java
 */
public final class BytesInput implements DataInput {

    private final ByteBuffer bb;

    public BytesInput(byte[] bb) {
        this(ByteBuffer.wrap(bb));
    }

    public BytesInput(ByteBuffer bb) {
        this.bb = bb;
    }

    public void readFully(byte[] b) throws IOException {
        this.readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        try {
            this.bb.get(b, off, len);
        } catch (BufferUnderflowException var5) {
            throw new EOFException(var5.getMessage());
        }
    }

    public int skipBytes(int n) {
        int skip = Math.min(n, this.bb.remaining());
        this.bb.position(this.bb.position() + skip);
        return skip;
    }

    public boolean readBoolean() {

        int ch = this.bb.get();
        return ch != 0;

    }

    public byte readByte() {
        return this.bb.get();

    }

    public int readUnsignedByte() {
        return this.bb.get() & 255;

    }

    public short readShort() {
        return this.bb.getShort();

    }

    public int readUnsignedShort() {
        return this.bb.getShort() & '\uffff';

    }

    public char readChar() {
        return this.bb.getChar();

    }

    public int readInt() {
        return this.bb.getInt();

    }

    public long readLong() {
        return this.bb.getLong();

    }

    public float readFloat() {
        return this.bb.getFloat();

    }

    public double readDouble() {
        return this.bb.getDouble();

    }

    public String readLine() {
        throw new RuntimeException("not implemented");
    }

    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }
}
