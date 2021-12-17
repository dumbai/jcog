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

import jcog.data.list.Lst;
import jcog.io.BinTxt;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * This class represents a variable term.
 * Variables are identified by a name (which must starts with
 * an upper case letter) or the anonymous ('_') name.
 *
 * @see Term
 */
public class Var extends Term {

    /* Identify kind of renaming */
    static final int ORIGINAL = -1;
    static final int PROGRESSIVE = -2;
    private static final String ANY = "_";
    public Term link;            /* link is used for unification process */
    protected long timestamp;        /* timestamp is used for fix vars order */
    private String name;
    private String completeName;     /* Reviewed by Paolo Contessi: String -> StringBuilder */


    /**
     * Creates a variable identified by a name.
     * <p>
     * The name must starts with an upper case letter or the underscore. If an underscore is
     * specified as a name, the variable is anonymous.
     *
     * @param n is the name
     * @throws InvalidTermException if n is not a valid Prolog variable name
     */
    public Var(String n) {
        link = null;
        if (n.equals(ANY)) {
            name = null;
            completeName = "";
        } else if (Character.isUpperCase(n.charAt(0)) ||
                (n.startsWith(ANY))) {
            name = n;
            completeName = name;
        } else {
            throw new InvalidTermException("Illegal variable name: " + n);
        }
    }

    /**
     * Creates an anonymous variable
     * <p>
     * This is equivalent to builder a variable with name _
     */
    public Var() {
        name = null;
        completeName = "";
        link = null;
        timestamp = 0;
    }

    /**
     * Creates a internal engine variable.
     *
     * @param n     is the name
     * @param id    is the id of ExecCtx
     * @param alias code to discriminate external vars
     * @param time  is timestamp
     */
    private Var(String n, long time) {
        name = n;
        completeName = n;
        timestamp = time;
        link = null;
    }
    private Var(String n, int id, int alias, long time) {
        this(n, time);
        if (id != ORIGINAL)
            rename(id, alias);
    }

    static void free(Collection<Var> varsUnified) {
        free((Iterable)varsUnified);
        varsUnified.clear();
    }

