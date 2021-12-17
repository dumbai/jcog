package jcog.data.pool;

/**
 * Created by me on 6/8/15.
 */
public interface Pool<X> {

    void put(X i);

    X get();

    X create();

    void delete();
}
