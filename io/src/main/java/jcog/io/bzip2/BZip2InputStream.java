/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http:
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http:
 */

/*
 * This package is based on the work done by Keiron Liddle, Aftex Software
 * <keiron@aftexsw.com> to whom the Ant project is very grateful for his
 * great code.
 */
package jcog.io.bzip2;

import jcog.WTF;
import jcog.data.bit.MetalBitSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * An input stream that decompresses from the BZip2 format (without the file
 * header chars) to be read as any other stream.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * <p>
 * http:
 */
public class BZip2InputStream extends InputStream implements BZip2Constants {
	private static final int START_BLOCK_STATE = 1;
	private static final int RAND_PART_A_STATE = 2;
	private static final int RAND_PART_B_STATE = 3;
	private static final int RAND_PART_C_STATE = 4;
	private static final int NO_RAND_PART_A_STATE = 5;
	private static final int NO_RAND_PART_B_STATE = 6;
	private static final int NO_RAND_PART_C_STATE = 7;
	private final CRC mCrc = new CRC();
	private final MetalBitSet inUse = MetalBitSet.bits(256);
	private final char[] seqToUnseq = new char[256];
	private final char[] unseqToSeq = new char[256];
	private final char[] selector = new char[MAX_SELECTORS];
	private final char[] selectorMtf = new char[MAX_SELECTORS];
	/*
	  freq table collected to save a pass over the data
	  during decompression.
	*/
	private final int[] unzftab = new int[256];
	private final int[][] limit = new int[N_GROUPS][MAX_ALPHA_SIZE];
	private final int[][] base = new int[N_GROUPS][MAX_ALPHA_SIZE];
	private final int[][] perm = new int[N_GROUPS][MAX_ALPHA_SIZE];
	private final int[] minLens = new int[N_GROUPS];
	/*
	  index of the last char in the block, so
	  the block size == last + 1.
	*/
	private int last;
	/*
	  index in zptr[] of original string after sorting.
	*/
	private int origPtr;
	/*
	  always: in the range 0 .. 9.
	  The current block size is 100000 * this number.
	*/
	private int blockSize100k;
	private boolean blockRandomised;
	private int bsBuff;
	private int bsLive;
	private int nInUse;
	private int[] tt;
	private char[] ll8;
	private InputStream bsStream;
	private boolean streamEnd = false;
	private int currentChar = -1;
	private int currentState = START_BLOCK_STATE;
	private int storedBlockCRC;
	private int computedCombinedCRC;
	private int i2;
	private int count;
	private int chPrev;
	private int ch2;
	private int tPos;
	private int rNToGo = 0;
	private int rTPos = 0;
	private int j2;
	private char z;
	/**
	 * if from BZip2File is true, it skips the 2-byte header identifying a .bzip2 file
	 */
	public BZip2InputStream(boolean fromBZip2File, InputStream zStream) throws IOException {
		ll8 = null;
		tt = null;

		if (fromBZip2File) {
			byte[] header = new byte[2];
			int nh;
			try {
				nh = zStream.read(header);
			} catch (IOException e) {
				nh = 0;
			}
			if (nh != 2 || ((header[0] != 'B') && (header[1] != 'Z')))
				throw new RuntimeException("BZip2 header not detected");
		}
		bsSetStream(zStream);
		initialize();
		initBlock();
		setupBlock();
	}
	public BZip2InputStream(InputStream zStream) throws IOException {
		this(false, zStream);
	}

	private static void cadvise() {
		throw new WTF(BZip2InputStream.class + " CRC Error");
	}

	private static void badBGLengths() {
		cadvise();
	}

	private static void bitStreamEOF() {
		cadvise();
	}

	private static void compressedStreamEOF() {
		cadvise();
	}

	private static void blockOverrun() {
		cadvise();
	}

	private static void badBlockHeader() {
		cadvise();
	}

	private static void crcError() {
		cadvise();
	}

