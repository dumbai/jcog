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


import jcog.Log;
import jcog.data.graph.MapNodeGraph;
import jcog.data.graph.Node;
import jcog.data.graph.PathVisitor;
import jcog.data.graph.edge.ImmutableDirectedEdge;
import jcog.data.graph.path.BasicPath;
import jcog.data.graph.path.FromTo;
import jcog.data.graph.path.Path;
import jcog.data.graph.search.Search;
import jcog.data.list.Lst;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.api.tuple.primitive.BooleanObjectPair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Граф конвертирования типов. <br>
 * В качестве вершин графа - Java тип <br>
 * <br>
 * В качестве ребра графа - функция преобразования типа. <br>
 * Ребро может быть взешенно (интерфейс GetWeight). <br>
 * <br>
 * При преобразованиях подбирается кратчайший путь пробразования (GetWeight). <br>
 * <br>
 * В процессе преобразования учитывается возможность
 * автоматического приведения типа (конструкция assignable from)
 * для начальной вершины в пути преобразования.
 *
 * @author Kamnev Georgiy (nt.gocha@gmail.com)
 */
public class CastGraph extends MapNodeGraph<Class<?>, Function<?,?>> {

    private static final Logger logger = Log.log(CastGraph.class);

//    static final int PATH_CAPACITY = 64;

//    private static final int findPathMinimum = 1;

    protected final ClassSet classes = new ClassSet();

    protected Function<FromTo<Node<Class, Function>, Function>, Double> edgeWeightFunction;

    /**
     * Конструктор по умолчанию
     */
    public CastGraph() {
        super();
    }


    //</editor-fold>

    /**
     * Создание конвертора ребро графа -&gt; вес
     *
     * @return конвертор ребр графа в их веса
     */
    public static Function<FromTo<Node<Class, Function>, Function>, Double> createEdgeWeight() {
        return
                from -> {
                    Object edge = from.id();
                    if (edge instanceof PrioritizedDouble)
                        return ((PrioritizedDouble) edge).weight();
                    return (double) 1;
                };
    }

//    /**
//     * Поиск возможных конверторов для типа
//     * @param type Тип
//     * @param strongCompare true - жесткое сравнение типов; false - использование конструкции instanceof в сравнении
//     * @param childToParent true - последовательность в порядке от дочерних классов, к родительскому классу <br>
//     * false - обратная последовательность: в порядке от родительского класса к дочерним
//     * @param incParent true - включать в поиск родитеслькие классы
//     * @param incChildren true - включать в поиск дочерние классы
//     * @return Возможные альтернативы преобразований
//     */
//    public Map<Class,UnaryOperator<Object>> getConvertorsFrom(
//        Class type,
//        boolean strongCompare,
//        boolean childToParent,
//        boolean incParent,
//        boolean incChildren
//    ){
//        Map<Class,UnaryOperator<Object>> convs
//            = new TreeMap<Class, Function>(
//                new ClassSet.ClassHeirarchyComparer( childToParent )
//            );
//
//        Iterable<Class> fromClasses = strongCompare ?
//            Iterators.single(type) :
//            classes.getAssignableFrom(type, incParent, incChildren);
//
//        for( Class cnode : fromClasses ){
//            for( Edge<Class,UnaryOperator<Object>> e : this.edgesOfNodeA(cnode) ){
//                UnaryOperator<Object> conv = e.getEdge();
//                Class target = e.getNodeB();
//                convs.put(target, conv);
//            }
//        }
//
//        return convs;
//    }

    @Override
    protected void onAdd(Node<Class<?>, Function<?,?>> r) {
        super.onAdd(r);
        Class type = r.id();
        if (type != null)
            classes.add(type);
    }

    @Override
    protected void onRemoved(Node<Class<?>, Function<?,?>> r) {
        Class type = r.id();
        if (type != null)
            classes.remove(type);
        super.onRemoved(r);
    }

