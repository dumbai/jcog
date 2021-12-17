package jcog.decision.data;

import com.google.common.base.Preconditions;
import jcog.data.map.UnifriedMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Ignas
 */
public final class SimpleValue<L> implements Function<String, L> {

	private final Map<String, L> values;

	private SimpleValue(String[] header, L[] dataValues) {
		super();
        UnifriedMap<String,L> v = new UnifriedMap<>(header.length);
		for (int i = 0; i < header.length; i++)
			v.put(header[i], dataValues[i]);
		v.trimToSize();
		values = v;
	}

	/**
	 * Create data sample without labels which is used on trained tree.
	 */
	@SafeVarargs
	public static <L> SimpleValue<L> classification(String[] header, L... values) {
		Preconditions.checkArgument(header.length == values.length);
		return new SimpleValue<>(header, values);
	}

	/**
	 * @param header
	 * @param values
	 * @return
	 */
	@SafeVarargs
	public static <L> SimpleValue<L> data(String[] header, L... values) {
		Preconditions.checkArgument(header.length == values.length);
		return new SimpleValue<>(header, values);
	}

	@Override
	public final @Nullable L apply(String column) {
		return values.get(column);
	}

	@Override
	public String toString() {
		return "[values=" + values + ']';
	}

}