	private static void hbCreateDecodeTables(int[] limit, int[] base,
											 int[] perm, char[] length,
											 int minLen, int maxLen, int alphaSize) {

		int pp = 0;
		for (int i = minLen; i <= maxLen; i++) {
			for (int j = 0; j < alphaSize; j++) {
				if (length[j] == i) perm[pp++] = j;
			}
		}

		Arrays.fill(base, 0, MAX_CODE_LEN, 0);
		for (int i = 0; i < alphaSize; i++)
			base[length[i] + 1]++;


		for (int i = 1; i < MAX_CODE_LEN; i++)
			base[i] += base[i - 1];

		Arrays.fill(limit, 0, MAX_CODE_LEN, 0);
		for (int vec = 0, i = minLen; i <= maxLen; i++) {
			vec += (base[i + 1] - base[i]);
			limit[i] = vec - 1;
			vec <<= 1;
		}
		for (int i = minLen + 1; i <= maxLen; i++)
			base[i] = ((limit[i - 1] + 1) << 1) - base[i];
	}

	private void makeMaps() {
		int nInUse = 0;
		for (int i = 0; i < 256; i++) {
			if (inUse.test(i)) {
				seqToUnseq[nInUse] = (char) i;
				unseqToSeq[i] = (char) nInUse++;
			}
		}
		this.nInUse = nInUse;
	}

	public int read() throws IOException {
		if (streamEnd) {
			return -1;
		} else {
			int retChar = currentChar;
			switch (currentState) {
				case RAND_PART_B_STATE:
					setupRandPartB();
					break;
				case RAND_PART_C_STATE:
					setupRandPartC();
					break;
				case NO_RAND_PART_B_STATE:
					setupNoRandPartB();
					break;
				case NO_RAND_PART_C_STATE:
					setupNoRandPartC();
					break;
				default:
					break;
			}
			return retChar;
		}
	}

	private void initialize() throws IOException {
		char magic3 = bsGetUChar();
		char magic4 = bsGetUChar();
		if (magic3 != 'h' || magic4 < '1' || magic4 > '9') {
			bsFinishedWithStream();
			streamEnd = true;
			return;
		}

		setDecompressStructureSizes(magic4 - '0');
		computedCombinedCRC = 0;
	}

	private void initBlock() throws IOException {
		char magic1 = bsGetUChar();
		char magic2 = bsGetUChar();
		char magic3 = bsGetUChar();
		char magic4 = bsGetUChar();
		char magic5 = bsGetUChar();
		char magic6 = bsGetUChar();
		if (magic1 == 0x17 && magic2 == 0x72 && magic3 == 0x45
			&& magic4 == 0x38 && magic5 == 0x50 && magic6 == 0x90) {
			complete();
			return;
		}

		if (magic1 != 0x31 || magic2 != 0x41 || magic3 != 0x59
			|| magic4 != 0x26 || magic5 != 0x53 || magic6 != 0x59) {
			badBlockHeader();
			streamEnd = true;
			return;
		}

		storedBlockCRC = bsGetInt32();

		blockRandomised = bsR(1) == 1;


		getAndMoveToFrontDecode();

		mCrc.initialiseCRC();
		currentState = START_BLOCK_STATE;
	}

	private void endBlock() {
		int computedBlockCRC = mCrc.getFinalCRC();
		/* A bad CRC is considered a fatal error. */
		if (storedBlockCRC != computedBlockCRC) {
			crcError();
		}

		computedCombinedCRC = (computedCombinedCRC << 1)
			| (computedCombinedCRC >>> 31);
		computedCombinedCRC ^= computedBlockCRC;
	}

	private void complete() throws IOException {
		if (bsGetInt32() != computedCombinedCRC)
			crcError();


		bsFinishedWithStream();
		streamEnd = true;
	}

	private void bsFinishedWithStream() {
		try {
			if (this.bsStream != null) {
				if (this.bsStream != System.in) {
					this.bsStream.close();
					this.bsStream = null;
				}
			}
		} catch (IOException ioe) {

		}
	}

	private void bsSetStream(InputStream f) {
		bsStream = f;
		bsLive = 0;
		bsBuff = 0;
	}

