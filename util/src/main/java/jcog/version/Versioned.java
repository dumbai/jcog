package jcog.version;

/** LIFO queue of >=1 data values
 *  representing a history of changing value assignments */
public interface Versioned<X> {
    X get();

    void pop();

    default X getAndPop() {
        X x = get();
        if (x!=null)
            pop();
        return x;
    }


//
//
//    /**
//     * replaces the existing value (or invokes ordinary set(x) if none),
//     * an implementation can refuse the new value, indicated by return value
//     */
//    boolean replace(X y, Versioning<X> context);

}
