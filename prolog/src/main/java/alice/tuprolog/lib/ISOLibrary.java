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
package alice.tuprolog.lib;

import alice.tuprolog.*;

import java.util.stream.IntStream;

/**
 * This class represents a tuProlog library providing most of the built-ins
 * predicates and functors defined by ISO standard.
 *
 * Library/Theory dependency: BasicLibrary
 *
 *
 *
 */
public class ISOLibrary extends PrologLib {
    private static final long serialVersionUID = 1L;
    public ISOLibrary() {
    }

    public boolean atom_length_2(Term arg0, Term len) throws PrologError {
        arg0 = arg0.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (!arg0.isAtomic())
            throw PrologError.type_error(prolog, 1, "atom",
                    arg0);
        Struct atom = (Struct) arg0;
        return unify(len, new NumberTerm.Int(atom.name().length()));
    }

    public boolean atom_chars_2(Term arg0, Term arg1) throws PrologError {
        arg0 = arg0.term();
        arg1 = arg1.term();
        if (arg0 instanceof Var) {
            if (!arg1.isList()) {
                throw PrologError.type_error(prolog, 2,
                        "list", arg1);
            }
            Struct list = (Struct) arg1;
            if (list.isEmptyList()) {
                return unify(arg0, new Struct(""));
            }
            String st = "";
            while (!(list.isEmptyList())) {
                String st1 = list.subResolve(0).toString();
                try {
                    if (st1.startsWith("'") && st1.endsWith("'")) {
                        st1 = st1.substring(1, st1.length() - 1);
                    }
                    /*else
                    {
                    	byte[] b= st1.getBytes();
                    	st1=""+b[0];
                    }*/

                } catch (Exception ex) {
                }
                st += st1;
                list = (Struct) list.subResolve(1);
            }
            return unify(arg0, new Struct(st));
        } else {
            if (!arg0.isAtomic()) {
                throw PrologError.type_error(prolog, 1,
                        "atom", arg0);
            }
            String st = ((Struct) arg0).name();
            int bound = st.length();
            Term[] tlist = IntStream.range(0, bound).mapToObj(i -> new Struct(String.valueOf(st.charAt(i)))).toArray(Term[]::new);
            Struct list = new Struct(tlist);
            /*
             * for (int i=0; i<st.length(); i++){ Struct ch=new Struct(new
             * String(new char[]{ st.charAt(st.length()-i-1)} )); list=new
             * Struct( ch, list); }
             */

            return unify(arg1, list);
        }
    }

    public boolean char_code_2(Term arg0, Term arg1) throws PrologError {
        arg0 = arg0.term();
        arg1 = arg1.term();
        if (arg1 instanceof Var) {
            if (arg0.isAtomic()) {
                String st = ((Struct) arg0).name();
                if (st.length() <= 1)
                    return unify(arg1, new NumberTerm.Int(st.charAt(0)));
                else
                    throw PrologError.type_error(prolog, 1,
                            "character", arg0);
            } else
                throw PrologError.type_error(prolog, 1,
                        "character", arg0);
        } else if ((arg1 instanceof NumberTerm.Int)
                || (arg1 instanceof NumberTerm.Long)) {
            char c = (char) ((NumberTerm) arg1).intValue();
            return unify(arg0, new Struct(String.valueOf(c)));
        } else
            throw PrologError.type_error(prolog, 2,
                    "integer", arg1);
    }





