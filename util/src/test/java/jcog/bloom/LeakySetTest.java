package jcog.bloom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by jeff on 14/05/16.
 */
class LeakySetTest {

    private LeakySet<String> filter;

    @BeforeEach
    void before() {
        this.filter = BloomFilterBuilder.get().buildFilter();
    }

    @Test
    void whenAskedIfContainsAddedObject_returnsTrue() {
        String string = "somestr";

        filter.add(string);
        boolean isContained = filter.contains(string);

        assertTrue(isContained);
    }

    
    
    @Test
    void whenAskedIfContainsNotAddedObject_returnsFalse() {
        String string1 = "somestr";
        String string2 = "someotherstr";
        assertNotEquals(string1, string2);

        filter.add(string1);
        boolean isStr2Contained = filter.contains(string2);

        assertFalse(isStr2Contained);
    }

}
