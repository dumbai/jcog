package alice.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
/**
 * This class let a Writer be viewed as an OutputStream.
 * The write() method takes an integer b:
 * - if b is -1, -1 is written (end of stream)
 * - otherwise the lowest byte of b is written
 * Since writers deal with characters, they usually buffer
 * some data and then flush when '-1' or '\n'  is received.
 * OutputStreams (usually) don't do buffering, so we're flushing 
 * the stream everytime a byte is written.
 * @author Andrea Bucaletti
 *
 */
public class OutputStreamAdapter extends OutputStream {
	
	private final Writer writer;
	
	public OutputStreamAdapter(Writer wr) {
		writer = wr;
	}

	@Override
	public void write(int b) throws IOException {
		
		if(b == -1) 
			writer.write(-1);
		else 
			writer.write(0xFF & b);
		
		flush();
	}
	
	@Override
	public void flush() throws IOException {
		writer.flush();
	}
	
	@Override
	public void close() throws IOException {
		flush();
		writer.close();
	}
}
