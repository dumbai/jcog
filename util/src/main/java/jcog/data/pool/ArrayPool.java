/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http:
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose, 
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package jcog.data.pool;

import jcog.TODO;
import jcog.data.list.Lst;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Object pool for arrays.
 *
 * @author jezek2
 */
public class ArrayPool<T> extends Lst<T> {

    private static final ThreadLocal<ArrayPool<byte[]>> bytes =
            ThreadLocal.withInitial(() -> new ArrayPool<>(byte[].class) {
                @Override
                protected byte[] create(int length) {
                    return new byte[length];
                }

                @Override
                protected int len(byte[] bytes) {
                    return bytes.length;
                }
            });
    private static final ThreadLocal<ArrayPool<short[]>> shorts =
            ThreadLocal.withInitial(() -> new ArrayPool<>(short[].class) {
                @Override
                protected short[] create(int length) {
                    return new short[length];
                }

                @Override
                protected int len(short[] bytes) {
                    return bytes.length;
                }
            });

    static final int capacity = 16;

    private static final ThreadLocal<Map<Class, ArrayPool>> typed = ThreadLocal.withInitial(HashMap::new);

    private final Class componentType;
    private final boolean primitive;

    /**
     * Returns per-thread array pool for given type, or create one if it doesn't exist.
     *
     * @param cls type
     * @return object pool
     */
    public static ArrayPool the(Class cls) {
        return typed.get().computeIfAbsent(cls, ArrayPool::new);
    }

    public static void cleanCurrentThread() {
        typed.remove();
    }

    public static ArrayPool<byte[]> bytes() {
        return bytes.get();
    }
    public static ArrayPool<short[]> shorts() {
        return shorts.get();
    }

    @Deprecated
    abstract static class ArrayLengthMatcher implements Comparator {
        public int value;

        @Override
        public int compare(Object o1, Object o2) {
            boolean isOne = (o1 == this);
            int len1 = isOne ? value : len(o1);
            int len2 = isOne ? len(o2) : value;
            return Integer.compare(len1, len2);
        }

        abstract int len(Object o);
    }

    static class ObjectArrayLengthMatcher extends ArrayLengthMatcher {

        @Override
        int len(Object o) {
            return ((Object[]) o).length;
        }
    }

    /**
     * Creates object pool.
     *
     * @param arrayType
     */
    public ArrayPool(Class arrayType) {

        super(capacity);

        this.componentType = arrayType.getComponentType();

        this.primitive = componentType.isPrimitive();
        ArrayLengthMatcher comparator;
        if (this.primitive) {
            if (componentType == byte.class) {
                comparator = new ByteArrayLengthMatcher();
            } else if (componentType == short.class) {
                comparator = new ShortArrayLengthMatcher();
            } else {
                throw new UnsupportedOperationException("TODO " + componentType);
            }
        } else {
            comparator = new ObjectArrayLengthMatcher();
        }














    }

    @SuppressWarnings("unchecked")
    protected T create(int length) {
        return (T) Array.newInstance(componentType, length);
    }

    /**
     * Returns array of exactly the same length as demanded, or create one if not
     * present in the pool.
     *
     * @param length
     * @return array
     */
    @SuppressWarnings("unchecked")
    public T getExact(int length) {
        throw new TODO();
//        comparator.value = length;
//        int index = Collections.binarySearch(this, comparator, comparator);
//        if (index < 0) {
//            return create(length);
//        }
//        return remove(index);
    }

    /**
     * Returns array that has same or greater length, or create one if not present
     * in the pool.
     *
     * @param minLength the minimum length required
     * @return array
     */
    @SuppressWarnings("unchecked")
    public T getMin(int minLength) {
//        comparator.value = minLength;
//        int index = Collections.binarySearch(this, comparator, comparator);
//        if (index < 0) {
//            index = -index - 1;
//            return index < size ? remove(index) : create(minLength);
//        }
//        return remove(index);

        //HACK TODO use binary selection
        int s = this.size;
        for (int i = s-1; i >= 0; i--) {
            T t = get(i);
            if (len(t) >= minLength) {
                removeFast(i);
                return t;
            }
        }
        return create(minLength);
    }

    /**
     * Releases array into object pool.
     *
     * @param array previously obtained array from this pool
     */
    @SuppressWarnings("unchecked")
    public void put(T array) {

        int s = this.size;
        if (s+1 > capacity) {
            //remove middle element to preserve dynamic range
            removeFast(s/2);
        }

        switch (s) {
            case 0:
                add(array);
                break;
            case 1:
                if (len(0) >= len(array))
                    add(array);
                else
                    add(0, array);
                break;
            default:


                //TODO use binary insertion
                int l = len(array);
                int index = s;
                for (int i = 0; i < s; i++) {
                    if (l > len(i)) {
                        index = i;
                        break;
                    }
                }
                add(index, array);
                //assertSorted();
                break;
        }
        //System.out.println(System.identityHashCode(this) + " " + size());

        
        if (!primitive)
            Arrays.fill((Object[]) array, null);
    }


    protected final int len(int index) {
        return len(get(index));
    }
    protected int len(T t) {
        return ((Object[])t).length;
    }

    private static class ByteArrayLengthMatcher extends ArrayLengthMatcher {
        @Override final int len(Object o) {
            return ((byte[])o).length;
        }
    }
    private static class ShortArrayLengthMatcher extends ArrayLengthMatcher {
        @Override final int len(Object o) {
            return ((short[])o).length;
        }
    }
}
