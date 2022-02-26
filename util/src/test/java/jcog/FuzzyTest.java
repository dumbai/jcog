package jcog;

import org.junit.jupiter.api.Test;

import static jcog.Fuzzy.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuzzyTest {

    @Test void eqNorm1() {
        assertEquals(0.5f, eqNorm(1, 0.5f), 0.01f);
        assertEquals(0.5f, eqNorm(0, 0.5f), 0.01f);
        assertEquals(0, eqNorm(1, 0), 0.01f);
        assertEquals(1, eqNorm(1, 1), 0.01f);
        assertEquals(1, eqNorm(0, 0), 0.01f);

        assertEquals(0.5f, eqNorm(0.75f, 0.5f), 0.01f);
        assertEquals(0.75f, eqNorm(1f, 0.75f), 0.01f);

        assertEquals(0f, eqNorm(0.5f-0.25f/2f, 0.5+0.25f/2f), 0.01f);
    }

    @Test void eq1() {
        assertEquals(0.5f, Fuzzy.equals(0.5f, 1), 0.01f);

        assertEquals(0.75f, Fuzzy.equals(0.5f, 0.75f), 0.01f);
        assertEquals(0.75f, Fuzzy.equals(1f, 0.75f), 0.01f);

        assertEquals(0.75f, Fuzzy.equals(0.5f-0.25f/2f, 0.5+0.25f/2f), 0.01f);
    }

    @Test void eqPolar1() {
        final int extreme = 4;
        assertEquals(Fuzzy.equals(0.5, 0.6), Fuzzy.equals(0.4, 0.5));
        assertTrue(equalsPolar(0.45, 0.55, extreme) < equalsPolar(0.5, 0.6, extreme));
    }

    @Test void testXNR() {
        assertEquals(0.5, xnr(0.5, 0.5), 0.01);
        assertEquals(0.5, xnr(0.5, 1), 0.01);

        assertEquals(0.55, xnr(0.6, 0.75), 0.01);
        assertEquals(0.48, xnr(0.6, 0.4), 0.01);
    }
    @Test void XNRNorm_() {
        assertEquals(0.8, xnrNorm(0.6, 0.75), 0.01);
        assertEquals(0.82, xnrNorm(0.7, 0.85), 0.01);
    }
    @Test void eqNorm_() {
        assertEquals(0.90, eqNorm(0.0, 0.1), 0.01);
        assertEquals(0.88, eqNorm(0.1, 0.2), 0.01);
        assertEquals(0.83, eqNorm(0.2, 0.3), 0.01);
        assertEquals(0.75, eqNorm(0.3, 0.4), 0.01);
        assertEquals(0.50, eqNorm(0.4, 0.5), 0.01);
        assertEquals(0.50, eqNorm(0.5, 0.6), 0.01);
        assertEquals(0.75, eqNorm(0.6, 0.7), 0.01);
        assertEquals(0.83, eqNorm(0.7, 0.8), 0.01);
        assertEquals(0.88, eqNorm(0.8, 0.9), 0.01);
        assertEquals(0.90, eqNorm(0.9, 1), 0.01);
    }
}