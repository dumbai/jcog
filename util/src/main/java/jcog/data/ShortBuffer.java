package jcog.data;

import jcog.util.ArrayUtil;

import java.util.Arrays;

public final class ShortBuffer {

    public final short[] items;
    public int size = 0;

    public ShortBuffer(int cap) {
        this.items = new short[cap];
    }

    public short[] toArray() {
        return toArray(false);
    }

    public final short[] toArray(boolean clone) {
        int size = this.size;
        if (size == 0)
            return ArrayUtil.EMPTY_SHORT_ARRAY;
        else if (!clone && size == items.length)
            return items;
        else
            return Arrays.copyOf(items, size);
    }

    public void add(short i) {
        items[size++] = i;
    }

    public void clear() {
        size = 0;
    }

    public void addAll(short[] can) {
        //int s = size;
        //short[] items = this.items;
        System.arraycopy(can, 0, items, size, can.length);
//        for (short x : can)
//            items[s++] = x;
        size += can.length;
    }
}
