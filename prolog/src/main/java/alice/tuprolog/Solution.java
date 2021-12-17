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

import java.io.Serializable;
import java.util.List;


/**
 *
 * SolveInfo class represents the result of a solve
 * request made to the engine, providing information
 * about the solution
 * 
 * @author Alex Benini
 */
public class Solution implements Serializable/*, ISolution<Term,Term,Term>*/  {
    /*
     * possible values returned by step functions
     * and used as eval state flags
     */
    static final int HALT    = PrologRun.HALT;
    static final int FALSE   = PrologRun.FALSE;
    static final int TRUE    = PrologRun.TRUE;
    static final int TRUE_CP = PrologRun.TRUE_CP;
    
    private int     endState;
    private final boolean isSuccess;
    
    public final Term query;
    public final Struct goal;
    private final List<Var>   bindings;
    String setOfSolution;
    
    
    /**
     * 
     */
    Solution(Term initGoal){
        query = initGoal;
        isSuccess = false;
        setOfSolution=null;
        goal = null;
        bindings = null;
    }
    
    /**
     * 
     * @param initGoal
     * @param resultGoal
     * @param resultDemo
     * @param resultVars
     */
    Solution(Term initGoal, Struct resultGoal, int resultDemo, List<Var> resultVars) {
        query = initGoal;
        goal = resultGoal;
        bindings = resultVars;
        endState = resultDemo;
        isSuccess = (endState > FALSE);
        setOfSolution=null;
    }
    
    
    
    /**
	 * Checks if the solve request was successful
	 * @return  true if the solve was successful
	 */
    public boolean isSuccess() {
        return isSuccess;
    }
    
    
    /**
     * Checks if the solve request was halted
     *
     * @return true if the solve was successful
     */
    public boolean isHalted() {
        return (endState == HALT);
    }
    
    
    /**
     * Checks if the solve request was halted
     *
     * @return true if the solve was successful
     */
    public boolean hasOpenAlternatives() {
        return (endState == TRUE_CP);
    }
    
    
    /**
	 * Gets the query
	 * @return  the query
	 */
    public Term getQuery() {
        return query;
    }

    public void setSetOfSolution(String s) {
        setOfSolution=s;
    }
    
    
    /**
     *  Gets the solution of the request
     *
     *  @exception NoSolutionException if the solve request has not
     *             solution
     */
    public Term  getSolution() throws NoSolutionException {
        if (isSuccess)
            return goal;
        else
            throw new NoSolutionException();

    }

    public Term getSolutionOrNull() {
        if (isSuccess)
            return goal;
        else
            return null;
    }
    
    /**
     * Gets the list of the variables in the solution.
     * @return the array of variables.
     * 
     * @throws NoSolutionException if current solve information
     * does not concern a successful 
     */
    public List<Var> getBindingVars() throws NoSolutionException {
        if (isSuccess){
            return bindings;
        }else {
            throw new NoSolutionException();
        }
    }
    
    /**
     * Gets the value of a variable in the substitution.
     * @throws NoSolutionException if the solve request has no solution
     * @throws Var.UnknownVarException if the variable does not appear in the substitution.
     */
    public Term getTerm(String varName) throws NoSolutionException, Var.UnknownVarException {
        Term t = getVarValue(varName);
        if (t == null)
            throw new Var.UnknownVarException();
        return t;
    }
    
    /**
     * Gets the value of a variable in the substitution. Returns <code>null</code>
     * if the variable does not appear in the substitution.
     */
    public Term getVarValue(String varName) throws NoSolutionException {
        if (isSuccess) {
            for (Var v : bindings) {
                if (v != null && v.name().equals(varName)) {
                    return v.term();
                }
            }
            return null;
        } else
            throw new NoSolutionException();
    }
    
    /**
     * Returns the string representation of the result of the demonstration.
     * 
     * For successful demonstration, the representation concerns 
     * variables with bindings.  For failed demo, the method returns false string.
     * 
     */    
    public String toString() {
        if (isSuccess) {
            StringBuilder st = new StringBuilder("yes");
            if (!bindings.isEmpty()) {
                st.append(".\n");
            } else {
                st.append(". ");
            }
            for (Var v : bindings) {
                if (v != null && !v.isAnonymous() && v.isBound() &&
                        (!(v.term() instanceof Var) || (!((Var) (v.term())).name().startsWith("_")))) {
                    st.append(v);
                    st.append("  ");
                }
            }
            return st.toString().trim();
        } else {
        	/*Castagna 06/2011*/
            return endState == PrologRun.HALT ? Term.HALT : Term.NO;
        }
    }


    public int result() {
        return endState;
    }



}