    /**
     * Получение начального узла преобразований
     *
     * @param type          Искомый тип
     * @param strongCompare true - жесткое сравнение типов; false - использование конструкции instanceof в сравнении
     * @param childToParent true - последовательность в порядке от дочерних классов, к родительскому классу <br>
     *                      false - обратная последовательность: в порядке от родительского класса к дочерним
     * @param incParent     true - включать в поиск родитеслькие классы
     * @param incChildren   true - включать в поиск дочерние классы
     * @return Перечень классов удовлетворяющих критерию поиска
     */
    public List<Class> roots(Class type, boolean strongCompare, boolean childToParent, boolean incParent, boolean incChildren
    ) {
        Collection<Class> fromClasses = strongCompare ?
                List.of(type) :
                classes.getAssignableFrom(type, incParent, incChildren);
        if (fromClasses.size() <= 1) {
            return fromClasses instanceof List ? ((List)fromClasses) : List.copyOf(fromClasses);
        } else {
            Lst<Class> list = new Lst(fromClasses);
            list.sort(new ClassSet.ClassHierarchyComparer(childToParent));
            return list;
        }
    }

    /**
     * Получение конвертора ребра графа в его вес
     *
     * @return конвертор ребр графа в веса
     */
    public Function<FromTo<Node<Class, Function>, Function>, Double> getEdgeWeight() {
        if (edgeWeightFunction != null) return edgeWeightFunction;
        edgeWeightFunction = createEdgeWeight();
        return edgeWeightFunction;
    }

    public List<Path<Class, Function>> paths(Class fromType, Class targetType) {
        if (fromType == null) throw new IllegalArgumentException("fromType==null");
        if (targetType == null) throw new IllegalArgumentException("targetType==null");

        List<Class> starts = roots(fromType, false, true, true, false);
        if (starts == null || starts.isEmpty()) {
            throw new ClassCastException("can't cast " + fromType + " to " + targetType + ", can't find start class");
        }

        List<Path<Class, Function>> p = new Lst<>();

        bfs(starts, new Search<>() {
            @Override
            protected Iterable<FromTo<Node<Class<?>, Function<?,?>>, Function<?,?>>> search(Node<Class<?>, Function<?,?>> n, List<BooleanObjectPair<FromTo<Node<Class<?>, Function<?,?>>, Function<?,?>>>> path) {
                return n.edges(false, true);
            }

            @Override
            protected boolean go(List<BooleanObjectPair<FromTo<Node<Class<?>, Function<?,?>>, Function<?,?>>>> path, Node<Class<?>, Function<?,?>> next) {
                if (targetType.isAssignableFrom(pathEnd(path).id())) {
                    Node<Class<?>, Function<?,?>> ps = pathStart(path);
                    List<FromTo> pp = new Lst<>(path.size());
                    new PathVisitor<>(path) {
                        @Override protected void acceptEdge(Function<?,?> id, Node<Class<?>, Function<?,?>> from, Node<Class<?>, Function<?,?>> to) {
                            pp.add(new ImmutableDirectedEdge(from, id, to));
                        }
                    };
                    p.add(new BasicPath(CastGraph.this, pp, ps));
                }
                return true;
            }
        });

//        for (Class startCls : starts) {
//
//
//
//            findPath(
//                    startCls,
//                    targetType,
//                    // java 8
//                /*(pathFound) -> {
//                    variants.addAt(pathFound);
//                    if( variants.size()<findPathMinimum && findPathMinimum>=0 ){
//                        return false;
//                    }
//                    //if( findPathMinimum<2 )return true;
//                    return true;
//                } */
//                    pathFound -> {
//                        p.add(pathFound);
//                        //return variants.size() >= findPathMinimum || findPathMinimum < 0;//if( findPathMinimum<2 )return true;
//                        return true; //continue
//                    }
//            );
//
//            /*if( path!=null ){
//                lvariants.addAt(path);
//            }*/
//
//        }


        return p;
    }

