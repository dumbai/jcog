













package jcog.grammar.synthesize.util;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class OracleUtils {

    public interface Oracle extends UnaryOperator<String> {

    }

    @FunctionalInterface
    public interface DiscriminativeOracle extends Predicate<String> {
    }

    public interface Wrapper extends UnaryOperator<String> {

    }

    public static class IdentityWrapper implements Wrapper {
        @Override
        public String apply(String input) {
            return input;
        }
    }

    public static class WrappedOracle implements Oracle {
        private final Oracle oracle;
        private final Wrapper wrapper;

        public WrappedOracle(Oracle oracle, Wrapper wrapper) {
            this.oracle = oracle;
            this.wrapper = wrapper;
        }

        @Override
        public String apply(String query) {
            return this.oracle.apply(this.wrapper.apply(query));
        }
    }

    public static class WrappedDiscriminativeOracle implements DiscriminativeOracle {
        private final Predicate<String> oracle;
        private final Wrapper wrapper;

        public WrappedDiscriminativeOracle(Predicate<String> oracle, Wrapper wrapper) {
            this.oracle = oracle;
            this.wrapper = wrapper;
        }

        @Override
        public boolean test(String query) {
            return this.oracle.test(this.wrapper.apply(query));
        }
    }
}
