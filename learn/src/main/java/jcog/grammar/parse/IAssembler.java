package jcog.grammar.parse;

import java.util.function.Consumer;

/**
 * This is the one method all subclasses must implement. It specifies what
 * to do when a parser successfully matches against a assembly.
 *
 * @param Assembly
 *            the assembly to work on
 */
@FunctionalInterface  public interface IAssembler extends Consumer<Assembly> {

}