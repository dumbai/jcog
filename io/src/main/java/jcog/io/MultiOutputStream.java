/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package jcog.io;

import java.io.IOException;
import java.io.OutputStream;


/**
     * @author
     * http:
     */

public class MultiOutputStream extends OutputStream {

    private final OutputStream[] out;

    public MultiOutputStream(OutputStream... o) {
        out = o;
    }

    @Override public void write(int arg0) throws IOException {
        for (OutputStream anOut : out) {
            anOut.write(arg0);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (OutputStream anOut : out) {
            anOut.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream anOut : out) {
            anOut.write(b, off, len);
        }
    }

    @Override
    public void close() throws IOException {
        for (OutputStream anOut : out) {
            anOut.close();
        }
    }

    @Override
    public void flush() throws IOException {
        for (OutputStream anOut : out) {
            anOut.flush();
        }
    }

}
