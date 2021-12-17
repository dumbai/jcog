package alice.tuprolog;

public interface MutableClauseIndex extends ClauseIndex {
	void add(String key, ClauseInfo d, boolean first);

	FamilyClausesList remove(String key);

	void clear();
}