    public Term sin_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Double(Math.sin(((NumberTerm) val0)
                    .doubleValue()));
        return null;
    }

    public Term cos_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Double(Math.cos(((NumberTerm) val0)
                    .doubleValue()));
        return null;
    }

    public Term exp_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Double(Math.exp(((NumberTerm) val0)
                    .doubleValue()));
        return null;
    }

    public Term atan_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Double(Math.atan(((NumberTerm) val0)
                    .doubleValue()));
        return null;
    }

    public Term log_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Double(Math.log(((NumberTerm) val0)
                    .doubleValue()));
        return null;
    }

    public Term sqrt_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Double(Math.sqrt(((NumberTerm) val0)
                    .doubleValue()));
        return null;
    }

    public Term abs_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm.Int || val0 instanceof NumberTerm.Long)
            return new NumberTerm.Int(Math.abs(((NumberTerm) val0).intValue()));
        if (val0 instanceof NumberTerm.Double
                || val0 instanceof NumberTerm.Float)
            return new NumberTerm.Double(Math.abs(((NumberTerm) val0)
                    .doubleValue()));
        return null;
    }

    public Term sign_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm.Int || val0 instanceof NumberTerm.Long)
            return new NumberTerm.Double(
                    ((NumberTerm) val0).intValue() > 0 ? 1.0 : -1.0);
        if (val0 instanceof NumberTerm.Double
                || val0 instanceof NumberTerm.Float)
            return new NumberTerm.Double(
                    ((NumberTerm) val0).doubleValue() > 0 ? 1.0 : -1.0);
        return null;
    }

    public Term float_integer_part_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Double((long) Math.rint(((NumberTerm) val0)
                    .doubleValue()));
        return null;
    }

    public Term float_fractional_part_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm) {
            double fl = ((NumberTerm) val0).doubleValue();
            return new NumberTerm.Double(Math.abs(fl - Math.rint(fl)));
        }
        return null;
    }

    public Term float_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Double(((NumberTerm) val0).doubleValue());
        return null;
    }

    public Term floor_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Int((int) Math.floor(((NumberTerm) val0).doubleValue()));
        return null;
    }

    public Term round_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Long(Math.round(((NumberTerm) val0)
                    .doubleValue()));
        return null;
    }

    public Term truncate_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Int((int) Math.rint(((NumberTerm) val0).doubleValue()));
        return null;
    }

    public Term ceiling_1(Term val) {
        Term val0 = null;
        try {
            val0 = evalExpression(val);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm)
            return new NumberTerm.Int((int) Math.ceil(((NumberTerm) val0).doubleValue()));
        return null;
    }

    public Term div_2(Term v0, Term v1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(v0);
            val1 = evalExpression(v1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm && val1 instanceof NumberTerm)
            return new NumberTerm.Int(((NumberTerm) val0).intValue()
                    / ((NumberTerm) val1).intValue());
        return null;
    }

    public Term mod_2(Term v0, Term v1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(v0);
            val1 = evalExpression(v1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm && val1 instanceof NumberTerm) {
            int x = ((NumberTerm) val0).intValue();
            int y = ((NumberTerm) val1).intValue();
            int f = (int) Math.round(Math.floor((double) x / y));
            return new NumberTerm.Int(x - (f * y));
        }
        return null;
    }

    public Term rem_2(Term v0, Term v1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(v0);
            val1 = evalExpression(v1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm && val1 instanceof NumberTerm) {
            return new NumberTerm.Double(Math.IEEEremainder(((NumberTerm) val0)
                    .doubleValue(), ((NumberTerm) val1).doubleValue()));
        }
        return null;
    }

    /**
     * library theory
     */
    @Override
    public String getTheory() {
        return



                ":- op(  300, yfx,  'div'). \n"
                        + ":- op(  400, yfx,  'mod'). \n"
                        + ":- op(  400, yfx,  'rem'). \n"
                        + ":- op(  200, fx,   'sin'). \n"
                        + ":- op(  200, fx,   'cos'). \n"
                        + ":- op(  200, fx,   'sqrt'). \n"
                        + ":- op(  200, fx,   'atan'). \n"
                        + ":- op(  200, fx,   'exp'). \n"
                        + ":- op(  200, fx,   'log'). \n"
                        +



                        ":- flag(bounded, [true,false], true, false).\n"
                        + ":- flag(max_integer, ["
                        + Integer.MAX_VALUE
                        + "], "
                        + Integer.MAX_VALUE
                        + ",false).\n"
                        + ":- flag(min_integer, ["
                        + Integer.MIN_VALUE
                        + "], "
                        + Integer.MIN_VALUE
                        + ",false).\n"
                        + ":- flag(integer_rounding_function, [up,down], down, false).\n"
                        + ":- flag(char_conversion,[on,off],off,false).\n"
                        + ":- flag(debug,[on,off],off,false).\n"
                        + ":- flag(max_arity, ["
                        + Integer.MAX_VALUE
                        + "], "
                        + Integer.MAX_VALUE
                        + ",false).\n"
                        + ":- flag(undefined_predicate, [error,fail,warning], fail, false).\n"
                        + ":- flag(double_quotes, [atom,chars,codes], atom, false).\n"


                        + "bound(X):-ground(X).\n                                                                                  "
                        + "unbound(X):-not(ground(X)).\n                                                                          "


                        + "atom_concat(F,S,R) :- catch(atom_concat0(F,S,R), Error, false).\n"
                        + "atom_concat0(F,S,R) :- var(R), !,(atom_chars(S,SL),append(FL,SL,RS),atom_chars(F,FL),atom_chars(R,RS)).  \n"
                        + "atom_concat0(F,S,R) :-(atom_chars(R,RS), append(FL,SL,RS),atom_chars(F,FL),atom_chars(S,SL)).\n"

                        + "atom_codes(A,L):- catch(atom_codes0(A,L), Error, false).\n"
                        + "atom_codes0(A,L):-nonvar(A),atom_chars(A,L1),!,chars_codes(L1,L).\n"
                        + "atom_codes0(A,L):-nonvar(L), list(L), !,chars_codes(L1,L),atom_chars(A,L1).\n"
                        + "chars_codes([],[]).\n"
                        + "chars_codes([X|L1],[Y|L2]):-char_code(X,Y),chars_codes(L1,L2).\n"

                        + "sub_atom(Atom,B,L,A,Sub):- sub_atom_guard(Atom,B,L,A,Sub), sub_atom0(Atom,B,L,A,Sub).\n"
                        + "sub_atom0(Atom,B,L,A,Sub):-atom_chars(Atom,L1),sub_list(L2,L1,B),atom_chars(Sub,L2),length(L2,L), length(L1,Len), A is Len-(B+L).\n"
                        + "sub_list([],_,0).\n"
                        + "sub_list([X|L1],[X|L2],0):- sub_list_seq(L1,L2).\n"
                        + "sub_list(L1,[_|L2],N):- sub_list(L1,L2,M), N is M + 1.\n"
                        + "sub_list_seq([],L).\n"
                        + "sub_list_seq([X|L1],[X|L2]):-sub_list_seq(L1,L2).\n"

                        + "number_chars(Number,List):-catch(number_chars0(Number,List), Error, false).\n"
                        + "number_chars0(Number,List):-nonvar(Number),!,num_atom(Number,Struct),atom_chars(Struct,List).\n"
                        + "number_chars0(Number,List):-atom_chars(Struct,List),num_atom(Number,Struct).\n"

                        + "number_codes(Number,List):-catch(number_codes0(Number,List), Error, false).\n"
                        + "number_codes0(Number,List):-nonvar(Number),!,num_atom(Number,Struct),atom_codes(Struct,List).\n"
                        + "number_codes0(Number,List):-atom_codes(Struct,List),num_atom(Number,Struct).\n";





    }



    public boolean sub_atom_guard_5(Term arg0, Term arg1, Term arg2, Term arg3, Term arg4)
            throws PrologError {
        arg0 = arg0.term();
        if (!arg0.isAtomic())
            throw PrologError.type_error(prolog, 1, "atom", arg0);
        return true;
    }

}