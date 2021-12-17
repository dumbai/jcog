/*
 *   Automaton.java
 *
 * Copyright 2000-2001-2002  aliCE team at deis.unibo.it
 *
 * This software is the proprietary information of deis.unibo.it
 * Use is subject to license terms.
 *
 */
package alice.util;

import java.io.Serializable;

/**
 * this abstract class is the base class for
 * implementing automaton abstraction
 *
 * automaton state behaviour is expressed
 * in public method and the become method
 * allows to move computation from state to state
 *
 * method representing state must be public
 * (to allow correct behaviour of reflection)
 *
 */
@SuppressWarnings("serial")
public abstract class Automaton implements Runnable, Serializable {

    /**
	 * method name representing state behaviour
	 */
    protected String state="boot";

    /**
	 * arguments value eventually associated to state transition
	 */
    protected Object[] arguments;

    /**
	 * arguments class eventually associated to state transition
	 */
    protected Class<?>[] argType;

    public Automaton(){
        try {
            argType=new Class[]{ Class.forName("[Ljava.lang.Object;") };
        } catch (Exception ex){
        }
    }

    protected void become(String s){
        if (!"end".equals(state)){
            state=s;
            arguments=null;
        }
    }

    protected void become(String s, Object... args){
        if (!"end".equals(state)){
            state=s;
            arguments=args;
        }
    }

    /** boot state of automaton */
    public abstract void boot();

    @Override
    public void run(){
        while (true){
            try {
                if (!"end".equals(state)){
                    if (arguments==null){
                        this.getClass().getDeclaredMethod(state,(Class<?>[])null).invoke(this,(Object[])null);
                    } else {
                        this.getClass().getDeclaredMethod(state,argType).invoke(this,arguments);
                    }
                } else {
                    end();
                    break;
                }
            } catch (Exception ex){
                ex.printStackTrace();
                error();
            }
        }
    }

    /** idle state */
    public void idle(){
        try {
            wait();
        } catch (Exception ex){
        }
    }

    /** shutdown state */
    public void end(){
    }

    /** error state */
    public void error(){
        become("end");
    }
}
