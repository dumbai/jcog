/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package alice.tuprolog;

import alice.tuprolog.event.OutputListener;

/**
 * Provides a prolog virtual machine embedded in a separate thread.
 * It needs a theory and optionally a goal.
 * It parses the theory, solves the goal and stops.
 *
 * @see Prolog
 *
 */
public class PrologAgent extends Prolog {
    
    private final String theoryText;
    private String goalText;
    
  
    private static final OutputListener defaultOutputListener = ev -> System.out.print(ev.msg);
    
    
    /**
     * Builds a prolog agent providing it a theory
     *
     * @param theory the text representing the theory
     */
    public PrologAgent(String theory, ClauseIndex statics, MutableClauseIndex dyn){
        super(statics, dyn);
        theoryText=theory;
        addOutputListener(defaultOutputListener);
    }





    /**
     * Builds a prolog agent providing it a theory and a goal
     */
    @Deprecated public PrologAgent(String theory, String goal){

        theoryText=theory;
        goalText=goal;

        addOutputListener(defaultOutputListener);
    }
    




















    /**
     * Starts agent execution in another thread
     */
    public final Thread spawn(){
        Thread t = new Thread(this::run);
        t.start();
        return t;
    }

    public Solution run(){
        return run(theoryText, goalText);
    }






















    
    
}