package alice.tuprolog;

import jcog.data.list.Lst;

import java.util.Iterator;


public final class SubGoalTree extends Lst<SubTree> implements SubTree {

    public SubGoalTree() {
        super(1);
    }

    public SubGoalTree(Term body) {
        this();
        while (body instanceof Struct s) {
            if (!",".equals(s.name()))
                break;

            Term t = s.sub(0);
            if (t instanceof Struct && ",".equals(((Struct) t).name())) {
                addChild(t);
            } else {
                add(t);
            }
            body = s.sub(1);
        }
        add(body);
    }

    private void addChild(Term t) {
        add(new SubGoalTree(t));
    }

    public SubGoalTree addChild() {
        SubGoalTree r = new SubGoalTree();
        add(r);
        return r;
    }


    @Override
    public final boolean isLeaf() { return false; }

    public String toString() {
        StringBuilder result = new StringBuilder(" [ ");
        Iterator<SubTree> i = iterator();
        if (i.hasNext())
            result.append(i.next());
        while (i.hasNext()) {
            result.append(" , ").append(i.next());
        }
        return result + " ] ";
    }



}