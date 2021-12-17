package jcog.decision.feature;

import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EnumFeature extends DiscreteFeature<String> implements Function<Function,Object> {

	final ObjectIntHashMap<String> values = new ObjectIntHashMap<>();

	public EnumFeature(int c, String name) {
		super(c, name);
	}

	@Override
	public void learn(String s) {
		values.getIfAbsentPut(s, values::size);
	}


	@Override
	public Stream<Function<Function, Object>> classifiers() {
		return Stream.of(this);
	}

	@Override
	public Object apply(Function function) {
		return function.apply(id); //assert: in values
	}

	@Deprecated public class StringMatch implements Predicate<Function<Integer, String>> {
		public final String x;

		public StringMatch(String x) {
			this.x = x;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;

			StringMatch that = (StringMatch) o;
			return Objects.equals(x, that.x) && Objects.equals(name, that.name());
		}

		public String name() { return name; }

		@Override
		public int hashCode() {
			return x != null ? x.hashCode() : 0;
		}

		@Override
		public boolean test(Function<Integer, String> f) {
			return x.equals(f.apply(id));
		}

		@Override
		public String toString() {
			return name + '=' + x;
		}

		public String condition(boolean isTrue) {
			return name + (isTrue ? "==" : "!=") + x;
		}
	}
}
