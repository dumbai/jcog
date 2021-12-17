package jcog.version;

import jcog.TODO;
import jcog.data.list.Lst;
import jcog.data.set.ArrayUnenforcedSet;
import jcog.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static java.util.Collections.EMPTY_SET;
import static java.util.stream.Collectors.toList;


public class VersionMap<X, Y> implements Map<X, Y>, Function<X, Versioned<Y>> {

	public final Versioning context;
	public final Map<X, Versioned<Y>> map;

	/**
	 * @param context
	 * @param mapCap  initial capacity of map (but can grow
	 */
	public VersionMap(Versioning<Y> context) {
		this(context, new HashMap<>(0));
	}

	public VersionMap(Versioning<Y> context, Map<X, Versioned<Y>/*<Y>*/> map) {
		this.context = context;
		this.map = map;
	}

	@Override
	public void replaceAll(BiFunction<? super X, ? super Y, ? extends Y> function) {
		throw new UnsupportedOperationException();
	}

//    public boolean tryReplaceAll(BiFunction<? super X, ? super Y, ? extends Y> function) {
//        final boolean[] ok = {true};
//        map.forEach((v,val)->{
//            if (ok[0] && val!=null) {
//                Y x = val.get();
//                if (x!=null) {
//                    Y y = function.apply(v, x);
//                    if (x != y) {
//                        if (!val.replace(y))
//                            ok[0] = false;
//                    }
//                }
//            }
//        });
//        return ok[0];
//    }

	public boolean replace(BiFunction<? super X, ? super Y, ? extends Y> function) {
		Lst r = new Lst(map.size()*2);
		map.forEach((v, val) -> {
			if (val != null) {
				Y x = val.get();
				if (x != null) {
					Y y = function.apply(v, x);
					if (!x.equals(y)) {
						r.addFast(v);
						r.addFast(y);
					}
				}
			}
		});
		if (!r.isEmpty()) {
			Object[] rr = r.array();
			for (int i = 0, rrn = r.size(); i < rrn; ) {
				if (!set((X)rr[i++], (Y)rr[i++])) return false;
			}
		}
		return true;
	}

	@Override
	public @Nullable Y remove(Object key) {
		Versioned<Y> x = map.remove(key);
		return x != null ? x.get() : null;
	}

	@Override
	public void putAll(Map<? extends X, ? extends Y> map) {
		throw new TODO();
	}

