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
import java.util.Iterator;

/**
 * This class represents a prolog Flag
 */
class Flag implements Serializable {

    private Term value;
    private final Struct valueList;

    private final boolean modifiable;
    private final String libraryName;

    /**
     * Builds a Prolog flag
     *
     * @param name       is the name of the flag
     * @param valueSet   is the Prolog list of the possible values
     * @param defValue   is the default value
     * @param modifiable states if the flag is modifiable
     * @param library    is the library defining the flag
     */
    Flag(Struct valueSet, Term defValue, boolean modifiable, String library) {
        this.valueList = valueSet;

        this.modifiable = modifiable;
        this.libraryName = library;
        this.value = defValue;
    }


    /**
     * Checks if a value is valid according to flag description
     *
     * @param value the possible value of the flag
     * @return flag validity
     */
    public boolean isValidValue(Term value) {
        Iterator<? extends Term> it = valueList.listIterator();
        while (it.hasNext()) {
            if (value.unifiable(it.next())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Gets the list of flag possible values
     *
     * @return a Prolog list
     */
    public Struct getValueList() {
        return valueList;
    }

    /**
     * Sets the value of a flag
     *
     * @param value new value of the flag
     * @return true if the value is valid
     */
    public boolean setValue(Term value) {
        if (isValidValue(value) && modifiable) {
            this.value = value;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the current value of the flag
     *
     * @return flag current value
     */
    public Term getValue() {
        return value;
    }

    /**
     * Checks if the value is modifiable
     *
     * @return
     */
    public boolean isModifiable() {
        return modifiable;
    }

    /**
     * Gets the name of the library where the flag has been defined
     *
     * @return the library name
     */
    public String getLibraryName() {
        return libraryName;
    }

}