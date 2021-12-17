//package jcog.lab;
//
//import jcog.lab.util.Imputer;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//class ImputerTest {
//
//    static class SimplePOJO {
//        public int a;
//        public boolean b;
//    }
//
//    @Test
//    public void testSimple() {
//        Imputer i = new Imputer();
//
//        SimplePOJO x = new SimplePOJO();
//        x.a = 1;
//        x.b = false;
//
//        Variables<SimplePOJO> ti = i.learn(x, "default");
//        assertEquals(2, ti.tweaks.size());
//
//
//        SimplePOJO y = new SimplePOJO();
//        Imputer.Imputing<SimplePOJO> yy = i.apply(y, "default");
//        assertEquals(2, yy.log.size());
//        assertEquals(0, yy.issues.size());
//        assertEquals(x.a, y.a);
//        assertEquals(x.b, y.b);
//    }
//
//}