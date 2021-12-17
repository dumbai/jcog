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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Administrator of flags declared
 * 
 * @author Alex Benini
 */
class Flags extends ConcurrentHashMap<String,Flag> {


    /**
     * Defines a new flag
     */
    public void add(String name, Struct valueList, Term defValue,
                    boolean modifiable, String libName) {
        put(name, new Flag(valueList, defValue, modifiable, libName));
    }


    @Deprecated public Struct flags() {
        Struct flist = Struct.emptyList();
        for (Map.Entry<String,Flag> fl : entrySet()) {
            flist = new Struct(new Struct("flag", new Struct(fl.getKey()), fl
                    .getValue().getValue()), flist);
        }
        return flist;
    }




}
