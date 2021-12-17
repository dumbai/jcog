package jcog.data.graph;

import com.google.common.base.Joiner;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KrushkalMSTTest {

    @Test
    void test1() {
        MinTree.Graph graph = new MinTree.Graph(3);
        graph.edge(0, 1, 1);
        graph.edge(0, 2, 1);
        List<MinTree.Graph.IntTree> trees = graph.apply();
        assertEquals("{0->1}x1,{0->2}x1", Joiner.on(",").join(graph.mst));
        assertEquals("0[1, 2]", Joiner.on(",").join(trees));
    }

    @Test
    void test2() {
        MinTree.Graph graph = new MinTree.Graph(6);
        graph.edge(0, 1, 4);
        graph.edge(0, 2, 3);
        graph.edge(1, 2, 1);
        graph.edge(1, 3, 2);
        graph.edge(2, 3, 4);
        graph.edge(3, 4, 2);
        graph.edge(4, 5, 6);

        List<MinTree.Graph.IntTree> trees = graph.apply();
        assertEquals("{1->2}x1,{1->3}x2,{3->4}x2,{0->2}x3,{4->5}x6", Joiner.on(",").join(graph.mst));
        assertEquals("0[2[1[3[4[5]]]]]", Joiner.on(",").join(trees));
    }
}