package jcog.memoize.byt;

import jcog.Str;

import java.util.Arrays;

public interface ByteKey  {

    /** of size equal or greater than length() */
    byte[] array();

    int length();

    @Override int hashCode();

    boolean equals(ByteKeyExternal y, int at, int len);

//    byte at(int i);

    default boolean equals(ByteKeyExternal y, int len) {
        return equals(y, 0, len);
    }

    static String toString(ByteKey b) {
        return Str.i(b.array(),0, b.length(), 16) + " [" + Integer.toUnsignedString(b.hashCode(),32) + ']';
    }

    static boolean equals(ByteKey x, ByteKey y) {
        return x==y || (x.hashCode() == y.hashCode() && _equals(x, y));
    }

    static boolean _equals(ByteKey x, ByteKey y) {
        int l = x.length();
        if (l == y.length()) {
            if (x instanceof ByteKeyExternal) {
                if (y instanceof ByteKeyExternal)
                    throw new UnsupportedOperationException();
                    //for (int i = 0; i < l; i++) { if (x.at(i)!=y.at(i)) return false; } //TODO optmize by array comparison
                    //return true;

                return y.equals((ByteKeyExternal)x, l);

            } else {
                if (y instanceof ByteKeyExternal) {
                    return x.equals((ByteKeyExternal)y, l);
                } else {
                    return Arrays.equals(x.array(), 0, l, y.array(), 0, l);
                }
            }
        }
        return false;
    }
}
