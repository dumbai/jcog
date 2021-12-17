/*
 * The MIT License
 *
 * Copyright 2015 Kamnev Georgiy (nt.gocha@gmail.com).
 *
 * Данная лицензия разрешает, безвозмездно, лицам, получившим копию данного программного
 * обеспечения и сопутствующей документации (в дальнейшем именуемыми "Программное Обеспечение"),
 * использовать Программное Обеспечение без ограничений, включая неограниченное право на
 * использование, копирование, изменение, объединение, публикацию, распространение, сублицензирование
 * и/или продажу копий Программного Обеспечения, также как и лицам, которым предоставляется
 * данное Программное Обеспечение, при соблюдении следующих условий:
 *
 * Вышеупомянутый копирайт и данные условия должны быть включены во все копии
 * или значимые части данного Программного Обеспечения.
 *
 * ДАННОЕ ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ ПРЕДОСТАВЛЯЕТСЯ «КАК ЕСТЬ», БЕЗ ЛЮБОГО ВИДА ГАРАНТИЙ,
 * ЯВНО ВЫРАЖЕННЫХ ИЛИ ПОДРАЗУМЕВАЕМЫХ, ВКЛЮЧАЯ, НО НЕ ОГРАНИЧИВАЯСЬ ГАРАНТИЯМИ ТОВАРНОЙ ПРИГОДНОСТИ,
 * СООТВЕТСТВИЯ ПО ЕГО КОНКРЕТНОМУ НАЗНАЧЕНИЮ И НЕНАРУШЕНИЯ ПРАВ. НИ В КАКОМ СЛУЧАЕ АВТОРЫ
 * ИЛИ ПРАВООБЛАДАТЕЛИ НЕ НЕСУТ ОТВЕТСТВЕННОСТИ ПО ИСКАМ О ВОЗМЕЩЕНИИ УЩЕРБА, УБЫТКОВ
 * ИЛИ ДРУГИХ ТРЕБОВАНИЙ ПО ДЕЙСТВУЮЩИМ КОНТРАКТАМ, ДЕЛИКТАМ ИЛИ ИНОМУ, ВОЗНИКШИМ ИЗ, ИМЕЮЩИМ
 * ПРИЧИНОЙ ИЛИ СВЯЗАННЫМ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ ИЛИ ИСПОЛЬЗОВАНИЕМ ПРОГРАММНОГО ОБЕСПЕЧЕНИЯ
 * ИЛИ ИНЫМИ ДЕЙСТВИЯМИ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ.
 */
package jcog.reflect;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Kamnev Georgiy (nt.gocha@gmail.com)
 */
class TypeCastTest {


    @Test
    void testString2byteNewAPI(){
        //BaseCastGraph bcast = new BaseCastGraph();
        ExtendedCastGraph t = new ExtendedCastGraph();

        List<Function<String, Byte>> convertors = t.applicable(String.class, byte.class);
        for (Function<String, Byte> convertor : convertors) {
            System.out.println(convertor);
        }

        assertEquals((byte)9, (Object)convertors.get(0).apply("9"));
    }

//    @Test
//    public void test1(){
//        System.out.println("test1");
//        System.out.println("=====");
//
//        TypeCastGraph tcast = new TypeCastGraph();
//
//        tcast.setAt(Integer.class, int.class, new Convertor<Object, Object>() {
//            @Override
//            public Object convert(Object from) {
//                return (int)((Integer)from);
//            }
//        });
//
//        tcast.setAt(int.class, Integer.class, new Convertor<Object, Object>() {
//            @Override
//            public Object convert(Object from) {
//                return (Integer)from;
//            }
//        });
//
//        tcast.setAt(Integer.class, String.class, new Convertor<Object, Object>() {
//            @Override
//            public Object convert(Object from) {
//                return ((Integer)from).toString();
//            }
//        });
//
//        Object v = 100;
//
//        Map<Class,Convertor> m = tcast.getConvertorsFrom(v.getClass(), false, true, true, true);
//        for( Class tc : m.keySet() ){
//            System.out.println(""+tc);
//        }
//    }

