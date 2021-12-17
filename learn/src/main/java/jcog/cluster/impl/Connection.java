package jcog.cluster.impl;


import jcog.Util;
import jcog.data.Centroid;


public class Connection<N extends Centroid>  {
    public final N from;
    public final N to;
    private final int hash;
    private int age;

    public Connection(N from, N to) {
        
        if (from.id > to.id) {
            N t = to;
            to = from;
            from = t;
        }

        this.age = 0;
        this.from = from;
        this.to = to;
        this.hash = Util.hashCombine(from.id, to.id);

    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) return true;
        Connection c = (Connection)obj;
        return (from.id == c.from.id && to.id == c.to.id);
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    public final int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public final void age() {
        age += 1;
    }

    @Override
    public String toString() {
        return from.id + ":" + to.id;
    }
}
