package alice.tuprolog;

import alice.tuprolog.event.LibraryEvent;
import alice.tuprolog.event.PrologEventAdapter;
import alice.tuprolog.event.QueryEvent;
import alice.tuprolog.event.TheoryEvent;

class TestPrologEventAdapter extends PrologEventAdapter {
	String firstMessage = "";
	String secondMessage = "";
    
    @Override
    public void theoryChanged(TheoryEvent ev) {
    	firstMessage = ev.getOldTheory().toString();
    	secondMessage = ev.getNewTheory().toString();
    }
    
    @Override
    public void accept(QueryEvent ev) {
    	firstMessage = ev.getSolveInfo().getQuery().toString();
    	secondMessage = ev.getSolveInfo().toString();
    }
    
    @Override
    public void libraryLoaded(LibraryEvent ev) {
    	firstMessage = ev.getLibraryName();
    }

    @Override
    public void libraryUnloaded(LibraryEvent ev) {
    	firstMessage = ev.getLibraryName();
    }
}
