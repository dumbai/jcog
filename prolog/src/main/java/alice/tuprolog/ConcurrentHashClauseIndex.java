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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Customized HashMap for storing clauses in the TheoryManager
 *
 * @author ivar.orstavik@hist.no
 * <p>
 * Reviewed by Paolo Contessi
 */

public class ConcurrentHashClauseIndex extends ConcurrentHashMap<String, FamilyClausesList> implements MutableClauseIndex {

    @Override
    public void add(String key, ClauseInfo d, boolean first) {
        computeIfAbsent(key, k -> new FamilyClausesList()).add(d, first);
    }

    @Override
    public boolean containsKey(String key) {
        return super.containsKey(key);
    }

    @Override
    public FamilyClausesList remove(String key) {
        return super.remove(key);
    }

    @Override
    public FamilyClausesList clauses(String key) {
        return super.get(key);
    }


    @Override
    public Iterator<ClauseInfo> iterator() {
        return new CompleteIterator(this);
    }


    private static final class CompleteIterator implements Iterator<ClauseInfo> {
        final Iterator<FamilyClausesList> values;
        Iterator<ClauseInfo> workingList;

        CompleteIterator(ConcurrentHashClauseIndex clauseDatabase) {
            values = clauseDatabase.values().iterator();
        }

        @Override
        public boolean hasNext() {
            while (true) {
                if (workingList != null && workingList.hasNext())
                    return true;
                if (values.hasNext()) {
                    workingList = values.next().iterator();
                    continue;
                }
                return false;
            }
        }

        @Override
        public ClauseInfo next() {
            return workingList.hasNext() ? workingList.next() : null;
        }

        @Override
        public void remove() {
            workingList.remove();
        }
    }

}