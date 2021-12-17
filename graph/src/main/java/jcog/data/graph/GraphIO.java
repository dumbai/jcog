/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package jcog.data.graph;

import java.io.PrintStream;
import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Implements static methods to load and write graphs.
 */
public enum GraphIO { ;

    /**
     * Prints graph in edge list format. Each line contains exactly two
     * node IDs separated by whitespace.
     */
    public static void writeEdgeList(IntIndexedGraph g, PrintStream out) {

        for (int i = 0; i < g.size(); ++i) {
            int ii = i;
            g.neighborsOut(i).forEach(o -> out.println(ii + " " + o));
        }
    }



    /**
     * Prints graph in neighbor list format. Each line starts with the
     * id of a node followed by the ids of its neighbors separated by space.
     */
    public static void writeNeighborList(IntIndexedGraph g, PrintStream out) {

        out.println("# " + g.size());

        for (int i = 0; i < g.size(); ++i) {
            out.print(i + " ");
            g.neighborsOut(i).forEach(o -> out.print(o + " "));
            out.println();
        }
    }



    /**
     * Saves the given graph to
     * the given stream in DOT format. Good for the graphviz package.
     */
    public static void writeDOT(IntIndexedGraph g, PrintStream out) {

        out.println((g.directed() ? "digraph" : "graph") + " {");

        for (int i = 0; i < g.size(); ++i) {
            int ii = i;
            g.neighborsOut(i).forEach(j -> {
                if (g.directed())
                    out.println(ii + " -> " + j + ';');
                else if (ii <= j)
                    out.println(ii + " -- " + j + ';');
            });
        }

        out.println("}");
    }



    /**
     * Saves the given graph to
     * the given stream in GML format.
     * https://www.fim.uni-passau.de/fileadmin/files/lehrstuhl/brandenburg/projekte/gml/gml-technical-report.pdf
     */
    public static void writeGML(IntIndexedGraph g, PrintStream out) {

        out.println("graph [ directed " + (g.directed() ? "1" : "0"));

        int size = g.size();
        for (int i = 0; i < size; ++i)
            out.println("node [ id " + i + " label \"" + g.vertex(i) + "\" ]");

        for (int i = 0; i < size; ++i) {
            int ii = i;
            g.neighborsOut(i).forEach(o -> {
                out.println(
                        "edge [ source " + ii + " target " + o + " ]"); //TODO edge label
            });
        }

        out.println("]");
    }



    /**
     * Saves the given graph to
     * the given stream to be read by NETMETER. It should be ok also for Pajek.
     */
    public static void writeNetmeter(IntIndexedGraph g, PrintStream out) {

        out.println("*Vertices " + g.size());
        for (int i = 0; i < g.size(); ++i)
            out.println((i + 1) + " \"" + (i + 1) + '"');

        out.println("*Arcs");
        for (int i = 0; i < g.size(); ++i) {
            int ii = i;
            g.neighborsOut(i).forEach(o -> out.println((ii + 1) + " " +
                    (o + 1) + " 1"));
        }
        out.println("*Edges");
    }



    /**
     * Saves the given graph to
     * the given stream in UCINET DL nodelist format.
     */
    public static void writeUCINET_DL(IntIndexedGraph g, PrintStream out) {

        out.println("DL\nN=" + g.size() + "\nFORMAT=NODELIST\nDATA:");

        for (int i = 0; i < g.size(); ++i) {
            out.print(" " + (i + 1));
            g.neighborsOut(i).forEach(o -> out.print(" " + (o + 1)));
            out.println();
        }
        out.println();
    }



    /**
     * Saves the given graph to
     * the given stream in UCINET DL matrix format.
     */
    public static void writeUCINET_DLMatrix(IntIndexedGraph g, PrintStream out) {

        out.println("DL\nN=" + g.size() + "\nDATA:");

        for (int i = 0; i < g.size(); ++i) {
            BitSet bs = new BitSet(g.size());
            g.neighborsOut(i).forEach(bs::set);
            for (int j = 0; j < g.size(); ++j) {
                out.print(bs.get(j) ? " 1" : " 0");
            }
            out.println();
        }
        out.println();
    }



    /**
     * Saves the given graph to
     * the given stream in Chaco format. We need to output the number of edges
     * so they have to be counted first which might not be very efficient.
     * Note that this format is designed for undirected graphs only.
     */
    public static void writeChaco(IntIndexedGraph g, PrintStream out) {

        if (g.directed()) System.err.println(
                "warning: you're saving a directed graph in Chaco format");

        int bound = g.size();
        long edges = IntStream.range(0, bound).mapToLong(i1 -> g.neighborsOut(i1).size()).sum();

        out.println(g.size() + " " + edges / 2);

        for (int i = 0; i < g.size(); ++i) {
            g.neighborsOut(i).forEach(o -> out.print((o + 1) + " "));
            out.println();
        }

        out.println();
    }


//
//    /**
//     * Read a graph in newscast graph format.
//     * The format depends on mode, the parameter.
//     * The file begins with the three byte latin 1 coded "NCG" string followed
//     * by the int MODE which is the
//     * given parameter. The formats are the following as a function of mode:
//     * <ul>
//     * <li> 1: Begins with cacheSize in binary format (int), followed by the
//     * numberOfNodes (int), and then a continuous series of exactly
//     * numberOfNodes records, where a record describes a node's
//     * neighbours and their timestamps.
//     * A record is a series of exactly cacheSize (int,long) pairs where
//     * the int is the node id, and the long is the timestamp.
//     * Node id-s start from 1. Node id 0 means no node and used if the parent
//     * node has less that cacheSize nodes.</li>
//     * </ul>
//     *
//     * @param file      Filename to read
//     * @param direction If 0, the original directionality is preserved, if 1,
//     *                  than each edge is reversed, if 2 then directionality is dropped and the
//     *                  returned graph will be undirected.
//     */
//    public static IntIndexedGraph readNewscastGraph(String file, int direction)
//            throws IOException {
//
//        AdjGraph gr = new AdjGraph(direction != 2);
//        FileInputStream fis = new FileInputStream(file);
//        DataInputStream dis = new DataInputStream(fis);
//
//        dis.readByte();
//        dis.readByte();
//        dis.readByte();
//
//        int MODE = dis.readInt();
//        if (MODE != 1) throw new IOException("Unknown mode " + MODE);
//
//        int CACHESIZE = dis.readInt();
//        int GRAPHSIZE = dis.readInt();
//
//
//
//        for (int i = 1; i <= GRAPHSIZE; ++i) {
//            int iind = gr.addNode(i);
//
//            for (int j = 0; j < CACHESIZE; ++j) {
//                int a = dis.readInt();
//                dis.readLong();
//
//                int agentIndex = gr.addNode(a);
//                if (direction == 0) gr.setEdge(iind, agentIndex);
//                else gr.setEdge(agentIndex, iind);
//            }
//        }
//
//        dis.close();
//
//        return gr;
//    }


}

