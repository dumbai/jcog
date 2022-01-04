package jcog;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.primitives.*;
import jcog.data.byt.ArrayBytes;
import jcog.data.byt.ByteSequence;
import jcog.data.list.Lst;
import jcog.data.set.ArrayUnenforcedSet;
import jcog.math.FloatSupplier;
import jcog.math.NumberException;
import jcog.util.ArrayUtil;
import org.eclipse.collections.api.block.function.primitive.DoubleToFloatFunction;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;
import org.eclipse.collections.api.block.predicate.primitive.FloatPredicate;
import org.eclipse.collections.api.list.primitive.ByteList;
import org.eclipse.collections.api.list.primitive.ImmutableByteList;
import org.eclipse.collections.api.map.primitive.ObjectIntMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.impl.EmptyIterator;
import org.eclipse.collections.impl.bag.mutable.HashBag;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.collections.impl.list.mutable.primitive.ByteArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.hipparchus.fitting.PolynomialCurveFitter;
import org.hipparchus.fitting.WeightedObservedPoint;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.StampedLock;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Thread.onSpinWait;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;

//import jcog.pri.Prioritizable;

/**
 *
 */
public enum Util {
	;


	//	static {
//		try {
//			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
//			unsafeField.setAccessible(true);
//			unsafe = (Unsafe) unsafeField.get(null);
//
////			Field unsafeNewField = Unsafe.class.getDeclaredField("theInternalUnsafe");
////			unsafeNewField.setAccessible(true);
////			unsafeNew = (jdk.internal.misc.Unsafe)..
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//
//	}
	public static final Iterator emptyIterator = EmptyIterator.getInstance();
	public static final Iterable emptyIterable = Collections.EMPTY_LIST;

	public static final double PHI = (1+Math.sqrt(5))/2;
	public static final float PHIf = (float) PHI;

	/** note: phi-1 == 1/phi */
	public static final double PHI_min_1 = PHI - 1;
	public static final float PHI_min_1f = (float) PHI_min_1;

	public static final int PRIME3 = 524287;
	public static final int PRIME2 = 92821;
	public static final int PRIME1 = 31;
	//public static final int MAX_CONCURRENCY = Runtime.getRuntime().availableProcessors();
	public static final ImmutableByteList EmptyByteList = ByteLists.immutable.empty();
	public static final double log2 = Math.log(2);
	private static final int BIG_ENOUGH_INT = 16 * 1024;
	private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
	public static final float MIN_NORMALsqrt = (float) Math.sqrt(Float.MIN_NORMAL);
	public static final Consumer[] EmptyConsumerArray = new Consumer[0];

	/** fused-multiply-add computations.
	 *  disable if VM doesnt impl fma intrinsics for the CPU. */
	private static final boolean MATH_FMA = Config.IS("math_fma", false);

	/** low precision alternatives */
	private static final boolean MATH_FAST = Config.IS("math_fast", false);

	public static double logFast(double x) {
		return 6 * (x - 1) / (x + 1 + 4 * Math.sqrt(x));
	}

	public static double log(double x) {
		return MATH_FAST ? logFast(x) : Math.log(x);
	}

    /**
	 * It is basically the same as a lookup table with 2048 entries and linear interpolation between the entries, but all this with IEEE floating point tricks.
	 * https://stackoverflow.com/questions/66402/faster-math-exp-via-jni/424985#424985
	 */
	public static double expFast(double val) {
		return Double.longBitsToDouble((long)
			(1512775 * val + (1072693248 - 60801)) << 32);
	}

	public static double exp(double x) {
		return MATH_FAST ? expFast(x) : Math.exp(x);
	}

	public static double fma(double a, double b, double c) {
		return MATH_FMA ? Math.fma(a, b, c) : a * b + c;

	}
	public static float fma(float a, float b, float c) {
		return MATH_FMA ? Math.fma(a, b, c) : a * b + c;

	}

	public static String UUIDbase64() {
		long low = UUID.randomUUID().getLeastSignificantBits();
		long high = UUID.randomUUID().getMostSignificantBits();
		return new String(Base64.getEncoder().encode(
			Bytes.concat(
				Longs.toByteArray(low),
				Longs.toByteArray(high)
			)
		));
	}


//    static final LongHashFunction _hashFn = LongHashFunction.xx();

//    public static int hash(long[] longs) {
//        return Long.hashCode(_hashFn.hashLongs(longs));
//    }

	public static int hash(ByteSequence x, int from, int to) {

		int len = to - from;
		return switch (len) {
			case 0 -> 1;
			case 1 -> x.at(from);
			case 2 -> Shorts.fromBytes(x.at(from), x.at(from+1));
			case 3 -> Ints.fromBytes(x.at(from), x.at(from+1), x.at(from + 2), (byte) 0);
			case 4 -> Ints.fromBytes(x.at(from), x.at(from+1), x.at(from + 2), x.at(from + 3));
			default -> hashFNV(x, from, to);
		};

		//return hashFNV(bytes, from, to);
		//return hashBytes(bytes, from, to);
	}

	public static int hashJava(byte[] bytes, int len) {
		int result = 1;

		for (int i = 0; i < len; ++i) {
			result = 31 * result + bytes[i];
		}

		return result;
	}

	public static int hash(byte[] bytes) {
		return hash(new ArrayBytes(bytes), 0, bytes.length);
	}

	public static <X> Predicate<X> limit(Predicate<X> x, int max) {
		if (max <= 0)
			throw new WTF();

		if (max == 1) {
			return (z) -> {
				x.test(z);
				return false;
			};
		} else {
			int[] remain = {max};
			return (z) -> {
				boolean next = (--remain[0] > 0);
				return x.test(z) && next;
			};
		}
	}

//    /**
//     * untested custom byte[] hash function
//     */
//    private static int hashBytes(byte[] bytes, int from, int to) {
//        int x = 1; //0x811c9dc5;
//        int y = 0;
//        int count = 3;
//        for (int i = from; i < to; i++) {
//            if (count-- == 0) {
//                x = Util.hashCombine(x, y);
//                y = 0;
//                count = 3;
//            }
//
//            y = (y << 8) | bytes[i];
//
//        }
//        return x | y;
//    }

//	public static int hashFNV(byte[] bytes, int from, int to) {
//		int h = 0x811c9dc5;
//		for (int i = from; i < to; i++)
//			h = (h * 16777619) ^ bytes[i];
//		return h;
//	}
	public static int hashFNV(ByteSequence bytes, int from, int to) {
		int h = 0x811c9dc5;
		for (int i = from; i < to; i++)
			h = (h * 16777619) ^ bytes.at(i);
		return h;
	}

	public static void assertNotNull(Object test, String varName) {
		if (test == null) {
			throw new NullPointerException(varName);
		}
	}

	public static void assertNotEmpty(Object[] test, String varName) {
		if (test == null) {
			throw new NullPointerException(varName);
		}
		if (test.length == 0) {
			throw new IllegalArgumentException("empty " + varName);
		}
	}




//
//    public static <E> void assertNotEmpty(Collection<E> test, String varName) {
//        if (test == null) {
//            throw new NullPointerException(varName);
//        }
//        if (test.isEmpty()) {
//            throw new IllegalArgumentException("empty " + varName);
//        }
//    }
//    /* End Of  P. J. Weinberger Hash Function */


	public static String globToRegEx(String line) {

		line = line.trim();
		int strLen = line.length();
		StringBuilder sb = new StringBuilder(strLen);

		if (strLen > 0 && line.charAt(0) == '*') {
			line = line.substring(1);
			strLen--;
		}
		if (strLen > 0 && line.charAt(strLen - 1) == '*') {
			line = line.substring(0, strLen - 1);
			strLen--;
		}
		boolean escaping = false;
		int inCurlies = 0;
		for (char currentChar : line.toCharArray()) {
			switch (currentChar) {
				case '*':
					if (escaping)
						sb.append("\\*");
					else
						sb.append(".*");
					escaping = false;
					break;
				case '?':
					if (escaping)
						sb.append("\\?");
					else
						sb.append('.');
					escaping = false;
					break;
				case '.':
				case '(':
				case ')':
				case '+':
				case '|':
				case '^':
				case '$':
				case '@':
				case '%':
					sb.append('\\');
					sb.append(currentChar);
					escaping = false;
					break;
				case '\\':
					if (escaping) {
						sb.append("\\\\");
						escaping = false;
					} else
						escaping = true;
					break;
				case '{':
					if (escaping) {
						sb.append("\\{");
					} else {
						sb.append('(');
						inCurlies++;
					}
					escaping = false;
					break;
				case '}':
					if (inCurlies > 0 && !escaping) {
						sb.append(')');
						inCurlies--;
					} else if (escaping)
						sb.append("\\}");
					else
						sb.append('}');
					escaping = false;
					break;
				case ',':
					if (inCurlies > 0 && !escaping) {
						sb.append('|');
					} else if (escaping)
						sb.append("\\,");
					else
						sb.append(',');
					break;
				default:
					escaping = false;
					sb.append(currentChar);
			}
		}
		return sb.toString();
	}

	public static long hashPJW(String str) {
		long BitsInUnsignedInt = (4 * 8);
		long ThreeQuarters = (BitsInUnsignedInt * 3) / 4;
		long OneEighth = BitsInUnsignedInt / 8;
		long HighBits = (0xFFFFFFFFL) << (BitsInUnsignedInt - OneEighth);
		long hash = 0;
		long test = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash << OneEighth) + str.charAt(i);

			if ((test = hash & HighBits) != 0) {
				hash = ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
			}
		}

		return hash;
	}

	public static long hashELF(String str) {
		long hash = 0;
		long x = 0;

		int l = str.length();
		for (int i = 0; i < l; i++) {
			hash = (hash << 4) + str.charAt(i);

			if ((x = hash & 0xF0000000L) != 0) {
				hash ^= (x >> 24);
			}
			hash &= ~x;
		}

		return hash;
	}



	public static int hashJava(int a, int b) {
		return a * 31 + b;
	}

	public static int hashJavaX(int a, int b) {
		return a * PRIME2 + b;
	}

	/**
	 * from clojure.Util - not tested
	 * also appears in https://www.boost.org/doc/libs/1_35_0/doc/html/boost/hash_combine_id241013.html
	 */
	public static int hashCombine(int a, int b) {
		return a ^ (b + 0x9e3779b9 + (a << 6) + (a >> 2));
	}

	public static int hashCombine(int a, int b, int c) {
		return hashCombine(hashCombine(a, b), c);
	}
	public static int hashCombine(int a, int b, int c, int d) {
		return hashCombine(hashCombine(hashCombine(a, b), c), d);
	}


	public static int hashCombine(int i, long x, long y) {
		//return hashCombine(hashCombine(i, x), Long.hashCode(y));
		int ix = hashCombine(i, x);
		return x == y ? ix : hashCombine(ix, y);
	}

	public static int hashCombine(long x, long y) {
		return hashCombine(x, Long.hashCode(y)); //TODO better
	}
	public static int hashCombine(long x, int y) {
		return hashCombine(y, x);
	}
	public static int hashCombine(int x, long y) {
		//return Util.hashCombine(a, (int) b, (int) (b >> 32));
		return hashCombine(x, (int)(y>>32), (int)(y&0xffff));
	}

	public static int hashCombine(int a, long[] b) {
		int x = hashCombine(a, b[0]);
		for (int i = 1; i < b.length; i++) {
			x = hashCombine(x, b[i]);
		}
		return x;
	}

	public static int hashCombine(int a, Object b) {
		return hashCombine(a, b.hashCode());
	}

	public static int hashCombine(Object a, Object b, Object c) {
		return hashCombine(hashCombine(a, b), c);
	}

	public static int hashCombine(Object a, Object b) {
		if (a != b) {
			return hashCombine(a.hashCode(), b.hashCode());
		} else {
			int ah = a.hashCode();
			return hashCombine(ah, ah);
		}
	}

	/**
	 * hashCombine(1, b)
	 */
	public static int hashCombine1(Object bb) {
		return hashCombine(1, bb.hashCode());
	}


