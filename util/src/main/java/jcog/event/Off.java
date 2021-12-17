package jcog.event;

import jcog.signal.meter.SafeAutoCloseable;

/** something that can be disabled. */
@FunctionalInterface public interface Off extends SafeAutoCloseable {

}