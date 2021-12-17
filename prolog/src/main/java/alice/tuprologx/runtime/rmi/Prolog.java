package alice.tuprologx.runtime.rmi;
import alice.tuprolog.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author  ale
 */
public interface Prolog extends Remote {


    void clearTheory() throws RemoteException;

   Theory getTheory() throws RemoteException;

    /**
	 * @param theory
	 * @throws InvalidTheoryException
	 * @throws RemoteException
	 */
    void setTheory(Theory theory) throws InvalidTheoryException, RemoteException;

    void addTheory(Theory theory) throws InvalidTheoryException, RemoteException;


    Solution solve(Term g) throws RemoteException;

    Solution solve(String g) throws MalformedGoalException, RemoteException;

    boolean   hasOpenAlternatives() throws RemoteException;

//    Solution solveNext() throws NoMoreSolutionException, java.rmi.RemoteException;

    void solveHalt() throws RemoteException;

    void solveEnd() throws RemoteException;


    void loadLibrary(String className) throws InvalidLibraryException, RemoteException;

    void unloadLibrary(String className) throws InvalidLibraryException, RemoteException;

}
