//package jcog.data.map;
//
//import org.eclipse.collections.impl.map.mutable.primitive.ObjectFloatHashMap;
//
///** adds some size management to avoid it growing too large. also avoids unnecessary
// * clear when already empty which superclass probably should be responsible for */
//public class SaneObjectFloatHashMap<X> extends ObjectFloatHashMap<X> {
//
//    private final int sizeThresh;
//
//    public SaneObjectFloatHashMap(int sizeThresh) {
//        super(sizeThresh);
//        this.sizeThresh = sizeThresh;
//    }
//
//    @Override
//    public void clear() {
//        int s = size();
//        if (s > 0) {
//            super.clear();
//            if (s > sizeThresh) {
//                compact();
//            }
//        }
//
//    }
//}
