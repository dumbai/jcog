package jcog.signal.meter;

/** does not throw exception */
@FunctionalInterface public interface SafeAutoCloseable extends AutoCloseable {
    void close();
}
