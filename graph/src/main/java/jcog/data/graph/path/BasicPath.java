/*
 * The MIT License
 *
 * Copyright 2014 Kamnev Georgiy (nt.gocha@gmail.com).
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
package jcog.data.graph.path;

import jcog.data.graph.MapNodeGraph;
import jcog.data.graph.Node;
import jcog.data.graph.edge.ImmutableDirectedEdge;
import jcog.data.list.Lst;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Базовый путь
 *
 * @param <N> Тип вершины
 * @param <E> Тип ребра
 * @author gocha
 */
public class BasicPath<N, E> extends AbstractPath<N, E> {
    /**
     * Описывает направление движения
     */
    protected Direction direction = Direction.AB;

    protected final List<FromTo<Node<N, E>, E>> list;
    private ObjectIntHashMap<N> countMap;
    final N start;

    public BasicPath(MapNodeGraph<N, E> graph, List<FromTo<Node<N, E>, E>> edges, @Deprecated N start) {
        super(graph);
        this.list = edges;
        this.start = start;
    }

    public BasicPath(MapNodeGraph<N, E> graph, N start) {
        super(graph);
        this.list = new Lst<>(0);
        this.start = start;
    }


    public BasicPath<N, E> clone() {
        BasicPath p = new BasicPath(graph, start);
        p.list.addAll(list);
        return p;
    }



    @Override
    public List<FromTo<Node<N, E>, E>> fetch(int from, int to) {
        int ncnt = nodeCount();

        if (from < 0) from = Math.max((ncnt + from), 0);

        if (to < 0) to = (ncnt + to) < 0 ? ncnt : ncnt + to;

        to = Math.min(list.size(), to);

        if (ncnt == 0 || ncnt == 1) return Collections.EMPTY_LIST; //empty

        int dir = to - from;

        List<FromTo<Node<N, E>, E>> l = new Lst<>(to - from);

        if (dir > 0) {
            for (int i = from; i < to; i++) {
                FromTo<Node<N, E>, E> e = list.get(i);
                if (e != null && e.id() != null)
                    l.add(e);
            }
        } else if (dir < 0) {
            for (int i = Math.min(from, to); i < Math.max(from, to); i++) {
                FromTo<Node<N, E>, E> e = list.get(i);
                if (e != null && e.id() != null) {
                    l.add(0, new ImmutableDirectedEdge<>(e.to(), e.id(), e.from()));
                }
            }
        }
        return l;
    }

    @Override
    public @Nullable E edge(int i) {
        return list.get(i).id();
    }

    protected ImmutableDirectedEdge edge(N f, E e, N t) {
        ImmutableDirectedEdge<N, E> z = new ImmutableDirectedEdge(graph.addNode(f), e, graph.addNode(t));
        graph.addEdge(z);
        return z;
    }


    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public BasicPath<N, E> clear() {
        return new BasicPath<>(graph, start);
    }

    @Override
    public int count(N n) {
        if (n == null) return 0;
        int s = nodeCount();
        long count = 0L;
        for (int ni = 0; ni < s; ni++) {
            if (n.equals(node(ni)))
                count++;
        }
        return (int) count;
    }

    private Set<N> nodeSet() {
        int nc = nodeCount();
        Set<N> nset = new UnifiedSet<>(nc);
        for (int ni = 0; ni < nc; ni++) {
            nset.add(node(ni));
        }
        return nset;
    }

    private ObjectIntHashMap<N> getCountMap() {
//        if (countMap != null) return countMap;
        //synchronized (this) {
            if (countMap != null) return countMap;

            Set<N> ns = nodeSet();
            countMap = new ObjectIntHashMap<>(ns.size());
            for (N n : ns)
                countMap.put(n, count(n));

            return countMap;
        //}
    }

    @Override
    public int nodeCount() {
        if (list == null) return 0;
        int lsize = list.size();
        if (lsize < 1)
            return 0;
        else if (lsize == 1) {
            FromTo<Node<N, E>, E> e = list.get(0);
            return ((e.id() != null) && (e.to().id() != null)) ? 2 : 1;
        } else
            return lsize + 1; //???
    }

    @Override
    public N node(int nodeIndex) {
        int ncnt = nodeCount();
        if (nodeIndex < 0) {
            return (ncnt + nodeIndex) < 0 ? null : node(ncnt + nodeIndex);
        }
//        if (nodeIndex >= nodeCount())
//            return null;
        //            switch (direction) {
        //                case AB:
        //                    return edge.to().id();
        //                case BA:
        //                default:
        //                    return edge.from().id();
        //            }
        return switch (nodeIndex) {
            case 0 -> Direction.AB.next(list.get(0));
            case 1 -> Direction.BA.next(list.get(0));
//??
            default -> Direction.BA.next(list.get(nodeIndex - 1));
        };
//        return null;
    }


}
