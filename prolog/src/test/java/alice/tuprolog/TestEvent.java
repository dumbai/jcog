/*
 * Created on May 23, 2005
 * 
 */
package alice.tuprolog;

import alice.tuprolog.event.LibraryEvent;
import alice.tuprolog.event.PrologEventAdapter;
import alice.tuprolog.event.QueryEvent;
import alice.tuprolog.event.TheoryEvent;

class MyListener extends PrologEventAdapter {
    
    @Override
    public void theoryChanged(TheoryEvent ev){
        System.out.println("THEORY CHANGED: \n old: \n"+
                ev.getOldTheory()+"\n new: \n"+ev.getNewTheory());
    }
    
    @Override
    public void accept(QueryEvent ev){
        System.out.println("NEW QUERY RESULT AVAILABLE: \nquery\n "+
                ev.getSolveInfo().getQuery() +"\nresult\n"+
                ev.getSolveInfo());
    }
    
    @Override
    public void libraryLoaded(LibraryEvent ev){
        System.out.println("NEW LIB loaded: "+ev.getLibraryName());
    }

    @Override
    public void libraryUnloaded(LibraryEvent ev){
        System.out.println("LIB unloaded: "+ev.getLibraryName());
    }

}
/**
 * 
 *
 * @author aricci
 *
 */
public class TestEvent {

    public static void main(String... args) throws InvalidLibraryException, InvalidTheoryException, MalformedGoalException, NoMoreSolutionException {
    
        Prolog engine = new Prolog(new String[]{});
        
        MyListener l = new MyListener();
        engine.addTheoryListener(l);
        engine.addQueryListener(l);
        engine.addLibraryListener(l);

        engine.addLibrary("alice.tuprolog.lib.BasicLibrary");
        engine.addLibrary("alice.tuprolog.lib.OOLibrary");
        engine.addLibrary("alice.tuprolog.lib.IOLibrary");
        
        Theory th = new Theory(
                """
                        a(1).
                        a(2).
                        """);
        
        engine.setTheory(th);
        
        Solution sinfo = engine.solve("a(X).");
        
        while (sinfo.isSuccess() && engine.hasOpenAlternatives()){
            sinfo = engine.solveNext();
        }
        
        engine.removeLibrary("alice.tuprolog.lib.IOLibrary");
        
        th = new Theory(
                """
                        a(3).
                        a(4).
                        """);
    
        
        engine.input(th);
        
        sinfo = engine.solve("a(X).");
        
        while (sinfo.isSuccess() && engine.hasOpenAlternatives()){
            sinfo = engine.solveNext();
        }
        
    }
}