    /**
     * Преборазования значения
     *
     * @param <TARGET>   Тип данных который хотим получить
     * @param value      Исходное значение
     * @param targetType Целевой тип
     * @return Преобразованное значение
     * @throws ClassCastException если невозможно преобразование
     */
    public <TARGET> TARGET cast(Object value, Class<TARGET> targetType) {
        if (value == null) throw new IllegalArgumentException("value==null");
        if (targetType == null) throw new IllegalArgumentException("targetType==null");
        Class c = value.getClass();
        if (c == targetType) return (TARGET) value;
        return (TARGET) cast(value, targetType, null, null);
    }

    /**
     * Преборазования значения
     *
     * @param value               Исходное значение
     * @param targetType          Целевой тип
     * @param castedConvertor     Convertor который удачно отработал
     * @param failedCastConvertor Convertor который не удачно отработал
     * @return Преобразованное значение
     * @throws ClassCastException если невозможно преобразование
     */
    public Object cast(
            Object value,
            Class targetType,
            Consumer<Function> castedConvertor,
            @Nullable Consumer<Pair<Function, Throwable>> failedCastConvertor
    ) {
        if (value == null) throw new IllegalArgumentException("value==null");
        if (targetType == null) throw new IllegalArgumentException("targetType==null");

        Class cv = value.getClass();

        List<Path<Class, Function>> lvariants = paths(cv, targetType);
//        lvariants.removeIf(p -> p.nodeCount() > 2);
//
//        Collection<Path<Class, Function>> removeSet = new LinkedHashSet<>();
//        for (Path<Class, Function> p : lvariants) {
//            if (p.nodeCount() < 2) {
//                removeSet.addAt(p);
//            }
//        }
//        lvariants.removeAll(removeSet);

        if (lvariants.isEmpty()) {
            throw new ClassCastException("can't cast " + cv + " to " + targetType
                    + ", no available casters"
            );
        }

        Collection<Throwable> castErrors = new Lst<>();
        Collection<Converter> scasters = new Lst<>();

        for (Path<Class, Function> path : lvariants) {
            //int psize = path.size();
            int ncount = path.nodeCount();
            //if( psize==1 ){
            if (ncount == 1) {
                Function conv = path.edge(0);
                if (conv!=null) {
                    try {
                        Object res = conv.apply(value);
                        if (castedConvertor != null) castedConvertor.accept(conv);
                        return res;
                    } catch (RuntimeException ex) {
                        fail(failedCastConvertor, castErrors, conv, ex);
                    }
                }
            } else {
                Converter c = Converter.the(path);
                scasters.add(c);
            }
        }

        for (Converter c : scasters) {
            try {
                Object res = c.apply(value);
                if (castedConvertor != null)
                    castedConvertor.accept(c);
                return res;
            } catch (RuntimeException ex) {
                fail(failedCastConvertor, castErrors, c, ex);
            }
        }

        int ci = -1;
        StringBuilder castErrMess = new StringBuilder();
        for (Throwable err : castErrors) {
            ci++;
            if (ci > 0) castErrMess.append('\n');
            castErrMess.append(err.getMessage());
        }

        throw new ClassCastException("can't cast " + cv + " to " + targetType
                + ", cast failed:\n" + castErrMess
        );
    }

    private static void fail(@Nullable Consumer<Pair<Function, Throwable>> failedCastConvertor, Collection<Throwable> castErrors, Function conv, Throwable ex) {

        logger.error(" {}", ex);

        castErrors.add(ex);
        if (failedCastConvertor != null)
            failedCastConvertor.accept(Tuples.pair(conv, ex));
    }

    public <X,Y> List<Function<X, Y>> applicable(Class<? extends X> cfrom, Class<? extends Y> cto) {

        List<Class> roots = roots(cfrom, false, true, true, false);
        if (roots.isEmpty())
            return Collections.EMPTY_LIST;

        List<Function<X, Y>> convertors = new Lst(roots.size());
        for( Class cf : roots ){
            List<Path<Class, Function>> paths = paths(cf, cto);
            if( paths != null ) {
                for (Path<Class, Function> c : paths) {
                    convertors.add(Converter.the(c));
                }
            }
        }

        return convertors;
    }
}