//    /**
//     * custom designed to preserve some alpha numeric natural ordering
//     */
//    public static int hashByteString(byte[] str) {
//        switch (str.length) {
//            case 0:
//                return 0;
//            case 1:
//                return str[0];
//            case 2:
//                return str[0] << 8 | str[1];
//            case 3:
//                return str[0] << 16 | str[1] << 8 | str[2];
//            case 4:
//                return str[0] << 24 | str[1] << 16 | str[2] << 8 | str[3];
//            default:
//                return Long.hashCode(hashELF(str, 0));
//        }
//
//    }
//
//    public static long hashELF(byte[] str, long seed) {
//
//        long hash = seed;
//
//
//        for (byte aStr : str) {
//            hash = (hash << 4) + aStr;
//
//            long x;
//            if ((x = hash & 0xF0000000L) != 0) {
//                hash ^= (x >> 24);
//            }
//            hash &= ~x;
//        }
//
//        return hash;
//    }
//
//    public static long hashELF(byte[] str, long seed, int start, int end) {
//
//        long hash = seed;
//
//        for (int i = start; i < end; i++) {
//            hash = (hash << 4) + str[i];
//
//            long x;
//            if ((x = hash & 0xF0000000L) != 0) {
//                hash ^= (x >> 24);
//            }
//            hash &= ~x;
//        }
//
//        return hash;
//    }
//
//    /**
//     * http:
//     */
//    public static int hashROT(Object... x) {
//        long h = 2166136261L;
//        for (Object o : x)
//            h = (h << 4) ^ (h >> 28) ^ o.hashCode();
//        return (int) h;
//    }

//	/**
//	 * returns the next index
//	 */
//	public static int longToBytes(long l, byte[] target, int offset) {
//		for (int i = offset + 7; i >= offset; i--) {
//			target[i] = (byte) (l & 0xFF);
//			l >>= 8;
//		}
//		return offset + 8;
//	}
//
//	/**
//	 * returns the next index
//	 */
//	public static int intToBytes(int l, byte[] target, int offset) {
//		for (int i = offset + 3; i >= offset; i--) {
//			target[i] = (byte) (l & 0xFF);
//			l >>= 8;
//		}
//		return offset + 4;
//	}

//    public static byte[] bytePlusIntToBytes(byte prefix, int l) {
//        byte[] target = new byte[/*5*/]{prefix, 0, 0, 0, 0};
//        for (int i = 4; i >= 1; i--) {
//            target[i] = (byte) (l & 0xFF);
//            l >>= 8;
//        }
//        return target;
//    }

//	/**
//	 * returns the next index
//	 */
//	public static int short2Bytes(int l, byte[] target, int offset) {
//		target[offset++] = (byte) ((l >> 8) & 0xff);
//		target[offset++] = (byte) ((l) & 0xff);
//		return offset;
//	}
//
//	/**
//	 * http:
//	 */
//	public static int floorInt(float x) {
//		return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
//	}

	/**
	 * linear interpolate between target & current, factor is between 0 and 1.0
	 * targetFactor=1:   full target
	 * targetfactor=0.5: average
	 * targetFactor=0:   full current
	 */
	public static float lerp(float x, float min, float max) {
		return lerpSafe(unitize(x), min, max);
	}
	public static double lerp(double x, double min, double max) {
		return lerpSafe(unitize(x), min, max);
	}


	public static float lerpSafe(float x, float min, float max) {
		//return min + x * (max - min);
		return fma(x, max-min, min);
	}

	public static double lerpSafe(double x, double min, double max) {
//		return min + x * (max - min);
		return fma(x, max-min, min);
	}

	public static float unlerp(float y, float min, float max) {
		return (y - min) / (max-min);
	}


	public static long lerpLong(float x, long min, long max) {
		if (min == max) return min;
		return Math.round(
			//min + (max - min) * unitize((double) x)
			fma(max-min, unitize((double) x), min)
		);
	}

	public static int lerpInt(float x, int min, int max) {
		if (min == max) return min;
		return Math.round(
			//min + (max - min) * unitize(x)
			fma(max-min, unitize(x), min)
		);
	}


	public static float max(float a, float b, float c) {
		return Math.max(Math.max(a, b), c);
	}

	public static float mean(float a, float b) {
		return (float) mean((double)a, b);
	}
	public static float mean(float a, float b, float c) {
		return (float) ((((double)a) + b + c) / 3);
	}
	public static double mean(double a, double b) {
		return (a + b) / 2;
	}
	public static double mean(double... d) {
		return mean(d, 0, d.length);
	}
	public static double mean(float... d) {
		return mean(d, 0, d.length);
	}
	public static double mean(float[] d, int s, int e) {
		return sum(d, s, e) / (e-s);
	}
	public static double mean(double[] d, int s, int e) {
		return sum(d, s, e) / (e-s);
	}
//	/**
//	 * Generic utility method for running a list of tasks in current thread
//	 */
//	public static void run(Deque<Runnable> tasks) {
//		run(tasks, tasks.size(), Runnable::run);
//	}
//
//	public static void run(Deque<Runnable> tasks, int maxTasksToRun, Consumer<Runnable> runner) {
//		while (!tasks.isEmpty() && maxTasksToRun-- > 0) {
//			runner.accept(tasks.removeFirst());
//		}
//	}

	/**
	 * clamps a value to 0..1 range
	 */
	public static double unitize(double x) {
		assertFinite(x);
		return unitizeSafe(x);
	}

	/**
	 * clamps a value to 0..1 range
	 */
	public static float unitize(float x) {
		assertFinite(x);
		return unitizeSafe(x);
	}

	public static float unitizeSafe(float x) {
		return clampSafe(x, 0, 1);
	}

	public static double unitizeSafe(double x) {
		return clampSafe(x, 0, 1);
	}


	public static float assertFinite(float x) throws NumberException {
		if (!Float.isFinite(x))
			throw new NumberException("non-finite", x);
		return x;
	}

	public static double assertFinite(double x) throws NumberException {
		if (!Double.isFinite(x))
			throw new NumberException("non-finite", x);
		return x;
	}

	public static float notNaN(float x) throws NumberException {
		if (x != x) throw NumberException.NaN(x);
		return x;
	}

	public static double notNaN(double x) throws NumberException {
		if (x != x) throw NumberException.NaN(x);
		return x;
	}

//    /**
//     * clamps a value to -1..1 range
//     */
//    public static float clampBi(float p) {
//        if (p > 1f)
//            return 1f;
//        return Math.max(p, -1f);
//        return p;
//    }

	/**
	 * discretizes values to nearest finite resolution real number determined by epsilon spacing
	 */
	public static float round(float value, float epsilon) {
		if (epsilon <= Float.MIN_NORMAL) return value;
		if (value!=value) return Float.NaN;
		assertFinite(epsilon);
		assertFinite(value);
		return (float) roundSafe(value, epsilon);
	}

	public static double round(double value, double epsilon) {
		if (epsilon <= Double.MIN_NORMAL) return value;
		if (value!=value) return Float.NaN;
		assertFinite(epsilon);
		assertFinite(value);
		return roundSafe(value, epsilon);
	}

	public static double roundSafe(double value, double epsilon) {
		return Math.round(value / epsilon) * epsilon;
	}

	/**
	 * rounds x to the nearest multiple of the dither parameter
	 */
	public static long round(long x, int m) {
//		assert(x!=Long.MIN_VALUE && x!=Long.MAX_VALUE): "possibly reserved Long values"; //HACK

		if (m <= 1 || x == 0) return x;

		return x < 0 ? -_round(-x, m) : _round(x, m);
//		//long lo = (x / dither) * dither,  hi = lo + dither;
//		long lo = roundDown(x, m), hi = roundUp(x, m);
//		return (x - lo > hi - x)? hi : lo; //closest
	}
	private static long _round(long x, int m) {
		long base = x - x%m; x -= base;
		long lo = (x / m) * m;
		long hi = lo + m;
		return base + (x - lo > hi - x ? hi : lo); //closest
	}

//	/**
//	 * round n down to nearest multiple of m
//	 * from: https://gist.github.com/aslakhellesoy/1134482 */
//	public static long roundDown(long n, int m) {
//		return n >= 0 ? (n / m) * m : ((n - m + 1) / m) * m;
//	}
//
//	/**
//	 * round n up to nearest multiple of m
//	 * from: https://gist.github.com/aslakhellesoy/1134482
//	 */
//	public static long roundUp(long n, int m) {
//		return n >= 0 ? ((n + m - 1) / m) * m : (n / m) * m;
//	}

	public static int toInt(double f, int discretness) {
		return (int) Math.round(f * discretness);
	}

//	public static long toInt(double f, int discretness) {
//		return Math.round(f * discretness);
//	}

	public static float toFloat(int i, int discretness) {
		return (float)toDouble(i, discretness);
	}
	public static double toDouble(int i, int discretness) {
		return ((double) i) / discretness;
	}

	public static @Nullable <X> X get(@Nullable Supplier<X> s) {
		return s != null ? s.get() : null;
	}

	public static @Nullable <X> X get(Object xOrSupplierOfX) {
		return xOrSupplierOfX instanceof Supplier ? ((Supplier<X>)xOrSupplierOfX).get() : (X) xOrSupplierOfX;
	}

	public static boolean equals(float a, float b) {
		return equals(a, b, Float.MIN_NORMAL * 2);
	}

	public static boolean equals(long a, long b, int tolerance) {
		//assert(tolerance > 0);
		return Math.abs(a - b) < tolerance;
	}

//	/**
//	 * tests equivalence (according to epsilon precision)
//	 */
//	public static boolean equals(float a, float b, float epsilon) {
////		if (a == b)
////			return true;
//		//if (Float.isFinite(a) && Float.isFinite(b))
//		return Math.abs(a - b) < epsilon;
////        else
////            return (a != a) && (b != b); //both NaN
//	}

	public static boolean equals(double a, double b) {
		return equals(a, b, Double.MIN_NORMAL * 2);
	}

	/**
	 * tests equivalence (according to epsilon precision)
	 */
	public static boolean equals(double a, double b, double epsilon) {
//		if (a == b)
//			return true;
//        if (Double.isFinite(a) && Double.isFinite(b))
		return Math.abs(a - b) < epsilon;
//        else
//            return (a != a) && (b != b); //both NaN
	}


	public static boolean equals(float[] a, float[] b, float epsilon) {
		if (a == b) return true;
		int l = a.length;
		for (int i = 0; i < l; i++) {
			if (!equals(a[i], b[i], epsilon))
				return false;
		}
		return true;
	}

	/**
	 * applies a quick, non-lexicographic ordering compare
	 * by first testing their lengths
	 */
	public static int compare(long[] x, long[] y) {
		if (x == y) return 0;

		int xlen = x.length;

		int yLen = y.length;
		if (xlen != yLen) {
			return Integer.compare(xlen, yLen);
		} else {

			for (int i = 0; i < xlen; i++) {
				int c = Long.compare(x[i], y[i]);
				if (c != 0)
					return c;
			}

			return 0;
		}
	}

