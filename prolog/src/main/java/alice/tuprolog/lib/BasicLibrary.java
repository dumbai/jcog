/*
 * tuProlog - Copyright (C) 2001-2007 aliCE team at deis.unibo.it
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
import alice.util.Tools;
import jcog.data.list.Lst;

import java.util.Iterator;
import java.util.List;

/**
 * This class defines a set of basic built-in predicates for the tuProlog engine
 *
 * Library/Theory dependency: none
 *
 *
 *
 */
public class BasicLibrary extends PrologLib {
    public BasicLibrary() {
    }





    /**
     * sets a new theory provided as a text
     *
     * @throws PrologError
     */
    public boolean set_theory_1(Term th) throws PrologError {
        th = th.term();
        if (th instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (!th.isAtomic()) {
            throw PrologError.type_error(prolog, 1, "atom",
                    th);
        }
        try {
            Struct theory = (Struct) th;
            prolog.setTheory(new Theory(theory.name()));
            return true;
        } catch (InvalidTheoryException ex) {
            /*Castagna 06/2011*/

            throw PrologError.syntax_error(prolog, ex.clause, ex.line, ex.pos, new Struct(ex.getMessage()));
            /**/
        }
    }

    /**
     * adds a new theory provided as a text
     *
     * @throws PrologError
     */
    public boolean add_theory_1(Term th) throws PrologError {
        th = th.term();
        if (th instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (!th.isAtomic()) {
            throw PrologError.type_error(prolog, 1, "atom",
                    th);
        }
        try {
            Struct theory = (Struct) th.term();
            prolog.input(new Theory(theory.name()));
            return true;
        } catch (InvalidTheoryException ex) {
            /*Castagna 06/2011*/

            throw PrologError.syntax_error(prolog, ex.clause, ex.line, ex.pos, new Struct(ex.getMessage()));
            /**/
        }
    }

    /** gets current theory text */
    public boolean get_theory_1(Term arg) {
        arg = arg.term();
        try {
            Term theory = new Struct(prolog.getTheory().toString());
            return (unify(arg, theory));
        } catch (Exception ex) {
            return false;
        }
    }















    /**
     * Loads a library constructed from a theory.
     *
     * @param theory
     *            theory text
     * @param libName
     *            name of the library
     * @return true if the library has been succesfully loaded.
     */
    public boolean load_library_from_theory_2(Term th, Term libName) {
        Struct theory = (Struct) th.term();
        Struct libN = (Struct) libName.term();
        try {
            if (!theory.isAtomic()) {
                return false;
            }
            if (!libN.isAtomic()) {
                return false;
            }
            Theory t = new Theory(theory.name());
            TheoryLibrary thlib = new TheoryLibrary(libN.name(), t);
            prolog.addLibrary(thlib);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean get_operators_list_1(Term argument) {
        Term arg = argument.term();
        Struct list = Struct.emptyList();
        for (PrologOp o : prolog.operators()) {
            list = new Struct(new Struct("op", new NumberTerm.Int(o.prio),
                    new Struct(o.type), new Struct(o.name)), list);
        }
        return unify(arg, list);
    }

//    /**
//     * spawns a separate prolog agent providing it a theory text
//     *
//     * @throws PrologError
//     */
//    public boolean agent_1(Term th) throws PrologError {
//        th = th.term();
//        if (th instanceof Var)
//            throw PrologError.instantiation_error(prolog, 1);
//        if (!(th.isAtomic()))
//            throw PrologError.type_error(prolog, 1, "atom",
//                    th);
//        Struct theory = (Struct) th;
//        try {
//            new PrologAgent(alice.util.Tools.removeApostrophes(theory.toString()), new ConcurrentHashClauseIndex()).spawn();
//            return true;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return false;
//        }
//    }

    /**
     * spawns a separate prolog agent providing it a theory text and a goal
     *
     * @throws PrologError
     */
    public boolean agent_2(Term th, Term g) throws PrologError {
        th = th.term();
        g = g.term();
        if (th instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (g instanceof Var)
            throw PrologError.instantiation_error(prolog, 2);
        if (!(th.isAtomic()))
            throw PrologError.type_error(prolog, 1, "atom",
                    th);
        if (!(g instanceof Struct goal))
            throw PrologError.type_error(prolog, 2,
                    "struct", g);
        Struct theory = (Struct) th;
        try {
            new PrologAgent(Tools.removeApostrophes(theory.toString()), goal
                    .toString()
                    + '.').spawn();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean spy_0() {
        prolog.setSpy(true);
        return true;
    }

    public boolean nospy_0() {
        prolog.setSpy(false);
        return true;
    }

    /*Castagna 16/09*/
    public boolean trace_0() {
        return this.spy_0();
    }

    public boolean notrace_0() {
        return nospy_0();
    }
    /**/

    public boolean warning_0() {
        prolog.setWarning(true);
        return true;
    }

    public boolean nowarning_0() {
        prolog.setWarning(false);
        return true;
    }





    public static boolean constant_1(Term t) {
        t = t.term();
        return (t.isAtom());
    }

    public static boolean number_1(Term t) {
        return (t.term() instanceof NumberTerm);
    }

    public static boolean integer_1(Term t) {
        if (!(t.term() instanceof NumberTerm n))
            return false;
        return (n.isInteger());
    }

    public static boolean float_1(Term t) {
        if (!(t instanceof NumberTerm))
            return false;
        NumberTerm n = (NumberTerm) t.term();
        return (n.isReal());
    }

    public static boolean atom_1(Term t) {
        t = t.term();
        return (t.isAtomic());
    }

    public static boolean compound_1(Term t) {
        t = t.term();
        return t.isCompound();
    }

    public static boolean list_1(Term t) {
        t = t.term();
        return (t.isList());
    }

    public static boolean var_1(Term t) {
        t = t.term();
        return (t instanceof Var);
    }

    public static boolean nonvar_1(Term t) {
        t = t.term();
        return !(t instanceof Var);
    }

    public static boolean atomic_1(Term t) {
        t = t.term();
        return t.isAtom();
    }

    public static boolean ground_1(Term t) {
        t = t.term();
        return (t.isGround());
    }





    private void handleError(Throwable t, int arg) throws PrologError {

        if (t instanceof ArithmeticException cause) {

            if ("/ by zero".equals(cause.getMessage()))
                throw PrologError.evaluation_error(prolog,
                        arg, "zero_divisor");
        }
    }

    public boolean expression_equality_2(Term arg0, Term arg1)
            throws PrologError {
        not_var(arg0, 1);
        not_var(arg1, 2);
        Term val0 = null;
        try {
            val0 = evalExpression(arg0);
        } catch (Throwable e) {
            handleError(e, 1);
        }
        Term val1 = null;
        try {
            val1 = evalExpression(arg1);
        } catch (Throwable e) {
            handleError(e, 2);
        }
        if (!(val0 instanceof NumberTerm val0n))
            throw PrologError.type_error(prolog, 1,
                    "evaluable", arg0.term());
        if (!(val1 instanceof NumberTerm val1n))
            throw PrologError.type_error(prolog, 2,
                    "evaluable", arg1.term());
        return val0n.equals(val1n);
        //val0n.isInteger() && val1n.isInteger() ? val0n.longValue() == val1n.longValue() : val0n.doubleValue() == val1n.doubleValue();
    }

    public boolean expression_greater_than_2(Term arg0, Term arg1)
            throws PrologError {
        int argNo = 1;
        not_var(arg0, argNo);
        not_var(arg1, 2);
        Term val0 = null;
        try {
            val0 = evalExpression(arg0);
        } catch (Throwable e) {
            handleError(e, 1);
        }
        Term val1 = null;
        try {
            val1 = evalExpression(arg1);
        } catch (Throwable e) {
            handleError(e, 2);
        }
        if (!(val0 instanceof NumberTerm))
            throw PrologError.type_error(prolog, 1,
                    "evaluable", arg0.term());
        if (!(val1 instanceof NumberTerm))
            throw PrologError.type_error(prolog, 2,
                    "evaluable", arg1.term());
        return expression_greater_than((NumberTerm) val0,
                (NumberTerm) val1);
    }


    public boolean expression_less_or_equal_than_2(Term arg0, Term arg1)
            throws PrologError {
        not_var(arg0, 1);
        not_var(arg1, 2);
        Term val0 = null;
        try {
            val0 = evalExpression(arg0);
        } catch (Throwable e) {
            handleError(e, 1);
        }
        Term val1 = null;
        try {
            val1 = evalExpression(arg1);
        } catch (Throwable e) {
            handleError(e, 2);
        }
        if (!(val0 instanceof NumberTerm))
            throw PrologError.type_error(prolog, 1,
                    "evaluable", arg0.term());
        if (!(val1 instanceof NumberTerm))
            throw PrologError.type_error(prolog, 2,
                    "evaluable", arg1.term());
        return !expression_greater_than((NumberTerm) val0,
                (NumberTerm) val1);
    }

    private static boolean expression_greater_than(NumberTerm num0,
                                                   NumberTerm num1) {
        return num0.isInteger() && num1.isInteger() ? num0.longValue() > num1.longValue() : num0.doubleValue() > num1.doubleValue();
    }

    public boolean expression_less_than_2(Term arg0, Term arg1)
            throws PrologError {
        not_var(arg0, 1);
        not_var(arg1, 2);
        Term val0 = null;
        try {
            val0 = evalExpression(arg0);
        } catch (Throwable e) {
            handleError(e, 1);
        }
        Term val1 = null;
        try {
            val1 = evalExpression(arg1);
        } catch (Throwable e) {
            handleError(e, 2);
        }
        if (!(val0 instanceof NumberTerm))
            throw PrologError.type_error(prolog, 1,
                    "evaluable", arg0.term());
        if (!(val1 instanceof NumberTerm))
            throw PrologError.type_error(prolog, 2,
                    "evaluable", arg1.term());
        return expression_less_than((NumberTerm) val0,
                (NumberTerm) val1);
    }

    public boolean expression_greater_or_equal_than_2(Term arg0, Term arg1)
            throws PrologError {
        not_var(arg0, 1);
        not_var(arg1, 2);
        Term val0 = null;
        try {
            val0 = evalExpression(arg0);
        } catch (Throwable e) {
            handleError(e, 1);
        }
        Term val1 = null;
        try {
            val1 = evalExpression(arg1);
        } catch (Throwable e) {
            handleError(e, 2);
        }
        if (!(val0 instanceof NumberTerm))
            throw PrologError.type_error(prolog, 1,
                    "evaluable", arg0.term());
        if (!(val1 instanceof NumberTerm))
            throw PrologError.type_error(prolog, 2,
                    "evaluable", arg1.term());
        return !expression_less_than((NumberTerm) val0,
                (NumberTerm) val1);
    }

    private static boolean expression_less_than(NumberTerm num0,
                                                NumberTerm num1) {
        return num0.isInteger() && num1.isInteger() ? num0.longValue() < num1.longValue() : num0.doubleValue() < num1.doubleValue();
    }

    public static boolean term_equality_2(Term arg0, Term arg1) {
        arg0 = arg0.term();
        arg1 = arg1.term();
        return arg0.isEqual(arg1);
    }

    public static boolean term_greater_than_2(Term arg0, Term arg1) {
        arg0 = arg0.term();
        arg1 = arg1.term();

        return arg0.isGreater(arg1);
    }

    public static boolean term_less_than_2(Term arg0, Term arg1) {
        arg0 = arg0.term();
        arg1 = arg1.term();
        return !(arg0.isGreater(arg1) || arg0.isEqual(arg1));
    }

    public Term expression_plus_1(Term arg0) {
        Term val0 = null;
        try {
            val0 = evalExpression(arg0);
        } catch (Throwable e) {

        }
        return val0 instanceof NumberTerm ? val0 : null;
    }

    public Term expression_minus_1(Term arg0) {
        Term val0 = null;
        try {
            val0 = evalExpression(arg0);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n) {
            if (val0n instanceof NumberTerm.Int) {
                return new NumberTerm.Int(val0n.intValue() * -1);
            } else if (val0n instanceof NumberTerm.Double) {
                return new NumberTerm.Double(val0n.doubleValue() * -1);
            } else if (val0n instanceof NumberTerm.Long) {
                return new NumberTerm.Long(val0n.longValue() * -1);
            } else if (val0n instanceof NumberTerm.Float) {
                return new NumberTerm.Float(val0n.floatValue() * -1);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Term expression_bitwise_not_1(Term arg0) {
        Term val0 = null;
        try {
            val0 = evalExpression(arg0);
        } catch (Throwable e) {

        }
        return val0 instanceof NumberTerm ? new NumberTerm.Long(~((NumberTerm) val0).longValue()) : null;
    }

    static NumberTerm getIntegerNumber(long num) {
        return num > Integer.MIN_VALUE && num < Integer.MAX_VALUE ? new NumberTerm.Int((int) num) : new NumberTerm.Long(num);
    }

    public Term expression_plus_2(Term arg0, Term arg1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(arg0);
            val1 = evalExpression(arg1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n
                && (val1 instanceof NumberTerm val1n)) {
            return val0n.isInteger() && (val1n.isInteger()) ? getIntegerNumber(val0n.longValue() + val1n.longValue()) : new NumberTerm.Double(val0n.doubleValue()
                    + val1n.doubleValue());
        } else
            return null;
    }

    public Term expression_minus_2(Term arg0, Term arg1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(arg0);
            val1 = evalExpression(arg1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n
                && (val1 instanceof NumberTerm val1n)) {
            return val0n.isInteger() && (val1n.isInteger()) ? getIntegerNumber(val0n.longValue() - val1n.longValue()) : new NumberTerm.Double(val0n.doubleValue()
                    - val1n.doubleValue());
        } else
            return null;
    }

    public Term expression_multiply_2(Term arg0, Term arg1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(arg0);
            val1 = evalExpression(arg1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n
                && (val1 instanceof NumberTerm val1n)) {
            return val0n.isInteger() && (val1n.isInteger()) ? getIntegerNumber(val0n.longValue() * val1n.longValue()) : new NumberTerm.Double(val0n.doubleValue()
                    * val1n.doubleValue());
        } else
            return null;
    }

    public Term expression_div_2(Term arg0, Term arg1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(arg0);
            val1 = evalExpression(arg1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n
                && val1 instanceof NumberTerm val1n) {
            return val0n.isInteger() && val1n.isInteger() ? getIntegerNumber(val0n.longValue() / val1n.longValue()) : new NumberTerm.Double(val0n.doubleValue()
                    / val1n.doubleValue());
        } else
            return null;
    }

    public Term expression_integer_div_2(Term arg0, Term arg1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(arg0);
            val1 = evalExpression(arg1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n
                && (val1 instanceof NumberTerm val1n)) {
            return getIntegerNumber(val0n.longValue() / val1n.longValue());
        } else {
            return null;
        }
    }

    public Term expression_pow_2(Term arg0, Term arg1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(arg0);
            val1 = evalExpression(arg1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n
                && (val1 instanceof NumberTerm val1n)) {
            return new NumberTerm.Double(Math.pow(val0n.doubleValue(),
                    val1n.doubleValue()));
        } else {
            return null;
        }
    }

    public Term expression_bitwise_shift_right_2(Term arg0, Term arg1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(arg0);
            val1 = evalExpression(arg1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n
                && val1 instanceof NumberTerm val1n) {


            return new NumberTerm.Long(val0n.longValue() >> val1n.longValue());
        } else {
            return null;
        }
    }

    public Term expression_bitwise_shift_left_2(Term arg0, Term arg1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(arg0);
            val1 = evalExpression(arg1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n && val1 instanceof NumberTerm val1n) {


            return new NumberTerm.Long(val0n.longValue() << val1n.longValue());
        } else {
            return null;
        }
    }

    public Term expression_bitwise_and_2(Term arg0, Term arg1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(arg0);
            val1 = evalExpression(arg1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n
                && val1 instanceof NumberTerm val1n) {


            return new NumberTerm.Long(val0n.longValue() & val1n.longValue());
        } else {
            return null;
        }
    }

    public Term expression_bitwise_or_2(Term arg0, Term arg1) {
        Term val0 = null;
        Term val1 = null;
        try {
            val0 = evalExpression(arg0);
            val1 = evalExpression(arg1);
        } catch (Throwable e) {

        }
        if (val0 instanceof NumberTerm val0n
                && val1 instanceof NumberTerm val1n) {


            return new NumberTerm.Long(val0n.longValue() | val1n.longValue());
        } else {
            return null;
        }
    }





    /**
     * bidirectional text/term conversion.
     */
    public boolean text_term_2(Term arg0, Term arg1) {
        arg0 = arg0.term();
        arg1 = arg1.term();
        /*System.out.println(arg0);
        System.out.println(arg1);*/
        prolog.output(arg0.toString() +
                '\n' + arg1);
        if (!arg0.isGround()) {
            return unify(arg0, new Struct(arg1.toString()));
        } else {
            try {
                String text = Tools.removeApostrophes(arg0.toString());
                return unify(arg1, prolog.toTerm(text));
            } catch (Exception ex) {
                return false;
            }
        }
    }

    public boolean text_concat_3(Term source1, Term source2, Term dest)
            throws PrologError {
        source1 = source1.term();
        source2 = source2.term();
        dest = dest.term();
        if (source1 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (source2 instanceof Var)
            throw PrologError.instantiation_error(prolog, 2);
        if (!source1.isAtomic())
            throw PrologError.type_error(prolog, 1, "atom",
                    source1);
        if (!source2.isAtomic())
            throw PrologError.type_error(prolog, 2, "atom",
                    source2);
        return unify(dest, new Struct(((Struct) source1).name()
                + ((Struct) source2).name()));
    }

    public boolean num_atom_2(Term arg0, Term arg1) throws PrologError {
        arg0 = arg0.term();
        arg1 = arg1.term();
        if (arg1 instanceof Var) {
            if (!(arg0 instanceof NumberTerm n0)) {
                throw PrologError.type_error(prolog, 1,
                        "number", arg0);
            }
            String st = n0.isInteger() ? Integer.valueOf(n0.intValue()).toString() : Double.toString(n0.doubleValue());
            return (unify(arg1, new Struct(st)));
        } else {
            if (!arg1.isAtomic()) {
                throw PrologError.type_error(prolog, 2,
                        "atom", arg1);
            }
            String st = ((Struct) arg1).name();
            String st2="";
            int stl = st.length();
            for(int i = 0; i< stl; i++)
            {
                st2+=st.charAt(i);

                if (st.charAt(0)=='0' && st.charAt(1)==39 && st.charAt(2)==39 && stl ==4) {
                    st2 = String.valueOf(String.valueOf(st.charAt(3)).getBytes()[0]);
                } else if (st.charAt(0)=='0' && st.charAt(1)=='x' && st.charAt(2)>='a' && st.charAt(2)<='f' && stl ==3) {
                    st2 = String.valueOf(Integer.parseInt(String.valueOf(st.charAt(2)), 16));
                }
            }
            boolean before=true;
            boolean after=false;
            boolean between;
            int numBefore=0;
            int numAfter=0;
            int numBetween=0;
            String iBetween="";
            for(int i=0; i<st2.length(); i++)
            {
                if((st2.charAt(i)<'0' || st2.charAt(i)>'9') && before)
                    numBefore++;

                between=false;
                if(st2.charAt(i)>='0' && st2.charAt(i)<='9')
                {
                    int k=0;
                    for(int j=i+1; j<st2.length(); j++)
                    {
                        if(st2.charAt(j)>='0' && st2.charAt(j)<='9' && j-i>1)
                        {
                            k+=i+1;
                            numBetween+=2;
                            iBetween= String.valueOf(k) + j;
                            i+=j;
                            j=st2.length();
                            between=true;
                        }
                        else if(st2.charAt(j)>='0' && st2.charAt(j)<='9' && j-i==1)
                            k++;
                    }

                    if (!between)
                    {
                        before=false;
                        after=true;
                    }
                    else
                        before=false;
                }

                if((st2.charAt(i)<'0' || st2.charAt(i)>'9') && after)
                    numAfter++;
            }
            String st3 = null;
            for(int i = 0; i<numBefore; i++)
            {
                if (st2.charAt(i)==' ')
                    st3=st2.substring(i+1);
                else if (st2.charAt(i)=='\\' && (st2.charAt(i+1)=='n' || st2.charAt(i+1)=='t'))
                {
                    st3=st2.substring(i+2);
                    i++;
                }
                else if (st2.charAt(i)!='-' && st2.charAt(i)!='+')
                    st3="";
            }
            for(int i=0; i<numBetween; i+=2)
            {
                for(int j = Integer.parseInt(String.valueOf(iBetween.charAt(i))); j< Integer.parseInt(String.valueOf(iBetween.charAt(i + 1))); j++)
                {
                    char st2i = st2.charAt(i);
                    if (st2.charAt(j) != '.' && st2i != 'E' && st2i != 'e')
                    {
                        st3="";
                    }
                }
            }
            for(int i=0; i<numAfter; i++)
            {
                boolean b = st2.charAt(i + 1) != '+' || st2.charAt(i + 1) != '-';
                if ((st2.charAt(i) != 'E' || (st2.charAt(i) != 'E' && b)) && st2.charAt(i) != '.' && (st2.charAt(i) != 'e' || (st2.charAt(i) != 'e' && b))) {
                    st3 = "";
                    break;
                }
            }
            if (st3!=null)
                st2=st3;
            Term term = null;
            try {
                term = new NumberTerm.Int(Integer.parseInt(st2));
            } catch (Exception ex) {
            }
            if (term == null) {
                try {
                    term = new NumberTerm.Double(Double
                            .parseDouble(st2));
                } catch (Exception ex) {
                }
            }
            if (term == null) {
                throw PrologError.domain_error(prolog, 2,
                        "num_atom", arg1);
            }
            return (unify(arg0, term));
        }
    }


    public static boolean throw_1(Term error) throws PrologError {
        throw new PrologError(error);
    }

    @Override
    public String getTheory() {
        return



                "':-'(op( 1200, fx,   ':-')). \n"
                        + ":- op( 1200, xfx,  ':-'). \n"
                        + ":- op( 1200, fx,   '?-'). \n"
                        + ":- op( 1100, xfy,  ';'). \n"
                        + ":- op( 1050, xfy,  '->'). \n"
                        + ":- op( 1000, xfy,  ','). \n"
                        + ":- op(  900, fy,   '\\+'). \n"
                        + ":- op(  900, fy,   'not'). \n"
                        +

                        ":- op(  700, xfx,  '='). \n"
                        + ":- op(  700, xfx,  '\\='). \n"
                        + ":- op(  700, xfx,  '=='). \n"
                        + ":- op(  700, xfx,  '\\=='). \n"

                        + ":- op(  700, xfx,  '@>'). \n"
                        + ":- op(  700, xfx,  '@<'). \n"
                        + ":- op(  700, xfx,  '@=<'). \n"
                        + ":- op(  700, xfx,  '@>='). \n"
                        + ":- op(  700, xfx,  '=:='). \n"
                        + ":- op(  700, xfx,  '=\\='). \n"
                        + ":- op(  700, xfx,  '>'). \n"
                        + ":- op(  700, xfx,  '<'). \n"
                        + ":- op(  700, xfx,  '=<'). \n"
                        + ":- op(  700, xfx,  '>='). \n"
                        +

                        ":- op(  700, xfx,  'is'). \n"
                        + ":- op(  700, xfx,  '=..'). \n"
                        + ":- op(  500, yfx,  '+'). \n"
                        + ":- op(  500, yfx,  '-'). \n"
                        + ":- op(  500, yfx,  '/\\'). \n"
                        + ":- op(  500, yfx,  '\\/'). \n"
                        + ":- op(  400, yfx,  '*'). \n"
                        + ":- op(  400, yfx,  '/'). \n"
                        + ":- op(  400, yfx,  '//'). \n"
                        + ":- op(  400, yfx,  '>>'). \n"
                        + ":- op(  400, yfx,  '<<'). \n"
                        + ":- op(  400, yfx,  'rem'). \n"
                        + ":- op(  400, yfx,  'mod'). \n"
                        + ":- op(  200, xfx,  '**'). \n"
                        + ":- op(  200, xfy,  '^'). \n"
                        + ":- op(  200, fy,   '\\'). \n"
                        + ":- op(  200, fy,   '-'). \n"

                        //
                        // flag management
                        //
                        + "current_prolog_flag(Name,Value) :- catch(get_prolog_flag(Name,Value), Error, false),!.\n"
                        + "current_prolog_flag(Name,Value) :- flag_list(L), member(flag(Name,Value),L).\n"
                        +
                        //
                        // espression/term comparison
                        //
                          "'=:='(X,Y):- expression_equality(X,Y). \n"
                        + "'=\\='(X,Y):- not expression_equality(X,Y). \n"
                        + "'>'(X,Y):- expression_greater_than(X,Y). \n"
                        + "'<'(X,Y):- expression_less_than(X,Y). \n"
                        + "'>='(X,Y):- expression_greater_or_equal_than(X,Y). \n"
                        + "'=<'(X,Y):- expression_less_or_equal_than(X,Y). \n"
                        + "'=='(X,Y):- term_equality(X,Y).\n"
                        + "'\\=='(X,Y):- not term_equality(X,Y).\n"
                        + "'@>'(X,Y):- term_greater_than(X,Y).\n"
                        + "'@<'(X,Y):- term_less_than(X,Y).\n"
                        + "'@>='(X,Y):- not term_less_than(X,Y).\n"
                        + "'@=<'(X,Y):- not term_greater_than(X,Y).\n"

                        //
                        // meta-predicates
                        //
                        +"'=..'(T, [T]) :- atomic(T), !. \n                                                          "
                        + "'=..'(T,L)  :- compound(T),!, '$tolist'(T,L). \n                                                          "
                        + "'=..'(T,L)  :- nonvar(L), catch('$fromlist'(T,L),Error,false). \n                                                          "

                /* commented by Roberta Calegari 29/04/14 it causes a loop: functor bug issue 14 google code, rewritten functor predicate
                + "functor(Term, Name, Arity) :- atomic(Term), !, Name = Term, Arity = 0. \n"
                + "functor(Term, Name, Arity) :- compound(Term), !, Term =.. [Name | Args], length(Args, Arity). \n"
                + "functor(Term, Name, Arity) :- var(Term), atomic(Name), Arity == 0, !, Term = Name. \n"
                + "functor(Term, Name, Arity) :- var(Term), atom(Name), integer(Arity), Arity > 0, current_prolog_flag(max_arity, Max), Arity=<Max, !, newlist([], Arity, L), Term =.. [Name | L]. \n"
                */
                        + "functor(Term, Functor, Arity) :- \\+ var(Term), !, Term =.. [Functor|ArgList],length(ArgList, Arity).\n"
                        + "functor(Term, Functor, Arity) :- var(Term), atomic(Functor), Arity == 0, !, Term = Functor.\n"
                        + "functor(Term, Functor, Arity) :- var(Term), current_prolog_flag(max_arity, Max), Arity>Max, !, false.\n"
                        + "functor(Term, Functor, Arity) :- var(Term), atom(Functor), number(Arity), Arity > 0, !,length(ArgList, Arity),Term =.. [Functor|ArgList].\n"

                        + "functor(_Term, _Functor, _Arity) :-false.\n"


                        + "arg(N,C,T):- arg_guard(N,C,T), C =.. [_|Args], element(N,Args,T).\n"
                        + "clause(H, B) :- clause_guard(H,B), L = [], '$find'(H, L), copy_term(L, LC), member((':-'(H, B)), LC). \n"
                        +
                        //
                        // call/1 is coded both in Prolog, to feature the desired
                        // opacity
                        // to cut, and in Java as a primitive built-in, to account for
                        // goal transformations that should be performed before
                        // execution
                        // as mandated by ISO Standard, see section 7.8.3.1
                        "call(G) :- call_guard(G), '$call'(G). \n"
                        + "'\\+'(P):- P,!,fail.\n                                                                            "
                        + "'\\+'(_).\n                                                                                             "
                        + "C -> T ; B :- !, or((call(C), !, call(T)), '$call'(B)). \n"
                        + "C -> T :- call(C), !, call(T). \n"
                        + "or(A, B) :- '$call'(A). \n"
                        + "or(A, B) :- '$call'(B). \n"
                        + "A ; B :- A =.. ['->', C, T], !, ('$call'(C), !, '$call'(T) ; '$call'(B)). \n"
                        + "A ; B :- '$call'(A). \n"
                        + "A ; B :- '$call'(B). \n "

                        + "unify_with_occurs_check(X,Y):- "+ /*not_occurs_in(X,Y),*/"!,X=Y.\n"
                /*+ "not_occurs_in(X,Y) :- var(Y), X \\== Y.\n"
                + "not_occurs_in(X,Y) :- nonvar(Y), constant(Y).\n"
                + "not_occurs_in(X,Y) :- nonvar(Y), compound(Y), functor(Y,F,N), not_occurs_in(N,X,Y).\n"
                + "not_occurs_in(N,X,Y) :-	N > 0, arg(N,Y,Arg), not_occurs_in(X,Arg), N1 is N-1, not_occurs_in(N1,X,Y).\n"
                + "not_occurs_in(0,X,Y).\n"*/

                        + "current_op(Pri,Type,Name):-get_operators_list(L),member(op(Pri,Type,Name),L).\n                          "
                        + "once(X) :- myonce(X).\n                                                                                  "
                        + "myonce(X):-X,!.\n                                                                                        "
                        + "repeat. \n                                                                                              "
                        + "repeat        :- repeat. \n                                                                             "
                        + "not(G)        :- G,!,fail. \n                                                                     "
                        + "not(_). \n"


                        //+ "catch(Goal, Catcher, Handler) :- call(Goal).\n"

                        + "findall(Template, Goal, Instances) :- \n"
                        + "all_solutions_predicates_guard(Template, Goal, Instances), \n"
                        + "L = [], \n"
                        + "'$findall0'(Template, Goal, L), \n"
                        + "Instances = L. \n"
                        + "'$findall0'(Template, Goal, L) :- \n"
                        + "call(Goal), \n"
                        + "copy_term(Template, CL), \n"
                        + "'$append'(CL, L), \n"
                        + "fail. \n"
                        + "'$findall0'(_, _, _). \n"

                        + "variable_set(T, []) :- atomic(T), !. \n"
                        + "variable_set(T, [T]) :- var(T), !. \n"
                        + "variable_set([H | T], [SH | ST]) :- \n"
                        + "variable_set(H, SH), variable_set(T, ST). \n"
                        + "variable_set(T, S) :- \n"
                        + "T =.. [_ | Args], variable_set(Args, L), flatten(L, FL), no_duplicates(FL, S), !. \n"
                        + "flatten(L, FL) :- '$flatten0'(L, FL), !. \n"
                        + "'$flatten0'(T, []) :- nonvar(T), T = []. \n"
                        + "'$flatten0'(T, [T]) :- var(T). \n"
                        + "'$flatten0'([H | T], [H | FT]) :- \n"
                        + "not(islist(H)), !, '$flatten0'(T, FT). \n"
                        + "'$flatten0'([H | T], FL) :- \n"
                        + "'$flatten0'(H, FH), '$flatten0'(T, FT), append(FH, FT, FL). \n"
                        + "islist([]). \n"
                        + "islist([_|L]):- islist(L). \n "
                        + "existential_variables_set(Term, Set) :- '$existential_variables_set0'(Term, Set), !. \n"
                        + "'$existential_variables_set0'(Term, []) :- var(Term), !. \n"
                        + "'$existential_variables_set0'(Term, []) :- atomic(Term), !. \n"
                        + "'$existential_variables_set0'(V ^ G, Set) :- \n"
                        + "variable_set(V, VS), '$existential_variables_set0'(G, EVS), append(VS, EVS, Set). \n"
                        + "'$existential_variables_set0'(Term, []) :- nonvar(Term), !. \n"
                        + "free_variables_set(Term, WithRespectTo, Set) :- \n"
                        + "variable_set(Term, VS), \n"
                        + "variable_set(WithRespectTo, VS1), existential_variables_set(Term, EVS1), append(VS1, EVS1, BV), \n"
                        + "list_difference(VS, BV, List), no_duplicates(List, Set), !. \n"
                        + "list_difference(List, Subtrahend, Difference) :- '$ld'(List, Subtrahend, Difference). \n"
                        + "'$ld'([], _, []). \n"
                        + "'$ld'([H | T], S, D) :- is_member(H, S), !, '$ld'(T, S, D). \n"
                        + "'$ld'([H | T], S, [H | TD]) :- '$ld'(T, S, TD). \n"
                        + "no_duplicates([], []). \n"
                        + "no_duplicates([H | T], L) :- is_member(H, T), !, no_duplicates(T, L). \n"
                        + "no_duplicates([H | T], [H | L]) :- no_duplicates(T, L). \n"
                        + "is_member(E, [H | _]) :- E == H, !. \n"
                        + "is_member(E, [_ | T]) :- is_member(E, T). \n"
                        + "'$wt_list'([], []). \n"
                        + "'$wt_list'([W + T | STail], [WW + T | WTTail]) :- copy_term(W, WW), '$wt_list'(STail, WTTail). \n"
                        + "'$s_next'(Witness, WT_List, S_Next) :- copy_term(Witness, W2), '$s_next0'(W2, WT_List, S_Next), !. \n"

                        /*Roberta Calegari Sep 2013*/
                        +"bagof(Template, Goal, Instances) :- \n"
                        + "all_solutions_predicates_guard(Template, Goal, Instances), \n"
                        + "free_variables_set(Goal, Template, Set), \n"
                        + "Witness =.. [witness | Set], \n"
                        + "iterated_goal_term(Goal, G), \n"
                        +"all_solutions_predicates_guard(Template, G, Instances),"
                        + "'splitAndSolve'(Witness, S, Instances,Set,Template,G,Goal). \n"




                        /*INIT utility function used by bagof*/
                        +"count([],0). \n"
                        +"count([T1|Ts],N):- count(Ts,N1), N is (N1+1). \n"
                        +"is_list(X) :- var(X), !,fail. \n"
                        +"is_list([]). \n"
                        +"is_list([_|T]) :- is_list(T). \n"
                        +"list_to_term([T1|Ts],N):- count(Ts,K),K==0 \n"
                        +"->  N = T1,! \n"
                        +"; list_to_term(Ts,N1), N = ';'(T1,N1),!. \n"
                        +"list_to_term(Atom,Atom). \n"
                        +"quantVar(X^Others, [X|ListOthers]) :- !,quantVar(Others, ListOthers). \n"
                        +"quantVar(_Others, []). \n"
                        +"'splitAndSolve'(Witness, S, Instances,Set,Template,G,Goal):- splitSemicolon(G,L), \n"
                        +"variable_set(Template,TT), \n"
                        +"quantVar(Goal, Qvars), \n"
                        +"append(TT,Qvars,L1), \n"
                        +"'aggregateSubgoals'(L1,L,OutputList), \n"
                        +"member(E,OutputList), \n"
                        +"list_to_term(E, GoalE), \n"
                        +"findall(Witness + Template, GoalE, S), \n"
                        +"'bag0'(Witness, S, Instances,Set,Template,GoalE). \n"

                        +"splitSemicolon(';'(G1,Gs),[G1|Ls]) :-!, splitSemicolon(Gs,Ls). \n"
                        +"splitSemicolon(G1,[G1]). \n"
                        +"aggregateSubgoals(Template, List, OutputList) :- \n"
                        +"aggregateSubgoals(Template, List, [], [], OutputList). \n"
                        +"aggregateSubgoals(Template, [H|T], CurrentAccumulator, Others, OutputList) :- \n"
                        +"'occurs0'(Template, H) \n"
                        +"-> aggregateSubgoals(Template, T, [H|CurrentAccumulator], Others, OutputList) \n"
                        +"; aggregateSubgoals(Template, T, CurrentAccumulator, [H|Others], OutputList). \n"
                        +"aggregateSubgoals(_, [], CurrentAccumulator, NonAggregatedList, [Result1|Result2]):- \n"
                        +"reverse(CurrentAccumulator, Result1), reverse(NonAggregatedList, Result2). \n"
                        +"occurs_member_list([], L):-!,fail. \n"
                        +"occurs_member_list([H|T], L) :- is_member(H,L) \n"
                        +"-> true,! \n"
                        +"; occurs_member_list(T,L). \n"
                        +"occurs_member_list_of_term(L, []):-!,fail. \n"
                        +"occurs_member_list_of_term(Template,[H|T]):-'occurs0'(Template, H) \n"
                        +"-> true,! \n"
                        +"; occurs_member_list_of_term(Template,T). \n"
                        +"'check_sub_goal'(Template, H, _Functor, Arguments):- ((_Functor==';';_Functor==',') \n"
                        +"-> 'occurs0'(Template,Arguments) \n"
                        +"; ((_Functor=='.') \n"
                        +"->  occurs_member_list_of_term(Template,Arguments) \n"
                        +"; occurs_member_list(Template, Arguments))). \n"
                        +"'occurs0'(Template, H):- \n"
                        +"H =.. [_Functor | Arguments], \n"
                        +"'check_sub_goal'(Template, H, _Functor, Arguments). \n"
                        +"'bag0'(_, [], _,_,_,_) :- !, fail. \n"
                        +"'bag0'(Witness, S, Instances,Set,Template,Goal) :- \n"
                        +"S==[] -> fail, !; \n"
                        +"'$wt_list'(S, WT_List), \n"
                        +"'$wt_unify'(Witness, WT_List, Instances,Set,Template,Goal). \n"




                        +"'bag0'(Witness, S, Instances,Set,Template,Goal) :- \n"
                        +"'$wt_list'(S, WT_List), \n"
                        +"'$s_next'(Witness, WT_List, S_Next),  \n"
                        +"'bag0'(Witness, S_Next, Instances,Set,Template,Goal). \n"

                        /*END utility function used by bagof*/
                /*+ "bagof(Template, Goal, Instances) :- \n"
                + "all_solutions_predicates_guard(Template, Goal, Instances), \n"
                + "free_variables_set(Goal, Template, Set), \n"
                + "Witness =.. [witness | Set], \n"
                + "iterated_goal_term(Goal, G), \n"
                + "findall(Witness + Template, G, S), \n"
                + "'$bagof0'(Witness, S, Instances). \n"
                + "'$bagof0'(_, [], _) :- !, fail. \n"
                + "'$bagof0'(Witness, S, Instances) :- \n"
                + "'$wt_list'(S, WT_List), \n"
                + "'$wt_unify'(Witness, WT_List, T_List), \n"
                + "Instances = T_List. \n"
                + "'$bagof0'(Witness, S, Instances) :- \n"
                + "'$wt_list'(S, WT_List), \n"
                + "'$s_next'(Witness, WT_List, S_Next), \n"
                + "'$bagof0'(Witness, S_Next, Instances). \n"*/

                        + "setof(Template, Goal, Instances) :- \n"
                        + "all_solutions_predicates_guard(Template, Goal, Instances), \n"
                        + "bagof(Template, Goal, List), \n"
                        + "quicksort(List, '@<', OrderedList), \n"
                        + "no_duplicates(OrderedList, Instances). \n"


                        +"forall(A,B):- \\+(call(A),\\+call(B)). \n"




                        + "assert(C) :- assertz(C). \n"
//            /*
//                + "retract(Rule) :- retract_guard(Rule), Rule = ':-'(Head, Body), !, clause(Head, Body), '$retract'(Rule). \n"
//                + "retract(Fact) :- retract_guard(Fact), clause(Fact, true), '$retract'(Fact).\n"
//             */
                        + "retract(Clause) :-     clause(Head, _),     '$retract'(Clause).\n"
                        + "retractall(Head) :- retract_guard(Head), findall(':-'(Head, Body), clause(Head, Body), L), '$retract_clause_list'(L), !.\n"
                        + "'$retract_clause_list'([]). \n"
                        + "'$retract_clause_list'([E | T]) :- !, '$retract'(E), '$retract_clause_list'(T). \n"
                        +




                        " member(E,L) :- member_guard(E,L), member0(E,L).\n                                                                                     "
                        + "member0(E,[E|_]). \n                                                                       "
                        + "member0(E,[_|L]):- member0(E,L). \n                                                                       "
                        + "length(L, S) :- number(S), !, lengthN(L, S), !. \n"
                        + "length(L, S) :- var(S), lengthX(L, S). \n"
                        + "lengthN([],0). \n"
                        + "lengthN(_, N) :- N < 0, !, fail. \n"
                        + "lengthN([_|L], N) :- M is N - 1, lengthN(L,M). \n"
                        + "lengthX([],0). \n"
                        + "lengthX([_|L], N) :- lengthX(L,M), N is M + 1. \n"
                        + "append([],L2,L2). \n                                                                                    "
                        + "append([E|T1],L2,[E|T2]):- append(T1,L2,T2). \n                                                         "
                        + "reverse(L1,L2):- reverse_guard(L1,L2), reverse0(L1,[],L2). \n                                                                 "
                        + "reverse0([],Acc,Acc). \n                                                                                "
                        + "reverse0([H|T],Acc,Y):- reverse0(T,[H|Acc],Y). \n                                                       "
                        + "delete(E,S,D) :- delete_guard(E,S,D), delete0(E,S,D). \n                                                                                     "
                        + "delete0(E,[],[]). \n                                                                                     "
                        + "delete0(E,[E|T],L):- !,delete0(E,T,L). \n                                                                 "
                        + "delete0(E,[H|T],[H|L]):- delete0(E,T,L). \n                                                               "
                        + "element(P,L,E):- element_guard(P,L,E), element0(P,L,E). \n                                                                              "
                        + "element0(1,[E|L],E):- !. \n                                                                              "
                        + "element0(N,[_|L],E):- M is N - 1,element0(M,L,E). \n                                                      "
                /* commented by Roberta Calegari 29/04/14 it causes a loop
                + "newlist(Ls,0,Ls):- !. \n                                                                                "
                + "newlist(Ls,N,Ld):- !, append(X,Ls,Ld), length(X,N). \n                                                                                "
                + "newlist(Ls,N,Ld):- M is N - 1,newlist([_|Ls],M,Ld). \n                                                  "
                */
                        + "quicksort([],Pred,[]).                             \n"
                        + "quicksort([X|Tail],Pred,Sorted):-                  \n"
                        + "   split(X,Tail,Pred,Small,Big),                   \n"
                        + "   quicksort(Small,Pred,SortedSmall),              \n"
                        + "   quicksort(Big,Pred,SortedBig),                  \n"
                        + "   append(SortedSmall,[X|SortedBig],Sorted).       \n"
                        + "split(_,[],_,[],[]).                               \n"
                        + "split(X,[Y|Tail],Pred,Small,[Y|Big]):-             \n"
                        + "   Predicate =..[Pred,X,Y],                        \n"
                        + "   call(Predicate),!,                              \n"
                        + "   split(X,Tail,Pred,Small,Big).                   \n"
                        + "split(X,[Y|Tail],Pred,[Y|Small],Big):-             \n"
                        + "   split(X,Tail,Pred,Small,Big).                   \n";
    }



    public boolean arg_guard_3(Term arg0, Term arg1, Term arg2)
            throws PrologError {
        arg0 = arg0.term();
        arg1 = arg1.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (arg1 instanceof Var)
            throw PrologError.instantiation_error(prolog, 2);
        if (!(arg0 instanceof NumberTerm.Int arg0int))
            throw PrologError.type_error(prolog, 1,
                    "integer", arg0);
        if (!arg1.isCompound())
            throw PrologError.type_error(prolog, 2,
                    "compound", arg1);
        if (arg0int.intValue() < 1)
            throw PrologError.domain_error(prolog, 1,
                    "greater_than_zero", arg0);
        return true;
    }

    public final boolean clause_guard_2(Term arg0, Term arg1) throws PrologError {
        arg0 = arg0.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        return true;
    }

    public final boolean call_guard_1(Term arg0) throws PrologError {
        arg0 = arg0.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (!arg0.isAtomic() && !arg0.isCompound())
            throw PrologError.type_error(prolog, 1,
                    "callable", arg0);
        return true;
    }

    public boolean all_solutions_predicates_guard_3(Term arg0, Term arg1,
                                                    Term arg2) throws PrologError {

        arg1 = arg1.term();

        if (arg1 instanceof Var){

            throw PrologError.instantiation_error(prolog, 2);
        }
        if (!arg1.isAtomic() && !arg1.isCompound()){

            throw PrologError.type_error(prolog, 2,
                    "callable", arg1);
        }
        return true;
    }

    public boolean retract_guard_1(Term arg0) throws PrologError {
        arg0 = arg0.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (!(arg0 instanceof Struct))
            throw PrologError.type_error(prolog, 1,
                    "clause", arg0);
        return true;
    }

    public boolean member_guard_2(Term arg0, Term arg1) throws PrologError {
        arg1 = arg1.term();
        if (!(arg1 instanceof Var) && !(arg1.isList()))
            throw PrologError.type_error(prolog, 2, "list",
                    arg1);
        return true;
    }

    public boolean reverse_guard_2(Term arg0, Term arg1) throws PrologError {
        arg0 = arg0.term();
        if (!(arg0 instanceof Var) && !(arg0.isList()))
            throw PrologError.type_error(prolog, 1, "list",
                    arg0);
        return true;
    }

    public boolean delete_guard_3(Term arg0, Term arg1, Term arg2)
            throws PrologError {
        arg1 = arg1.term();
        if (!(arg1 instanceof Var) && !(arg1.isList()))
            throw PrologError.type_error(prolog, 2, "list",
                    arg1);
        return true;
    }

    public boolean element_guard_3(Term arg0, Term arg1, Term arg2)
            throws PrologError {
        arg1 = arg1.term();
        if (!(arg1 instanceof Var) && !(arg1.isList()))
            throw PrologError.type_error(prolog, 2, "list",
                    arg1);
        return true;
    }




    public boolean $wt_unify_3(Term witness, Term wtList, Term tList) {
        Struct list = (Struct) wtList.term();
        Struct result = Struct.emptyList();
        for (Iterator<? extends Term> it = list.listIterator(); it.hasNext();) {
            Struct e = (Struct) it.next();
            if (unify(witness, e.sub(0)))
                result.append(e.sub(1));
        }
        return unify(tList, result);
    }

    public boolean $wt_unify_6(Term witness, Term wtList, Term tList, Term varSet,Term temp, Term goal) {
    	/*System.out.println("witness "+witness);
    	System.out.println("wtList "+wtList);
    	System.out.println("tList "+tList+" "+((Var)tList).getLink());
    	System.out.println("varSet "+varSet);
    	System.out.println("template "+temp);
    	*/

        Struct freeVarList = (Struct) varSet.term();
        Iterator<? extends Term> it1 = freeVarList.listIterator();
        if (it1.hasNext()) {
            prolog.relinkVar(true);


            prolog.setBagOFvarSet(varSet);
            prolog.setBagOFgoal(goal);
            if (goal /*(engine.engine).getBagOFbag()*/==null)
                prolog.setBagOFbag(tList);

        }




        Struct list = (Struct) wtList.term();





        Struct result = Struct.emptyList();
        for (Iterator<? extends Term> it = list.listIterator(); it.hasNext();) {
            Struct element = (Struct) it.next();

            Term w = element.sub(0);
            Term t = element.sub(1);


            if (unify(witness, w)){

                result.append(t);

                List<Term> l = prolog.getBagOFres();
                List<String> lString = prolog.getBagOFresString();
                if(l==null){
                    l= new Lst<>();
                    lString= new Lst<>();
                }
                l.add(t);

                lString.add(t.toString());
            	/*for(int m=0; m<l.size(); m++){
            		System.out.println("=====****elemento lista engine.getEngineManager()).getBagOFres()  "+l.get(m));
            	}*/
                prolog.setBagOFres(l);
                prolog.setBagOFresString(lString);

            }
        }
        return unify(tList, result);
    }


    public boolean $s_next0_3(Term witness, Term wtList, Term sNext) {
        Struct list = (Struct) wtList.term();
        Struct result = Struct.emptyList();
        for (Iterator<? extends Term> it = list.listIterator(); it.hasNext();) {
            Struct element = (Struct) it.next();
            Term w = element.sub(0);
            if (!unify(witness, w))
                result.append(element);
        }
        return unify(sNext, result);
    }

    public boolean iterated_goal_term_2(Term term, Term goal) {
        Term t = term.term();
        Term igt = t.iteratedGoalTerm();
        return unify(igt, goal);
    }

    /**
     * Defines some synonyms
     */
    @Override
    public String[][] buildSynonyms() {
        return new String[][] { { "+", "expression_plus", "functor" },
                { "-", "expression_minus", "functor" },
                { "*", "expression_multiply", "functor" },
                { "/", "expression_div", "functor" },
                { "**", "expression_pow", "functor" },
                { ">>", "expression_bitwise_shift_right", "functor" },
                { "<<", "expression_bitwise_shift_left", "functor" },
                { "/\\", "expression_bitwise_and", "functor" },
                { "\\/", "expression_bitwise_or", "functor" },
                { "//", "expression_integer_div", "functor" },
                { "\\", "expression_bitwise_not", "functor" } };
    }

}