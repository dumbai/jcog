/*
 * Copyright (C) 2015 Machine Learning Lab - University of Trieste, 
 * Italy (http:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:
 */
package jcog.grammar.evolve.configuration;


import jcog.TODO;

import java.io.*;
import java.util.stream.Collectors;

/**
 * @author MaleLabTs
 */
public enum Configurator {
	;


	public static Configuration configure(String json) {
        return configure(new BufferedReader( new StringReader(json)) );
    }

    public static Configuration configureFile(String filename) throws IOException {
        return configure(new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(filename))));
    }

    public static Configuration configure(BufferedReader r) {

        String sb = r.lines().collect(Collectors.joining());

        String json = sb;
        return configureFromJson(json);
    }

    public static Configuration configureFromJson(String jsonConfiguration) {
        throw new TODO();




    }
}