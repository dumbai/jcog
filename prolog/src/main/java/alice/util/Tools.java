/*
 *   Tools.java
 *
 * Copyright 2000-2001-2002  aliCE team at deis.unibo.it
 *
 * This software is the proprietary information of deis.unibo.it
 * Use is subject to license terms.
 *
 */
package alice.util;

/**
 * miscellaneous static services
 */
public class Tools {


    public static String removeApostrophes(String st) {
        return st.charAt(0) == '\'' && st.endsWith("'") ? st.substring(1, st.length() - 1) : st;
    }
}
