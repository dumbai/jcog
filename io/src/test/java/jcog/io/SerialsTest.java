package jcog.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerialsTest {

    @Test
    void test1(){
        assertTranscode("skjfldksf", Object.class);
    }

    private static void assertTranscode(Object x, Class cl) {
        try {
            Object result = Serials.fromBytes(Serials.toBytes(x, cl), cl);
            assertEquals(x, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}