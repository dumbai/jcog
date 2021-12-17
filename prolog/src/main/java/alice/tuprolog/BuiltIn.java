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
package alice.tuprolog;

import alice.util.Tools;
import jcog.util.ArrayUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

import static alice.tuprolog.PrologPrim.PREDICATE;

/**
 * Library of built-in predicates
 *
 * @author Alex Benini
 */

public final class BuiltIn extends PrologLib {
    private final Theories theories;
    private final PrologLibraries libs;
    private final Flags flags;
    private final PrologPrimitives prims;
    private final PrologOperators ops;

    public BuiltIn(Prolog p) {
        super();
        setProlog(p);
        theories = p.theories;
        libs = p.libs;
        flags = p.flags;
        prims = p.prims;
        ops = p.ops;
    }

    /**
     * Defines some synonyms
     */
    @Override
    public String[][] buildSynonyms() {
        return new String[][]{{"!", "cut", "predicate"},
                {"=", "unify", "predicate"},
                {"\\=", "deunify", "predicate"},
                {",", "comma", "predicate"},
                {"op", "$op", "predicate"},
                {"solve", "initialization", "directive"},
                {"consult", "include", "directive"},
                {"load_library", "$load_library", "directive"}};
    }

    /*
     * PREDICATES
     */

    public static boolean fail_0() {
        return false;
    }

    public static boolean true_0() {
        return true;
    }

    /*Castagna 06/2011*/
	 /*
	public boolean halt_0() throws HaltException {
		throw new HaltException();
	}
	  */

    public static boolean halt_0() {
        System.exit(0);
        return true;
    }
    /**/

    public boolean cut_0() {
        prolog.cut();
        return true;
    }

    public boolean asserta_1(Term arg0) throws PrologError {
        arg0 = arg0.term();
        if (arg0 instanceof Struct ss) {

            if (":-".equals(ss.name())) {
                Iterator<Term> ssi = ss.listIterator();
                while (ssi.hasNext()) {
                    Term argi = ssi.next();
                    if (!(argi instanceof Struct)) {
                        if (argi instanceof Var)
                            throw PrologError.instantiation_error(prolog, 1);
                        else
                            throw PrologError.type_error(prolog, 1, "clause", arg0);
                    }
                }
            }
            theories.assertA(ss, null);
            return true;
        }
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        else
            throw PrologError.type_error(prolog, 1, "clause", arg0);
    }

    public boolean assertz_1(Term _arg0) throws PrologError {
        Term arg0 = _arg0.term();
        if (arg0 instanceof Struct ss) {
            if (":-".equals(ss.name())) {
                Iterator<Term> ssi = ss.listIterator();
                while (ssi.hasNext()) {
                    Term argi = ssi.next();
                    if (!(argi instanceof Struct)) {
                        if (argi instanceof Var)
                            throw PrologError.instantiation_error(prolog, 1);
                        else
                            throw PrologError.type_error(prolog, 1, "clause", arg0);
                    }
                }
            }
            theories.assertZ(ss, true, null, false);
            return true;
        }
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        else
            throw PrologError.type_error(prolog, 1, "clause", arg0);
    }

    public boolean $retract_1(Term arg1) throws PrologError {
        Term arg0 = arg1.term();

        if (!(arg0 instanceof Struct sarg0)) {
            if (arg0 instanceof Var)
                throw PrologError.instantiation_error(prolog, 1);
            else
                throw PrologError.type_error(prolog, 1, "clause", arg0);
        }

        boolean sClause = sarg0.isClause();


        theories.retract(sarg0, c -> unify(sClause ? sarg0 : new Struct(":-", arg0, new Struct("true")), c.clause));

        return true;
    }

    public boolean abolish_1(Term arg0) throws PrologError {
        arg0 = arg0.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (!(arg0 instanceof Struct) || !arg0.isGround())
            throw PrologError.type_error(prolog, 1, "predicate_indicator", arg0);

        if ("abolish".equals(((Struct) arg0).sub(0).toString()))
            throw PrologError.permission_error(prolog, "modify", "static_procedure", arg0, new Struct(""));

        return theories.abolish((Struct) arg0);
    }

    /*Castagna 06/2011*/
	 /*
	public boolean halt_1(Term arg0) throws HaltException, PrologError {
		if (arg0 instanceof Int)
			throw new HaltException(((Int) arg0).intValue());
		if (arg0 instanceof Var)
			throw PrologError.instantiation_error(engineManager, 1);
		else {
			throw PrologError.type_error(engineManager, 1, "integer", arg0);
		}
	}
	  */