//	public static byte[] intAsByteArray(int index) {
//
//		if (index < 36) {
//			byte x = base36(index);
//			return new byte[]{x};
//		} else if (index < (36 * 36)) {
//			byte x1 = base36(index % 36);
//			byte x2 = base36(index / 36);
//			return new byte[]{x2, x1};
//		} else {
//			throw new RuntimeException("variable index out of range for this method");
//		}
//
//
//	}

	public static int bin(FloatSupplier x, int bins) {
		return bin(x.asFloat(), bins);
	}

	public static int bin(float x, int bins) {
//        assertFinite(x);
		//assert(bins > 0);
		return clampSafe((int) (x * bins), 0, bins - 1);
		//return (int) Math.floor(x * bins);
		//return (int) (x  * bins);

		//return Math.round(x * (bins - 1));
		//return Util.clamp((int)((x * bins) + 0.5f/bins), 0, bins-1);


		//return (int) ((x + 0.5f/bins) * (bins-1));
		//        return (int) Math.floor((x + (0.5 / bins)) * bins);
		//        return Util.clamp(b, 0, bins-1);
	}

//    /**
//     * bins a priority value to an integer
//     */
//    public static int decimalize(float v) {
//        return bin(v, 10);
//    }
//
//	/**
//	 * finds the mean value of a given bin
//	 */
//	public static float unbinCenter(int b, int bins) {
//		return ((float) b) / bins;
//	}

//    public static <D> D runProbability(Random rng, float[] probs, D[] choices) {
//        float tProb = 0;
//        for (int i = 0; i < probs.length; i++) {
//            tProb += probs[i];
//        }
//        float s = rng.nextFloat() * tProb;
//        int c = 0;
//        for (int i = 0; i < probs.length; i++) {
//            s -= probs[i];
//            if (s <= 0) {
//                c = i;
//                break;
//            }
//        }
//        return choices[c];
//    }

	public static MethodHandle mhRef(Class<?> type, String name) {
		try {
			for (Method m : type.getMethods()) {
				if (m.getName().equals(name)) {
					return lookup()
							.unreflect(m);
				}
			}
			return null;
		} catch (IllegalAccessException e) {
			throw new Error(e);
		}
	}

	public static <F> MethodHandle mh(String name, F fun) {
		return mh(name, fun.getClass(), fun);
	}

	public static <F> MethodHandle mh(String name, Class<? extends F> type, F fun) {
		return mhRef(type, name).bindTo(fun);
	}

	@SafeVarargs
    public static <F> MethodHandle mh(String name, F... fun) {
		F fun0 = fun[0];
		MethodHandle m = mh(name, fun0.getClass(), fun0);
		for (int i = 1; i < fun.length; i++) {
			m = m.bindTo(fun[i]);
		}
		return m;
	}

	public static byte base36(int index) {
		if (index < 10)
			return (byte) ('0' + index);
		else if (index < (10 + 26))
			return (byte) ((index - 10) + 'a');
		else
			throw new RuntimeException("out of bounds");
	}

	/**
	 * clamps output to 0..+1.  y=0.5 at x=0
	 */
	public static float sigmoid(float x) {
		return (float) sigmoid((double)x);
	}

	public static double sigmoid(double x) {
		return 1 / (1 + Math.exp(-x));
	}

//	public static float sigmoidDiff(float a, float b) {
//		float sum = a + b;
//		float delta = a - b;
//		float deltaNorm = delta / sum;
//		return sigmoid(deltaNorm);
//	}

//	public static float sigmoidDiffAbs(float a, float b) {
//		float sum = a + b;
//		float delta = Math.abs(a - b);
//		float deltaNorm = delta / sum;
//		return sigmoid(deltaNorm);
//	}

//	public static List<String> inputToStrings(InputStream is) throws IOException {
//		List<String> x = CharStreams.readLines(new InputStreamReader(is, Charsets.UTF_8));
//		Closeables.closeQuietly(is);
//		return x;
//	}
//
//	public static String inputToString(InputStream is) throws IOException {
//		String s = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
//		Closeables.closeQuietly(is);
//		return s;
//	}

	public static int[] reverse(IntArrayList l) {
		return switch (l.size()) {
			case 0 -> throw new UnsupportedOperationException();
			case 1 -> new int[]{l.get(0)};
			case 2 -> new int[]{l.get(1), l.get(0)};
			case 3 -> new int[]{l.get(2), l.get(1), l.get(0)};
			default -> l.asReversed().toArray();
		};
	}

	public static byte[] reverse(ByteArrayList l) {
		int s = l.size();
		switch (s) {
			case 0:
				return ArrayUtil.EMPTY_BYTE_ARRAY;
			case 1:
				return new byte[]{l.get(0)};
			case 2:
				return new byte[]{l.get(1), l.get(0)};
			default:
				byte[] b = new byte[s];
				for (int i = 0; i < s; i++)
					b[i] = l.get(--s);
				return b;
		}
	}

	public static String s(String s, int maxLen) {
		if (s.length() < maxLen) return s;
		return s.substring(0, maxLen - 2) + "..";
	}

//	public static void writeBits(int x, int numBits, float[] y, int offset) {
//		for (int i = 0, j = offset; i < numBits; i++, j++) {
//			y[j] = ((x & 1 << i) == 1) ? 1 : 0;
//		}
//	}

//	/**
//	 * a and b must be instances of input, and output must be of size input.length-2
//	 */
//	public static <X> X[] except(X[] input, X a, X b, X[] output) {
//		int targetLen = input.length - 2;
//		if (output.length != targetLen) {
//			throw new RuntimeException("wrong size");
//		}
//		int j = 0;
//		for (X x : input) {
//			if ((x != a) && (x != b))
//				output[j++] = x;
//		}
//
//		return output;
//	}


	/** to unit interval: [0, 1] */
	public static float[] normalize(float[] x) {
		float[] minmax = minmax(x);
		return normalize(x, minmax[0], minmax[1]);
	}

	public static double[] normalize(double... x) {
		return normalize(x, x.length);
	}

	public static double[] normalize(double[] x, int n) {
		double[] minmax = minmax(x, 0, n);
		return normalize(x, 0, n, minmax[0], minmax[1]);
	}


//	public static float[] normalizeCartesian(float[] x, float epsilon) {
//		return normalizeCartesian(x, x.length, epsilon);
//	}
//
//	public static float[] normalizeCartesian(float[] x, int n, float epsilon) {
//
//		double magSq = 0;
//		for (int i = 0; i < n; i++) magSq += sqr(x[i]);
//
//		if (magSq >= sqr(((double) epsilon * n))) {
//			double mag = Math.sqrt(magSq);
//			for (int i = 0; i < n; i++) x[i] /= mag;
//		} else {
//			Arrays.fill(x, 0, n, 0); //zero vector
//		}
//
//		return x;
//	}
//
//	public static double[] normalizeCartesian(double[] x, double epsilon) {
//		return normalizeCartesian(x, x.length, epsilon);
//	}

	public static double[] normalizeCartesian(double[] x, int n, double epsilon) {

		double magSq = 0;
		for (int i = 0; i < n; i++) magSq += sqr(x[i]);

		if (magSq < sqr(epsilon * n)) {
			Arrays.fill(x, 0);
		} else {
			double mag = Math.sqrt(magSq);
			for (int i = 0; i < n; i++) x[i] /= mag;
		}
		return x;
	}
//
//	public static float[] normalizeMargin(float lowerPct, float upperPct, float[] x) {
//		float[] minmax = minmax(x);
//		float range = minmax[1] - minmax[0];
//		return normalize(x, minmax[0] - lowerPct * range, minmax[1] + upperPct * range);
//	}

	public static float[] normalize(float[] x, float min, float max) {
		return normalize(x, 0, x.length, min, max);
	}


//	public static double[] normalizeSubArray(double[] x, int s, int e) {
//		//final double min = Util.min(s, e, x);
//		//return normalize(x, s, e, min, min + Util.sum(x, s, e));
//		return normalize(x, s, e, min(x, s, e), max(x, s, e));
//	}

//	public static double[] normalizeSubArraySum1(double[] x, int s, int e) {
//		double min = min(x, s, e);
//		return normalize(x, s, e, min, min + sum(x, s, e));
//	}

//	public static float[] normalizeSubArray(float[] x, int s, int e) {
//		return normalize(x, s, e, Util.min(s, e, x), Util.max(s, e, x));
//	}

	/**
	 * https://en.wikipedia.org/wiki/Feature_scaling#Rescaling_(min-max_normalization)
	 */
	@Is("Feature_scaling") public static float[] normalize(float[] x, int s, int e, float min, float max) {
		int n = e-s;
		float range = max - min;
		if (range < Float.MIN_NORMAL*n) {
			for (int i = s; i < e; i++) {
				if (x[i]==x[i]) //skip NaN's
					x[i] = 0.5f;
			}
		} else {
			for (int i = s; i < e; i++)
				x[i] = normalizeSafer(x[i], min, range);
		}
		return x;
	}


	/** normalize to unit min/max range */
	public static double[] normalize(double[] x, int s, int e, double min, double max) {
		int n = e-s;
		double range = max - min;
		if (range < Double.MIN_NORMAL*n) {
			for (int i = s; i < e; i++) {
				if (x[i]==x[i]) //skip NaN's
					x[i] = 0.5;
			}
		} else {
			for (int i = s; i < e; i++)
				x[i] = normalizeSafer(x[i], min, range);
		}
		return x;
	}

	public static double normalize(double x, double min, double max) {
		assertFinite(x);
		assertFinite(min);
		assertFinite(max);
		assert (max >= min);
		if (max - min <= Double.MIN_NORMAL)
			return 0.5f;
		else {
			return (x - min) / (max - min);
		}
	}

	public static float normalizeSafe(float x, float min, float max) {
		return ((max - min) <= Float.MIN_NORMAL) ? 0.5f : normalizeSafer(x, min, max-min);
	}
	public static float normalizeSafer(float x, float min, float range) {
		return (x - min) / range;
	}
	public static double normalizeSafe(double x, double min, double max) {
		return ((max - min) <= Double.MIN_NORMAL) ? 0.5 : normalizeSafer(x, min ,max-min);
	}
	public static double normalizeSafer(double x, double min, double range) {
		return (x - min) / range;
	}

	public static float normalize(float x, float min, float max) {
		assertFinite(x);

		assertFinite(min);
		assertFinite(max);
		assert(max >= min);

		return normalizeSafe(x, min, max);
	}

	public static float variance(float[] population) {
		double average = 0.0f;
		for (float p : population) {
			average += p;
		}
		int n = population.length;
		average /= n;

		double variance = 0.0f;
		for (float p : population) {
			double d = p - average;
			variance += d * d;
		}
		return (float) (variance / n);
	}



	public static double[] variance(DoubleStream s) {
		DoubleArrayList dd = new DoubleArrayList();
		s.forEach(dd::add);
		if (dd.isEmpty())
			return null;

		double mean = dd.average();

		double variance = 0.0;
		int n = dd.size();
		for (int i = 0; i < n; i++)
			variance += sqr(dd.get(i) - mean);

		variance /= n;

		return new double[]{mean, variance};
	}

//	public static String className(Object p) {
//		Class<?> pClass = p.getClass();
//		String s = pClass.getSimpleName();
//		return s.isEmpty() ? pClass.toString().replace("class ", "") : s;
//	}

	public static float[] toFloat(double[] d) {
		return toFloat(d, new float[d.length]);
	}

	public static float[] toFloat(double[] from, float[] to) {
		int l = from.length;
		for (int i = 0; i < l; i++)
			to[i] = (float) from[i];
		return to;
	}

	public static double[] toDouble(float[] d) {
		return toDouble(d, new double[d.length]);
	}

	public static double[] toDouble(float[] from, double[] to) {
		int l = from.length;
		if (to==null || to.length<l)
			to = new double[l];
		for (int i = 0; i < l; i++)
			to[i] = from[i];
		return to;
	}