	private int bsR(int n) throws IOException {
		while (bsLive < n) {
            int zzi = (char) bsStream.read();
			bsBuff = (bsBuff << 8) | (zzi & 0xff);
			bsLive += 8;
		}

		int v = (bsBuff >> (bsLive - n)) & ((1 << n) - 1);
		bsLive -= n;
		return v;
	}

	private char bsGetUChar() throws IOException {
		return (char) bsR(8);
	}

	private int bsGetint() throws IOException {
		int u = 0;
		u = (u << 8) | bsR(8);
		u = (u << 8) | bsR(8);
		u = (u << 8) | bsR(8);
		u = (u << 8) | bsR(8);
		return u;
	}

	private int bsGetIntVS(int numBits) throws IOException {
		return bsR(numBits);
	}

	private int bsGetInt32() throws IOException {
		return bsGetint();
	}

	private void recvDecodingTables() throws IOException {
		int i;
		MetalBitSet inUse16 = MetalBitSet.bits(16);//boolean[] inUse16 = new boolean[16];

		/* Receive the mapping table */
		for (i = 0; i < 16; i++)
			if (bsR(1) == 1)
				inUse16.set(i);

		inUse.clear();


		for (i = 0; i < 16; i++) {
			if (inUse16.test(i)) {
				for (int j = 0; j < 16; j++) {
					if (bsR(1) == 1)
						inUse.set(i * 16 + j);
				}
			}
		}

		makeMaps();
		int alphaSize = nInUse + 2;

		/* Now the selectors */
		int nGroups = bsR(3);
		int nSelectors = bsR(15);
		{
			int j;
			for (i = 0; i < nSelectors; i++) {
				j = 0;
				while (bsR(1) == 1) {
					j++;
				}
				selectorMtf[i] = (char) j;
			}
		}

		/* Undo the MTF values for the selectors. */
		{
			char[] pos = new char[N_GROUPS];
			char v;
			for (v = 0; v < nGroups; v++)
				pos[v] = v;


			for (i = 0; i < nSelectors; i++) {
				v = selectorMtf[i];
				char tmp = pos[v];
				while (v > 0) {
					pos[v] = pos[v - 1];
					v--;
				}
				pos[0] = tmp;
				selector[i] = tmp;
			}
		}

		/* Now the coding tables */
		int t;
		char[][] len = new char[N_GROUPS][MAX_ALPHA_SIZE];
		for (t = 0; t < nGroups; t++) {
			int curr = bsR(5);
			for (i = 0; i < alphaSize; i++) {
				while (bsR(1) == 1) {
					if (bsR(1) == 0) {
						curr++;
					} else {
						curr--;
					}
				}
				len[t][i] = (char) curr;
			}
		}

		/* Create the Huffman decoding tables */
		for (t = 0; t < nGroups; t++) {
			int minLen = 32;
			int maxLen = 0;
			char[] lt = len[t];
			for (i = 0; i < alphaSize; i++) {
				if (lt[i] > maxLen) maxLen = lt[i];
				if (lt[i] < minLen) minLen = lt[i];
			}
			hbCreateDecodeTables(limit[t], base[t], perm[t], len[t], minLen, maxLen, alphaSize);
			minLens[t] = minLen;
		}
	}