    public boolean halt_1(Term arg0) throws PrologError {
        if (arg0 instanceof NumberTerm.Int)
            System.exit(((NumberTerm) arg0).intValue());
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        else {
            throw PrologError.type_error(prolog, 1, "integer", arg0);
        }
    }
    /**/

    /*
     * loads a tuprolog library, given its java class name
     */
    public boolean load_library_1(Term arg0) throws PrologError {
        arg0 = arg0.term();
        if (!arg0.isAtomic()) {
            if (arg0 instanceof Var)
                throw PrologError.instantiation_error(prolog, 1);
            else
                throw PrologError.type_error(prolog, 1, "atom", arg0);
        }
        try {
            libs.loadClass(((Struct) arg0).name());
            return true;
        } catch (Exception ex) {
            throw PrologError.existence_error(prolog, 1, "class", arg0,
                    new Struct(ex.getMessage()));
        }
    }

    /*
     * loads a tuprolog library, given its java class name and the list of the paths where may be contained
     */
    public boolean load_library_2(Term arg0, Term arg1) throws PrologError {
        arg0 = arg0.term();
        arg1 = arg1.term();
        if (!arg0.isAtomic()) {
            if (arg0 instanceof Var)
                throw PrologError.instantiation_error(prolog, 1);
            else
                throw PrologError.type_error(prolog, 1, "atom", arg0);
        }
        if (!arg1.isList()) {
            throw PrologError.type_error(prolog, 2, "list", arg1);
        }

        try {
            String[] paths = getStringArrayFromStruct((Struct) arg1);
            if (paths == null || paths.length == 0)
                throw PrologError.existence_error(prolog, 2, "paths", arg1, new Struct("Invalid paths' list."));
            libs.loadClass(((Struct) arg0).name(), paths);
            return true;

        } catch (Exception ex) {
            throw PrologError.existence_error(prolog, 1, "class", arg0,
                    new Struct(ex.getMessage()));
        }
    }

    private static String[] getStringArrayFromStruct(Struct list) {
        int i = list.listSize();
        if (i == 0)
            return ArrayUtil.EMPTY_STRING_ARRAY;

        String[] args = new String[i];
        Iterator<? extends Term> it = list.listIterator();
        int count = 0;
        while (it.hasNext()) {
            String path = Tools.removeApostrophes(it.next().toString());
            args[count++] = path;
        }
        return args;
    }

    /*
     * unloads a tuprolog library, given its java class name
     */
    public boolean unload_library_1(Term arg0) throws PrologError {
        arg0 = arg0.term();
        if (!arg0.isAtomic()) {
            if (arg0 instanceof Var)
                throw PrologError.instantiation_error(prolog, 1);
            else
                throw PrologError.type_error(prolog, 1, "atom", arg0);
        }
        try {
            libs.unload(((Struct) arg0).name());
            return true;
        } catch (Exception ex) {
            throw PrologError.existence_error(prolog, 1, "class", arg0,
                    new Struct(ex.getMessage()));
        }
    }

    /*
     * get flag list: flag_list(-List)
     */
    public boolean flag_list_1(Term arg0) {
        arg0 = arg0.term();
        Struct flist = flags.flags();
        return unify(arg0, flist);
    }

    public boolean comma_2(Term arg0, Term arg1) {
        prolog.pushSubGoal(ClauseInfo.extractBody(new Struct(",", arg0.term(), arg1.term())));
        return true;
    }

