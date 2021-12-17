package jcog.grammar.parse.examples.coffee;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Tokenizer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * Show the recognition of a list of types of coffee, 
 * reading from a file.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowCoffee {
	/**
	 * Show how to recognize coffees in a file.
	 */
	public static void main(String[] args) throws Exception {

		InputStream is = ClassLoader.getSystemResourceAsStream("coffee.txt");
		BufferedReader r = new BufferedReader(new InputStreamReader(is));

		Tokenizer t = CoffeeParser.tokenizer();
		Parser p = CoffeeParser.start();
		while (true) {
			String s = r.readLine();
			if (s == null) {
				break;
			}
			t.setString(s);
			Assembly in = new TokenAssembly(t);
			Assembly out = p.bestMatch(in);
			System.out.println(out.getTarget());
		}
	}
}