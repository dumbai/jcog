package jcog.io;

import jcog.table.ARFF;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ARFFTest {

    /**
     * Main function for debugging. Loads files from the argument list
     * and dumps their contents to the System.out.
     */
    @Test
    void testParse() throws IOException, ARFF.ARFFParseError {

        String file = "% oh yes, this is great!\n" +
                "% and even better than I thought!\n" +
                "@relation foobar\n" +
                "@attribute x real\n" +
                "@attribute y string\n" +
                "@attribute z {yes, no}\n" +
                "@data\n" +
                "1.0,what,yes\n" +
                "0,ffukk,no\n\n";


        ARFF arff = new ARFF(file);
        //System.out.println(arff.describe());
        arff.write(System.out);

        assertEquals(3, arff.columnCount());
        assertEquals(2, arff.size());






    }

    @Test
    void testGenerate() throws IOException {
        ARFF a = new ARFF();
        a.defineText("test").defineNumeric("score");

        a.add("should be in quotes", 238428834);
        for (int i = 0; i < 4; i++) {
            a.add("x" + (i^2934), Math.random());
        }
        a.write(System.out);

    }

    static class Schema1 {
        final String name;
        final float score;
        final boolean ok;

        Schema1(String name, float score, boolean ok) {
            this.name = name;
            this.score = score;
            this.ok = ok;
        }
    }

    @Test
    void testARFFObjectAdd() throws IOException {
        ARFF.ARFFObject<Schema1> a = new ARFF.ARFFObject<>(Schema1.class);
        a.put(new Schema1("abc", 0.5f, true));
        a.put(new Schema1("def", 0.75f, false));
//        assertSame(ARFF.AttributeType.Text, a.attrType("name"));
//        assertSame(ARFF.AttributeType.Numeric, a.attrType("score"));
//        assertSame(ARFF.AttributeType.Nominal, a.attrType("ok"));
        a.write(System.out);
        assertEquals(2, a.size());
    }

}