	@Override
	public boolean remove(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * clears the unifier while simultaneously fast iterating assignments (in reverse) before they are pop()'d
	 */
	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Versioned<Y> apply(X x) {
		return new KeyUniVersioned<>(x);
	}

	@Override
	public int size() {
		return context.size;
	}

	@Override
	public final boolean isEmpty() {
		//return size() == 0;
		return context.size == 0; //because size() may be overriden
	}

	/**
	 * copied from AbstractMap.java
	 */
	@Override
	public String toString() {
		Iterator<Entry<X, Y>> i = this.entrySet().iterator();
		if (!i.hasNext()) {
			return "{}";
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append('{');

			while (true) {
				Entry<X, Y> e = i.next();
				X key = e.getKey();
				Y value = e.getValue();
				sb.append(key == this ? "(this Map)" : key);
				sb.append('=');
				sb.append(value == this ? "(this Map)" : value);
				if (!i.hasNext()) {
					return sb.append('}').toString();
				}

				sb.append(',').append(' ');
			}
		}
	}

	/**
	 * avoid using this if possible because it involves transforming the entries from the internal map to the external form
	 */
	@Override
	public Set<Entry<X, Y>> entrySet() {

		int s = size();
		if (s == 0)
			return EMPTY_SET;

//			Entry<X, Y>[] e = new Entry[s];
//			int[] i = new int[1];
//			forEach((k, v) -> {
//				//Y vv = v;//.get();
//				//if (vv != null)
//				AbstractMap.SimpleImmutableEntry<X, Y> x = new AbstractMap.SimpleImmutableEntry<>(k, v);
//				//e.add(x);
//				e[i[0]++] = x;
//			});
//			return new ArrayUnenforcedSet<>(e);//.toArrayRecycled(Entry[]::new));

		Lst<Entry<X, Y>> e = new Lst<>(s);
		forEach((k, v) -> {
			Y vv = v;//.get();
			if (vv != null)
				e.add(new AbstractMap.SimpleImmutableEntry<>(k, vv));
		});
		return new ArrayUnenforcedSet<>(e.toArrayRecycled(Entry[]::new));

	}

	@Override
	public Set<X> keySet() {
		int s = size();
		if (s == 0)
			return EMPTY_SET;
			//else if (s == 1) ...
		else {
			Lst<X> e = new Lst<>(s);
            forEach((k, v) -> {
                //if (v!=null)
                    e.add(k);
            });
			return new ArrayUnenforcedSet(e.toArray(ArrayUtil.EMPTY_OBJECT_ARRAY));
		}
	}

	/**
	 * @noinspection SimplifyStreamApiCallChains
	 */
	@Override
	public Collection<Y> values() {
		return entrySet().stream().map(Entry::getValue).collect(toList()); //HACK
	}

	/**
	 * records an assignment operation
	 * follows semantics of setAt()
	 */
	@Override
	public final Y put(X key, Y value) {
		throw new UnsupportedOperationException("use force(k,v)");
	}


	public final boolean set(X key, Y value) {
		UniVersioned<Y> k = (UniVersioned<Y>) getOrCreateIfAbsent(key);
		Y prev = k.value;
		if (prev == value)
			return true;

		if (!k.valid(value, context))
			return false;

		if (prev != null) {
			switch (merge(prev, value)) {
				case 0:
					return true;
				case +1:
					break; //bypasses constraints; assumes that merge has tested the new value, if necessary: valid(next, context)
				default:
					return false;
			}
		}

		if (prev != null || context.add(k)) {
			k.value = value;
			return true;
		} else
			return false; //zero capacity

	}

	/**
	 * @return value:
	 *      +1 accept, replace with new value
	 *      0 accept, keep original value
	 *      -1  refuse
	 *
	 */
	public int merge(Y prev, Y next) {
		return prev.equals(next) ? 0 : -1;
	}


	public final Versioned<Y> getOrCreateIfAbsent(X key) {
		return map.computeIfAbsent(key, this);
	}

	public void forEach(BiConsumer<? super X, ? super Y> each) {
		int s = size();
		if (s == 0) return;
		Versioned[] ii = context.items;
		for (int i = 0; i < s; i++) {
			Versioned I = ii[i];
			if (I instanceof KeyUniVersioned) {
				KeyUniVersioned<X,Y> kv = (KeyUniVersioned) I;
				each.accept(kv.key, kv.value);
			} /*else if (I instanceof KeyMultiVersioned) {
				KeyMultiVersioned<X,Y> kv = (KeyMultiVersioned) I;
				each.accept(kv.key, kv.get());
			} else*/ {
				//TODO
			}
		}
//		if (!isEmpty()) {
//			map.forEach((x, yy) -> {
//				Y y = yy.get();
//				if (y != null) each.accept(x, y);
//			});
//		}
	}

	@Deprecated public void forEachVersioned(BiPredicate<? super X, ? super Y> each) {
		if (!isEmpty()) {
			Set<Entry<X, Versioned<Y>>> ee = map.entrySet();
			for (Entry<X, Versioned<Y>> e : ee) {
				Y y = e.getValue().get();
				if (y != null && !each.test(e.getKey(), y))
					return;
			}
		}
	}

//    /** TODO test */
//    public boolean compute(/*X*/X key, UnaryOperator<Y> f) {
//        final boolean[] result = {false};
//        map.compute(key, (k, v)->{
//
//            Y prev, next;
//
//            prev = v != null ? v.get() : null;
//
//            next = f.apply(prev);
//
//            result[0] = (next != null) &&
//                    context.set((v != null ? v : (v = newEntry(k))), next);
//
//            return v;
//        });
//        return result[0];
//    }

	@Override
	public Y get(/*X*/Object key) {
		Versioned<Y> v = map.get(key);
		return v != null ? v.get() : null;
	}

	@Override
	public final boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object o) {
		throw new TODO();
	}


}