	private void getAndMoveToFrontDecode() throws IOException {

		int limitLast = baseBlockSize * blockSize100k;
		origPtr = bsGetIntVS(24);

		recvDecodingTables();
		int EOB = nInUse + 1;

        /*
          Setting up the unzftab entries here is not strictly
          necessary, but it does save having to do it later
          in a separate pass, and so saves a block's worth of
          cache misses.
        */
		int i;
		for (i = 0; i <= 255; i++) {
			unzftab[i] = 0;
		}

		char[] yy = new char[256];
		for (i = 0; i <= 255; i++) {
			yy[i] = (char) i;
		}

		last = -1;

		int groupPos = 0;
		int groupNo = -1;
		int nextSym;
		{
			if (groupPos == 0) {
				groupNo++;
				groupPos = G_SIZE;
			}
			groupPos--;
			int zt = selector[groupNo];
			int zn = minLens[zt];
			int zvec = bsR(zn);
			while (zvec > limit[zt][zn]) {
				zn++;
				int zj;
				{
					{
						while (bsLive < 1) {
							char thech = 0;
							try {
								thech = (char) bsStream.read();
							} catch (IOException e) {
								compressedStreamEOF();
							}
							if (thech == -1) {
								compressedStreamEOF();
							}
							int zzi = thech;
							bsBuff = (bsBuff << 8) | (zzi & 0xff);
							bsLive += 8;
						}
					}
					zj = (bsBuff >> (bsLive - 1)) & 1;
					bsLive--;
				}
				zvec = (zvec << 1) | zj;
			}
			nextSym = perm[zt][zvec - base[zt][zn]];
		}

		while (nextSym != EOB) {

			if (nextSym == RUNA || nextSym == RUNB) {
				int s = -1;
				int N = 1;
				do {
                    switch (nextSym) {
                        case RUNA -> s += N;
                        case RUNB -> s += (1 + 1) * N;
                    }
					N *= 2;
					{
						if (groupPos == 0) {
							groupNo++;
							groupPos = G_SIZE;
						}
						groupPos--;
						int zt = selector[groupNo];
						int zn = minLens[zt];
						int zvec = bsR(zn);
						while (zvec > limit[zt][zn]) {
							zn++;
							int zj;
							{
								{
									while (bsLive < 1) {
										char thech = 0;
										try {
											thech = (char) bsStream.read();
										} catch (IOException e) {
											compressedStreamEOF();
										}
										if (thech == -1) {
											compressedStreamEOF();
										}
										int zzi = thech;
										bsBuff = (bsBuff << 8) | (zzi & 0xff);
										bsLive += 8;
									}
								}
								zj = (bsBuff >> (bsLive - 1)) & 1;
								bsLive--;
							}
							zvec = (zvec << 1) | zj;
						}
						nextSym = perm[zt][zvec - base[zt][zn]];
					}
				} while (nextSym == RUNA || nextSym == RUNB);

				s++;
				char ch = seqToUnseq[yy[0]];
				unzftab[ch] += s;

				while (s > 0) {
					last++;
					ll8[last] = ch;
					s--;
				}

				if (last >= limitLast) {
					blockOverrun();
				}
			} else {
				last++;
				if (last >= limitLast) {
					blockOverrun();
				}

				char tmp = yy[nextSym - 1];
				unzftab[seqToUnseq[tmp]]++;
				ll8[last] = seqToUnseq[tmp];

                /*
                  This loop is hammered during decompression,
                  hence the unrolling.

                  for (j = nextSym-1; j > 0; j--) yy[j] = yy[j-1];
                */

				int j = nextSym - 1;
				for (; j > 3; j -= 4) {
					yy[j] = yy[j - 1];
					yy[j - 1] = yy[j - 2];
					yy[j - 2] = yy[j - 3];
					yy[j - 3] = yy[j - 4];
				}
				for (; j > 0; j--)
					yy[j] = yy[j - 1];


				yy[0] = tmp;
                if (groupPos == 0) {
                    groupNo++;
                    groupPos = G_SIZE;
                }
                groupPos--;
                int zt = selector[groupNo];
                int[] lzt = limit[zt];
                int zn = minLens[zt];
                int zvec = bsR(zn);
				while (zvec > lzt[zn]) {
					zn++;
					int zj;
					try {
						while (bsLive < 1) {
							bsBuff = (bsBuff << 8) | (((int) ((char) bsStream.read())) & 0xff);
							bsLive += 8;
						}
					} catch (IOException e) {
						compressedStreamEOF();
					}
					zj = (bsBuff >> (bsLive - 1)) & 1;
					bsLive--;
					zvec = (zvec << 1) | zj;
				}
                nextSym = perm[zt][zvec - base[zt][zn]];
            }
		}
	}