    interface Interface1 {
    }

    interface Interface2 {
    }

    private static class Intf1Impl implements Interface1 {
    }

//    @Test
//    public void test2(){
//        System.out.println("test2");
//        System.out.println("=====");
//
//        TypeCastGraph tcast = new TypeCastGraph();
//
//        tcast.setAt(Interface1.class, Interface2.class, new Convertor<Object, Object>() {
//            @Override
//            public Object convert(Object from) {
//                return null;
//            }
//        });
//
//        Class clsFrom = Intf1Impl.class;
//
//        Map<Class,Convertor> m = tcast.getConvertorsFrom(clsFrom, false, true, true, false);
//
//        for( Class tc : m.keySet() ){
//            System.out.println(""+tc);
//        }
//
//        int co = 0;
//        for( Class c : tcast.getNodes() ){
//            if( c.equals(clsFrom) )co++;
//        }
//
//        assertTrue( co==0 );
//        assertTrue( m.keySet().size()>0 );
//    }

    @Test
    void testFindStart(){
        System.out.println("testFindStart");
        System.out.println("=============");

        CastGraph tcast = new CastGraph();

        tcast.addEdge(Interface1.class, from -> null, Interface2.class);

        Class clsFrom = Intf1Impl.class;

        List<Class> m = tcast.roots(clsFrom, false, true, true, false);

        for( Class tc : m ){
            System.out.println(""+tc);
        }

        int co = 0;
        for( Class c : tcast.nodeIDs() ) {
            if(c == clsFrom)
                co++;
        }

        assertEquals(0, co);
        assertTrue( m.size()>0 );
    }
//
//    @Test
//    void testString2byte(){
//        //BaseCastGraph bcast = new BaseCastGraph();
//        ExtendedCastGraph bcast = new ExtendedCastGraph();
//
//        Class cfrom = String.class;
//        Class cto = byte.class;
//
//        System.out.println("from="+cfrom+" to="+cto);
//
//        List<Class> roots = bcast.roots(cfrom, false, true, true, false);
//        assert(!roots.isEmpty());
//
//            System.out.println("convert variants:");
//            int i = -1;
//            for( Class cf : roots ){
//                if (cf == null) throw new IllegalArgumentException("from==null");
//                if (cto == null) throw new IllegalArgumentException("to==null");
//                Path<Class,Function>[] y1 = new Path[1];
//                /* just the first one */
//                //        if (from == null) throw new IllegalArgumentException("from==null");
////        if (to == null) throw new IllegalArgumentException("to==null");
//
//                //        pfinder = new PathFinder<>(
////            this, from, Path.Direction.AB, (Edge<Class, Function> from1) -> {
////                Object edge = from1.getEdge();
////                if( edge instanceof GetWeight )
////                    return ((GetWeight)edge).getWeight();
////                return (double)1;
////            });
//
//                PathFinder<Class, Function> pfinder = new PathFinder(
//                        CastGraph.PATH_CAPACITY,
//                        bcast,
//                        cf,
//                        Path.Direction.AB,
//                        bcast.getEdgeWeight()
//                );
//
//                while (pfinder.hasNext()) {
//                    Path<Class, Function> path1 = pfinder.next();
//                    if (path1 == null) break;
//                    Class lastnode = path1.node(-1);
//                    //assert(!lastnode.equals(to));
//                    if (lastnode != null && lastnode == cto) {
//                        y1[0] = path1;
//                        break;
//                    }
//                }
//                //continue
//                Path<Class, Function> path = y1[0];
//                if( path!=null ){
//                    i++;
//                    Converter sc = Converter.the(path);
//                    System.out.println(i+": "+ sc);
//
//                    Object y = sc.apply("9");
//                    assertEquals((byte)9,y);
//                }
//            }
//
//    }


    @Test
    void testString2byteCast(){
        assertEquals(
            new ExtendedCastGraph().cast("12", byte.class),
            (byte) 12);

    }
}
