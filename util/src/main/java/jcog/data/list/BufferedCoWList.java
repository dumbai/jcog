//package jcog.data.list;
//
//import jcog.TODO;
//import org.eclipse.collections.api.block.procedure.Procedure;
//import org.eclipse.collections.api.tuple.primitive.ObjectBooleanPair;
//import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
//
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.function.Consumer;
//import java.util.function.IntFunction;
//import java.util.function.Predicate;
//
//public class BufferedCoWList<X> extends FastCoWList<X> {
//
//    private final ConcurrentLinkedQueue<ObjectBooleanPair<X>> q;
//
//    public BufferedCoWList(int initialCap, IntFunction<X[]> arrayBuilder) {
//        super(initialCap, arrayBuilder);
//        q = new ConcurrentLinkedQueue<>();
//    }
//
//    @Override
//    public boolean addAt(X o) {
//        q.addAt(PrimitiveTuples.pair(o, true));
//        return true;
//    }
//
//    @Override
//    public boolean remove(Object o) {
//        q.addAt(PrimitiveTuples.pair((X)o, false));
//        return true;
//    }
//
//    @Override
//    public boolean addAll(Collection<? extends X> source) {
//        throw new TODO();
//    }
//
//
//
//
//
//
//
//    @Override
//    public void clear() {
//        synchronized (this) {
//            super.clear();
//            q.clear();
//        }
//    }
//
//    @Override
//    public void forEach(Consumer c) {
//        commit();
//        super.forEach(c);
//    }
//
//    @Override
//    public Iterator<X> iterator() {
//        commit();
//        return super.iterator();
//    }
//
//    @Override
//    public void reverseForEach(Procedure procedure) {
//        commit();
//        super.reverseForEach(procedure);
//    }
//
//    public void commit() {
//        if (q.isEmpty())
//            return;
//        synchronized (this) {
//           q.removeIf(x -> {
//              if (x.getTwo()) {
//                  addDirect(x.getOne());
//              } else {
//                  removeDirect(x.getOne());
//              }
//              return true;
//           });
//           super.commit();
//        }
//    }
//
//    @Override
//    public boolean whileEach(Predicate<X> o) {
//        commit();
//        return super.whileEach(o);
//    }
//}
