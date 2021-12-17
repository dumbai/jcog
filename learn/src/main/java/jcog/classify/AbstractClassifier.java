package jcog.classify;

import org.roaringbitmap.RoaringBitmap;

public abstract class AbstractClassifier<X> {

    public final Object name;

    public AbstractClassifier(Object name) {
        this.name = name;
    }

    /** dimensionality of this, ie. how many bits it requires */
    public abstract int dimension();

    /** sets the applicable bits between offset and offset+dimensoinality (exclusive) */
    public abstract void classify(X x, RoaringBitmap bmp, int offset);

    public String name(int i) {
        return dimension()==1 ? name.toString() : name.toString() + i; 
    }
}
