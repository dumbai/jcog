package jcog.data.map;

import jcog.TODO;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;


/** compact thread-safe atomic Map implemented as an array of key,value pairs
 *  items are, by default, unsorted so access is sequential O(n) so this is
 *  limited to small size.
 *
 *  TODO make a key-sorted impl for faster access
 * */
public class CompactArrayMap<K, V> implements LazyMap<K, V> {

    private static final AtomicReferenceFieldUpdater<CompactArrayMap, Object[]> ITEMS = AtomicReferenceFieldUpdater.newUpdater(CompactArrayMap.class, Object[].class, "items");
    //private static final VarHandle ITEMS = Util.VAR(CompactArrayMap.class, "items", Object[].class);
//    private static final VarHandle AA = MethodHandles.arrayElementVarHandle(Object[].class);

    @SuppressWarnings("VolatileArrayField")
    volatile Object[] items;

    public CompactArrayMap() {
    }

    public CompactArrayMap(K initialKey, V initialValue) {
        this(new Object[]{initialKey, initialValue});
    }

    public CompactArrayMap(@Nullable Object[] initial) {
        assert(initial == null || initial.length%2==0);
        this.items = initial;
    }

    public static boolean containsValue(Object aValue) {
        throw new TODO();
    }

    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public V get(Object key) {
        Object[] a = array();
        if (a != null) {
            int i = keyIndex(a, key);
            if (i!=-1)
                return (V)a[i];
        }
        return null;
    }

    public int size() {
        Object[] o = array();
        return o != null ? o.length / 2 : 0;
    }

    /**
     * returns previous value, or null if none - like Map.put
     * interpets value==null as removal
     */
    public V put(Object key, V value) {
        Insertion i = new Insertion();
        ITEMS.accumulateAndGet(this, new Object[]{key, value}, i);
        return (V)i.returned;
    }

    /** TODO binary search if comparable */
    static int keyIndex(Object[] a, Object k) {
        int s = a.length;
        for (int i = 0; i < s; i += 2) {
            if (keyEquals(k, a[i]))
                return i+1;
        }
        return -1;
    }

    @Override
    public V computeIfAbsent(K key, Supplier<? extends V> f) {
        V e = get(key);
        if (e != null)
            return e;

        V v = f.get();
        V v2 = put(key, v);
        return v2!=null ? v2 /* previously computed ('putIfAbsent') */: v;
    }

    public V remove(Object key) {
        return put(key, null);
    }

    /**
     * override for alternate equality test
     */
    static boolean keyEquals(Object a, Object b) {
        return a.equals(b);
    }

    public void clear() {
        ITEMS.lazySet(this, null);
    }

    public Object[] clearPut(K key, V value) {
        return clearPut(new Object[] { key, value });

    }
    public Object[] clearPut(Object[] v) {
        return ITEMS.getAndSet(this, v);
    }

    public void forEach(BiConsumer<K, V> each) {
        whileEach((k,v)->{
            each.accept(k,v);
            return true;
        });
    }

    /** plain reads */
    public boolean whileEach(BiPredicate<K, V> each) {
        Object[] ii = array();
        for (int i = 0, iiLength = ii.length; i < iiLength; ) {
            Object k = ii[i++], v = ii[i++];
            if (!each.test((K)k,(V)v))
                return false;
        }
        return true;
    }

    public final Object[] array() {
        return ITEMS.get(this);
    }

    private static class Insertion implements BinaryOperator<Object[]> {

        Object returned;

        @Override
        public Object[] apply(Object[] a, Object[] kv) {
            if (a == null) {
                return kv;
            } else {

                int n = a.length;
                Object k = kv[0];
                Object v = kv[1];

                int found = keyIndex(a, k);
                if (found != -1) {
                    returned = a[found];
                    if (v != null) {
                        a[found] = v;
                        return a;
                    } else {
                        if (n != 2) {
                            Object[] b = Arrays.copyOf(a, n - 2);
                            if (found - 1 < n - 2)
                                System.arraycopy(a, found + 1, b, found - 1, n - (found - 1) - 2); //TODO test

                            return b;
                        } else {
                            return null; //map emptied
                        }
                    }
                } else {
                    if (v != null) {
                        Object[] b = Arrays.copyOf(a, n + 2);
                        b[n++] = k;
                        b[n] = v;
                        return b;
                    } else {
                        return a; //tried to remove key which isnt presented; no effect
                    }
                }

            }
        }
    }


//    public void clearExcept(K key) {
//
//        V exist = get(key);
//        clear();
//        if (exist != null)
//            put(key, exist);
//
//    }

}