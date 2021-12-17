//package jcog.data.list;
//
//import org.eclipse.collections.api.block.predicate.Predicate2;
//
//import java.util.AbstractList;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//import java.util.stream.IntStream;
//
//public abstract class LazyList<X> extends AbstractList<X> {
//
//    private final FasterList<Object> list;
//
//    public LazyList(int capacity) {
//        this.list = new FasterList<>(capacity);
//    }
//
//    public LazyList(Object... x) {
//        this.list = new FasterList(x.length, x);
//    }
//
//    @Override
//    public void forEach(Consumer<? super X> action) {
//        int s = size();
//        for (int i = 0; i < s; i++)
//            action.accept(get(i));
//    }
//
//    public boolean add(X x) {
//        return list.add(x);
//    }
//
//    public boolean add(Supplier<X> t) {
//        return list.add(t);
//    }
//
//    public void activateAll() {
//        int s = size();
//        for (int i = 0; i < s; i++) {
//            get(i,true);
//        }
//    }
//
//    public boolean allSatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super X> predicate) {
//        //return InternalArrayIterate.allSatisfy(this.items, this.size, predicate);
//        int s = size();
//        return IntStream.range(0, s).allMatch(i -> predicate.test(get(i)));
//    }
//    public <P> boolean allSatisfyWith(Predicate2<? super X, ? super P> predicate2, P parameter) {
//        int s = size();
//        if (s > 0) {
//            return IntStream.range(0, s).allMatch(i -> predicate2.accept(get(i), parameter));
//        }
//        return true;
//    }
//
////    public int count(Predicate<? super X> predicate) {
////        int s = 0;
////        int c = 0;
////        for (int i = 0; i < s; i++)
////            c += predicate.test(get(i)) ? 1 : 0;
////        return c;
////    }
//
////    public int sumOfInt(ToIntFunction<? super X> predicate) {
////        int s = 0;
////        int c = 0;
////        for (int i = 0; i < s; i++)
////            c += predicate.applyAsInt(get(i));
////        return c;
////    }
//
//
//    @Override
//    public final X get(int i) {
//        return get(i, false);
//    }
//
//    public X get(int i, boolean activate) {
//        Object x = list.get(i);
//        if (x instanceof Supplier) {
//            if (!activate)
//                return inactive();
//            else {
//                Object y = ((Supplier) x).get();
//                list.setFast(i, y);
//                x = y;
//            }
//        }
//        return (X) x;
//    }
//
//    /** default value supplied for an inactive element */
//    protected abstract X inactive();
//
//    @Override
//    public int size() {
//        return list.size();
//    }
//}
