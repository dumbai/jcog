package jcog.util;

import com.google.common.primitives.Bytes;
import jcog.Is;

/** from: https://stackoverflow.com/questions/21341027/find-indexof-a-byte-array-within-another-byte-array */
public enum ByteSearch { ;

    @Is("Knuth–Morris–Pratt_algorithm") public static int indexOfDirect(byte[] data, byte[] pattern) {
        return Bytes.indexOf(data, pattern);
    }

    /**
     * Search the data byte array for the first occurrence
     * of the byte array pattern using Knuth-Morris-Pratt
     */
    @Is("Knuth–Morris–Pratt_algorithm") public static int indexOfKMP(byte[] data, byte[] pattern) {
        int[] failure = computeFailure(pattern);

        int j = 0;

        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     */
    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }
}