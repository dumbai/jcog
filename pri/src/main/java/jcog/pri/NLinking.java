package jcog.pri;

import org.eclipse.collections.api.block.function.primitive.FloatFunction;

import static jcog.Str.n4;

/** lightweight recycable non-threadsafe version of NLink */
public class NLinking<X> extends MutablePri {

	public X id;

	@Override
	public boolean equals(Object that) {
		return (this == that) || id.equals(
			(that instanceof NLinking) ? ((NLinking) that).id
				:
				that
		);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return n4(pri) + ' ' + id;
	}

	public float priElse(FloatFunction<X> update) {
		float p = pri;
		return p == p ? p : pri(update);
	}

	public float pri(FloatFunction<X> update) {
		return this.pri = update.floatValueOf(id);
	}

	public NLinking<X> set(X x, float pri) {
		this.id = x;
		pri(pri);
		return this;
	}
}