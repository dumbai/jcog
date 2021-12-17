package alice.tuprolog;

import org.jetbrains.annotations.Nullable;

import java.util.Deque;


public interface ClauseIndex extends Iterable<ClauseInfo> {

	boolean containsKey(String key);
    FamilyClausesList clauses(String key);


	/**
	 * Retrieves a list of the predicates which has the same name and arity
	 * as the goal and which has a compatible first-arg for matching.
	 *
	 * @param headt The goal
	 * @return  The list of matching-compatible predicates
	 */
	default @Nullable Deque<ClauseInfo> predicates(Struct headt) {
		FamilyClausesList family = clauses(headt.key());
		return family == null ? null : family.get(headt);
	}


}
