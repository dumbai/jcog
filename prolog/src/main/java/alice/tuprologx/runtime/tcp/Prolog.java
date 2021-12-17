package alice.tuprologx.runtime.tcp;

import alice.tuprolog.Solution;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;

public interface Prolog {

    void clearTheory() throws Exception;
    Theory getTheory() throws Exception;
    /**
	 * @param theory
	 * @throws Exception
	 */
    void setTheory(Theory theory) throws Exception;
    void addTheory(Theory theory) throws Exception;

    Solution solve(String g) throws Exception;
    Solution solve(Term th) throws Exception;
    Solution solveNext() throws Exception;
    boolean     hasOpenAlternatives() throws Exception;
    void solveHalt() throws Exception;
    void solveEnd() throws Exception;

    void loadLibrary(String className) throws Exception;
    void unloadLibrary(String className) throws Exception;
}
