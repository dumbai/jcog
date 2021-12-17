package alice.tuprolog;

import alice.tuprolog.lib.OOLibrary;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JavaTests {
    private static final Prolog p = new Prolog();
    static {
        try {
            p.addLibrary(OOLibrary.class);
        } catch (InvalidLibraryException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void testHashTable() throws IOException, InvalidTheoryException {
        p.input(
            Theory.resource(JavaTests.class, "hash_table.pl")
        );


        p.addOutputListener(System.err::println);
        p.solve("test(4).", System.out::println);
        p.solve("test2(4).", System.out::println);
    }

    @Test
    void testPoints2() throws IOException, InvalidTheoryException {
        p.input(
            Theory.resource(JavaTests.class, "points_test2.pl")
        );

        p.solve("test(2).", System.out::println);
        p.solve("test(3).", System.out::println);
    }

}
