package jcog.grammar.parse.examples.utensil;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This class has a static method that returns a file's characters as a single
 * String.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */

class FileString {

	/**
	 * Returns a string that represents the contents of a file.
	 * 
	 * @param fileName
	 *            the name of the file to read
	 * 
	 * @return string the contents of a file as a String
	 * 
	 * @exception IOException
	 *                if the file is not found, or if there is any problem
	 *                reading the file
	 */
	public static String stringFromFileNamed(String fileName) throws IOException {

        StringWriter out = new StringWriter();

        try (FileReader in = new FileReader(fileName); out) {
            final int BUFLEN = 1024;
            char[] buf = new char[BUFLEN];
            while (true) {
                int len = in.read(buf, 0, BUFLEN);
                if (len == -1) {
                    break;
                }
                out.write(buf, 0, len);
            }
        }
		return out.toString();
	}
}
