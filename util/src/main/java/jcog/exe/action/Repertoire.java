package jcog.exe.action;

/**
 * "Repertoire - the entire stock of skills, techniques, or devices used in
 * a particular field or occupation"
 *
 * set of invokable procedures (actions) that can be invoked by a controller
 *
 * the prototype implementation only supports a fixed setAt
 * they are indexed by number
 *
 * each action's mutable priority is modeled as a thread-local fuzzy bitmap (32-bit float
 * each).  this allows each thread to individually determine its execution
 * strategy
 *      ex: applying bitmap masks
 *
 * additionally a user object can be stored for each action for custom
 * instrumentation, etc.
 *
 * an executor uses the priority values to determine
 * qualities such as the delay, the repetition frequency,
 * the execution duration, intensity to apply, what to instrument before/during/after
 * execution, etc.
 *
 */
public class Repertoire {

}
