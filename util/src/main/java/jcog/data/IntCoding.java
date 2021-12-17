package jcog.data;

import com.google.common.io.ByteArrayDataOutput;
import jcog.data.byt.DynBytes;

import java.io.DataInput;
import java.io.IOException;

/**
 * ZigZagCodings implements encoder and decoder of ZigZag encoding.
 * adapted from:
 *      https://github.com/intbit/intbit
 *
 * <pre>
 * import static io.github.intbit.ZigZagCodings.*;
 *
 * long encoded = encodeZigZagLong(-32L);
 * encodeZigZagLong(encoded) == -32;
 * </pre>
 */
public enum IntCoding {;

    /**
     * Encode a signed long into an unsigned long using a ZigZag encoding.
     *
     * @param signed a 64-bit signed integer to be encoded.
     * @return encoded 64-bit unsigned integer.
     */
    public static long encodeZigZagLong(long signed) {
        return (signed << 1) ^ (signed >> 63);
    }

    /**
     * Decode an unsigned long encoded by ZigZag encoding into a signed long.
     *
     * @param unsigned a 64-bit unsigned integer to be decoded.
     * @return decoded 64-bit signed integer.
     */
    public static long decodeZigZagLong(long unsigned) {
        long t = (((unsigned << 63) >> 63) ^ unsigned) >> 1;
        return t ^ (unsigned & (1L << 63));
    }

    /**
     * Encode a signed int into an unsigned int using a ZigZag encoding.
     *
     * @param signed a 32-bit signed integer to be encoded.
     * @return encoded 32-bit unsigned integer.
     */
    public static int encodeZigZagInt(int signed) {
        return (signed << 1) ^ (signed >> 31);
    }

    /**
     * Decode an unsigned int encoded by ZigZag encoding into a signed int.
     *
     * @param unsigned a 32-bit unsigned integer to be decoded.
     * @return decoded 32-bit signed integer.
     */
    public static int decodeZigZagInt(int unsigned) {
        int t = (((unsigned << 31) >> 31) ^ unsigned) >> 1;
        return t ^ (unsigned & (1 << 31));
    }

    /**
     * Calculate number of necessary bytes to encode an unsigned long using variable byte encoding.
     * <p>
     * Return value is at least 1 and at most 10.
     *
     * @param unsigned a 64-bit unsigned integer to be encoded.
     * @return number of bytes to be used by variable byte encoding.
     */
    public static int variableByteLengthOfUnsignedLong(long unsigned) {
        if (unsigned == 0) {
            return 1;
        }
        int bits = 64 - Long.numberOfLeadingZeros(unsigned);
        return (bits + 6) / 7;
    }

    /**
     * Calculate number of necessary bytes to encode an unsigned int using variable byte encoding.
     * <p>
     * Return value is at least 1 and at most 5.
     *
     * @param unsigned a 32-bit unsigned integer to be encoded.
     * @return number of bytes to be used by variable byte encoding.
     */
    public static int variableByteLengthOfUnsignedInt(int unsigned) {
        return unsigned == 0 ? 1 : (((32 - Integer.numberOfLeadingZeros(unsigned)) + 6) / 7);
    }

    /**
     * Calculate number of necessary bytes to encode a signed long using ZigZag encoding and variable byte encoding.
     *
     * @param signed a 64-bit signed integer to be encoded.
     * @return number of bytes to be used by ZigZag encoding and variable byte encoding.
     */
    public static int variableByteLengthOfZigZagLong(long signed) {
        return variableByteLengthOfUnsignedLong(encodeZigZagLong(signed));
    }

    /**
     * Calculate number of necessary bytes to encode a signed int using ZigZag encoding and variable byte encoding.
     *
     * @param signed a 32-bit signed integer to be encoded.
     * @return number of bytes to be used by ZigZag encoding and variable byte encoding.
     */
    public static int variableByteLengthOfZigZagInt(int signed) {
        return variableByteLengthOfUnsignedInt(encodeZigZagInt(signed));
    }

