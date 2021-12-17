package alice.tuprologx.runtime.rmi;

import alice.tuprolog.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@SuppressWarnings("serial")
public class PrologImpl extends UnicastRemoteObject
    implements Prolog {

   private alice.tuprolog.Prolog imp;

    public PrologImpl() throws RemoteException {
        try {
            imp=new alice.tuprolog.Prolog();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void clearTheory() {
        imp.clearTheory();
    }

    @Override
    public Theory getTheory() {
        return imp.getTheory();
    }

    @Override
    public void setTheory(Theory theory) throws InvalidTheoryException {
        imp.setTheory(theory);
    }

    @Override
    public void addTheory(Theory theory) throws InvalidTheoryException {
        imp.input(theory);
    }


    @Override
    public Solution solve(Term g) {
        return imp.solve(g);
    }

    @Override
    public Solution solve(String g) throws MalformedGoalException {
        return imp.solve(g);
    }

    @Override
    public boolean hasOpenAlternatives() {
        return imp.hasOpenAlternatives();
    }

//    @Override
//    public Solution solveNext() throws NoMoreSolutionException {
//        return imp.solveNext();
//    }

    @Override
    public void solveHalt() {
        imp.solveHalt();
    }

    @Override
    public void solveEnd() {
        imp.solveEnd();
    }


    @Override
    public void loadLibrary(String className) throws InvalidLibraryException {
        imp.addLibrary(className);
    }

    @Override
    public void unloadLibrary(String className) throws InvalidLibraryException {
        imp.removeLibrary(className);
    }

}
