package jcog.decisiontree; /**
 *
 */

import java.util.HashMap;
import java.util.Map;


public class Decisions {
  public final Map<String, Attribute> decisions = new HashMap<>();


  public void put(String decision, Attribute attribute) {
    decisions.put(decision, attribute);
  }

  public void clear() {
    decisions.clear();
  }

  /**
   * Returns the attribute based on the decision matching the provided value.
   *
   * Throws BadDecisionException if no decision matches.
   */
  public Attribute apply(String value) throws BadDecisionException {
    Attribute result = decisions.get(value);

    if ( result == null )
      throw new BadDecisionException();

    return result;
  }
}