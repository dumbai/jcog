package jcog.bloom.hash;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Created by jeff on 16/05/16.
 */
class FNV1aTest {

    @Test
    void whenInvoked_returnsCorrectHash() {
        byte[] data = "hello world".getBytes();

        int hash = FNV1aHash.hash(data);

        assertEquals(hash, 0xd58b3fa7);
    }

}
