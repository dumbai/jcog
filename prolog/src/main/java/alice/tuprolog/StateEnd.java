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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_LIST;

/**
 * @author Alex Benini
 * <p>
 * End state of demostration.
 */
public class StateEnd extends State {

    public final int endState;
    Struct goal;
    List<Var> vars;
    private int setOfCounter;

    /**
     * Constructor
     *
     * @param end Terminal state of computation
     */
    StateEnd(int end) {
        endState = end;
    }

    @Override State run(Solve s) {
        int gv = s.goalVars.size();
        if (gv > 0) {
            Lst<Var> vars = new Lst<>(gv);
            goal = (Struct) s.startGoal.copyResult(s.goalVars, vars);
            if (vars.isEmpty())
                this.vars = EMPTY_LIST;
            else
                this.vars = vars;

            int end = this.endState;
            if (end == PrologRun.TRUE || end == PrologRun.TRUE_CP)
                if (s.run.prolog.relinkVar())
                    relinkVar(s);

        } else {
            vars = EMPTY_LIST;
            goal = s.startGoal;
        }

        return null;
    }

    private static Term solve(Term a1, Object[] a, Term initGoalBag) {

        while (a1 instanceof Struct && ((Struct) a1).subs() > 0) {

            Term a10 = ((Struct) a1).sub(0);
            if (a10 instanceof Var) {


                initGoalBag = findVarName(a10, a, a1, 0);


            } else if (a10 instanceof Struct) {

                a10 = solve(((Struct) a10).sub(0), a, a10);
                ((Struct) a1).setSub(0, a10);

            }
            Term a11 = null;
            if (((Struct) a1).subs() > 1)
                a11 = ((Struct) a1).sub(1);
            a1 = a11;
        }

        if (a1 instanceof Var)
            return findVarName(a1, a, initGoalBag, 0);
        else
            return initGoalBag;
    }

    private static Term findVarName(Term link, Object[] a, Term initGoalBag, int pos) {
        boolean findName = false;

        while (link instanceof Var && !findName) {

            int y = 0;
            while (!findName && y < a.length) {
                Term gVar = (Term) a[y];
                while (!findName && gVar instanceof Var) {

                    if (gVar == link || ((Var) gVar).name().equals(((Var) link).name())) {


                        ((Struct) initGoalBag).setSub(pos, new Var(((Var) a[y]).name()));
                        findName = true;
                    }
                    gVar = ((Var) gVar).link();
                }
                y++;
            }
            link = ((Var) link).link();
        }
        return initGoalBag;
    }

    private static Var varValue2(Var v) {
        Term l;
        while ((l = v.link()) != null) {

            if (l instanceof Var)
                v = (Var) l;
            else
                break;
        }
        return v;
    }

    private static Var structValue(Var v, int i) {
        structValue:
        while (true) {
            Var vStruct = new Var();
            Term l;
            while ((l = v.link()) != null) {

                if (l instanceof Var) {

                    v = (Var) l;
                } else if (l instanceof Struct s) {


                    while (i > 0) {
                        Term s1 = s.sub(1);

                        if (s1 instanceof Struct) {
                            s = (Struct) s1;
                        } else if (s1 instanceof Var) {
                            vStruct = ((Var) s1);
                            if (vStruct.link() != null) {
                                i--;
                                v = vStruct;
                                continue structValue;
                            }
                            return vStruct;
                        }
                        i--;
                    }
                    vStruct = ((Var) s.sub(0));
                    break;
                } else break;
            }

            return vStruct;
        }
    }

    private static void setStructValue(Var v, int i, Var v1) {
        Term l;
        while ((l = v.link()) != null) {

            if (l instanceof Var) {

                v = (Var) l;
            } else if (l instanceof Struct s) {


                while (i > 0) {

                    Term s1 = s.sub(1);
                    if (s1 instanceof Struct)
                        s = (Struct) s1;
                    else if (s1 instanceof Var) {
                        v = (Var) s1;
                        s = ((Struct) v.link());
                    }
                    i--;
                }
                s.setSub(0, v1);
                break;
            } else break;
        }

    }

    private static Lst<String> findVar(Struct s, Lst<String> l) {
        Lst<String> allVar = l;
        if (allVar == null) allVar = new Lst<>(0);
        if (s.subs() > 0) {
            Term t = s.sub(0);
            if (s.subs() > 1) {
                Term tt = s.sub(1);

                if (tt instanceof Var) {
                    allVar.add(((Var) tt).name());
                } else if (tt instanceof Struct) {
                    findVar((Struct) tt, allVar);
                }
            }
            if (t instanceof Var) {
                allVar.add(((Var) t).name());
            } else if (t instanceof Struct) {
                findVar((Struct) t, allVar);
            }
        }
        return allVar;
    }

