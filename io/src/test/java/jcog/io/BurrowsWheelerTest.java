package jcog.io;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static jcog.io.BurrowsWheeler.decode;
import static jcog.io.BurrowsWheeler.encode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BurrowsWheelerTest {

    @ParameterizedTest @ValueSource(strings = {
        "sdkfjklsdfklsdfj",
        "lasjdfljdakj fdasfkldjsfa dsfdasfjdasjfkl"
    })
    void test1(String p) {
        assertBWT(p);
    }


    private static void assertBWT(String i) {
        byte[] ib = i.getBytes();

        byte[] eb = new byte[ib.length];
        int key = encode(ib, eb);

        byte[] ob = new byte[eb.length];
        decode(eb, key, ob);
        String o = new String(ob);
        String io = new String(eb);

        assertNotEquals(i, io);
        assertEquals(i, o);
        //System.out.println(i + " " + io + " " + o + "\t" + i.equals(o));
    }

}