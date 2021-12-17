package jcog.decision.utils;

import org.junit.jupiter.api.Test;

import static com.google.common.math.DoubleMath.log2;
import static org.junit.jupiter.api.Assertions.assertEquals;


class MathUtilsTests {

    @Test
    void testLog2() {
        assertEquals(1.0, log2(2.0), 0.01);
        assertEquals(3.32192809, log2(10.0), 0.00000001);
    }

}