    /**
     * De-unify the variables of list
     */
    private static void free(Iterable<Var> varsUnified) {
        for (Var t : varsUnified)
            t.link = null;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    /**
     * Rename variable (assign completeName)
     */
    void rename(int idExecCtx, int count) { /* Reviewed by Paolo Contessi: String -> StringBuilder */

        switch (idExecCtx) {
            case ORIGINAL -> this.completeName = name;
            case PROGRESSIVE -> {
                int extra = idExecCtx < BinTxt.maxBase ? 1 : 2;
                StringBuilder c = new StringBuilder(1 + extra /* estimate */);
                c.append('_');
                BinTxt.append(c, count, BinTxt.maxBase);
                this.completeName = c.toString();
            }
            default -> {
                String name = this.name;
                int extra;
                if (idExecCtx < BinTxt.maxBase) {
                    extra = 1;
                } else if (idExecCtx < BinTxt.maxBase * BinTxt.maxBase) {
                    extra = 2;
                } else {
                    extra = 3;
                    //etc.
                }
                StringBuilder c =
                        name != null ?
                                new StringBuilder(1 + name.length() + extra /* estimate */).append(name) :
                                new StringBuilder(1 + extra);
                c.append('_');
                //c.append(Integer.toString(idExecCtx, Character.MAX_RADIX));
                BinTxt.append(c, idExecCtx, BinTxt.maxBase);
                this.completeName = c.toString();
            }
        }
    }

    /**
     * Gets a copy of this variable.
     * <p>
     * if the variable is not present in the list passed as argument,
     * a copy of this variable is returned and added to the list. If instead
     * a variable with the same time identifier is found in the list,
     * then the variable in the list is returned.
     */
    @Override
    public Term copy(Map<Var, Var> vMap, int idExecCtx) {
        Term tt = term();
        if (tt == this) {
            if (idExecCtx == ORIGINAL) {
                vMap.putIfAbsent(this, this);
                return this;
            } else {
                Var y = vMap.computeIfAbsent(this, k -> new Var(k.name, k.timestamp));
                y.rename(idExecCtx, 0);
                return y;
            }
        } else {
            return tt.copy(vMap, idExecCtx);
        }
    }

    /**
     * Gets a copy of this variable.
     *
     * @param vMap
     * @param substMap
     */
    @Override
    Term copy(Map<Var, Var> vMap, Map<Term, Var> substMap) {
        Var v = vMap.computeIfAbsent(this,
                tis -> new Var(tis.name, PROGRESSIVE, vMap.size(), tis.timestamp)
        );

        Term t = term();

        if (t instanceof Var) {
            Var tt;
            if (substMap == null) {
                substMap = new IdentityHashMap<>();
                tt = null;
            } else {
                tt = substMap.get(t);
            }

            if (tt == null) {
                substMap.put(t, v);
                v.link = null;
            } else {
                v.link = (tt == v) ? null : tt;
            }
        } else if (t instanceof Struct)
            v.link = t.copy(vMap, substMap);
        else if (t instanceof NumberTerm)
            v.link = t;

        return v;
    }

    /**
     * Gets the name of the variable
     */
    public String name() {
        return name != null ? completeName : ANY;
    }

    /**
     * Gets the name of the variable
     */
    public String getOriginalName() {
        return name != null ? name : ANY + hashString();
    }


    /**
     * Gets the term which is referred by the variable.
     * <p>
     * For unbound variable it is the variable itself, while
     * for bound variable it is the bound term.
     */
    @Override
    public final Term term() {
        Term tt = this;
        Term t = link;
        while (t != null) {
            tt = t;
            if (t instanceof Var) {
                t = ((Var) t).link;
            } else
                break;
        }

        return tt;
    }


    /**
     * Gets the term which is direct referred by the variable.
     */
    public final Term link() {
        return link;
    }

    /**
     * Set the term which is direct bound
     */
    void setLink(Term l) {
        link = l;
    }

    /**
     * Set the timestamp
     */
    void setTimestamp(long t) {
        timestamp = t;
    }


    @Override
    public boolean isEmptyList() {
        Term t = term();
        return t != this && t.isEmptyList();
    }

    @Override
    public boolean isAtom() {
        Term t = term();
        return t != this && t.isAtom();
    }

    @Override
    public boolean isCompound() {
        Term t = term();
        return t != this && t.isCompound();
    }

    @Override
    public boolean isAtomic() {
        Term t = term();
        return t != this && t.isAtomic();
    }

    @Override
    public boolean isList() {
        Term t = term();
        return t != this && t.isList();
    }

    @Override
    public boolean isGround() {
        Term t = term();
        return t != this && t.isGround();
    }


    /**
     * Tests if this variable is ANY
     */
    public boolean isAnonymous() {
        return name == null;
    }

    /**
     * Tests if this variable is bound
     */
    public boolean isBound() {
        return link != null;
    }


    /**
     * finds var occurence in a Struct, doing occur-check.
     * (era una findIn)
     *
     * @param vl TODO
     */
    private boolean occurCheck(Collection<Var> vl, Struct t) {

        if (t.containsIdentity(this))
            return true;

        int arity = t.subs();
        boolean hasVar = false;


        for (int c = 0; c < arity; c++) {
            Term x = t.sub(c);
            if (x instanceof Var) {
                hasVar = true; //processed below
            } else if (x instanceof Struct) {
                if (occurCheck(vl, (Struct) x))
                    return true;
            }
        }

        if (!hasVar)
            return false;

        for (int c = 0; c < arity; c++) {
            Term x = t.sub(c);
            if (!(x instanceof Var)) continue;
            Term y = x.term();
            if (x!=y) {
                if (y instanceof Struct) {
                    if (occurCheck(vl, (Struct) y))
                        return true;
                } else if (y instanceof Var v) {
                    if (this == y)
                        return true;
                    if (v.link == null)
                        vl.add(v);
                }
            }
        }

        return false;

    }



    @Override
    public void resolveTerm() {
        resolveTerm(now());
    }

    /**
     * Resolve the occurence of variables in a Term
     */
    @Override
    void resolveTerm(long count) {
        Term tt = term();
        if (tt != this) {
            tt.resolveTerm(count);
        } else {
            timestamp = count;
        }
    }


    /**
     * var unification.
     * <p>
     * First, verify the Term eventually already unified with the same Var
     * if the Term exist, unify var with that term, in order to handle situation
     * as (A = p(X) , A = p(1)) which must produce X/1.
     * <p>
     * If instead the var is not already unified, then:
     * <p>
     * if the Term is a var bound to X, then try unification with X
     * so for example if A=1, B=A then B is unified to 1 and not to A
     * (note that it's coherent with chronological backtracking:
     * the eventually backtracked A unification is always after
     * backtracking of B unification.
     * <p>
     * if are the same Var, unification must succeed, but without any new
     * bindings (to avoid cycles for extends in A = B, B = A)
     * <p>
     * if the term is a number, then it's a success and new link is created
     * (retractable by means of a code)
     * <p>
     * if the term is a compound, then occur check test is executed:
     * the var must not appear in the compound ( avoid X=p(X),
     * or p(X,X)=p(Y,f(Y)) ); if occur check is ok
     * then it's success and a new link is created (retractable by a code)
     */
    @Override
    boolean unify(Term t, Collection<Var> vl1, Collection<Var> vl2) {
        Term tt = term();
        if (tt == this) {

            if (t instanceof Var) {
                if (this == t) {
                    vl1.add(this);
                    return true;
                }
            } else if (t instanceof Struct) {

                if (occurCheck(vl2, (Struct) t)) {
                    //clear to relieve GC pressure
                    vl1.clear();
                    vl2.clear();
                    return false;
                }

            } else if (!(t instanceof NumberTerm) && !(t instanceof AbstractSocket)) {
                return false;
            }
            link = t;

            vl1.add(this);
            return true;
        } else {
            return tt.unify(t, vl1, vl2);
        }
    }


    /**
     * Gets a copy of this variable
     */
     /*    public Term copy(int idExecCtx) {
     Term tt = getTerm();
     if(tt == this) {
     if(idExecCtx > 0 && id > 0) thisCopy++;
     return new Var(name,idExecCtx,thisCopy,antialias,timestamp);
     } else {
     return (tt.copy(idExecCtx));
     }
     }
	  */
    @Override
    public boolean isGreater(Term t) {
        Term tt = term();
        if (tt == this) {
            t = t.term();
            return t instanceof Var && timestamp > ((Var) t).timestamp;
        } else {
            return tt.isGreater(t);
        }
    }

    @Override
    public boolean isGreaterRelink(Term t, Lst<String> vorder) {
        Term tt = term();
        if (tt == this) {
            t = t.term();
            if (!(t instanceof Var)) return false;


            return vorder.indexOf(((Var) tt).name()) > vorder.indexOf(((Var) t).name());
        } else {
            return tt.isGreaterRelink(t, vorder);
        }
    }

    @Override
    public boolean isEqual(Term t) {
        Term tt = term();
        if (tt == this) {
            t = t.term();
            return (t instanceof Var && timestamp == ((Var) t).timestamp);
        } else {
            return tt.isEqual(t);
        }
    }

    public void setName(String s) {
        this.name = s;
    }

    /**
     * Gets the string representation of this variable.
     * <p>
     * For bounded variables, the string is <Var Name>/<bound Term>.
     */
    @Override
    public String toString() {
        Term tt = term();
        if (name != null) {
            return tt == this ? completeName : completeName + " / " + tt;
        } else {
            return tt == this ? ANY + hashString() : tt.toString();
        }
    }

    private String hashString() {
        //return Integer.toString(hashCode(), Character.MAX_RADIX);
        return BinTxt.toString(hashCode(), 63);
    }


    /**
     * Gets the string representation of this variable, providing
     * the string representation of the linked term in the case of
     * bound variable
     */
    public String toStringFlattened() {
        Term tt = term();
        if (name != null) {
            return tt == this ? completeName : tt.toString();
        } else {
            return tt == this ? ANY + hashString() : tt.toString();
        }
    }


    /**/

    /**
     * This exception means that a not well formed goal has been specified.
     */
    public static class UnknownVarException extends PrologException {
    }
}