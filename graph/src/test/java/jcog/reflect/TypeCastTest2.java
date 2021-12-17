/*
 * The MIT License
 *
 * Copyright 2017 Kamnev Georgiy <nt.gocha@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jcog.reflect;

import jcog.data.graph.Node;
import jcog.data.graph.path.FromTo;
import jcog.data.graph.path.Path;
import jcog.data.list.Lst;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author user
 */
public class TypeCastTest2 {
    


    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test
    public void test01(){
        ExtendedCastGraph tc = new ExtendedCastGraph();
        
        List<Path<Class, Function>> lp1 = tc.paths(String.class, BigDecimal.class);
        System.out.println(String.class + " to " + BigDecimal.class);
        for( Path<Class,Function> p : lp1 ){
            for( FromTo<Node<Class, Function>, Function> e : p.fetch(0, p.nodeCount()) ){
                System.out.print("\t"+ e.id());
            }
            System.out.println();
        }
        
        List<Path<Class,Function>> lp2 = tc.paths(BigDecimal.class, int.class);
        System.out.println(BigDecimal.class + " to " + int.class);
        for( Path<Class,Function> p : lp2 ){
            //p.forEach( e -> {
            for( FromTo<Node<Class, Function>, Function> e : p.fetch(0, p.nodeCount()) ){
                System.out.print("\t"+ e.id());
            }
            System.out.println();
        }
        
        // lp1.forEach( p1 -> {
        for( Path<Class,Function> p1 : lp1 ){
            // lp2.forEach( p2 -> {
            for( Path<Class,Function> p2 : lp2 ){
                int p1n = p1.nodeCount();
                int p2n = p2.nodeCount();
                Lst<Function> path = new Lst<>(p1n + p2n);
                //p1.forEach( e1 -> { path.addAt(e1.getEdge()); } );
                for( FromTo<Node<Class, Function>, Function> e1 : p1.fetch(0, p1n) ){
                    path.addWithoutResize(e1.id());
                }
                //p2.forEach( e2 -> { path.addAt(e2.getEdge()); } );
                for( FromTo<Node<Class, Function>, Function> e2 : p2.fetch(0, p2n) ){
                    path.addWithoutResize(e2.id());
                }
                
                Converter sc = new Converter( path );
                sc.setWeight(0.8);

                tc.addEdge(String.class, sc, int.class);
            }
        }

//        // SequenceCaster sc = new SequenceCaster
//        int v0 = tc.cast("1.0", int.class);
//        float v = tc.cast("1.0", float.class);
//        assertEquals(1f, v);
//        System.out.println("v="+v);
    }
}
