package jcog.version;

/** supports only one state and refuses change if a value is held */
public class UniVersioned<X> implements Versioned<X> {

    protected X value;

    UniVersioned() {
    }

    @Override
    public final X get() {
        return value;
    }

//    @Override
//    public final boolean replace(X x, Versioning<X> context) {
//        return set(x, context);
//    }

    @Override
    public final String toString() {
        X v = get();
        if (v != null)
            return v.toString();
        return "null";
    }

    @Override
    public final boolean equals(Object obj) {
        return this==obj;
    }


    /** override to filter */
    protected boolean valid(X x, Versioning<X> context) {
        return true;
    }


//    @Override
//    protected int merge(Term prevValue, Term nextValue) {
//        if (prevValue.equals(nextValue))
//            return 0;
//
////            if (prevValue.unify(nextValue, (Unify) context)) {
////                if (nextValue.TEMPORALABLE()) {
////                    //prefer more specific temporal matches, etc?
////                    if (prevValue.hasXternal() && !nextValue.hasXternal()) {
////                        return +1;
////                    }
////                }
////                return 0;
////            } else
//        else
//            return -1;
//    }


    @Override
    public void pop() {
        value = null;
    }

}
