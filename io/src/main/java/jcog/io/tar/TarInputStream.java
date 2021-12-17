/**
 * Copyright 2012 Kamran Zafar 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http:
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */

package jcog.io.tar;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamran Zafar
 * 
 */
public class TarInputStream extends FilterInputStream {

	private static final int SKIP_BUFFER_SIZE = 2048;
	private TarEntry currentEntry;
	private long currentFileSize;
	private long bytesRead;
	private boolean defaultSkip = false;

	public TarInputStream(InputStream in) {
		super(in);
		currentFileSize = 0;
		bytesRead = 0;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	/**
	 * Not supported
	 * 
	 */
	@Override
	public synchronized void mark(int readlimit) {
	}

	/**
	 * Not supported
	 * 
	 */
	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	/**
	 * Read a byte
	 * 
	 * @see FilterInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		byte[] buf = new byte[1];

		int res = this.read(buf, 0, 1);

		if (res != -1) {
			return 0xFF & buf[0];
		}

		return res;
	}

	/**
	 * Checks if the bytes being read exceed the entry size and adjusts the byte
	 * array length. Updates the byte counters
	 * 
	 * 
	 * @see FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (currentEntry != null) {
			long currentEntrySize = currentEntry.getSize();
			if (currentFileSize == currentEntrySize) {
				return -1;
			} else if ((currentEntrySize - currentFileSize) < len) {
				len = (int) (currentEntrySize - currentFileSize);
			}
		}

		int br = super.read(b, off, len);

		if (br != -1) {
			if (currentEntry != null) {
				currentFileSize += br;
			}

			bytesRead += br;
		}

		return br;
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		long eSize = currentEntry.getSize();
		if (eSize > Integer.MAX_VALUE)
			throw new UnsupportedOperationException();
		byte[] b = new byte[(int)eSize];
		int len = read(b, 0, b.length);
		if (len!=b.length)
			throw new IOException("incomplete read");
		return b;
	}

	/**
	 * Returns the next entry in the tar file
	 * 
	 * @return TarEntry
	 * @throws IOException
	 */
	public TarEntry getNextEntry() throws IOException {
		closeCurrentEntry();

        byte[] header = new byte[TarConstants.HEADER_BLOCK];
        byte[] theader = new byte[TarConstants.HEADER_BLOCK];
		int tr = 0;

		
		while (tr < TarConstants.HEADER_BLOCK) {
			int res = read(theader, 0, TarConstants.HEADER_BLOCK - tr);

			if (res < 0) {
				break;
			}

			System.arraycopy(theader, 0, header, tr, res);
			tr += res;
		}

		
		boolean eof = true;
		for (byte b : header) {
			if (b != 0) {
				eof = false;
				break;
			}
		}

		if (!eof) {
			currentEntry = new TarEntry(header);
		}

		return currentEntry;
	}

	/**
	 * Returns the current offset (in bytes) from the beginning of the stream. 
	 * This can be used to find out at which point in a tar file an entry's content begins, for instance. 
	 */
	public long getCurrentOffset() {
		return bytesRead;
	}
	
	/**
	 * Closes the current tar entry
	 * 
	 * @throws IOException
	 */
	private TarEntry closeCurrentEntry() throws IOException {
		TarEntry currentEntry = this.currentEntry;
		if (currentEntry != null) {
			long currentEntrySize = currentEntry.getSize();
			if (currentEntrySize > currentFileSize) {
				
				long bs = 0;
				while (bs < currentEntrySize - currentFileSize) {
					long res = skip(currentEntrySize - currentFileSize - bs);

					if (res == 0 && currentEntrySize - currentFileSize > 0) {
						
						throw new IOException("Possible tar file corruption");
					}

					bs += res;
				}
			}


			this.currentEntry = null;
			currentFileSize = 0L;
			skipPad();
			return currentEntry;
		}
		return currentEntry;
	}

	/**
	 * Skips the pad at the end of each tar entry file content
	 * 
	 * @throws IOException
	 */
	private void skipPad() throws IOException {
		if (bytesRead > 0) {
			int extra = (int) (bytesRead % TarConstants.DATA_BLOCK);

			if (extra > 0) {
				long bs = 0;
				while (bs < TarConstants.DATA_BLOCK - extra) {
					long res = skip(TarConstants.DATA_BLOCK - extra - bs);
					bs += res;
				}
			}
		}
	}

	/**
	 * Skips 'n' bytes on the InputStream<br>
	 * Overrides default implementation of skip
	 * 
	 */
	@Override
	public long skip(long n) throws IOException {
		if (defaultSkip) {
			
			
			long bs = super.skip(n);
			bytesRead += bs;

			return bs;
		}

		if (n <= 0) {
			return 0;
		}

		long left = n;
		byte[] sBuff = new byte[SKIP_BUFFER_SIZE];

		while (left > 0) {
			int res = read(sBuff, 0, (int) (left < SKIP_BUFFER_SIZE ? left : SKIP_BUFFER_SIZE));
			if (res < 0) {
				break;
			}
			left -= res;
		}

		return n - left;
	}

	public boolean isDefaultSkip() {
		return defaultSkip;
	}

	public void setDefaultSkip(boolean defaultSkip) {
		this.defaultSkip = defaultSkip;
	}
}
