/*
 * Copyright 2015 The SageTV Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcog.io;

import java.io.*;

/* buffers writes to a RandomAccessFile */
/* flush() must be called after you're done writing a chunk.
 * TODO needs Test */
public class FastRandomFile implements DataOutput, DataInput
{
  protected RandomAccessFile raf;
    protected byte[] buff = new byte[65536];
  protected int buffptr = 0;
  protected long fp = 0;

  public FastRandomFile(String name, String mode, String inCharset) throws IOException
  {
    myCharset = inCharset;



    raf = new RandomAccessFile(name, mode);


  }

















  
  protected FastRandomFile(String inCharset) {
    myCharset = inCharset;
    
  }

  public long getFilePointer()
  {
    return fp;
  }

  public void close() throws IOException
  {
    flush();

    try {
      raf.getFD().sync();
    } catch (IOException t) {

    }

    raf.close();
    buff = null;
  }

  public void fullFlush() throws IOException
  {
    flush();
    raf.getFD().sync();
  }

  public void write(int b) throws IOException
  {
    write((byte)(b & 0xFF));
  }

  public void write(byte b) throws IOException
  {




    if (circularFileSize > 0 && fp == circularFileSize)
      seek(0);
    fp++;
    buff[buffptr++] = b;
    if (buffptr == buff.length)
      flush();
  }

  public void writeUnencryptedByte(byte b) throws IOException
  {
    fp++;
    buff[buffptr++] = b;
    if (buffptr == buff.length)
      flush();
  }

  public void flush() throws IOException
  {
    if (buffptr > 0)
    {
      raf.write(buff, 0, buffptr);
      buffptr = 0;
    }
  }

  
  
  
  
  private final byte[] intWriteBuf = new byte[4];
  public void writeIntAtOffset(long targetFp, int s) throws IOException
  {
    byte b1 = (byte)((s >>> 24) & 0xFF);
    byte b2 = (byte)((s >>> 16) & 0xFF);
    byte b3 = (byte)((s >>> 8) & 0xFF);
    byte b4 = (byte)(s & 0xFF);







    
    
    int skipBackWriteAmount = Math.min(4, (int)(fp - buffptr - targetFp));
    if (skipBackWriteAmount > 0)
    {
      intWriteBuf[0] = b1; intWriteBuf[1] = b2; intWriteBuf[2] = b3; intWriteBuf[3] = b4;
      raf.seek(targetFp);
      raf.write(intWriteBuf, 0, skipBackWriteAmount);
      raf.seek(fp - buffptr);
    }

    if (skipBackWriteAmount < 1)
      buff[buffptr - ((int)(fp - targetFp))] = b1;
    if (skipBackWriteAmount < 2)
      buff[buffptr - ((int)(fp - targetFp)) + 1] = b2;
    if (skipBackWriteAmount < 3)
      buff[buffptr - ((int)(fp - targetFp)) + 2] = b3;
    if (skipBackWriteAmount < 4)
      buff[buffptr - ((int)(fp - targetFp)) + 3] = b4;
  }

  public void write(byte[] b, int off, int len) throws IOException
  {
    if (/*crypto || */circularFileSize > 0)
    {
      while (len-- > 0)
        write(b[off++]);
    }
    else
    {
      while (len > 0)
      {
        int rem = buff.length - buffptr;
        if (len < rem)
        {
          System.arraycopy(b, off, buff, buffptr, len);
          buffptr += len;
          fp += len;
          break;
        }
        else
        {
          System.arraycopy(b, off, buff, buffptr, rem);
          buffptr += rem;
          off += rem;
          len -= rem;
          fp += rem;
          flush();
        }
      }
    }
  }

  public void write(byte[] b)	throws IOException
  {
    write(b, 0, b.length);
  }

  public void seek(long newfp) throws IOException
  {
    flush();
    raf.seek(fp = newfp);
  }

  public int read() throws IOException
  {








    {
      fp++;
      return raf.read();
    }
  }

