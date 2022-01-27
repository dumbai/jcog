package jcog;

import com.google.common.util.concurrent.MoreExecutors;
import jcog.thing.Part;
import jcog.thing.Thing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** TODO add actual tests
 * @noinspection ALL*/
class ThingTest {

    private Thing<Object, Object> s;

    @BeforeEach
    void init() {
//        Exe.single();
        s = new Thing("theContext");
        assertEquals(MoreExecutors.directExecutor(), s.executor); //ensure single-threaded set
    }

    @Test
    void testParts1() {


        StringBuilder sb = new StringBuilder();
        s.add("x", new DummyPart(sb), false);
        s.add("y", new DummyPart(sb));
        assertEquals(2, s.size());
        s.print(System.out);
        assertEquals(1, s.partStream().filter(Part::isOn).count());
        assertEquals(1, s.partStream().filter(Part::isOff).count());

        s.print(System.out);

        s.stopAll();

        assertEquals(2, s.partStream().filter(Part::isOff).count());

        s.restart("x");
        assertEquals(1, s.partStream().filter(Part::isOn).count());
        s.stop("x");
        assertEquals(0, s.partStream().filter(Part::isOn).count());

        assertTrue(s.remove("y"));
        assertEquals(1, s.size());
    }


    public static class DummyPartWithContextConstructor extends DummyPart {
        public DummyPartWithContextConstructor(String context) {
            super(new StringBuilder("parts=" + context));
        }
    }

    public static class DummyPart extends Part<Object> {
        private final StringBuilder sb;

        public DummyPart() {
            this(new StringBuilder("no-arg_constructor"));
        }

        public DummyPart(StringBuilder sb) {
            this.sb = sb;
        }

        @Override
        protected void start(Object x) {
            sb.append(this).append(" start\n");
        }

        @Override
        protected void stop(Object x){
            sb.append(this).append(" stop\n");
        }
    }


    @Test void testConstructorInjection0() {



        s.add("x", DummyPart.class);
        s.add("y", DummyPart.class, false);

        assertEquals(2, s.size());
        assertEquals(1, s.partStream().filter(Part::isOn).count());
        assertEquals(1, s.partStream().filter(Part::isOff).count());

//        assertEquals("", s.partEntrySet().toString());

        s.add("z", DummyPartWithContextConstructor.class);
        assertEquals(3, s.size());

        s.add("x", DummyPart.class);

        s.print(System.out);

    }
}