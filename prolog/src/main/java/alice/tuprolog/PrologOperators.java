/*
 * tuProlog - Copyright (C) 2001-2006  aliCE team at deis.unibo.it
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

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages Prolog operators.
 *
 * @see PrologOp
 */
@SuppressWarnings("serial")
public class PrologOperators extends ConcurrentHashMap<String, PrologOp> /**/ {

    /**
     * lowest operator priority
     */
    public static final int OP_LOW = 1;

    /**
     * highest operator priority
     */
    public static final int OP_HIGH = 1200;

    public PrologOperators() {
        super(128, 0.9f);
    }

    public void addOperator(PrologOp op) {
        put(op.name + op.type, op);
    }

    public PrologOp getOperator(String name, String type) {
        return get(name + type);
    }


    /**
     * Creates a new operator. If the operator is already provided,
     * it replaces it with the new one
     */
    public void opNew(String name, String type, int prio) {
        PrologOp op = new PrologOp(name, type, prio);
        if (prio >= OP_LOW && prio <= OP_HIGH)
            addOperator(op);
    }

    /**
     * Returns the priority of an operator (0 if the operator is not defined).
     */
    public int opPrio(String name, String type) {
        PrologOp o = getOperator(name, type);
        return (o == null) ? 0 : o.prio;
    }

    public int opPrio(String nametype) {
        PrologOp o = get(nametype);
        return (o == null) ? 0 : o.prio;
    }


    /**
     * Gets the list of the operators currently defined
     *
     * @return the list of the operators
     */
    public Iterable<PrologOp> operators() {
        return values();
    }


    /**
     *  This class defines an operator manager with
     *  some standard operators defined
     *
     */
    static class DefaultOps extends PrologOperators {
        DefaultOps() {
            opNew(":-", "xfx", 1200);
            opNew("-->", "xfx", 1200);
            opNew(":-", "fx", 1200);
            opNew("?-", "fx", 1200);
            opNew(";", "xfy", 1100);
            opNew("->", "xfy", 1050);
            opNew(",", "xfy", 1000);
            opNew("\\+", "fy", 900);
            opNew("not", "fy", 900);
            opNew("=", "xfx", 700);
            opNew("\\=", "xfx", 700);
            opNew("==", "xfx", 700);
            opNew("\\==", "xfx", 700);


            opNew("@>", "xfx", 700);
            opNew("@<", "xfx", 700);
            opNew("@=<", "xfx", 700);
            opNew("@>=", "xfx", 700);
            opNew("=:=", "xfx", 700);
            opNew("=\\=", "xfx", 700);
            opNew(">", "xfx", 700);
            opNew("<", "xfx", 700);
            opNew("=<", "xfx", 700);
            opNew(">=", "xfx", 700);
            opNew("is", "xfx", 700);
            opNew("=..", "xfx", 700);


            opNew("+", "yfx", 500);
            opNew("-", "yfx", 500);
            opNew("/\\", "yfx", 500);
            opNew("\\/", "yfx", 500);
            opNew("*", "yfx", 400);
            opNew("/", "yfx", 400);
            opNew("//", "yfx", 400);
            opNew(">>", "yfx", 400);
            opNew("<<", "yfx", 400);
            opNew("rem", "yfx", 400);
            opNew("mod", "yfx", 400);
            opNew("**", "xfx", 200);
            opNew("^", "xfy", 200);
            opNew("\\", "fx", 200);
            opNew("-", "fy", 200);
        }

    }
}