  public boolean readBoolean() throws IOException
  {
    int ch = read();
    if (ch < 0)
      throw new EOFException();
    return (ch != 0);
  }

  public byte readByte() throws IOException
  {
    int ch = read();
    if (ch < 0)
      throw new EOFException();
    return (byte)(ch);
  }

  public byte readUnencryptedByte() throws IOException
  {
    fp++;
    int ch = raf.read();
    if (ch < 0)
      throw new EOFException();
    return (byte)(ch);
  }

  public int readInt() throws IOException
  {
    int b1 = this.read();
    int b2 = this.read();
    int b3 = this.read();
    int b4 = this.read();
    if ((b1 | b2 | b3 | b4) < 0)
      throw new EOFException();
    return (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
  }

  public long readLong() throws IOException
  {
    return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
  }

  
  private byte[] bytearr;
  private char[] chararr;
  
  
  public String readUTF()	throws IOException
  {
    if (isI18N)
    {
      int utflen = readUnsignedShort();
        switch (utflen) {
            case 0:
                return "";
            case 0xFFFF:
                utflen = readInt();
                break;
        }
      if (bytearr == null || bytearr.length < utflen)
      {
        bytearr = new byte[utflen*2];
        chararr = new char[utflen*2];
      }

        readFully(bytearr, 0, utflen);

        int outcount = 0;
        int incount = 0;
        int c;
        while (incount < utflen) {
        
        c = bytearr[incount] & 0xFF;
        if (c > 127) break;
        incount++;
        chararr[outcount++]=(char)c;
      }

        while (incount < utflen) {
        c = bytearr[incount] & 0xFF;
        if (c < 128) {
          incount++;
          chararr[outcount++]=(char)c;
          continue;
        }

            int x = c >> 4;
            int c2;
            switch (x) {
                case 12, 13 -> {
                    incount += 2;
                    if (incount > utflen)
                        throw new UTFDataFormatException("bad UTF data: missing second byte of 2 byte char at " + incount);
                    c2 = bytearr[incount - 1];
                    if ((c2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException("bad UTF data: second byte format after 110xxxx is wrong char: 0x" +
                                Integer.toString(c2, 16) + " count: " + incount);
                    chararr[outcount++] = (char) (((c & 0x1F) << 6) | (c2 & 0x3F));
                }
                case 14 -> {
                    incount += 3;
                    if (incount > utflen)
                        throw new UTFDataFormatException("bad UTF data: missing extra bytes of 3 byte char at " + incount);
                    c2 = bytearr[incount - 2];
                    int c3 = bytearr[incount - 1];
                    if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException("bad UTF data: extra byte format after 1110xxx is wrong char2: 0x" +
                                Integer.toString(c2, 16) + " char3: " + Integer.toString(c3, 16) + " count: " + incount);
                    chararr[outcount++] = (char) (((c & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F));
                }
                default -> throw new UTFDataFormatException("bad UTF data: we don't support more than 16 bit chars char: " +
                        Integer.toString(c, 16) + " count:" + incount);
            }
      }
      return new String(chararr, 0, outcount);
    }
    else
    {
      int len = readShort();
      if (len > 0)
      {
        byte[] bytes = new byte[len];
        readFully(bytes, 0, len);
        return new String(bytes, myCharset);
      }
      else return "";
    }
  }

  public StringBuffer readUTF(StringBuffer sb) throws IOException
  {
    if (isI18N)
    {
      int utflen = readUnsignedShort();
      if (utflen == 0xFFFF)
        utflen = readInt();
      if (bytearr == null || bytearr.length < utflen)
      {
        bytearr = new byte[utflen*2];
        chararr = new char[utflen*2];
      }
      if (sb == null)
        sb = new StringBuffer(utflen);
      else
        sb.setLength(utflen);

        readFully(bytearr, 0, utflen);

        int outcount = 0;
        int incount = 0;
        int c;
        while (incount < utflen) {
        
        c = bytearr[incount] & 0xFF;
        if (c > 127) break;
        incount++;
        sb.setCharAt(outcount++, (char)c);
      }

        while (incount < utflen) {
        c = bytearr[incount] & 0xFF;
        if (c < 128) {
          incount++;
          sb.setCharAt(outcount++, (char)c);
          continue;
        }

            int x = c >> 4;
            int c2;
            switch (x) {
                case 12, 13 -> {
                    incount += 2;
                    if (incount > utflen)
                        throw new UTFDataFormatException("bad UTF data: missing second byte of 2 byte char at " + incount);
                    c2 = bytearr[incount - 1];
                    if ((c2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException("bad UTF data: second byte format after 110xxxx is wrong char: 0x" +
                                Integer.toString(c2, 16) + " count: " + incount);
                    sb.setCharAt(outcount++, (char) (((c & 0x1F) << 6) | (c2 & 0x3F)));
                }
                case 14 -> {
                    incount += 3;
                    if (incount > utflen)
                        throw new UTFDataFormatException("bad UTF data: missing extra bytes of 3 byte char at " + incount);
                    c2 = bytearr[incount - 2];
                    int c3 = bytearr[incount - 1];
                    if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException("bad UTF data: extra byte format after 1110xxx is wrong char2: 0x" +
                                Integer.toString(c2, 16) + " char3: " + Integer.toString(c3, 16) + " count: " + incount);
                    sb.setCharAt(outcount++, (char) (((c & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F)));
                }
                default -> throw new UTFDataFormatException("bad UTF data: we don't support more than 16 bit chars char: " +
                        Integer.toString(c, 16) + " count:" + incount);
            }
      }
      sb.setLength(outcount);
      return sb;
    }
    else
    {
      
      int len = readShort();
      if (len > 0)
      {
        byte[] bytes = new byte[len];
        readFully(bytes, 0, len);
        return new StringBuffer(new String(bytes, myCharset));
      }
      else return new StringBuffer();
    }
  }
  public void writeUTF(String s) throws IOException
  {
    
    if (s == null) s = "";
    if (isI18N)
    {
      int strlen = s.length();
      int utflen = 0;
      int c = 0;

      for (int i = 0; i < strlen; i++) {
        c = s.charAt(i);
        if (c < 128) {
          utflen++;
        } else if (c > 0x07FF) {
          utflen += 3;
        } else {
          utflen += 2;
        }
      }

      if (utflen >= 0xFFFF)
      {
        write((byte)0xFF);
        write((byte)0xFF);
        write((byte) ((utflen >>> 24) & 0xFF));
        write((byte) ((utflen >>> 16) & 0xFF));
        write((byte) ((utflen >>> 8) & 0xFF));
        write((byte) ((utflen) & 0xFF));
      }
      else
      {
        write((byte) ((utflen >>> 8) & 0xFF));
        write((byte) ((utflen) & 0xFF));
      }
      for (int i = 0; i < strlen; i++) {
        c = s.charAt(i);
        if (c < 128) {
          write((byte) c);
        } else if (c > 0x07FF) {
          write((byte) (0xE0 | ((c >> 12) & 0x0F)));
          write((byte) (0x80 | ((c >>  6) & 0x3F)));
          write((byte) (0x80 | (c & 0x3F)));
        } else {
          write((byte) (0xC0 | ((c >>  6) & 0x1F)));
          write((byte) (0x80 | (c & 0x3F)));
        }
      }
    }
    else
    {
      if (s.length() > Short.MAX_VALUE)
      {
        System.out.println("WARNING: String length exceeded 32k!!! Truncating...");
        writeShort(Short.MAX_VALUE);
        write(s.substring(0, Short.MAX_VALUE).getBytes(myCharset));
      }
      else
      {
        writeShort((short)s.length());
        write(s.getBytes(myCharset));
      }
    }
  }

  public void writeBoolean(boolean b)	throws IOException
  {
    if (b)
      write((byte)1);
    else
      write((byte)0);
  }

  public void writeByte(int b) throws IOException
  {
    write(b);
  }

  public final void writeBytes(String s) throws IOException
  {
    int len = s.length();
    for (int i = 0; i < len; i++)
      write((byte)s.charAt(i));
  }

  private final byte[] writeBuffer = new byte[8];
  public void writeInt(int s)	throws IOException
  {
    writeBuffer[0] = (byte)((s >>> 24) & 0xFF);
    writeBuffer[1] = (byte)((s >>> 16) & 0xFF);
    writeBuffer[2] = (byte)((s >>> 8) & 0xFF);
    writeBuffer[3] = (byte)(s & 0xFF);
    write(writeBuffer, 0, 4);
  }

  public void writeLong(long s) throws IOException
  {
    writeBuffer[0] = (byte)((s >>> 56) & 0xFF);
    writeBuffer[1] = (byte)((s >>> 48) & 0xFF);
    writeBuffer[2] = (byte)((s >>> 40) & 0xFF);
    writeBuffer[3] = (byte)((s >>> 32) & 0xFF);
    writeBuffer[4] = (byte)((s >>> 24) & 0xFF);
    writeBuffer[5] = (byte)((s >>> 16) & 0xFF);
    writeBuffer[6] = (byte)((s >>> 8) & 0xFF);
    writeBuffer[7] = (byte)(s & 0xFF);
    write(writeBuffer, 0, 8);
  }

  public long length() throws IOException
  {
    flush();
    return raf.length();
  }

  public final void writeFloat(float v) throws IOException
  {
    writeInt(Float.floatToIntBits(v));
  }

  public final void writeDouble(double v) throws IOException
  {
    writeLong(Double.doubleToLongBits(v));
  }

  public final float readFloat() throws IOException
  {
    return Float.intBitsToFloat(readInt());
  }

  public final double readDouble() throws IOException
  {
    return Double.longBitsToDouble(readLong());
  }

  public void setLength(long len) throws IOException
  {
    flush();
    fp = Math.min(fp, len);
    raf.setLength(len);
  }

  public short readShort() throws IOException
  {
    int b1 = this.read();
    int b2 = this.read();
    if ((b1 | b2) < 0)
      throw new EOFException();
    return (short)((b1 << 8) + b2);
  }

  public final void writeShort(int v) throws IOException
  {
    write((byte)((v >>> 8) & 0xFF));
    write((byte)(v & 0xFF));
  }

  public final void writeChar(int v) throws IOException
  {
    writeShort(v);
  }

  public final void writeChars(String s) throws IOException
  {
    int len = s.length();
    for (int i = 0 ; i < len ; i++)
    {
      int v = s.charAt(i);
      write((byte)((v >>> 8) & 0xFF));
      write((byte)(v & 0xFF));
    }
  }

  public void readFully(byte[] b, int off, int len) throws IOException
  {
    raf.readFully(b, off, len);









      fp += len;
  }

  public char readChar() throws IOException
  {
    int b1 = this.read();
    int b2 = this.read();
    if ((b1 | b2) < 0)
      throw new EOFException();
    return (char)((b1 << 8) + b2);
  }

  public void readFully(byte[] b) throws IOException
  {
    readFully(b, 0, b.length);
  }

  public String readLine() {
    throw new UnsupportedOperationException();
  }

  public int readUnsignedByte() throws IOException
  {
    int b = this.read();
    if (b < 0)
      throw new EOFException();
    return b;
  }

  public int readUnsignedShort() throws IOException
  {
    int b1 = this.read();
    int b2 = this.read();
    if ((b1 | b2) < 0)
      throw new EOFException();
    return (b1 << 8) + b2;
  }

  public int skipBytes(int n) throws IOException
  {
    seek(getFilePointer() + n);
    return n;
  }



  public void setCircularSize(long x) { circularFileSize = x; }

  protected String myCharset;
  protected boolean isI18N;


  protected long circularFileSize;
}