    /**
     * It is the same as call/1, but it is not opaque to cut.
     *
     * @throws PrologError
     */
    public boolean $call_1(Term goal) throws PrologError {
        goal = goal.term();
        if (goal instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (!isCallable(goal))
            throw PrologError.type_error(prolog, 1, "callable", goal);
        goal = convertTermToGoal(goal);
        if (goal == null)
            throw PrologError.type_error(prolog, 1, "callable", goal);
        prolog.identify(goal);
        prolog.pushSubGoal(ClauseInfo.extractBody(goal));
        return true;
    }

    /**
     * Convert a term to a goal before executing it by means of call/1. See
     * section 7.6.2 of the ISO Standard for details.
     * <ul>
     * <li>If T is a variable then G is the control construct call, whose
     * argument is T.</li>
     * <li>If the principal functor of T is t ,?/2 or ;/2 or ->/2, then each
     * argument of T shall also be converted to a goal.</li>
     * <li>If T is an atom or compound term with principal functor FT, then G is
     * a predication whose predicate indicator is FT, and the arguments, if any,
     * of T and G are identical.</li>
     * </ul>
     * Note that a variable X and a term call(X) are converted to identical
     * bodies. Also note that if T is a number, then there is no goal which
     * corresponds to T.
     */
    static Term convertTermToGoal(Term term) {

        term = term.term();

        if (term instanceof NumberTerm)
            return null;

//        if (term instanceof Var && ((Var) term).link() instanceof NumberTerm)
//            return null;

        if (term instanceof Var)
            return new Struct("call", term);

        if (term instanceof Struct s) {
            String pi = s.key();
            if (List.of(";/2", ",/2", "->/2").contains(pi)) {
                int n = s.subs();
                for (int i = 0; i < n; i++) {
                    Term t = s.sub(i);
                    Term arg = convertTermToGoal(t);
                    if (arg == null)
                        return null;
                    s.setSub(i, arg);
                }
            }
        }
        return term;
    }

    /**
     * A callable term is an atom of a compound term. See the ISO Standard
     * definition in section 3.24.
     */
    private static boolean isCallable(Term goal) {
        return (goal.isAtomic() || goal.isCompound());
    }

    private void handleError(Throwable t) throws PrologError {

        if (t instanceof ArithmeticException cause) {

            if ("/ by zero".equals(cause.getMessage()))
                throw PrologError.evaluation_error(prolog, 2, "zero_divisor");
        }
    }

    public boolean is_2(Term arg0, Term arg1) throws PrologError {
        if (arg1.term() instanceof Var)
            throw PrologError.instantiation_error(prolog, 2);
        Term val1 = null;
        try {
            val1 = evalExpression(arg1);
        } catch (Throwable t) {
            handleError(t);
        }
        if (val1 == null)
            throw PrologError.type_error(prolog, 2, "evaluable", arg1.term());
        else
            return unify(arg0.term(), val1);
    }

    public boolean unify_2(Term arg0, Term arg1) {
        return unify(arg0, arg1);
    }


    public boolean deunify_2(Term arg0, Term arg1) {
        return !unify(arg0, arg1);
    }


    public boolean $tolist_2(Term arg0, Term arg1) throws PrologError {

        arg0 = arg0.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (arg0 instanceof Struct) {
            Term val0 = ((Struct) arg0).toList();
            return val0 != null && unify(arg1.term(), val0);
        }
        throw PrologError.type_error(prolog, 1, "struct", arg0);
    }


    public boolean $fromlist_2(Term arg0, Term arg1) throws PrologError {


        arg1 = arg1.term();
        if (arg1 instanceof Var)
            throw PrologError.instantiation_error(prolog, 2);
        if (!arg1.isList()) {
            throw PrologError.type_error(prolog, 2, "list", arg1);
        }
        Term val1 = ((Struct) arg1).fromList();
        return val1 != null && unify(arg0.term(), val1);
    }

    public boolean copy_term_2(Term arg0, Term arg1) {

        arg0 = arg0.term();
        arg1 = arg1.term();
        int id = prolog.getEnv().step;
        return unify(arg1, arg0.copy(new IdentityHashMap<>(), id));
    }


    public boolean $append_2(Term arg0, Term arg1) throws PrologError {

        arg1 = arg1.term();
        if (arg1 instanceof Var)
            throw PrologError.instantiation_error(prolog, 2);
        if (!arg1.isList()) {
            throw PrologError.type_error(prolog, 2, "list", arg1);
        }
        ((Struct) arg1).append(arg0.term());
        return true;
    }


    public boolean $find_2(Term arg0, Term arg1) throws PrologError {


        arg0 = arg0.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);

        arg1 = arg1.term();
        if (/* !arg0 instanceof Struct || */!arg1.isList())
            throw PrologError.type_error(prolog, 2, "list", arg1);

        Struct list = (Struct) arg1;
        Iterable<ClauseInfo> l = theories.find(arg0);
        for (ClauseInfo b : l) {
            if (arg0.unifiable(b.head)) {
                b.clause.resolveTerm();
                if (list == Struct.EmptyList)
                    list = Struct.emptyListMutable();
                list.append(b.clause);
            }
        }
        return true;
    }


