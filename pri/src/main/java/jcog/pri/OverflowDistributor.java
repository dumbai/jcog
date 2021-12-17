//package jcog.pri;
//
//import jcog.signal.MutableFloat;
//import jcog.data.list.FasterList;
//import jcog.pri.op.PriMerge;
//import jcog.pri.op.PriReturn;
//import org.eclipse.collections.api.block.procedure.primitive.ObjectFloatProcedure;
//import org.eclipse.collections.api.tuple.primitive.ObjectFloatPair;
//
//import java.util.Random;
//
//import static jcog.pri.ScalarValue.EPSILON;
//import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;
//
///** accumulates overflow priority from operations, and tracks opportunities how the overflow can
// * attempt (fair-ish) re-distribution after the first iteration.
//  */
//public class OverflowDistributor<X> extends MutableFloat {
//
//    final FasterList<ObjectFloatPair<X>> needs = new FasterList(0);
//    float totalHeadRoom = 0;
//
//    public final void add(X x, UnitPrioritizable a, float priToAdd) {
//        overflow(x, a.priAddOverflow(priToAdd), 1f - a.priElseZero());
//    }
//
//    public final void merge(X x, Prioritizable existing, float incoming, PriMerge merge) {
//        overflow(x, merge.merge(existing, incoming, PriReturn.Overflow), 1f - existing.priElseZero());
//    }
//
//    /** headroom = remaining demand that can be supplied */
//    public void overflow(X x, float overflow, float headroom) {
//        if (overflow > EPSILON && headroom > EPSILON) {
//            add(overflow);
//            needs.add(pair(x, headroom));
//            totalHeadRoom += headroom;
//        }
//    }
//
//    public OverflowDistributor<X> shuffle(Random r) {
//        needs.shuffleThis(r);
//        return this;
//    }
//
//    /** a final post-iteration. values are not updated for another */
//    public void redistribute(ObjectFloatProcedure<X> how) {
//
//
//        int s = needs.size();
//        if (s > 0) {
//            float pTotal = floatValue();
//            if (pTotal >= EPSILON * s) {
//
//                if (s == 1) {
//                    how.value(needs.getFirst().getOne(), pTotal);
//                } else {
//
//
//                    int remain = s;
//
//                    for (int i = 0; i < s; i++) {
//
//                        float p = floatValue();
//                        float maxAlloc = p / remain--;
//                        ObjectFloatPair<X> n = needs.get(i);
//
//                        float given = Math.min(maxAlloc, n.getTwo());
//                        if (given >= EPSILON) {
//                            how.value(n.getOne(), given);
//
//                            subtract(given);
//                        }
//                    }
//
//                }
//            }
//        }
//    }
//
//    public void clear() {
//        set(0);
//        needs.clear();
//        totalHeadRoom = 0;
//    }
//
//}