package jcog.bloom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * Created by jeff on 14/05/16.
 */
class CountingLeakySetTest {

    private StableBloomFilter<String> filter;

    @BeforeEach
    void before() {
        this.filter = BloomFilterBuilder.get().buildFilter();
    }

    @Test
    void whenAskedIfContainsDeletedObject_returnsFalse() {
        String string = "somestr";

        filter.add(string);
        filter.remove(string);

        boolean containsString = filter.contains(string);
        assertFalse(containsString);
    }

}
