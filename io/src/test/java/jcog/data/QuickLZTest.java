package jcog.data;

import jcog.Str;
import jcog.io.lz.QuickLZ;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class QuickLZTest {

	private static float testCompressDecompress(String s, int level) {
		System.out.print(s + "\n\t");
		return testCompressDecompress(s.getBytes(), level);
	}

	private static float testCompressDecompress(byte[] input, int level) {
		byte[] compressed = QuickLZ.compress(input, level);
		byte[] decompress = QuickLZ.decompress(compressed);

		assertArrayEquals(input, decompress);

		float ratio = ((float) compressed.length) / (input.length);
		System.out.println(input.length + " input, " + compressed.length + " compressed = " +
			Str.n2(100f * ratio) + '%');
		return ratio;
	}


	@ParameterizedTest
	@ValueSource(ints = {1, 3})
	void testSome(int level) {


		testCompressDecompress("x", level);
		testCompressDecompress("abc", level);
		testCompressDecompress("abcsdhfjdklsfjdklsfjd;s fja;dksfj;adskfj;adsfkdas;fjadksfj;kasdf", level);
		testCompressDecompress("222222222222211111111111111112122222222222111111122222", level);

		testCompressDecompress("(a --> (b --> (c --> (d --> e))))", level);

        for (int s : new int[] { 16, 32, 64, 128, 256, 512, 1024 }) {
            for (int i = 0; i < 10; i++) {
                testCompressDecompress(randomStr(s), level);
            }
        }

	}

    static byte[] randomStr(int size) {
        byte[] x = new byte[size];
        Random rng = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++)
            x[i] = (byte) rng.nextInt();
        return x;
    }
}