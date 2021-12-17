//package jcog.decide;
//
//
//import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;
//
//import java.util.Random;
//
///** metaheuristic planner: staged and buffered exploration / exploitation hierarchical executor
// *
// *  explore -> buffer -> rank -> exploit ->
// *                                          explore -> buffer -> rank -> exploit -> ...
// *
// *  TODO
// * */
//public class Possibilities extends MutableRoulette {
//    /**
//     * with no weight modification
//     *
//     * @param count
//     * @param initialWeights
//     * @param rng
//     */
//    public Possibilities(int count, IntToFloatFunction initialWeights, Random rng) {
//        super(count, initialWeights, rng);
//    }
//
//
////    public boolean commit(boolean requireAll, boolean sort) {
////        boolean removed = removeIf(p -> {
////            float pp = p.value();
////            p._value = pp;
////            if (pp!=pp)
////                return true;
////            return false;
////        });
////        if (removed && requireAll) {
////            return false;
////        }
////
////        if(sort && list.size() > 1)
////            list.sortThisByFloat(p->-p._value);
////
////        return true;
////    }
//
////    public void execute(BooleanSupplier kontinue, float exploration, Consumer<Y> each) {
////        ListIterator<Possibility<X,Y>> p = listIterator();
////        while (kontinue.getAsBoolean() && p.hasNext()) {
////            Object q = p.next().apply(ctx);
////            p.remove();
////            if (q == null) {
////                //done
////            } else {
////                if (q instanceof Possibility) {
////                    p.add((Possibility)q);
////                    p.next();
////                } else
////                    each.accept((Y)q);
////            }
////        }
////    }
//
//
//}