	private void setupBlock() throws IOException {
		int[] cftab = new int[257];

		cftab[0] = 0;
		int i;
		for (i = 1; i <= 256; i++) {
			cftab[i] = unzftab[i - 1];
		}
		for (i = 1; i <= 256; i++) {
			cftab[i] += cftab[i - 1];
		}

		for (i = 0; i <= last; i++) {
			char ch = ll8[i];
			tt[cftab[ch]] = i;
			cftab[ch]++;
		}
		cftab = null;

		tPos = tt[origPtr];

		count = 0;
		i2 = 0;
		ch2 = 256;   /* not a char and not EOF */

		if (blockRandomised) {
			rNToGo = 0;
			rTPos = 0;
			setupRandPartA();
		} else {
			setupNoRandPartA();
		}
	}

	private void setupRandPartA() throws IOException {
		if (i2 <= last) {
			chPrev = ch2;
			ch2 = ll8[tPos];
			tPos = tt[tPos];
			if (rNToGo == 0) {
				rNToGo = rNums[rTPos];
				rTPos++;
				if (rTPos == 512) {
					rTPos = 0;
				}
			}
			rNToGo--;
			ch2 ^= (rNToGo == 1) ? 1 : 0;
			i2++;

			currentChar = ch2;
			currentState = RAND_PART_B_STATE;
			mCrc.updateCRC(ch2);
		} else {
			endBlock();
			initBlock();
			setupBlock();
		}
	}

	private void setupNoRandPartA() throws IOException {
		if (i2 <= last) {
			chPrev = ch2;
			ch2 = ll8[tPos];
			tPos = tt[tPos];
			i2++;

			currentChar = ch2;
			currentState = NO_RAND_PART_B_STATE;
			mCrc.updateCRC(ch2);
		} else {
			endBlock();
			initBlock();
			setupBlock();
		}
	}

	private void setupRandPartB() throws IOException {
		if (ch2 != chPrev) {
			currentState = RAND_PART_A_STATE;
			count = 1;
			setupRandPartA();
		} else {
			count++;
			if (count >= 4) {
				z = ll8[tPos];
				tPos = tt[tPos];
				if (rNToGo == 0) {
					rNToGo = rNums[rTPos];
					rTPos++;
					if (rTPos == 512) {
						rTPos = 0;
					}
				}
				rNToGo--;
				z ^= ((rNToGo == 1) ? 1 : 0);
				j2 = 0;
				currentState = RAND_PART_C_STATE;
				setupRandPartC();
			} else {
				currentState = RAND_PART_A_STATE;
				setupRandPartA();
			}
		}
	}

	private void setupRandPartC() throws IOException {
		if (j2 < z) {
			currentChar = ch2;
			mCrc.updateCRC(ch2);
			j2++;
		} else {
			currentState = RAND_PART_A_STATE;
			i2++;
			count = 0;
			setupRandPartA();
		}
	}

	private void setupNoRandPartB() throws IOException {
		if (ch2 != chPrev) {
			currentState = NO_RAND_PART_A_STATE;
			count = 1;
			setupNoRandPartA();
		} else {
			count++;
			if (count >= 4) {
				z = ll8[tPos];
				tPos = tt[tPos];
				currentState = NO_RAND_PART_C_STATE;
				j2 = 0;
				setupNoRandPartC();
			} else {
				currentState = NO_RAND_PART_A_STATE;
				setupNoRandPartA();
			}
		}
	}

	private void setupNoRandPartC() throws IOException {
		if (j2 < z) {
			currentChar = ch2;
			mCrc.updateCRC(ch2);
			j2++;
		} else {
			currentState = NO_RAND_PART_A_STATE;
			i2++;
			count = 0;
			setupNoRandPartA();
		}
	}

	private void setDecompressStructureSizes(int newSize100k) {
//		if (!(0 <= newSize100k && newSize100k <= 9 && 0 <= blockSize100k
//			&& blockSize100k <= 9)) {
//
//		}

		blockSize100k = newSize100k;
		if (newSize100k == 0)
			return;

		int n = baseBlockSize * newSize100k;
		ll8 = new char[n];
		tt = new int[n];
	}
}