    /**
     * Encode an unsigned long using variable byte encoding.
     * <p>
     * Destination buffer must have at least enough capacity calculated by {@link #variableByteLengthOfUnsignedLong(long)}
     * method.
     *
     * @param unsigned a 64-bit unsigned integer to be encoded.
     * @param dst      byte array to store the encoded bytes.
     * @param dstOff   offset of the byte array.
     * @return number of written bytes.
     *
     */
    public static int encodeUnsignedVariableLong(long unsigned, byte[] dst, int dstOff) {
        int count = 0;
        while ((unsigned & 0xffffffffffffff80L) != 0L) {
            dst[dstOff + (count++)] = (byte) ((unsigned & 0x7fL) | 0x80L);
            unsigned >>>= 7L;
        }
        dst[dstOff + (count++)] = (byte) (unsigned & 0x7fL);
        return count;
    }

    /**
     * Encode an unsigned int using variable byte encoding.
     * <p>
     * Destination buffer must have at least enough capacity calculated by {@link #variableByteLengthOfUnsignedInt(int)}
     * method.
     *
     * @param unsigned a 32-bit unsigned integer to be encoded.
     * @param dst      byte array to store the encoded bytes.
     * @param dstOff   offset of the byte array.
     * @return number of written bytes.
     *
     */
    public static int encodeUnsignedVariableInt(int unsigned, byte[] dst, int dstOff) {
        int target = dstOff;
        while ((unsigned & 0xffffff80) != 0L) {
            dst[target++] = (byte) ((unsigned & 0x7f) | 0x80);
            unsigned >>>= 7;
        }
        dst[target++] = (byte) (unsigned & 0x7f);
        return target - dstOff;
    }

// TODO
//    private long readVarEncodedUnsignedLong() {
//        long unsigned = 0;
//        int i = 0;
//        long b;
//        while (((b = bytes[len] & 0xff) & 0x80) != 0) {
//            unsigned |= (b & 0x7f) << i;
//            i += 7;
//            len++;
//        }
//        len++;
//        return unsigned | (b << i);
//    }


    public static int readUnsignedVariableInt(DataInput in) throws IOException {
        if (in instanceof DynBytes)
            return ((DynBytes)in).readVarEncodedUnsignedInt();
        else {
            int unsigned = 0;
            int i = 0;
            int b;
            while (((b = in.readByte() & 0xff) & 0x80) != 0) {
                unsigned |= (b & 0x7f) << i;
                i += 7;
            }
            return unsigned | (b << i);
        }
    }

    public static int readZigZagInt(DataInput in) throws IOException {
        return decodeZigZagInt(readUnsignedVariableInt(in));
    }

    public static void writeZigZagInt(int dt, ByteArrayDataOutput out) {
        writeUnsignedVariableInt(encodeZigZagInt(dt), out);
    }

    public static void writeUnsignedVariableInt(int x, ByteArrayDataOutput out) {
        if (out instanceof DynBytes) {
            ((DynBytes)out).writeUnsignedInt(x);
        } else {
            while ((x & 0xffffff80) != 0L) {
                out.writeByte((byte) ((x & 0x7f) | 0x80));
                x >>>= 7;
            }
            out.writeByte((byte) (x & 0x7f));
        }
    }

    /**
     * Encode a signed long using ZigZag encoding and variable byte encoding.
     * <p>
     * Destination buffer must have at least enough capacity calculated by {@link #variableByteLengthOfZigZagLong(long)}
     * method.
     *
     * @param signed a 64-bit signed integer to be encoded.
     * @param dst    byte array to store the encoded bytes.
     * @param dstOff offset of the byte array.
     * @return dstOff + number of written bytes.
     *
     */
    public static int encodeZigZagVariableLong(long signed, byte[] dst, int dstOff) {
        return encodeUnsignedVariableLong(encodeZigZagLong(signed), dst, dstOff);
    }

    /**
     * Encode a signed int using ZigZag encoding and variable byte encoding.
     * <p>
     * Destination buffer must have at least enough capacity calculated by {@link #variableByteLengthOfZigZagInt(int)}
     * method.
     *
     * @param signed a 64-bit signed integer to be encoded.
     * @param dst    byte array to store the encoded bytes.
     * @param dstOff offset of the byte array.
     * @return dstOff + number of written bytes.
     */
    public static int encodeZigZagVariableInt(int signed, byte[] dst, int dstOff) {
        return encodeUnsignedVariableInt(encodeZigZagInt(signed), dst, dstOff);
    }



}
