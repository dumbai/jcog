package jcog.data.graph;


import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdjGraphTest {

    @Test
    void testGraph1() {

        AdjGraph<String,String> g = new AdjGraph<>(true);
        assertEquals(1, g.eid(0, 1));
        assertEquals(4294967296L, g.eid(1, 0));
        assertEquals(4294967297L, g.eid(1, 1));

        int xIndex = g.addNode("x");
        assertEquals(xIndex, g.addNode("x"));
        g.addNode("y");
        assertTrue( g.setEdge("x", "y", "xy") );
        assertFalse( g.setEdge("x", "y", "xy") ); 
        assertEquals("xy", g.edge("x", "y"));
        assertFalse( g.setEdge("x", "z", "xz") );

        g.setEdge("y", "x", "yx");
        g.addNode("z"); 

        assertTrue( g.removeEdge("x", "y") );
        assertNull( g.edge("x", "y" ));

        g.writeGML(System.out);

        GraphMetrics m = new GraphMetrics(g);

        List<IntHashSet> wc = m.weakly();
        

        
        IntIntHashMap tj = m.strongly();
        System.out.println(tj);


    }
}