package alice.tuprologx.runtime.tcp;

import alice.tuprolog.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@SuppressWarnings("serial")
public class PrologImpl implements Serializable {

    final alice.tuprolog.Prolog core;
    

    public PrologImpl(alice.tuprolog.Prolog core_){
        core=core_;
        
    }

    public void clearTheory(ObjectInputStream in,ObjectOutputStream out){
        core.clearTheory();
    }

    public void getTheory(ObjectInputStream in,ObjectOutputStream out) throws IOException {
        Theory th=core.getTheory();
        out.writeObject(Boolean.FALSE);
        out.writeObject(th);
    }

    public void setTheory(ObjectInputStream in,ObjectOutputStream out) throws IOException, ClassNotFoundException {
        try {
            Theory th=(Theory)in.readObject();
            core.setTheory(th);
            out.writeObject(Boolean.TRUE);
        } catch (InvalidTheoryException ex){
            out.writeObject(Boolean.FALSE);
        }
    }

    public void addTheory(ObjectInputStream in,ObjectOutputStream out) throws IOException, ClassNotFoundException {
        try {
            Theory th=(Theory)in.readObject();
            core.input(th);
            out.writeObject(Boolean.TRUE);
        } catch (InvalidTheoryException ex){
            out.writeObject(Boolean.FALSE);
        }
    }

    public void solveString(ObjectInputStream in,ObjectOutputStream out) throws IOException, ClassNotFoundException {
        try {
            String st=(String)in.readObject();
            Solution info=core.solve(st);
            out.writeObject(Boolean.TRUE);
            out.writeObject(info);
        } catch (MalformedGoalException ex){
            out.writeObject(Boolean.FALSE);
        }
    }

    public void hasOpenAlternatives(ObjectInputStream in,ObjectOutputStream out) throws IOException {
        out.writeObject(core.hasOpenAlternatives());
    }

    public void solveTerm(ObjectInputStream in,ObjectOutputStream out) throws IOException, ClassNotFoundException {
        Term th=(Term)in.readObject();
        Solution info=core.solve(th);
        out.writeObject(Boolean.TRUE);
        out.writeObject(info);
    }

//    public void solveNext(ObjectInputStream in,ObjectOutputStream out) throws java.io.IOException {
//        try {
//            Solution info=core.solveNext();
//            out.writeObject(Boolean.TRUE);
//            out.writeObject(info);
//        } catch (NoMoreSolutionException ex){
//            out.writeObject(Boolean.FALSE);
//        }
//    }

    public void solveHalt(ObjectInputStream in,ObjectOutputStream out){
        core.solveHalt();
    }

    public void solveEnd(ObjectInputStream in,ObjectOutputStream out){
        core.solveEnd();
    }


    public void loadLibrary(ObjectInputStream in,ObjectOutputStream out) throws IOException, ClassNotFoundException {
        try {
            String st=(String)in.readObject();
            core.addLibrary(st);
            out.writeObject(Boolean.TRUE);
        } catch (InvalidLibraryException ex){
            out.writeObject(Boolean.FALSE);
        }
    }

    public void unloadLibrary(ObjectInputStream in,ObjectOutputStream out) throws IOException, ClassNotFoundException {
        try {
            String st=(String)in.readObject();
            core.removeLibrary(st);
            out.writeObject(Boolean.TRUE);
        } catch (InvalidLibraryException ex){
            out.writeObject(Boolean.FALSE);
        }
    }
}
