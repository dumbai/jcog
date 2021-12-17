package jcog.sort;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface TopFilter<X> extends Consumer<X>, Iterable<X> {

    boolean add(X x);

    @Nullable X pop();

    boolean isEmpty();

    void clear();

    int size();

	@Nullable default X poll() {
	    if (isEmpty()) return null;
	    else return pop();
    }


	float minValueIfFull();

    default Iterable<X> apply(Iterable<X> values) {
        values.forEach(this);
        return this;
    }
}
