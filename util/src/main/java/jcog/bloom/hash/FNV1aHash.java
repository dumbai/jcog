package jcog.bloom.hash;

/**
 * Created by jeff on 16/05/16.
 */
public enum FNV1aHash {
	;

	private static final int OFFSET_BASIS = 0x811C9DC5;
    private static final int PRIME = 0x01000193;

    public static int hash(byte[] data) {
        return hash(data, data.length);
    }

    public static int hash(byte[] data, int len) {
        int hash = OFFSET_BASIS;

        for (int i = 0; i < len; i++) {
            byte octet = data[i];
            hash ^= octet;
            hash *= PRIME;
        }

        return hash;
    }

}
