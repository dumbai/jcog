package jcog.search.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ClosedSet<X> implements IClosedSet<X> {
    private final ArrayList<X> list;
    private final Comparator<X> comp;

    public ClosedSet(Comparator<X> comp) {
        this.list = new ArrayList<>();
        this.comp = comp;
    }

    @Override
    public boolean contains(X node) {
        return this.list.contains(node);
    }

    @Override
    public void add(X node) {
        this.list.add(node);

    }

    @Override
    public X min() {
        return Collections.min(this.list, this.comp);
    }

}
