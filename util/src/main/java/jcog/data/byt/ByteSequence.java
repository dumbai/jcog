package jcog.data.byt;

import jcog.Util;

public interface ByteSequence {

    ByteSequence EMPTY = new ByteSequence() {

        @Override
        public String toString() {
            return "";
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || ((ByteSequence)obj).length() == 0;
        }

        /** see: Util.hash(...) */
        @Override public int hashCode() {
            return 1;
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public byte at(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ByteSequence subSequence(int start, int end) {
            throw new UnsupportedOperationException();
        }
    };

    int length();

    byte at(int index);

    ByteSequence subSequence(int start, int end);

    default void toArray(byte[] c, int offset) {
        int l = length();
        for (int i = 0; i < l; )
            c[offset++] = at(i++);
    }

    /** clones a new copy TODO when can it share a ref, if start==0 and end==length ? */
    default byte[] arrayCompactDirect() {
        byte[] b = new byte[length()];
        toArray(b, 0);
        return b;
    }

    default int hashCode(int xStart, int xEnd) {
        return Util.hash(subSequence(xStart, xEnd), 0, xEnd-xStart);
    }

    class OneByteSeq implements ByteSequence /*implements CharSequence*/ {
        public final byte b;

        public OneByteSeq(byte b) {
            this.b = b;
        }

        @Override
        public void toArray(byte[] c, int offset) { c[offset] = b; }

        @Override
        public byte[] arrayCompactDirect() {
            return new byte[] { b };
        }

        @Override
        public int hashCode() {
            return Util.hash(this, 0, 1);
        }

        @Override
        public int length() {
            return 1;
        }

        @Override
        public byte at(int index) {
            if (index!=0)
                throw new RuntimeException();

            return this.b;
        }

        @Override
        public ByteSequence subSequence(int start, int end) {
            if ((start == 0) && (end == 1))
                return this;

            throw new UnsupportedOperationException();
        }

        public String toString() {
            return String.valueOf((char)b);
        }

    }


}