    private static Struct substituteVar(Struct s, Lst<String> lSol, Lst<String> lgoal) {
        Term t = s.sub(0);

        Term tt = null;
        if (s.subs() > 1)
            tt = s.sub(1);

        if (tt instanceof Var) {

            s.setSub(1, new Var(lgoal.get(lSol.indexOf(((Var) tt).name()))));

            if (t instanceof Var) {
                s.setSub(0, new Var(lgoal.get(lSol.indexOf(((Var) t).name()))));
            } else if (t instanceof Struct && ((Struct) t).subs() > 0) {
                s.setSub(0, substituteVar((Struct) t, lSol, lgoal));
            }
        } else {
            if (t instanceof Var) {

                s.setSub(0, new Var(lgoal.get(lSol.indexOf(((Var) t).name()))));
            } else if (t instanceof Struct) {

                s.setSub(0, substituteVar((Struct) t, lSol, lgoal));
            }
        }

        return s;
    }


    private void relinkVar(Solve e) {
        Prolog pParent = e.run.prolog;


        List<Term> bag = e.run.getBagOFres();
        Term initBag = pParent.getBagOFbag();



        /* itero nel goal per cercare una eventuale struttura che deve fare match con la
         * result bag ESEMPIO setof(X,member(X,[V,U,f(U),f(V)]),[a,b,f(b),f(a)]).
         */
        Term tgoal = pParent.getBagOFgoal();
        Object[] a = (e.goalVars).toArray();


        Term query = e.query;


        if (";".equals(((Struct) query).name())) {
            Struct query_temp = (Struct) ((Struct) query).sub(0);
            if ("setof".equals(query_temp.name()) && setOfCounter == 0) {
                query = query_temp;
                this.setOfCounter++;
            } else {
                query_temp = (Struct) ((Struct) query).sub(1);
                if ("setof".equals(query_temp.name()))
                    query = query_temp;
            }
        }

        if (((Struct) query).subs() > 2 && ((Struct) query).sub(2) instanceof Struct) {

            boolean findSamePredicateIndicator = false;
            boolean find = false;
            Term initGoalBag = null;

            Prolog p = null;

            while (tgoal instanceof Var && ((Var) tgoal).link() != null) {
                tgoal = ((Var) tgoal).link();

                if (tgoal instanceof Struct) {
                    tgoal = ((Struct) tgoal).sub(1);

                    if (p == null) p = new Prolog();

                    if (tgoal.unify(p, ((Var) initBag).link())) {

                        initGoalBag = tgoal;
                        find = true;
                        findSamePredicateIndicator = true;
                        break;
                    } else if (((Var) initBag).link() instanceof Struct s) {

                        if (tgoal instanceof Struct && s.key().compareTo(((Struct) tgoal).key()) == 0) {

                            findSamePredicateIndicator = true;
                            find = true;
                            initGoalBag = tgoal;
                        }
                    }

                    if (find || findSamePredicateIndicator && initGoalBag instanceof Struct) {

                        Term a0 = ((Struct) initGoalBag).sub(0);
                        Term a1 = ((Struct) initGoalBag).sub(1);
                        if (a0 instanceof Var) {


                            initGoalBag = findVarName(a0, a, initGoalBag, 0);

                        }
                        a1 = solve(a1, a, a1);
                        ((Struct) initGoalBag).setSub(1, a1);
                    }
                }
            }


            if (initGoalBag != null) {

                Lst<Term> initGoalBagList = new Lst<>();
                Struct initGoalBagTemp = (Struct) initGoalBag;
                while (initGoalBagTemp.subs() > 0) {
                    Term t1 = initGoalBagTemp.sub(0);
                    initGoalBagList.add(t1);
                    Term t2 = initGoalBagTemp.sub(1);
                    if (t2 instanceof Struct) {
                        initGoalBagTemp = (Struct) t2;
                    }
                }


                Lst<Term> initGoalBagListOrdered = new Lst<>();
                if ("setof".equals(((Struct) query).name())) {
                    Lst<String> initGoalBagListVar = initGoalBagList.stream().filter(anInitGoalBagList -> anInitGoalBagList instanceof Var).map(anInitGoalBagList -> ((Var) anInitGoalBagList).name()).collect(Collectors.toCollection(Lst::new));

                    Lst<Term> left = new Lst<>();
                    left.add(initGoalBagList.get(0));
                    List<Term> right = new Lst<>();
                    List<Term> right_temp = new Lst<>();

                    List<Term> left_temp = new Lst<>();
                    for (int m = 1; m < initGoalBagList.size(); m++) {
                        int k;
                        for (k = 0; k < left.size(); k++) {
                            if (initGoalBagList.get(m).isGreaterRelink(left.get(k), initGoalBagListVar)) {

                                left_temp.add(left.get(k));
                            } else {

                                left_temp.add(initGoalBagList.get(m));
                                break;
                            }
                        }
                        if (k == left.size())
                            left_temp.add(initGoalBagList.get(m));
                        for (int y = 0; y < left.size(); y++) {

                            boolean search = false;
                            for (Term aLeft_temp : left_temp) {
                                if (aLeft_temp.toString().equals(left.get(y).toString()))
                                    search = true;
                            }
                            if (!search) {

                                right_temp.add(left.get(y));
                            }
                            left.remove(y);
                            y--;
                        }
                        for (int y = 0; y < right.size(); y++) {
                            right_temp.add(right.get(y));
                            right.remove(y);
                            y--;
                        }
                        right.addAll(right_temp);

                        right_temp.clear();
                        left.addAll(left_temp);

                        left_temp.clear();

                    }


                    initGoalBagListOrdered.addAll(left);
                    initGoalBagListOrdered.addAll(right);


                } else initGoalBagListOrdered = initGoalBagList;

                initGoalBagTemp = (Struct) initGoalBag;

                Object[] t = initGoalBagListOrdered.toArray();
                Term[] t1 = Arrays.stream(t).map(item -> (Term) item).toArray(Term[]::new);


                initGoalBag = new Struct(initGoalBagTemp.name(), t1);


                List<Term> initBagList = new Lst<>();
                Struct initBagTemp = (Struct) ((Var) initBag).link();
                while (initBagTemp.subs() > 0) {
                    Term t0 = initBagTemp.sub(0);
                    initBagList.add(t0);
                    Term t2 = initBagTemp.sub(1);
                    if (t2 instanceof Struct) {
                        initBagTemp = (Struct) t2;
                    }
                }

                Object[] tNoOrd = initBagList.toArray();
                Term[] termNoOrd = Arrays.stream(tNoOrd).map(o -> (Term) o).toArray(Term[]::new);


                initBag = new Struct(initGoalBagTemp.name(), termNoOrd);
            }


            if (findSamePredicateIndicator) {

//                if (p == null) p = new Prolog();

                if (!(find && initGoalBag.unify(p, initBag))) {

                    e.next = PrologRun.END_FALSE;

                    PrologRun prologRun = pParent.run;
                    String ss = prologRun.sinfo != null ? prologRun.sinfo.setOfSolution : null;
                    String s = ss != null ? ss + "\n\nfalse." : "null\n\nfalse.";
                    pParent.endFalse(s);

                    return;
                }
            }
        }
        /*
         * STEP1: dalla struttura risultato bagof (bag = (c.getEngineMan()).getBagOFres())
         * estraggo la lista di tutte le variabili
         * memorizzate nell'Lst<String> lSolVar
         * lSolVar = [H_e2301, H_e2302, H_e2303, H_e2304, H_e2305, H_e2306, H_e2307, H_e2308]
         */

        Lst<String> lSolVar = new Lst<>();

        /*NB lSolVar ha lunghezza multipla di lGoal var, se ho pi soluzioni si ripete
         * servirebbe esempio con 2 bag */
        Lst<String> l_temp = new Lst<>();
        for (int i = 0; i < bag.size(); i++) {
            Var resVar = (Var) bag.get(i);

            Term t = resVar.link();

            if (t != null) {
                if (t instanceof Struct t1) {


                    l_temp.clear();
                    l_temp = findVar(t1, l_temp);
                    for (int w = l_temp.size() - 1; w >= 0; w--) {
                        lSolVar.add(l_temp.get(w));
                    }
                } else if (t instanceof Var) {
                    while (t instanceof Var) {
                        resVar = (Var) t;

                        t = resVar.link();

                    }
                    lSolVar.add(resVar.name());
                    bag.set(i, resVar);
                }
            } else lSolVar.add(resVar.name());
        }

        /*
         * STEP2: dalla struttura goal bagof (goalBO = (Var)(c.getEngineMan()).getBagOFgoal())
         * estraggo la lista di tutte le variabili
         * memorizzate nell'Lst<String> lgoalBOVar
         * lgoalBOVar = [Z_e0, X_e73, Y_e74, V_e59, WithRespectTo_e31, U_e588, V_e59, H_e562, X_e73, Y_e74, F_e900]
         */

        Var goalBO = (Var) pParent.getBagOFgoal();

        Lst<String> lgoalBOVar = new Lst<>();
        Term goalBOvalue = goalBO.link();
        if (goalBOvalue instanceof Struct t1) {

            l_temp.clear();
            l_temp = findVar(t1, l_temp);
            for (int w = l_temp.size() - 1; w >= 0; w--) {
                lgoalBOVar.add(l_temp.get(w));
            }
        }


        /*
         * STEP3: prendere il set di variabili libere della bagof
         * fare il match con le variabili del goal in modo da avere i nomi del goal esterno
         * questo elenco ci servirˆ per eliminare le variabili in pi che abbiamo in lgoalBOVar
         * ovvero tutte le variabili associate al template
         * lGoalVar [Y_e74, U_e588, V_e59, X_e73, Y_e74, U_e588, F_e900]
         * mette quindi in lGoalVar le variabili che compaiono in goalVars e sono anche libere
         * per la bagof c.getEngineMan().getBagOFvarSet()
         */

        Var v = (Var) pParent.getBagOFvarSet();
        Struct varList = (Struct) v.link();
        List<String> lGoalVar = new Lst<>();


        if (varList != null)
            for (Iterator<? extends Term> it = varList.listIterator(); it.hasNext(); ) {


                Term var = it.next();
                for (Object anA : a) {
                    Var vv = (Var) anA;
                    Term vLink = vv.link();
                    if (vLink != null && vLink.isEqual(var)/*&& !(var.toString().startsWith("_"))*/) {

                        lGoalVar.add(vv.name());
                    }
                }
            }


        /*
         * STEP4: pulisco lgoalBOVar lasciando solo i nomi che compaiono effettivamente in
         * lGoalVar (che  la rappresentazione con nomi esterni delle variabili libere nel
         * goal della bagof
         */
        lgoalBOVar.retainAll(lGoalVar);

        if (lGoalVar.size() > lgoalBOVar.size()) {

            for (int h = 0; h < lGoalVar.size(); h++)
                if (h >= lgoalBOVar.size()) {

                    lgoalBOVar.add(lGoalVar.get(h));
                }
        }
        /*
         * STEP5: sostituisco le variabili nel risultato (sia in goals che vars)
         * a) cerco l'indice della variabile in lSolVar
         * b) sostituisco con quella di stesso indice in lgoalBOVar
         */
        Var goalSolution = new Var();

        if (!lSolVar.isEmpty() && !lgoalBOVar.isEmpty() && !varList.isGround() && !goalBO.isGround()) {
            String bagVarName = null;
            for (int i = 0; i < bag.size(); i++) {

                Var resVar = (Var) bag.get(i);

                Term t = resVar.term();
//                Term t = resVar.link();
//                if (t == null)
//                    t = resVar;

                bagVarName = null;
                for (Object anA : a) {
                    Var vv = (Var) anA;
                    Var vv_link = structValue(vv, i);

                    if (vv_link.isEqual(t)) {


                        if (bagVarName == null) {
                            bagVarName = vv.getOriginalName();
                            goalSolution = vv;
                        }


                        Term vll = vv_link.link();

                        if (vll instanceof Struct) {
                            Struct s = substituteVar((Struct) vll, lSolVar, lgoalBOVar);

                        } else {
                            setStructValue(vv, i, new Var(lgoalBOVar.get(lSolVar.indexOf(resVar.name()))));
                        }
                    }
                }

            }

            int n = vars.size();
            for (int j = 0; j < n; j++) {
                Var vv = vars.get(j);
                String on = vv.getOriginalName();
                if (bagVarName.equals(on)) {
                    Var solVar = varValue2(goalSolution);

                    solVar.setName(on);
                    solVar.rename(0, 0);

                    vars.set(j, solVar);
                    break;
                }
            }
        }

        /*
         * STEP6: gestisco caso particolare SETOF in cui non stampa la soluzione
         */
        List<String> bagString = pParent.getBagOFresString();
        int i = 0;
        String s = "";

        int bs = bagString.size();
        for (int m = 0; m < bs; m++) {
            String bagResString = bag.get(m).toString();

            if (bag.get(m) instanceof Var && ((Var) bag.get(m)).link() != null && (((Var) bag.get(m)).link() instanceof Struct) && !((Var) bag.get(m)).link().isAtom() && bagResString.length() != bagString.get(m).length()) {

                StringTokenizer st = new StringTokenizer(bagString.get(m));
                StringTokenizer st1 = new StringTokenizer(bagResString);
                while (st.hasMoreTokens()) {
                    String t1 = st.nextToken(" /(),;");

                    String t2 = st1.nextToken(" /(),;");

                    if (t1.compareTo(t2) != 0 && !t2.contains("_")) {

                        s = s + lGoalVar.get(i) + '=' + t2 + ' ';

                        pParent.setSetOfSolution(s);
                        i++;
                    }
                }
            }
        }


        pParent.relinkVar(false);
        pParent.setBagOFres(null);
        pParent.setBagOFgoal(null);
        pParent.setBagOFvarSet(null);
        pParent.setBagOFbag(null);
    }

    public String toString() {
        return switch (endState) {
            case PrologRun.FALSE -> "FALSE";
            case PrologRun.TRUE -> "TRUE";
            case PrologRun.TRUE_CP -> "TRUE_CP";
            default -> "HALT";
        };
    }

}