//	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
//	public static long[] minmax(IntToLongFunction f, int from, int to) {
//
//		long min = Long.MAX_VALUE;
//		long max = Long.MIN_VALUE;
//		for (int i = from; i < to; i++) {
//			long y = f.applyAsLong(i);
//			if (y < min) min = y;
//			if (y > max) max = y;
//		}
//		return new long[]{min, max};
//
//	}
	public static float[] minmax(float[] x) {
		return minmax(x, 0, x.length);
	}

	public static float[] minmax(float[] x, int from, int to) {
		float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
		for (int i = from; i < to; i++) {
			float y = x[i];
			if (y < min) min = y;
			if (y > max) max = y;
		}
		return new float[]{min, max/*, sum */};
	}

//	public static float[] minmaxsum(float[] x) {
//		float sum = 0;
//		float min = Float.POSITIVE_INFINITY;
//		float max = Float.NEGATIVE_INFINITY;
//		for (float y : x) {
//			sum += y;
//			if (y < min) min = y;
//			if (y > max) max = y;
//		}
//		return new float[]{min, max, sum};
//	}

//	public static double[] minmax(double[] x) {
//		return minmax(x, 0, x.length);
//	}

	public static double[] minmax(double[] x, int from, int to) {
		double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
		for (int i = from; i < to; i++) {
			double xi = x[i];
			if (xi < min) min = xi;
			if (xi > max) max = xi;
		}
		return new double[]{ min, max/*, sum */};
	}
