package jcog.pri;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Set;
import java.util.function.Consumer;

public class ParentLink<X extends Prioritizable> implements PriReference<X> {

    public final X id;
    private Set<X> sub;

    public ParentLink(X x) {
        //super(x, v);
        this.id = x;
    }

    public void sub(X x) {
        if (x.equals(id))
            throw new UnsupportedOperationException();

        if (sub == null) sub = new UnifiedSet();
        sub.add(x);
    }

    public void forEach(Consumer<? super X> each) {
        Set<X> s = this.sub;
        if (s != null)
            s.forEach(each);
    }

    @Override
    public void pri(float p) {
        id.pri(p);
    }

    @Override
    public boolean delete() {
        if (id.delete()) {
            //sub.clear();
            sub = null;
            return true;
        }
        return false;
    }

    public Set<X> subs() {
        Set<X> s = sub;
        if (s == null) return Set.of(); else return s;
    }

    @Override
    public X get() {
        return id;
    }

    @Override
    public float pri() {
        return id.pri();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((ParentLink<?>) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}