    public boolean set_prolog_flag_2(Term arg0, Term arg1) throws PrologError {
        arg0 = arg0.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        arg1 = arg1.term();
        if (arg1 instanceof Var)
            throw PrologError.instantiation_error(prolog, 2);
        if ((!arg0.isAtomic() && !(arg0 instanceof Struct)))
            throw PrologError.type_error(prolog, 1, "struct", arg0);
        if (!arg1.isGround())
            throw PrologError.type_error(prolog, 2, "ground", arg1);

        String name = arg0.toString();

        Flag f = flags.get(name);
        if (f == null)
            throw PrologError.domain_error(prolog, 1, "prolog_flag",
                    arg0);
        if (!f.isModifiable())
            throw PrologError.permission_error(prolog, "modify", "flag",
                    arg0, new NumberTerm.Int(0));
        if (!f.isValidValue(arg1))
            throw PrologError
                    .domain_error(prolog, 2, "flag_value", arg1);
        return f.setValue(arg1);
    }


    public boolean get_prolog_flag_2(Term arg0, Term arg1) throws PrologError {
        arg0 = arg0.term();
        arg1 = arg1.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (!arg0.isAtomic() && !(arg0 instanceof Struct)) {
            throw PrologError.type_error(prolog, 1, "struct", arg0);
        }
        String name = arg0.toString();
        Flag flag = flags.get(name);
        if (flag == null)
            throw PrologError.domain_error(prolog, 1, "prolog_flag",
                    arg0);
        return unify(flag.getValue(), arg1);
    }

    public boolean $op_3(Term arg0, Term arg1, Term arg2) throws PrologError {
        arg0 = arg0.term();
        arg1 = arg1.term();
        arg2 = arg2.term();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(prolog, 1);
        if (arg1 instanceof Var)
            throw PrologError.instantiation_error(prolog, 2);
        if (arg2 instanceof Var)
            throw PrologError.instantiation_error(prolog, 3);
        if (!(arg0 instanceof NumberTerm.Int))
            throw PrologError.type_error(prolog, 1, "integer", arg0);
        if (!arg1.isAtomic())
            throw PrologError.type_error(prolog, 2, "atom", arg1);
        if (!arg2.isAtomic() && !arg2.isList())
            throw PrologError.type_error(prolog, 3, "atom_or_atom_list",
                    arg2);
        int priority = ((NumberTerm) arg0).intValue();
        if (priority < PrologOperators.OP_LOW || priority > PrologOperators.OP_HIGH)
            throw PrologError.domain_error(prolog, 1, "operator_priority", arg0);
        String specifier = ((Struct) arg1).name();


        switch (specifier) {
            case "fx":
            case "fy":
            case "xf":
            case "yf":
            case "xfx":
            case "xfy":
            case "yfx":
                break;
            default:
                throw PrologError.domain_error(prolog, 2,
                        "operator_specifier", arg1);
        }

        if (arg2.isList()) {
            for (Iterator<? extends Term> operators = ((Struct) arg2).listIterator(); operators.hasNext(); ) {
                ops.opNew(((Struct) operators.next()).name(), specifier, priority);
            }
        } else
            ops.opNew(((Struct) arg2).name(), specifier, priority);
        return true;
    }

    /*
     * DIRECTIVES
     */

    public void op_3(Term arg0, Term arg1, Term arg2) throws PrologError {
        $op_3(arg0, arg1, arg2);
    }

    public void flag_4(Term flagName, Term flagSet, Term flagDefault,
                       Term flagModifiable) {
        flagName = flagName.term();
        flagSet = flagSet.term();
        flagDefault = flagDefault.term();
        flagModifiable = flagModifiable.term();
        boolean isTrue;
        if (flagSet.isList()
                && (isTrue = flagModifiable.equals(Struct.TRUE) || flagModifiable.equals(Struct.FALSE))) {

            String libName = "";

            flags.add(flagName.toString(), (Struct) flagSet,
                    flagDefault, isTrue, libName);
        }
    }

    public void initialization_1(Term goal) {
        goal = goal.term();
        if (goal instanceof Struct) {
            prims.identify(goal, PREDICATE);
            theories.addStartGoal((Struct) goal);
        }
    }

    public void $load_library_1(Term lib) throws InvalidLibraryException {
        lib = lib.term();
        if (lib.isAtomic())
            libs.loadClass(((Struct) lib).name());
    }

    public void include_1(Term theory) throws
            InvalidTheoryException, IOException, java.io.FileNotFoundException {
        theory = theory.term();
        String path = Tools.removeApostrophes(theory.toString());
        if (!new File(path).isAbsolute()) {
            path = prolog.getCurrentDirectory() + File.separator + path;
        }
        prolog.pushDirectoryToList(new File(path).getParent());
        prolog.input(new Theory(new FileInputStream(path)));
        prolog.popDirectoryFromList();
    }

}