//	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
//	public static double[] minmax(IntToDoubleFunction x, int from, int to) {
//		double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
//		for (int i = from; i < to; i++) {
//			double xi = x.applyAsDouble(i);
//			if (xi < min) min = xi;
//			if (xi > max) max = xi;
//		}
//		return new double[]{ min, max/*, sum */};
//	}

	public static void time(Logger logger, String procName, Runnable procedure) {
		if (!logger.isInfoEnabled()) {
			procedure.run();
		} else {
			long dtNS = timeNS(procedure);
			logger.info("{} {}", procName, Str.timeStr(dtNS));
		}
	}

	public static long timeNS(Runnable procedure) {
		long start = System.nanoTime();

		procedure.run();

		long end = System.nanoTime();
		return end - start;
	}

	public static String tempDir() {
		return System.getProperty("java.io.tmpdir");
	}

	/**
	 * TODO make a version of this which can return the input array if no modifications occurr either by .equals() or identity
	 */
	@SafeVarargs public static <X, Y> Y[] map(Function<X, Y> f, Y[] y, X... x) {
		return map(f, y, Math.min(y.length, x.length), x);
	}

	@SafeVarargs
    public static <X, Y> Y[] map(Function<X, Y> f, Y[] y, int size, X... x) {
		return map(f, y, 0, x, 0, size);
	}

	public static <X, Y> Y[] map(Function<X, Y> f, Y[] y, int targetOffset, X[] x, int srcFrom, int srcTo) {
		assert(x.length > 0);
		for (int i = srcFrom; i < srcTo; i++)
			y[targetOffset++] = f.apply(x[i]);
		return y;
	}

	/**
	 * TODO make a version of this which can return the input array if no modifications occurr either by .equals() or identity
	 */
	@SafeVarargs
    public static <X, Y> Y[] map(Function<X, Y> f, IntFunction<Y[]> y, X... x) {
		int i = 0;
		Y[] target = y.apply(x.length);
		for (X xx : x)
			target[i++] = f.apply(xx);
		return target;
	}

	@SafeVarargs
    public static <X> X[] mapIfChanged(UnaryOperator<X> f, X... src) {
		X[] target = null;
		for (int i = 0, srcLength = src.length; i < srcLength; i++) {
			X x = src[i];
			X y = f.apply(x);
			if (y != x) {
				if (target == null)
					target = src.clone();

				target[i] = y;
			}
		}
		return target == null ? src : target;
	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	@SafeVarargs
    public static <X> double sum(FloatFunction<X> value, X... xx) {
		double y = 0;
		for (X x : xx)
			y += value.floatValueOf(x);
		return y;
	}


	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	@SafeVarargs
    public static <X> double sum(ToDoubleFunction<X> value, X... xx) {
		double y = 0.0;
		for (X x : xx)
			y += value.applyAsDouble(x);
		return y;
	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	public static <X> int sum(ToIntFunction<X> value, Iterable<X> xx) {
		int y = 0;
		for (X x : xx)
			y += value.applyAsInt(x);
		return y;
	}

//	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
//	public static <X> float sum(FloatFunction<X> value, Iterable<X> xx) {
//		float y = 0;
//		for (X x : xx)
//			y += value.floatValueOf(x);
//		return y;
//	}
//	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
//	public static <X> double sum(ToDoubleFunction<X> value, Iterable<X> xx) {
//		double y = 0;
//		for (X x : xx)
//			y += value.applyAsDouble(x);
//		return y;
//	}

//	public static <X> float mean(FloatFunction<X> value, Iterable<X> xx) {
//		float y = 0;
//		int count = 0;
//		for (X x : xx) {
//			y += value.floatValueOf(x);
//			count++;
//		}
//		return y / count;
//	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	@SafeVarargs
    public static <X> int sum(ToIntFunction<X> value, X... xx) {
		return sum(value, 0, xx.length, xx);
	}

	@SafeVarargs
    public static <X> int sum(ToIntFunction<X> value, int from, int to, X... xx) {
        int len = to - from;
        int y = 0;
		for (int i = from; i < len; i++)
			y += value.applyAsInt(xx[i]);
		return y;
	}

//	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
//	@SafeVarargs
//    public static <X> long sum(ToLongFunction<X> value, X... xx) {
//		long y = 0;
//		for (X x : xx)
//			y += value.applyAsLong(x);
//		return y;
//	}

//	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
//	@SafeVarargs
//    public static <X> long min(ToLongFunction<X> value, X... xx) {
//		long y = Long.MAX_VALUE;
//		for (X x : xx)
//			y = Math.min(y, value.applyAsLong(x));
//		return y;
//	}

	@Is("Smoothstep") public static double smoothstep(double x) {
		x = unitizeSafe(x);
		return x * x * (3 - 2 * x);
	}

	@Is("Smoothstep") public static double smootherstep(double x) {
		x = unitizeSafe(x);
		return x * x * x * (x * (x * 6 - 15) + 10);
	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	@SafeVarargs
    public static <X> long max(ToLongFunction<X> value, X... xx) {
		long y = Long.MIN_VALUE;
		for (X x : xx)
			y = Math.max(y, value.applyAsLong(x));
		return y;
	}

	public static <X> long max(ToLongFunction<X> value, X[] xx, int n) {
		long y = Long.MIN_VALUE;
		for (int i = 0; i < n; i++)
			y = Math.max(y, value.applyAsLong(xx[i]));

		return y;
	}

//	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
//	@SafeVarargs
//    public static <X> int max(ToIntFunction<X> value, X... xx) {
//		int y = Integer.MIN_VALUE;
//		for (X x : xx)
//			y = Math.max(y, value.applyAsInt(x));
//		return y;
//	}
//
//	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
//	public static <X> long max(ToLongFunction<X> value, Iterable<X> xx) {
//		long y = Long.MIN_VALUE;
//		for (X x : xx)
//			y = Math.max(y, value.applyAsLong(x));
//		return y;
//	}

//	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
//	public static <X> long min(ToLongFunction<X> value, Iterable<X> xx) {
//		long y = Long.MAX_VALUE;
//		for (X x : xx)
//			y = Math.min(y, value.applyAsLong(x));
//		return y;
//	}

//	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
//	public static <X> int min(ToIntFunction<X> value, Iterable<X> xx) {
//		int y = Integer.MAX_VALUE;
//		for (X x : xx)
//			y = Math.min(y, value.applyAsInt(x));
//		return y;
//	}

	public static double max(IntToDoubleFunction value, int start, int end) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = start; i < end; i++) {
			double v = value.applyAsDouble(i); if (v > max) max = v;
		}
		return max;
	}
	public static double min(IntToDoubleFunction value, int start, int end) {
		double min = Double.POSITIVE_INFINITY;
		for (int i = start; i < end; i++) {
			double v = value.applyAsDouble(i); if (v < min) min = v;
		}
		return min;
	}

	@SafeVarargs
    public static <X> boolean sumBetween(ToIntFunction<X> value, int min, int max, X... xx) {
		int y = 0;
		for (X x : xx) {
			if ((y += value.applyAsInt(x)) > max)
				return false;
		}
		return (y >= min);
	}

	@SafeVarargs
    public static <X> boolean sumExceeds(ToIntFunction<X> value, int max, X... xx) {
		int y = 0;
		for (X x : xx) {
			if ((y += value.applyAsInt(x)) > max)
				return true;
		}
		return false;
	}

	public static <X> boolean sumExceeds(ToDoubleFunction<X> value, double thresh, Iterable<X> xx) {
		double y = 0;
		for (X x : xx) {
			y += value.applyAsDouble(x);
			if (y > thresh) return true;
		}
		return false;
	}

//	public static int indexMin(int s, int e, long[] xx) {
//		long y = Long.MAX_VALUE;
//		int m = -1;
//		for (int i = s; i < e; i++) {
//			long x = xx[i];
//			if (x < y) {
//				y = x;
//				m = i;
//			}
//		}
//		return m;
//	}
//	public static int indexMax(int s, int e, long[] xx) {
//		long y = Long.MIN_VALUE;
//		int m = -1;
//		for (int i = s; i < e; i++) {
//			long x = xx[i];
//			if (x > y) {
//				y = x;
//				m = i;
//			}
//		}
//		return m;
//	}

//	/**
//	 * warning: if values are the same then biases towards the first
//	 * TODO make a random one for cases where equivalents exist
//	 */
//	public static int indexMax(float... xx) {
//		float y = Float.NEGATIVE_INFINITY;
//		int best = -1;
//		int n = xx.length;
//		for (int i = 0; i < n; i++) {
//			float x = xx[i];
//			if (x > y) {
//				y = x;
//				best = i;
//			}
//		}
//		return best;
//	}
//
//	public static int indexMaxReverse(float... xx) {
//		float y = Float.NEGATIVE_INFINITY;
//		int best = -1;
//		for (int i = xx.length - 1; i >= 0; i--) {
//			float x = xx[i];
//			if (x > y) {
//				y = x;
//				best = i;
//			}
//		}
//		return best;
//	}

	/** ignores NaN */
	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	@SafeVarargs public static <X> double mean(FloatFunction<X> value, X... xx) {
		double y = 0;
		int count = 0;
		for (X x : xx) {
			float v = value.floatValueOf(x);
			if (v == v) {
				y += v;
				count++;
			}
		}
		return count > 0 ? (y / count) : Double.NaN;
	}

	/** ignores NaN */
	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	@SafeVarargs
	public static <X> double mean(ToDoubleFunction<X> value, X... xx) {
		double y = 0;
		int count = 0;
		for (X x : xx) {
			double v = value.applyAsDouble(x);
			if (v == v) {
				y += v;
				count++;
			}
		}
		return count > 0 ? (y / count) : Double.NaN;
	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	@SafeVarargs
    public static <X> float max(FloatFunction<X> value, X... xx) {
		float y = Float.NEGATIVE_INFINITY;
		for (X x : xx)
			y = Math.max(y, value.floatValueOf(x));
		return y;
	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	public static <X> float max(FloatFunction<X> value, Iterable<X> xx) {
		float y = Float.NEGATIVE_INFINITY;
		for (X x : xx)
			y = Math.max(y, value.floatValueOf(x));
		return y;
	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	@SafeVarargs
    public static <X> float min(FloatFunction<X> value, X... xx) {
		float y = Float.POSITIVE_INFINITY;
		for (X x : xx)
			y = Math.min(y, value.floatValueOf(x));
		return y;
	}

	public static long sum(int[] x, int from, int to) {
		long y = 0;
		for (int j = from; j < to; j++)
			y += x[j];

		return y;
	}

	public static double sum(float[] x, int from, int to) {
		double y = 0;
		for (int j = from; j < to; j++)
			y += x[j];
		return y;

	}
	public static double sum(double[] x, int from, int to) {
		double y = 0;
		for (int j = from; j < to; j++)
			y += x[j];
		return y;

	}

	public static double max(double... x) {
		double y = Double.NEGATIVE_INFINITY;
		for (double f : x) {
			if (f > y) y = f;
		}
		return y;
	}

	public static byte max(byte... x) {
		byte y = Byte.MIN_VALUE;
		for (byte f : x) {
			if (f > y) y = f;
		}
		return y;
	}
	public static int max(int... x) {
		int y = Integer.MIN_VALUE;
		for (int f : x) { if (f > y) y = f; }
		return y;
	}
	public static short max(short... x) {
		short y = Short.MIN_VALUE;
		for (short f : x) { if (f > y) y = f; }
		return y;
	}

	public static float max(float... x) {
		return max(x, 0, x.length);
	}

	public static long max(long[] x, int s, int e) {
		long y = Long.MIN_VALUE;
		for (int i = s; i < e; i++) {
			long f = x[i]; if (f > y) y = f;
		}
		return y;
	}

	public static float max(float[] x, int s, int e) {
		float y = Float.NEGATIVE_INFINITY;
		for (int i = s; i < e; i++) {
			float f = x[i]; if (f > y) y = f;
		}
		return y;
	}
	public static double max(double[] x, int s, int e) {
		double y = Double.NEGATIVE_INFINITY;
		for (int i = s; i < e; i++) {
			double f = x[i]; if (f > y) y = f;
		}
		return y;
	}
	public static double min(double... x) {
		double y = Double.POSITIVE_INFINITY;
		for (double f : x) {
			if (f < y) y = f; //if (Double.compare(f, y) < 0)
		}
		return y;
	}

	/** possibly simpler but less robust than Math.min */
	public static float min(float x, float y) {
		return x <= y ? x : y;
	}
	/** possibly simpler but less robust than Math.min */
	public static double min(double x, double y) {
		return x <= y ? x : y;
	}

	/** possibly simpler but less robust than Math.max */
	public static float max(float x, float y) {
		return x <= y ? y : x;
	}
	/** possibly simpler but less robust than Math.max */
	public static double max(double x, double y) {
		return x <= y ? y : x;
	}

	@Deprecated public static long max(long x, long y) {
		throw new UnsupportedOperationException("use Math.max()");
	}
	@Deprecated public static long min(long x, long y) {
		throw new UnsupportedOperationException("use Math.min()");
	}
	@Deprecated public static int max(int x, int y) {
		throw new UnsupportedOperationException("use Math.max()");
	}
	@Deprecated public static int min(int x, int y) {
		throw new UnsupportedOperationException("use Math.min()");
	}

//	public static float min(float... x) {
//		return min(x, 0, x.length);
//	}

	public static float min(float[] x, int s, int e) {
		float y = Float.POSITIVE_INFINITY;
		for (int i = s; i < e; i++) {
			var f = x[i]; if (f < y) y = f;
		}
		return y;
	}
	public static double min(double[] x, int s, int e) {
		double y = Double.POSITIVE_INFINITY;
		for (int i = s; i < e; i++) {
			var f = x[i]; if (f < y) y = f;
		}
		return y;
	}



	public static double sum(float... x) {
		double y = 0;
		for (float f : x) {
			if (f == f)
				y += f;
		}
		return y;
	}
	public static double sum(double... x) {
		double y = 0;
		for (double f : x) {
			if (f == f)
				y += f;
		}
		return y;
	}
	public static double sumAbs(float... x) {
		double y = 0;
		for (float f : x) {
			if (f == f)
				y += Math.abs(f);
		}
		return y;
	}

	public static double sumAbs(double... x) {
		double y = 0;
		for (double f : x) {
			if (f == f)
				y += Math.abs(f);
		}
		return y;
	}
	public static double sumSqr(double... x) {
		double y = 0;
		for (double f : x) {
			if (f == f)
				y = fma(f,f,y); // y+=f*f
		}
		return y;
	}


	/**
	 * TODO fair random selection when exist equal values
	 */
	public static int argmax(double[] x) {
		int result = -1;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0, l = x.length; i < l; i++) {
			double xi = x[i];
			if (xi > max) {
				max = xi;
				result = i;
			}
		}
		return result;
	}

	/**
	 * TODO fair random selection when exist equal values
	 */
	public static int argmax(float... vec) {
		int result = -1;
		float max = Float.NEGATIVE_INFINITY;
		for (int i = 0, l = vec.length; i < l; i++) {
			float v = vec[i];
			if (v > max) {
				max = v;
				result = i;
			}
		}
		return result;
	}
	public static int argmin(float... vec) {
		int result = -1;
		float max = Float.POSITIVE_INFINITY;
		for (int i = 0, l = vec.length; i < l; i++) {
			float v = vec[i];
			if (v < max) {
				max = v;
				result = i;
			}
		}
		return result;
	}
	public static int argmax(int a, int b, float... vec) {
		int result = -1;
		float max = Float.NEGATIVE_INFINITY;
		for (int i = a; i < b; i++) {
			float v = vec[i];
			if (v > max) {
				max = v;
				result = i;
			}
		}
		return result;
	}



	public static int argmax(Random random, float... vec) {
		int result = -1;
		float max = Float.NEGATIVE_INFINITY;

		int l = vec.length;
		int start = random.nextInt(l);
		for (int i = 0; i < l; i++) {
			int ii = (i + start) % l;
			float v = vec[ii];
			if (v > max) {
				max = v;
				result = ii;
			}
		}
		return result;
	}

//    public static Pair tuple(Object a, Object b) {
//        return Tuples.pair(a, b);
//    }
//
//    public static Pair tuple(Object a, Object b, Object c) {
//        return tuple(tuple(a, b), c);
//    }
//
//    public static Pair tuple(Object a, Object b, Object c, Object d) {
//        return tuple(tuple(a, b, c), d);
//    }

	/**
	 * min is inclusive, max is exclusive: [min, max)
	 */
	public static int unitize(int x, int min, int max) {
		if (x < min) x = min;
		else if (x > max) x = max;
		return x;
	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	public static float sum(int count, IntToFloatFunction values) {
		double weightSum = 0;
		for (int i = 0; i < count; i++)
			weightSum += values.valueOf(i);
		return (float) weightSum;
	}
	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	public static double sum(int count, IntToDoubleFunction values) {
		double weightSum = 0;
		for (int i = 0; i < count; i++)
			weightSum += values.applyAsDouble(i);
		return weightSum;
	}

	public static double sumIfPositive(int count, IntToFloatFunction values) {
		double weightSum = 0;
		for (int i = 0; i < count; i++) {
			float w = values.valueOf(i);
			//assert (w == w);
			if (/*w == w && */w > Float.MIN_NORMAL)
				weightSum += w;
		}
		return weightSum;
	}


	public static float clamp(float f, float min, float max) {
		assertFinite(f);
		notNaN(min);
		notNaN(max);
		assert (min <= max);
		return clampSafe(f, min, max);
	}

	public static float clampSafe(float f, float min, float max) {
		//return Math.max(Math.min(f, max), min);
		if (f > max) return max;
		if (f < min) return min;
        return f;
	}

	public static double clamp(double f, double min, double max) {
		assertFinite(f);
		notNaN(min);
		notNaN(max);
		assert (min <= max);
		return clampSafe(f, min, max);
	}

	public static double clampSafe(double X, double min, double max) {
		return (X > max) ? max : ((X < min) ? min : X);
		//return Util.max(Util.min(X, max), min);
		//return Math.max(Math.min(X, max), min);
	}

	public static int clamp(int i, int min, int max) {
		assert (min <= max);
		return clampSafe(i, min, max);
	}

	public static int clampSafe(int i, int min, int max) {
		if (i < min) i = min;
		if (i > max) i = max;
		return i;
	}

	public static long clamp(long i, long min, long max) {
		assert (min <= max);
		return clampSafe(i, min, max);
	}

	public static long clampSafe(long i, long min, long max) {
		if (i < min) i = min;
		if (i > max) i = max;
		return i;
	}

	/**
	 * range [a, b)
	 */
	public static int[] intArray(int a, int b) {
		int ba = b - a;
		int[] x = new int[ba];
		for (int i = 0; i < ba; i++) {
			x[i] = a + i;
		}
		return x;
	}

	public static double sqr(long x) {
		return x * x;
	}

	public static int sqr(int x) {return x * x; }

	public static int cube(int x) {
		return x * x * x;
	}

	public static float sqr(float x) {
		return x * x;
	}

	public static float sqrt(float v) {
		return (float) Math.sqrt(v);
	}

	public static float cube(float x) {
		return x * x * x;
	}

	public static double cube(double x) {
		return x * x * x;
	}

	public static double sqr(double x) {
		return x * x;
	}


	/**
	 * adaptive spinlock behavior
	 * see: https:
	 * TODO tune
	 * TODO randomize?
	 */
	public static void pauseSpin(int previousContiguousPauses) {
		if (previousContiguousPauses < 2)
			return; //immediate

		if (previousContiguousPauses < 4096) {
			pauseSpinning(previousContiguousPauses);
			return;
		}
		
		Thread.yield();
	}

	private static void pauseSpinning(int previousContiguousPauses) {
		if (previousContiguousPauses > 512 && (previousContiguousPauses % 1024) == 0) {
			Thread.yield();
		} else {
			onSpinWait();
		}
	}

    /*
        static final long PARK_TIMEOUT = 50L;
    static final int MAX_PROG_YIELD = 2000;
            if(n > 500) {
            if(n<1000) {
                // "randomly" yield 1:8
                if((n & 0x7) == 0) {
                    LockSupport.parkNanos(PARK_TIMEOUT);
                } else {
                    onSpinWait();
                }
            } else if(n<MAX_PROG_YIELD) {
                // "randomly" yield 1:4
                if((n & 0x3) == 0) {
                    Thread.yield();
                } else {
                    onSpinWait();
                }
            } else {
                Thread.yield();
                return n;
            }
        } else {
            onSpinWait();
        }
        return n+1;

     */

//    /**
//     * adaptive spinlock behavior
//     * see: https:
//     */
//    public static void pauseNextCountDown(long timeRemainNS) {
//        if (timeRemainNS < 10 * (1000 /* uS */))
//            onSpinWait();
//        else
//            Thread.yield();
//    }

	public static void sleepMS(long periodMS) {
		sleepNS(periodMS * 1_000_000);
	}


	public static void sleep(long sleepFor, TimeUnit unit) {
		sleepNS(unit.toNanos(sleepFor));
	}


//	private static final int sleepThreshNS = (50 * 1000) / 10;
	/** https://hazelcast.com/blog/locksupport-parknanos-under-the-hood-and-the-curious-case-of-parking/ */
	public static void sleepNS(long ns) {
//
//		if (ns < sleepThreshNS)
//			return;

		LockSupport.parkNanos(ns);

//		sleepNS(ns,
//				 thresh /* 50uSec is the default linux kernel resolution result */
//		);
	}

//	/**
//	 * https://hazelcast.com/blog/locksupport-parknanos-under-the-hood-and-the-curious-case-of-parking/
//	 * expect ~50uSec resolution on linux
//	 */
//	private static void sleepNS(long sleepNS, int epsilonNS) {
//
//
//		if (sleepNS <= epsilonNS) return;
//
//		long end = System.nanoTime() + sleepNS;
//
//		do {
//
////			if (sleepNS >= spinThresholdNS) {
////				if (sleepNS >= 1_000_000 /* 1ms */) {
////					try {
////						Thread.sleep(sleepNS/1_000_000);
////					} catch (InterruptedException e) { }
////				} else {
//					LockSupport.parkNanos(sleepNS);
////				}
////			} else {
////				//Thread.onSpinWait();
////				Thread.yield();
////			}
//
//		} while ((sleepNS = end - System.nanoTime()) > epsilonNS);
//
//	}

//    public static void sleepNS(long periodNS) {
//        if (periodNS > 1_000_000_000 / 1000 / 2  /*0.5ms */) {
//            LockSupport.parkNanos(periodNS);
//            return;
//        }
//
//        final long thresholdNS = 1000; /** 1uS = 0.001ms */
//        if (periodNS <= thresholdNS)
//            return;
//
//        long end = System.nanoTime() + periodNS;
//        //long remainNS = end - System.nanoTime();
//        int pauses = 0;
//        long now;
//        while ((now = System.nanoTime()) < end) {
//            Util.pauseNextCountDown(end - now);
//            //while (remainNS > thresholdNS) {
//
////            if (remainNS <= 500000 /** 100uS = 0.5ms */) {
////                Thread.yield();
////            } else {
////                Thread.onSpinWait();
////            }
//            //Util.pauseNextIterative(pauses++);
//
//            //remainNS = end - System.nanoTime();
//        }
//
//
//    }


//	public static void sleepNSwhile(long periodNS, long napTimeNS, BooleanSupplier keepSleeping) {
//		if (!keepSleeping.getAsBoolean())
//			return;
//
//		if (periodNS <= napTimeNS) {
//			sleepNS(periodNS);
//		} else {
//			long now = System.nanoTime();
//			long end = now + periodNS;
//			do {
//				sleepNS(Math.min(napTimeNS, end - now));
//			} while (((now = System.nanoTime()) < end) && keepSleeping.getAsBoolean());
//		}
//	}

	public static int largestPowerOf2NoGreaterThan(int v) {
		if (v < 1) throw new IllegalArgumentException("x must be greater or equal 1");

		if ((v & v - 1) == 0) return v;

			v |= v >>> 1;
			v |= v >>> 2;
			v |= v >>> 4;
			v |= v >>> 8;
			v |= v >>> 16;
			return v + 1;

//		if (isPowerOf2(i))
//			return i;
//		else {
//			while (--i > 0) {
//				if (isPowerOf2(i))
//					return i;
//			}
//			return 0;
//		}
	}

	public static boolean isPowerOf2(int n) {
		if (n < 1) return false;

		double p_of_2 = (Math.log(n) / log2);
		return Math.abs(p_of_2 - Math.round(p_of_2)) == 0;
	}

	/**
	 * http:
	 * calculate height on a uniform grid, by splitting a quad into two triangles:
	 */
	public static float lerp2d(float x, float z, float nw, float ne, float se, float sw) {

		x -= (int) x;
		z -= (int) z;


		if (x > z)
			sw = nw + se - ne;
		else
			ne = se + nw - sw;


		float n = lerp(x, ne, nw);
		float s = lerp(x, se, sw);
		return lerp(z, s, n);
	}

	public static String secondStr(double s) {
		int decimals;
		if (s >= 0.01) decimals = 0;
		else if (s >= 0.00001) decimals = 3;
		else decimals = 6;

		return secondStr(s, decimals);
	}

	public static String secondStr(double s, int decimals) {
		if (decimals < 0)
			return secondStr(s);
		else {
			return switch (decimals) {
				case 0 -> Str.n2(s) + 's';
				case 3 -> Str.n2(s * 1000) + "ms";
				case 6 -> Str.n2(s * 1.0E6) + "us";
				default -> throw new UnsupportedOperationException("TODO");
			};
		}
	}

	/**
	 * A function where the output is disjunctively determined by the inputs
	 *
	 * @param arr The inputs, each in [0, 1]
	 * @return The output that is no smaller than each input
	 */

	public static <X> X[] sortUniquely(X[] arg) {
		int len = arg.length;
		Arrays.sort(arg);
		for (int i = 0; i < len - 1; i++) {
			int dups = 0;
			while (arg[i].equals(arg[i + 1])) {
				dups++;
				if (++i == len - 1)
					break;
			}
			if (dups > 0) {
				System.arraycopy(arg, i, arg, i - dups, len - i);
				len -= dups;
			}
		}

		return len == arg.length ? arg : Arrays.copyOfRange(arg, 0, len);
	}

	public static boolean calledBySomethingContaining(String s) {
		return Joiner.on(' ').join(Thread.currentThread().getStackTrace()).contains(s);
	}

	public static <X> int count(Predicate<X> p, X[] xx) {
		int i = 0;
		for (var x : xx) if (p.test(x)) i++;
        return i;
	}
	public static int count(FloatPredicate p, float[] xx) {
		int i = 0;
		for (var x : xx) if (p.accept(x)) i++;
		return i;
	}
	public static int count(DoublePredicate p, double[] xx) {
		int i = 0;
		for (var x : xx) if (p.test(x)) i++;
		return i;
	}

	public static <X> boolean and(Predicate<X> p, int from, int to, X[] xx) {
		for (int i = from; i < to; i++)
			if (!p.test(xx[i]))
				return false;
		return true;
	}

	public static <X> boolean or(Predicate<X> p, int from, int to, X[] xx) {
		for (int i = from; i < to; i++)
			if (p.test(xx[i]))
				return true;
		return false;
	}

	public static <X> boolean and(Predicate<X> p, X[] xx) {
		return and(p, 0, xx.length, xx);
	}

	public static <X> boolean or(Predicate<X> p, X[] xx) {
		return or(p, 0, xx.length, xx);
	}

	public static <X> boolean and(X x, Iterable<Predicate<? super X>> p) {
		for (Predicate pp : p) {
			if (!pp.test(x))
				return false;
		}
		return true;
	}

	public static <X> boolean and(Predicate<? super X> p, Iterable<X> xx) {
		for (X x : xx) {
			if (!p.test(x))
				return false;
		}
		return true;
	}

	public static <X> boolean or(Predicate<? super X> p, Iterable<X> xx) {
		for (X x : xx) {
			if (p.test(x))
				return true;
		}
		return false;
	}

	@Nullable public static <X> X first(Predicate<? super X> p, Iterable<X> xx) {
		for (X x : xx) {
			if (p.test(x))
				return x;
		}
		return null;
	}
	public static <X> int count(Predicate<? super X> p, Iterable<X> xx) {
		int c = 0;
		for (X x : xx) {
			if (p.test(x))
				c++;
		}
		return c;
	}


	/**
	 * x in -1..+1, y in -1..+1.   typical value for sharpen will be ~ >5
	 * http:
	 */
	public static float sigmoidBipolar(float x, float sharpen) {
		return (float) ((1 / (1 + Math.exp(-sharpen * x)) - 0.5) * 2);
	}


	public static float[] toFloat(double[] a, int from, int to, DoubleToFloatFunction df) {
		float[] result = new float[to - from];
		for (int j = 0, i = from; i < to; i++, j++) {
			result[j] = df.valueOf(a[i]);
		}
		return result;
	}

	public static float[] toFloat(double[] a, int from, int to) {
		float[] result = new float[to - from];
		for (int j = 0, i = from; i < to; i++, j++) {
			result[j] = (float) a[i];
		}
		return result;
	}

	public static void mul(float scale, float[] f) {
		int n = f.length;
		for (int i = 0; i < n; i++)
			f[i] *= scale;
	}

	public static void mul(double x, double[] f) {
		int n = f.length;
		for (int i = 0; i < n; i++)
			f[i] *= x;
	}
	public static void pow(double x, double[] f) {
		int n = f.length;
		for (int i = 0; i < n; i++)
			f[i] = Math.pow(f[i], x);
	}

	public static <X> X[] arrayOf(IntFunction<X> f, X[] a) {
		return arrayOf(f, 0, a.length, a);
	}

	public static <X> X[] arrayOf(IntFunction<X> f, int from, int to, IntFunction<X[]> arrayizer) {
		assert (to >= from);
		return arrayOf(f, from, to, arrayizer.apply(to - from));
	}

	public static <X> X[] arrayOf(IntFunction<X> f, int from, int to, X[] x) {
		for (int i = from, j = 0; i < to; )
			x[j++] = f.apply(i++);
		return x;
	}


//    /**
//     * builds a MarginMax weight array, which can be applied in a Roulette decision
//     * a lower margin > 0 controls the amount of exploration while values
//     * closer to zero prefer exploitation of provided probabilities
//     */
//    @Paper
//    public static float[] marginMax(int num, IntToFloatFunction build, float lower, float upper) {
//        float[] minmax = {Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
//
//        float[] w = Util.map(num, i -> {
//            float v = build.valueOf(i);
//            if (v < minmax[0]) minmax[0] = v;
//            if (v > minmax[1]) minmax[1] = v;
//            return v;
//        });
//
//        if (Util.equals(minmax[0], minmax[1], Float.MIN_NORMAL * 2)) {
//            Arrays.fill(w, 0.5f);
//        } else {
//
//
//            Util.normalize(w, minmax[0], minmax[1]);
//            Util.normalize(w, 0 - lower, 1 + upper);
//        }
//        return w;
//    }


	public static double softmax(double x, double temp) {
		double f = Math.exp(x / temp);
		return assertFinite(f);
	}

	public static float[] arrayOf(IntToFloatFunction build, @Nullable float[] target) {
		return arrayOf(build, target.length, target);
	}

	public static double[] arrayOf(IntToDoubleFunction build, @Nullable double[] target) {
		Arrays.setAll(target,build);
		return target;
	}

//	public static double[] arrayOf(IntToDoubleFunction build, int num, @Nullable double[] reuse) {
//		double[] f = (reuse != null && reuse.length == num) ? reuse : new double[num];
//		for (int i = 0; i < num; i++)
//			f[i] = build.applyAsDouble(i);
//		return f;
//	}

	public static float[] arrayOf(IntToFloatFunction build, int num, @Nullable float[] reuse) {
		float[] f = (reuse != null && reuse.length == num) ? reuse : new float[num];
		for (int i = 0; i < num; i++)
			f[i] = build.valueOf(i);
		return f;

	}
	public static float[] floatArrayOf(IntToFloatFunction build, int range) {
		return arrayOf(build, range, null);
	}


	public static <X> float[] floatArrayOf(X[] what, FloatFunction<X> value) {
		int num = what.length;
		float[] f = new float[num];
		for (int i = 0; i < num; i++) {
			f[i] = value.floatValueOf(what[i]);
		}
		return f;
	}

	/**
	 * returns amount of memory used as a value between 0 and 100% (1.0)
	 */
	public static float memoryUsed() {
		Runtime r = Runtime.getRuntime();
		long total = r.totalMemory();
		long free = r.freeMemory();
		long max = r.maxMemory();
		long usedMemory = total - free;
		long availableMemory = max - usedMemory;
		return (float) (1 - ((double) availableMemory) / max);
	}


//	public static void toMap(Frequency f, String header, BiConsumer<String, Object> x) {
//		toMap(f.entrySetIterator(), header, x);
//	}

	public static void toMap(HashBag<?> f, String header, BiConsumer<String, Object> x) {
		f.forEachWithIndex((e, n) -> x.accept(header + ' ' + e, n));
	}

	public static void toMap(ObjectIntMap<?> f, String header, BiConsumer<String, Object> x) {
		f.forEachKeyValue((e, n) -> x.accept(header + ' ' + e, n));
	}

	public static void toMap(Iterator<? extends Map.Entry<?, ?>> f, String header, BiConsumer<String, Object> x) {
		f.forEachRemaining((e) -> x.accept(header + ' ' + e.getKey(), e.getValue()));
	}


	/**
	 * pretty close
	 */
	public static float tanhFast(float x) {
		if (x <= -3) return -1f;
		if (x >= 3f) return +1f;
		return x * (27 + x * x) / (27 + 9 * x * x);
	}

	/**
	 * exponential unit-scaled function, take-off curve, x in 0..1, y in 0..1
	 * http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIoMl54LTEpLygyLTEpIiwiY29sb3IiOiIjOTYyQjFCIn0seyJ0eXBlIjowLCJlcSI6IigyXih4KjYpLTEpLygyXig2KS0xKSIsImNvbG9yIjoiIzkxQjgyNSJ9LHsidHlwZSI6MCwiZXEiOiIoM14oeCo4KS0xKS8oM14oOCktMSkiLCJjb2xvciI6IiMwRjU5QTMifSx7InR5cGUiOjAsImVxIjoiKDNeKHgqMTYpLTEpLygzXigxNiktMSkiLCJjb2xvciI6IiM4NDE2QjMifSx7InR5cGUiOjEwMDAsIndpbmRvdyI6WyItMS40NDQwNDQ3OTk5OTk5OTk3IiwiMS45NjM4MjcyIiwiLTAuNDE2MzU4Mzk5OTk5OTk4OSIsIjEuNjgwNzkzNTk5OTk5OTk5OSJdfV0-
	 */
	public static double expUnit(double x, float sharpness /* # decades */) {
		float base = 2; //TODO this affects the shape too
		return powMin1(base, x * sharpness) / powMin1(base, sharpness);
	}

	/** logarithmic saturation curve
	 *  expUnit vs. logUnit: http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIoMl4oeCo2KS0xKS8oMl4oNiktMSkiLCJjb2xvciI6IiMxMTE2QUIifSx7InR5cGUiOjAsImVxIjoiMS0oMl4oKDEteCkqNiktMSkvKDJeKDYpLTEpIiwiY29sb3IiOiIjQzQxMzEzIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiMCIsIjEiLCIwIiwiMSJdfV0-
	 * */
	public static double logUnit(double x, float sharpness) {
//		float base = 2; //TODO this affects the shape too
//		return 1 - powMin1(base, (1-x)*sharpness) / powMin1(base, sharpness);
		return 1-expUnit(1-x, sharpness);
	}

	private static double powMin1(float base, double v) {
		return Math.pow(base, v) - 1;
	}

	public static Object toString(Object x) {
		return x.getClass() + "@" + System.identityHashCode(x);
	}

	/** @noinspection ArrayEquality*/
	public static int compare(byte[] a, byte[] b) {
		if (a==b) return 0;
		int al = a.length;
		int l = Integer.compare(al, b.length);
		if (l != 0)
			return l;
		for (int i = 0; i < al; i++) {
			int d = a[i] - b[i];
			if (d != 0) return d < 0 ? -1 : +1;
		}
		return 0;
	}

	public static <X> Supplier<Stream<X>> buffer(Stream<X> x) {
		List<X> buffered = x.collect(toList());
		return buffered::stream;
	}

	/**
	 * creates an immutable sublist from a ByteList, since this isnt implemented yet in Eclipse collections
	 */
	public static ImmutableByteList subList(ByteList x, int a, int b) {
		int size = b - a;
		if (a == 0 && b == x.size())
			return x.toImmutable();

		return switch (size) {
			case 0 -> ByteLists.immutable.empty();
			case 1 -> ByteLists.immutable.of(x.get(a));
			case 2 -> ByteLists.immutable.of(x.get(a++), x.get(a));
			case 3 -> ByteLists.immutable.of(x.get(a++), x.get(a++), x.get(a));
			case 4 -> ByteLists.immutable.of(x.get(a++), x.get(a++), x.get(a++), x.get(a));
			case 5 -> ByteLists.immutable.of(x.get(a++), x.get(a++), x.get(a++), x.get(a++), x.get(a));
			case 6 -> ByteLists.immutable.of(x.get(a++), x.get(a++), x.get(a++), x.get(a++), x.get(a++), x.get(a));
			default -> ByteLists.immutable.of(ArrayUtil.subarray(x.toArray(), a, b));
		};
	}

	public static <X> X first(X[] x) {
		return x[0];
	}

	public static <X> X last(X[] x) {
		return x[x.length - 1];
	}


//    /* domain: [0..1], range: [0..1] */
//    public static float smoothDischarge(float x) {
//        x = unitize(x);
//        return 2 * (x - 1) / (x - 2);
//    }

	/**
	 * Get the location from which the supplied object's class was loaded.
	 *
	 * @param object the object for whose class the location should be retrieved
	 * @return an {@code Optional} containing the URL of the class' location; never
	 * {@code null} but potentially empty
	 */
	public static @Nullable URL locate(ClassLoader loader, String className) {


		if (loader == null) {
			loader = ClassLoader.getSystemClassLoader();
			while (loader != null && loader.getParent() != null) {
				loader = loader.getParent();
			}
		}

		if (loader != null) {


			try {
				return (loader.getResource(className));
			} catch (RuntimeException ignore) {
				/* ignore */
			}
		}


		return null;
	}

	public static int concurrency() {
		return concurrencyExcept(0);
	}

	public static int concurrencyExcept(int reserve) {

		String specifiedThreads = System.getenv("threads");
		int threads;
		threads = specifiedThreads != null ? Str.i(specifiedThreads) : Runtime.getRuntime().availableProcessors() - reserve;

		int maxThreads = Integer.MAX_VALUE;
		int minThreads = 2;
		return clamp(
			threads, minThreads, maxThreads);
	}

	/**
	 * modifies the input; instance compare, not .equals
	 */
	public static <X> X[] replaceDirect(X[] xx, X from, X to) {
		for (int i = 0, xxLength = xx.length; i < xxLength; i++) {
			X x = xx[i];
			if (x == from)
				xx[i] = to;
		}
		return xx;
	}

	public static <X> X[] replaceDirect(X[] xx, UnaryOperator<X> f) {
		return replaceDirect(xx, 0, xx.length, f);
	}

	public static <X> X[] replaceDirect(X[] xx, int start, int end, UnaryOperator<X> f) {
		for (int i = start; i < end; i++) {
			X x = xx[i];
			xx[i] = f.apply(x);
		}
		return xx;
	}

	public static void assertUnitized(float... f) {
		for (float x : f)
			assertUnitized(x);
	}

	public static float assertUnitized(float x) {
		if (!Float.isFinite(x) || x < 0 || x > 1)
			throw new NumberException("non-unitized value: ", x);
		return x;
	}

	public static double assertUnitized(double x) {
		if (!Double.isFinite(x) || x < 0 || x > 1)
			throw new NumberException("non-unitized value: ", x);
		return x;
	}


	/**
	 * tests if the array is already in natural order
	 */
	public static <X extends Comparable> boolean isSorted(X[] x) {
		int n = x.length;
		if (n < 2) return true;
		for (int i = 1; i < n; i++) {
			if (x[i - 1].compareTo(x[i]) > 0)
				return false;
		}
		return true;
	}


		public static int[] bytesToInts(byte[] x) {
			int n = x.length;
			if (n == 0)
				return ArrayUtil.EMPTY_INT_ARRAY;
			int[] y = new int[n];
			for (int i = 0; i < n; i++)
				y[i] = x[i];
			return y;
		}


	public static Class[] typesOfArray(Object[] orgs) {
		return typesOfArray(orgs, 0, orgs.length);
	}

	public static Class[] typesOfArray(Object[] orgs, int from, int to) {
		return orgs.length == 0 ? ArrayUtil.EMPTY_CLASS_ARRAY : map(x -> Primitives.unwrap(x.getClass()),
			new Class[to - from], 0, orgs, from, to);
	}

	public static Lst<Class<?>> typesOf(Object[] orgs, int from, int to) {
		return new Lst<>(typesOfArray(orgs, from, to));
	}


	/**
	 * fits a polynomial curve to the specified points and compiles an evaluator for it
	 */
	public static <X> ToIntFunction<X> curve(ToIntFunction<X> toInt, int... pairs) {
		if (pairs.length % 2 != 0)
			throw new RuntimeException("must be even # of arguments");

		int points = pairs.length / 2;
		if (points < 2) {
			//TODO return constant function
			throw new RuntimeException("must provide at least 2 points");
		}

		//https://commons.apache.org/proper/commons-math/userguide/fitting.html
		List<WeightedObservedPoint> obs = new Lst(points);
		int yMin = Integer.MAX_VALUE, yMax = Integer.MIN_VALUE;
		for (int i = 0; i < pairs.length; ) {
			int y;
			obs.add(new WeightedObservedPoint(1f, pairs[i++], y = pairs[i++]));
			if (y < yMin) yMin = y;
			if (y > yMax) yMax = y;
		}
		//TODO if yMin==yMax return constant function

		int degree =
			points - 1;
		//points;

		float[] coefficients = toFloat(PolynomialCurveFitter.create(degree).fit(obs));

        /* adapted from: PolynomialFunction
           https://en.wikipedia.org/wiki/Horner%27s_method
           */
		int YMin = yMin, YMax = yMax;
//		assert (yMin < yMax);
		return (X) -> {
			int n = coefficients.length;
			double x = toInt.applyAsInt(X);
			double y = coefficients[n - 1];
			for (int j = n - 2; j >= 0; j--) {
				y = x * y + coefficients[j];
			}
			return clampSafe((int)Math.round(y), YMin, YMax);
		};
	}

	public static int sqrtInt(float x) {
		if (x < 0) throw new NumberException("sqrt of negative value", x);
		return (int) Math.round(Math.sqrt(x));
	}

	public static int sqrtIntFloor(float x) {
		if (x < 0) throw new NumberException("sqrt of negative value", x);
		return (int)(Math.sqrt(x));
	}
	public static int sqrtIntCeil(float x) {
		if (x < 0) throw new NumberException("sqrt of negative value", x);
		return (int)(ceil(Math.sqrt(x)));
	}

	public static int cbrtInt(float x) {
		if (x < 0)
			throw new NumberException("sqrt of negative value", x);
		return (int) Math.round(Math.pow(x, 1/3.0));
	}

	public static int logInt(float x) {
		return (int) Math.round(Math.log(x));
	}


	/**
	 * scan either up or down within a capacity range
	 */
	public static int next(int current, boolean direction, int cap) {
		if (direction) {
			if (++current == cap) return 0;
		} else {
			if (--current == -1) return cap - 1;
		}
		return current;
	}

	/**
	 * if the collection is known to be of size==1, get that item in a possibly better-than-default way
	 * according to the Collection's implementation
	 */
	public static @Nullable <X> X only(Collection<X> next) {
		if (next instanceof List)
			return ((List<X>) next).get(0);
		else if (next instanceof MutableSet)
			return ((MutableSet<X>) next).getOnly();
		else if (next instanceof SortedSet)
			return ((SortedSet<X>) next).first();
		else
			return next.iterator().next();
		//TODO SortedSet.getFirst() etc
	}

	@SafeVarargs
	public static <X> IntSet intSet(ToIntFunction<X> f, X... items) {
		switch (items.length) {
			case 0:
				return IntSets.immutable.empty();
			case 1:
				return IntSets.immutable.of(f.applyAsInt(items[0]));
			case 2:
				return IntSets.immutable.of(f.applyAsInt(items[0]), f.applyAsInt(items[1]));
			//...
			default:
				IntHashSet i = new IntHashSet(items.length);
				for (X x : items) {
					i.add(f.applyAsInt(x));
				}
				return i;
		}
	}


//	public static double interpSum(float[] data, double sStart, double sEnd) {
//		return interpSum((i) -> data[i], data.length, sStart, sEnd, false);
//	}

	/** TODO not 100% working */
	public static double interpMean(IntToFloatFunction data, int capacity, double sStart, double sEnd, @Deprecated boolean wrap) {
		sStart = Math.max(0, sStart);
		sEnd = Math.min(capacity-1, sEnd);

		int iStart = (int) ceil(sStart);
		int iEnd = (int) (sEnd);
//
//		int i = iStart;
//		if (i < 0) {
//			if (wrap)
//				while (i < 0) i += capacity;
//			else
//				i = 0;
//		} else if (i >= capacity) {
//			i = 0; //wrap?
//		}

		if (iEnd < 0 || iStart >= capacity)
			return 0; //OOB

		iStart = Math.max(0, iStart);
		iEnd = Math.min(capacity-1, iEnd);

		double sum = 0;

		if (iStart > 0)
			sum += (iStart - sStart) * data.valueOf(iStart-1);

		for (int k = iStart; k < iEnd; k++) {
			//if (i == capacity) i = 0;
			sum += data.valueOf(k);
		}

		if (iEnd < capacity)
			sum += (sEnd - iEnd) * data.valueOf(iEnd);

		return sum/(sEnd-sStart);
	}


	public static int longToInt(long x) {
		if (x > Integer.MAX_VALUE  || x < Integer.MIN_VALUE)
			throw new NumberException("long exceeds int capacity", x);
		return (int) x;
	}

	/**
	 * faster than cartesian distance
	 */
	public static void normalizeHamming(float[] v, float target, float epsilon) {
		float current = 0;
		for (float value : v)
			current += Math.abs(value);

		if (current < epsilon) {
			Arrays.fill(v, target / v.length);
		} else {
			float scale = target / current;
			for (int i = 0; i < v.length; i++)
				v[i] *= scale;
		}
	}


	public static long readToWrite(long l, StampedLock lock) {
		return readToWrite(l, lock, true);
	}

	public static long readToWrite(long l, StampedLock lock, boolean strong) {

		if (l != 0) {
			if (StampedLock.isWriteLockStamp(l))
				return l;

			long ll = lock.tryConvertToWriteLock(l);
			if (ll != 0) return ll;

			if (!strong) return 0;

			lock.unlockRead(l);
		}

		return strong ? lock.writeLock() : lock.tryWriteLock();
	}

	public static long writeToRead(long l, StampedLock lock) {
		if (l != 0) {
			if (StampedLock.isReadLockStamp(l))
				return l;

			long ll = lock.tryConvertToReadLock(l);
			if (ll != 0) return ll;

			lock.unlockWrite(l);
		}

		return lock.readLock();
	}

	/** selects the previous instance if equal */
	public static <X, Y extends X, Z extends X> X maybeEqual(@Nullable Z next, @Nullable Y prev) {
		return Objects.equals(next, prev) ? prev : next;
	}

	public static long[] maybeEqual(long[] next, @Nullable long[] prev) {
		return next==prev || (prev!=null && ArrayUtil.equals(next, prev)) ?
				prev :
				next;
	}

	public static <X> X maybeEqual(X next, X prevA, X prevB) {
		if (Objects.equals(next, prevA)) return prevA;
		else if (Objects.equals(next, prevB)) return prevB;
		else return next;
	}

	/** NOP harness, useful for debugging */
	public static void nop() {
		//Thread.onSpinWait();
	}

	public static float sqrtBipolar(float d) {
		return Math.signum(d) * sqrt(Math.abs(d));
	}


	/** untested */
	public static float lerpInverse(float x, float min, float max) {
		return 1/lerp(x, 1/max, 1/min);
	}

    /**
     * compose filter from one or two filters
     */
    public static <X> Predicate<X> filter(@Nullable Predicate<X> a, @Nullable Predicate<X> b) {
        if (b == null || a == b) return a;
        else if (a == null) return b;
        else return a.and(b);
    }

    public static <X> Map.Entry<? extends X, X> firstEntry(Iterable<? extends Map.Entry<? extends X, X>> m) {
        Map.Entry<? extends X, X> e;
        if (m instanceof ArrayUnenforcedSet)
            e = ((ArrayUnenforcedSet<Map.Entry<? extends X, X>>) m).items[0]; //direct access
        else
            e = m.iterator().next();
        return e;
    }

    /** 2 pairs of key, values pairs */
    public static <X> X[] firstTwoEntries(X[] target, Iterable<? extends Map.Entry<? extends X, X>> m) {
		X a, aa, b, bb;
		if (m instanceof ArrayUnenforcedSet) {
			//direct access
			Map.Entry<? extends X, X>[] ee = ArrayUnenforcedSet.toArrayShared((ArrayUnenforcedSet<Map.Entry<? extends X, X>>)m);
			a = ee[0].getKey(); aa = ee[0].getValue();
			b = ee[1].getKey(); bb = ee[1].getValue();
		} else {
			Iterator<? extends Map.Entry<? extends X, X>> ii = m.iterator();
			Map.Entry<? extends X, X> aaa = ii.next(), bbb = ii.next();
			a = aaa.getKey();
			aa = aaa.getValue();
			b = bbb.getKey();
			bb = bbb.getValue();
		}
		target[0] = a; target[1] = aa; target[2] = b; target[3] = bb;
		return target;
	}

    public static MethodHandle sequence(MethodHandle[] yy, Object... args) {
        MethodHandle z = null;
        for (int i = yy.length - 1; i >= 0; i--) {
            MethodHandle y = yy[i];

            if (args!=null) y = MethodHandles.insertArguments(y, 0, args);

			z = z == null ? y : MethodHandles.filterReturnValue(y, z);
        }
        return z;
    }

    /**
     * Wang-Jenkings Hash Spreader
	 *
	 * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because we use power-of-two length hash tables, that otherwise
     * encounter collisions for hashCodes that do not differ in lower
     * or upper bits.
	 *
	 * from: ConcurrentReferenceHashMap.java found in Hazelcast
     */
    public static int spreadHash(int h) {
        h += (h << 15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h << 3);
        h ^= (h >>> 6);
        h += (h << 2) + (h << 14);
        return h ^ (h >>> 16);
    }


	public static VarHandle VAR(Class c, String field, Class<?> type) {
		try {
			return MethodHandles.privateLookupIn(c, lookup())
					.findVarHandle(c, field, type)
					.withInvokeExactBehavior();
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

    public static <X> X ifEqualThen(@Nullable X x, X maybeEqualX, X ifEqualsXThenThis) {
		return Objects.equals(x, maybeEqualX) ? ifEqualsXThenThis : x;
    }

	public static boolean inIncl(int x, int min, int max) {
		return x >= min && x <= max;
	}
	public static boolean inIncl(long x, long min, long max) {
		return x >= min && x <= max;
	}
	public static boolean inIncl(float x, float min, float max) {
		return x >= min && x <= max;
	}


	public static <X> Consumer<X> compose(Consumer<X> a, @Nullable Consumer<X> b) {
		return b == null ? a : a.andThen(b);
    }

	public static <X> Iterator<X> nonNull(Iterator<X> i) {
		return Iterators.filter(i, Objects::nonNull);
	}

	/** returns the ith byte in the 4 bytes of int x */
	public static int intByte(int x, int i) {
		return switch(i) {
			case 0 -> (x & 0x000000ff);
			case 1 -> (x & 0x0000ff00) >> 8;
			case 2 -> (x & 0x00ff0000) >> 16;
			case 3 -> (x & 0xff000000) >> 24;
			default -> throw new UnsupportedOperationException();
		};
	}

	/** returns a value between 0 and 1.0f for the proportion of the 8-bit range covered by the ith sub-byte of integer x */
	public static float intBytePct(int x, int i) {
		return intByte(x, i) / 256f;
	}

	@Is("Relative_change_and_difference") public static double pctDiff(double x, double y) {
		if (x == y) return 0;
		return abs(x - y) /
				mean(abs(x), abs(y));
				//max(abs(x), abs(y));
				//min(abs(x), abs(y));
	}

	public static void clampSafe(double[] x, double min, double max) {
		for (int a = 0; a < x.length; a++)
			x[a] = clampSafe(x[a], min, max);
	}

	public static double normalizePolar(double[] x, double lenThresh) {
		double[] minmax = minmax(x, 0, x.length);
		double len =
				//rad
				Math.max(abs(minmax[0]), abs(minmax[1]));

		//manhattan distance
		//Util.sumAbs(x);

		//cartesian
		//TODO

		if (len < Float.MIN_NORMAL) {
			Arrays.fill(x, 0);
			len = 0;
		} else {
			final boolean noThresh = lenThresh != lenThresh;
			if (noThresh || len > lenThresh) {
				double r = noThresh ? 1 : lenThresh / len; //normalization factor
				for (int i = 0; i < x.length; i++)
					x[i] *= r;
			}
		}
		return len;
	}

    /**
     * https://en.wikipedia.org/wiki/LogSumExp
     * https://pytorch.org/docs/stable/generated/torch.logsumexp.html
     * https://www.deeplearningbook.org/slides/04_numerical.pdf (page 31)
     * */
    @Is("LogSumExp") public static double logsumexp(double[] x, double innerPlus, float innerProd) {
        double mx = innerProd * (max(x) + innerPlus);
		double s = 0;
        for (double xx : x) {
			//s += Math.exp(innerProd * (xx + innerPlus) - mx);
			s += Math.exp(fma(innerProd, xx + innerPlus, -mx));
		}
        double y = Math.log(s) + mx;
        //System.out.println(logsumexp_simple(x, innerPlus, innerProd) + " "  + y);
        return y;
    }

    /** for reference */
	@Deprecated private static double logsumexp_simple(double[] x, double innerPlus, float innerMult) {
		double s = 0;
		for (double xx : x)
			s += Math.exp(innerMult * (xx + innerPlus));
		return Math.log(s);
	}

	public static int shortUpper(int x) {
		return x >> 16;
	}

	public static int shortLower(int x) {
		return x & 0xffff;
	}
	public static int shortToInt(short high, short low) {
		return high << 16 | low;
	}

	public static int shortToInt(int high, int low) {
		return high << 16 | low;
	}

	public static short shortToInt(int x, boolean high) {
		return (short) (high ? x >> 16 : x & 0xffff);
	}

	public static <X> Supplier<X> once(Supplier<X> f) {
		return new Supplier<X>() {
			X x = null;
			@Override
			public X get() {
				X x = this.x;
				return x == null ? (this.x = f.get()) : x;
			}
		};
	}

	public static double powAbs(double x, float p) {
		return x >= 0 ? Math.pow(x, p) : -Math.pow(-x, p);
	}

    public static float halflifeRate(float period) {
          return period < 1 ?
                  1 //instant
                  :
                  (float) (Math.log(2)/period); //half-life
    }
}