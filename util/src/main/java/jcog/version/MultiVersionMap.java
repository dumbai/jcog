//package jcog.version;
//
//public class MultiVersionMap<X,Y> extends VersionMap<X,Y> {
//    final int maxValuesPerItem;
//
//    public MultiVersionMap(Versioning<Y> context, int maxValuesPerItem) {
//        super(context);
//        this.maxValuesPerItem = maxValuesPerItem;
//    }
//
//    @Override
//    public int size() {
//        int cs = context.size;
//        if (cs == 0)
//            return 0;
//        int ms = map.size();
//        if (cs == ms)
//            return cs; //no multi-version
//
//        int count = 0;
//        for (Versioned<Y> yVersioned : map.values()) {
//            Y y = yVersioned.get();
//            if (y != null)
//                count++;
//        }
//        return count;
//    }
//
//    @Override
//    public Versioned<Y> apply(X x) {
//        return new KeyMultiVersioned<>(x, maxValuesPerItem);
//    }
//}
