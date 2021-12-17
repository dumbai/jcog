package jcog.data.graph;

import jcog.data.list.Lst;
import jcog.reflect.access.Accessor;
import jcog.reflect.access.ArrayAccessor;
import jcog.reflect.access.FieldAccessor;
import org.eclipse.collections.api.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.IdentityHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.eclipse.collections.impl.tuple.Tuples.pair;

/**
 * see: https:
 */
public abstract class ObjectGraph extends MapNodeGraph<Object, Accessor /* TODO specific types of edges */> {

    private static final Logger logger = LoggerFactory.getLogger(ObjectGraph.class);

    public ObjectGraph(int depth, Object... seeds) {
        this();
        add(depth, seeds);
    }

    public ObjectGraph() {
        super(new IdentityHashMap<>());
    }

    private static boolean includeNull() {
        return false;
    }

    /**
     * creates a field setter from a path
     */
    public static <X, V> BiConsumer<X, V> setter(Lst<Pair<Class, Accessor>> path) {
        return (root, val) -> accessor(path).set(resolve(path, root), val);
    }

    /**
     * creates a field getter from a path
     */
    public static <X, Y> Function<X, Y> getter(Lst<Pair<Class, Accessor>> path) {
        return root -> (Y) accessor(path).get(resolve(path, root));
    }

    private static Accessor accessor(Lst<Pair<Class, Accessor>> path) {
        return path.getLast().getTwo();
    }

    /**
     * resolves / de-references a path
     */
    private static <X> Object resolve(Lst<Pair<Class, Accessor>> path, X root) {
        Object x = root;
        for (int i = 0, n = path.size() - 1; i < n; i++)
            x = path.get(i).getTwo().get(x);
        return x;
    }

    public ObjectGraph add(int depth, Object... xx) {
        for (Object x : xx)
            add(x, depth);
        return this;
    }

    private Node<Object, Accessor> add(Object x, int depth) {
        return add(x, x, new Lst<>(), depth);
    }

    private MutableNode<Object, Accessor> add(Object root, Object x, Lst<Pair<Class, Accessor>> path, int level) {

        boolean meta = x instanceof Class;

        MutableNode<Object, Accessor> n = addNode(x);

        if (level == 0 || !recurse(x))
            return n;

        Class<?> clazz = meta ? (Class)x : x.getClass();

        if (!meta && clazz.isArray()) {


            if (includeClass(clazz.getComponentType())) {
                int len = Array.getLength(x);

                for (int i = 0; i < len; i++) {
                    Object aa = Array.get(x, i);
                    if ((aa != null || includeNull()) && includeValue(aa))
                        access(root, n, clazz, aa, new ArrayAccessor(clazz, i), path, level);
                }
            }

        } else {

            fields(clazz).forEach(field -> {

                if (!includeField(field)) return;

                Class<?> fieldType = field.getType();


                if (!includeClass(fieldType)) return;

                try {
                    //field.setAccessible(true);
                    field.trySetAccessible();

                    if (!meta) {
                        try {
                            Object value = field.get(x);
                            if ((value != null || includeNull()) && includeValue(value))
                                access(root, n, clazz, value, new FieldAccessor(field), path, level);
                        } catch (InaccessibleObjectException ioe) {
                            logger.debug("inaccessible: {} {}", field, ioe);
                        }
                    } else {
                        access(root, n, clazz, field.getType(), new FieldAccessor(field), path, level);
                    }
                } catch (IllegalAccessException e) {

                    logger.info("field access {}", e);
                }
            });
        }

        return n;
    }

    private void access(Object root, MutableNode<Object, Accessor> src, Class<?> srcClass, Object target, Accessor axe, Lst<Pair<Class, Accessor>> path, int level) {
        path.add(pair(srcClass, axe));

        if (access(root, path, target))
            addEdgeByNode(src, axe, add(root, target, path, level - 1));

        path.removeLastFast();
    }

    protected boolean access(Object root, Lst<Pair<Class, Accessor>> path, Object target) {
        return true;
    }

    /**
     * whether to recurse into a value, after having added it as a node
     */
    public boolean recurse(Object x) {
        return true;
    }

    public abstract boolean includeValue(Object v);

    public abstract boolean includeClass(Class<?> c);

    public abstract boolean includeField(Field f);

    /**
     * Return all declared and inherited fields for this class.
     * TODO cache
     */
    private Stream<Field> fields(Class<?> clazz) {

        Stream<Field> s = Stream.of(clazz.getDeclaredFields());

        Class<?> sc = clazz.getSuperclass();
        if (sc != null && includeClass(sc)) {
            s = Stream.concat(s, fields(sc));
        }

        return s;
    }
}