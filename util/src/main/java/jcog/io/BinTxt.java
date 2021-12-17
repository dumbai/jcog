package jcog.io;

import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Binar <-> Text Transducers
 */
public enum BinTxt {
	;


	public static String uuid128() {
		UUID u = UUID.randomUUID();
		long a = u.getLeastSignificantBits();
		StringBuilder sb = new StringBuilder(6);
		BinTxt.append(sb, a);
		long b = u.getMostSignificantBits();
		BinTxt.append(sb, b);
		return sb.toString();
	}

//    final static Random insecureRandom = new XoRoShiRo128PlusRandom(System.nanoTime());

	public static String uuid64() {
		//UUID u = UUID.randomUUID();
		//long a = u.getLeastSignificantBits();
		//long b = u.getMostSignificantBits();
		//long c = a ^ b;
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		long c = rng.nextLong();
		return BinTxt.toString(c);
	}

	public static final char[] symbols;

	static {
        StringJoiner joiner = new StringJoiner("", "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_.~", "");
        for (int i = 192; i <= 255; i++) {
            String s = String.valueOf((char) i);
            joiner.add(s);
        }
        String x = joiner.toString();


        symbols = x.toCharArray();
	}

	public static final int maxBase = symbols.length;


	/**
	 * URIchars must be at least base length
	 */
	public static String toString(long aNumber, int base) {
		StringBuilder result = new StringBuilder(16);

		append(result, aNumber, base);

		return result.toString();
	}

	public static String toString(long l) {
		return toString(l, maxBase);
	}

	public static void append(StringBuilder target, long v) {
		append(target, v, maxBase);
	}

	public static void append(StringBuilder target, long v, int base) {
		if (v < 0) {
			target.append('-');
			v = -v;
		}

		//_append(target, v, base);
		do {
			int r = (int)(v % base);
			v = (v-r)/base;
			target.append(symbols[r]);
		} while (v!=0);
	}

//	private static void _append(StringBuilder target, long v, int base) {
//		int r = (int) (v % base);
//
//		if (v - r != 0)
//			_append(target, (v - r) / base, base);
//
//		target.append(symbols[r]);
//	}
}
