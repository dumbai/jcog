package jcog.event;

/** something that can be enabled, disabled, and re-enabled. */
public interface OffOn extends Off {
    void on();
}
