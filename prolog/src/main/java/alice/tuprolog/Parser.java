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


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * This class defines a parser of prolog terms and sentences.
 * <p/>
 * BNF part 2: Parser
 * term ::= exprA(1200)
 * exprA(n) ::= exprB(n) { op(yfx,n) exprA(n-1) |
 *                         op(yf,n) }*
 * exprB(n) ::= exprC(n-1) { op(xfx,n) exprA(n-1) |
 *                           op(xfy,n) exprA(n) |
 *                           op(xf,n) }*
 * 
 * exprC(n) ::= '-' integer | '-' float |
 *              op( fx,n ) exprA(n-1) |
 *              op( fy,n ) exprA(n) |
 *              exprA(n)
 * exprA(0) ::= integer |
 *              float |
 *              atom |
 *              variable |
 *              atom'(' exprA(1200) { ',' exprA(1200) }* ')' |
 *              '[' [ exprA(1200) { ',' exprA(1200) }* [ '|' exprA(1200) ] ] ']' |
 *              '(' { exprA(1200) }* ')'
 *              '{' { exprA(1200) }* '}'
 * op(type,n) ::= atom | { symbol }+
 */
public class Parser {
	private record IdentifiedTerm(int priority, Term result) {
	}

	public static final PrologOperators defaultOps = new PrologOperators.DefaultOps();

	private final Tokenizer tokenizer;
	private PrologOperators ops = defaultOps;
	/*Castagna 06/2011*/
	private final HashMap<Term, Integer> offsetsMap;
	private int tokenStart;
	/**/    










	
	/*Castagna 06/2011*/    
	/**
	 * creating a Parser specifing how to handle operators
	 * and what text to parse
	 */	
	public Parser(String theoryText, PrologOperators op, HashMap<Term, Integer> mapping) {
		this(theoryText, mapping);		 
		if (op != null)		 
			ops = op;
	}


	/**
	 * creating a Parser specifing how to handle operators
	 * and what text to parse
	 */
	public Parser(String theoryText, PrologOperators op) {
		this(theoryText, op, null);
	}
	
	/*Castagna 06/2011*/
	/**
	 * creating a parser with default operator interpretation
	 */	
	public Parser(String theoryText, HashMap<Term, Integer> mapping) {		 
		tokenizer = new Tokenizer(theoryText);		 
		offsetsMap = mapping;		 
	}
	/**/

	public Parser(String theoryText) {
		this(theoryText, defaultOps);
	}








	

	public Iterator<Term> iterator() {
		return new TermIterator(this);
	}

	/**
	 * Parses next term from the stream built on string.
	 * @param endNeeded <tt>true</tt> if it is required to parse the end token
	 * (a period), <tt>false</tt> otherwise.
	 * @throws InvalidTermException if a syntax error is found. 
	 */
	public Term nextTerm(boolean endNeeded) {
		try {
			Tokenizer.Token t = tokenizer.readToken();
			if (t.isEOF())
				return null;

			tokenizer.unreadToken(t);
			Term term = expr(false);
			if (term == null)
				/*Castagna 06/2011*/
	            
	            throw new InvalidTermException("The parser is unable to finish.",
	            		tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            		tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
				/**/

			if (endNeeded && tokenizer.readToken().getType() != Tokenizer.END)
				/*Castagna 06/2011*/
	            
				throw new InvalidTermException("The term '" + term + "' is not ended with a period.",
						tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            		tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
				/**/

			term.resolveTerm();
			return term;
		} catch (IOException ex) {
			/*Castagna 06/2011*/
            
			throw new InvalidTermException("An I/O error occured.",
					tokenizer.offsetToRowColumn(getCurrentOffset())[0],
		            tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);					
			/**/
		}
	}

	/**
	 * Static service to get a term from its string representation
	 */
	public static Term parseSingleTerm(String st) throws InvalidTermException {
		return parseSingleTerm(st, null);
	}

	/**
	 * Static service to get a term from its string representation,
	 * providing a specific operator manager
	 */
	public static Term parseSingleTerm(String st, PrologOperators op) throws InvalidTermException {
		try {
			Parser p = new Parser(st, op);
			Tokenizer.Token t = p.tokenizer.readToken();
			if (t.isEOF())
	            throw new InvalidTermException("Term starts with EOF");

			p.tokenizer.unreadToken(t);
			Term term = p.expr(false);
			if (term == null)
				throw new InvalidTermException("Term is null");
			if (!p.tokenizer.readToken().isEOF())
				throw new InvalidTermException("The entire string could not be read as one term");
			term.resolveTerm();
			return term;
		} catch (IOException ex) {
			throw new InvalidTermException("An I/O error occured");
		}
	}

	

	private Term expr(boolean commaIsEndMarker) throws InvalidTermException, IOException {
		return exprA(PrologOperators.OP_HIGH, commaIsEndMarker).result;
	}

	private IdentifiedTerm exprA(int maxPriority, boolean commaIsEndMarker) throws InvalidTermException, IOException {

		IdentifiedTerm leftSide = exprB(maxPriority, commaIsEndMarker);
		
			

		
		Tokenizer.Token t = tokenizer.readToken();
		for (; t.isOperator(commaIsEndMarker); t = tokenizer.readToken()) {

			int YFX = ops.opPrio(t.seq, "yfx");
			int YF = ops.opPrio(t.seq, "yf");

			
			
			if (YF < leftSide.priority || YF > maxPriority) YF = -1;
			
			if (YFX < leftSide.priority || YFX > maxPriority) YFX = -1;

			
			if (YFX >= YF && YFX >= PrologOperators.OP_LOW){
				IdentifiedTerm ta = exprA(YFX-1, commaIsEndMarker);
				if (ta != null) {
					/*Castagna 06/2011*/
					
					 leftSide = identifyTerm(YFX, new Struct(t.seq, leftSide.result, ta.result), tokenStart);
					/**/
					continue;
				}
			}
			
			if (YF >= PrologOperators.OP_LOW) {
				/*Castagna 06/2011*/
				
				 leftSide = identifyTerm(YF, new Struct(t.seq, leftSide.result), tokenStart);
				/**/
				continue;
			}
			break;
		}
		tokenizer.unreadToken(t);
		return leftSide;
	}

	private IdentifiedTerm exprB(int maxPriority, boolean commaIsEndMarker) throws InvalidTermException, IOException {

		
		IdentifiedTerm left = parseLeftSide(commaIsEndMarker, maxPriority);

		
		Tokenizer.Token operator = tokenizer.readToken();
		for (; operator.isOperator(commaIsEndMarker); operator = tokenizer.readToken()) {
			int XFX = ops.opPrio(operator.seq, "xfx");
			int XFY = ops.opPrio(operator.seq, "xfy");
			int XF = ops.opPrio(operator.seq, "xf");

			
			
			if (XFX > maxPriority || XFX < PrologOperators.OP_LOW) XFX = -1;
			if (XFY > maxPriority || XFY < PrologOperators.OP_LOW) XFY = -1;
			if (XF > maxPriority || XF < PrologOperators.OP_LOW) XF = -1;

			
			boolean haveAttemptedXFX = false;
			if (XFX >= XFY && XFX >= XF && XFX >= left.priority) {     
				IdentifiedTerm found = exprA(XFX - 1, commaIsEndMarker);
				if (found != null) {
					/*Castagna 06/2011*/
					
					
					left = identifyTerm(XFX, new Struct(operator.seq, left.result, found.result), tokenStart);
					/**/
					continue;
				} else
					haveAttemptedXFX = true;
			}
			
			if (XFY >= XF && XFY >= left.priority){           
				IdentifiedTerm found = exprA(XFY, commaIsEndMarker);
				if (found != null) {
					/*Castagna 06/2011*/
					
					
					left = identifyTerm(XFY, new Struct(operator.seq, left.result, found.result), tokenStart);
					/**/
					continue;
				}
			}
			
			if (XF >= left.priority)                   
				/*Castagna 06/2011*/
				
				return identifyTerm(XF, new Struct(operator.seq, left.result), tokenStart);
				/**/

			
			if (!haveAttemptedXFX && XFX >= left.priority) {
				IdentifiedTerm found = exprA(XFX - 1, commaIsEndMarker);
				if (found != null) {
					/*Castagna 06/2011*/
					
					
					left = identifyTerm(XFX, new Struct(operator.seq, left.result, found.result), tokenStart);
					/**/
					continue;
				}
			}
			break;
		}
		tokenizer.unreadToken(operator);
		return left;
	}

	/**
	 * Parses and returns a valid 'leftside' of an expression.
	 * If the left side starts with a prefix, it consumes other expressions with a lower priority than itself.
	 * If the left side does not have a prefix it must be an expr0.
	 *
	 * @param commaIsEndMarker used when the leftside is part of and argument list of expressions
	 * @param maxPriority operators with a higher priority than this will effectivly end the expression
	 * @return a wrapper of: 1. term correctly structured and 2. the priority of its root operator
	 * @throws InvalidTermException
	 */
	private IdentifiedTerm parseLeftSide(boolean commaIsEndMarker, int maxPriority) throws InvalidTermException, IOException {
		
		Tokenizer.Token f = tokenizer.readToken();
		if (f.isOperator(commaIsEndMarker)) {
			int FX = ops.opPrio(f.seq, "fx");
			int FY = ops.opPrio(f.seq, "fy");

			if ("-".equals(f.seq)) {
				Tokenizer.Token t = tokenizer.readToken();
				if (t.isNumber())
					/*Michele Castagna 06/2011*/
					
					return identifyTerm(0, createNumber('-' + t.seq), tokenStart);
					/**/
				else
					tokenizer.unreadToken(t);
			}

			
			if (FY > maxPriority) FY = -1;
			if (FX > maxPriority) FX = -1;


			
			boolean haveAttemptedFX = false;
			if (FX >= FY && FX >= PrologOperators.OP_LOW){
				IdentifiedTerm found = exprA(FX-1, commaIsEndMarker);    
				if (found != null)
					/*Castagna 06/2011*/
					
					return identifyTerm(FX, new Struct(f.seq, found.result), tokenStart);
					/**/
				else
					haveAttemptedFX = true;
			}
			
			if (FY >= PrologOperators.OP_LOW) {
				IdentifiedTerm found = exprA(FY, commaIsEndMarker); 
				if (found != null)
					/*Castagna 06/2011*/
					
					return identifyTerm(FY, new Struct(f.seq, found.result), tokenStart);
				/**/
			}
			
			if (!haveAttemptedFX && FX >= PrologOperators.OP_LOW) {
				IdentifiedTerm found = exprA(FX-1, commaIsEndMarker);    
				if (found != null)
					/*Castagna 06/2011*/
					
					return identifyTerm(FX, new Struct(f.seq, found.result), tokenStart);
					/**/
			}
		}
		tokenizer.unreadToken(f);
		
		return new IdentifiedTerm(0, expr0());
	}

	/**
	 * exprA(0) ::= integer |
	 *              float |
	 *              variable |
	 *              atom |
	 *              atom( exprA(1200) { , exprA(1200) }* ) |
	 *              '[' exprA(1200) { , exprA(1200) }* [ | exprA(1200) ] ']' |
	 *              '{' [ exprA(1200) ] '}' |
	 *              '(' exprA(1200) ')'
	 */
	private Term expr0() throws InvalidTermException, IOException {
		Tokenizer.Token t1 = tokenizer.readToken();

		/*Castagna 06/2011*/
		/*
		if (t1.isType(Tokenizer.INTEGER))
			return Parser.parseInteger(t1.seq); 

		if (t1.isType(Tokenizer.FLOAT))
			return Parser.parseFloat(t1.seq);   

		if (t1.isType(Tokenizer.VARIABLE))
			return new Var(t1.seq);             
		*/
		
		int tempStart = tokenizer.tokenStart();

        if (t1.isType(Tokenizer.INTEGER)) {
        	Term i = parseInteger(t1.seq);
        	map(i, tokenizer.tokenStart());
            return i; 
        }

        if (t1.isType(Tokenizer.FLOAT)) {
        	Term f = parseFloat(t1.seq);
        	map(f, tokenizer.tokenStart());
            return f;   
        }

        if (t1.isType(Tokenizer.VARIABLE)) {
        	Term v = new Var(t1.seq);
        	map(v, tokenizer.tokenStart());
            return v;             
        }
		/**/

		boolean result = IntStream.of(Tokenizer.ATOM, Tokenizer.SQ_SEQUENCE, Tokenizer.DQ_SEQUENCE).anyMatch(t1::isType);
		if (result) {
			if (!t1.isFunctor())
			/*Castagna 06/2011*/
			{
				
				Term f = new Struct(t1.seq);
        		map(f, tokenizer.tokenStart());
        		return f;
			}
			/**/

			String functor = t1.seq;
			Tokenizer.Token t2 = tokenizer.readToken();
			if (!t2.isType(Tokenizer.LPAR))
				throw new InvalidTermException("Something identified as functor misses its first left parenthesis");
			LinkedList<Term> a = expr0_arglist();     
			Tokenizer.Token t3 = tokenizer.readToken();
			if (t3.isType(Tokenizer.RPAR))      
			/*Castagna 06/2011*/
			{
				
				Term c = new Struct(functor, a);
            	map(c, tempStart);                
            	return c;
			}
			/**/
			/*Castagna 06/2011*/
            
			throw new InvalidTermException("Missing right parenthesis '("+a + "' -> here <-",
					tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            	tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
			/**/
		}

		if (t1.isType(Tokenizer.LPAR)) {
			Term term = expr(false);
			if (tokenizer.readToken().isType(Tokenizer.RPAR))
				return term;
			/*Castagna 06/2011*/
            
			throw new InvalidTermException("Missing right parenthesis '("+term + "' -> here <-",
					tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            	tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
			/**/
		}

		if (t1.isType(Tokenizer.LBRA)) {
			Tokenizer.Token t2 = tokenizer.readToken();
			if (t2.isType(Tokenizer.RBRA))
				return Struct.emptyList();

			tokenizer.unreadToken(t2);
			Term term = expr0_list();
			if (tokenizer.readToken().isType(Tokenizer.RBRA))
				return term;
			/*Castagna 06/2011*/
            
			throw new InvalidTermException("Missing right bracket '["+term + " ->' here <-",
					tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            	tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
			/**/
		}

		if (t1.isType(Tokenizer.LBRA2)) {
			Tokenizer.Token t2 = tokenizer.readToken();
			if (t2.isType(Tokenizer.RBRA2))
			/*Castagna 06/2011*/
			{
				
				Term b = new Struct("{}");
            	map(b, tempStart);
                return b;
			}
			/**/
			tokenizer.unreadToken(t2);
			Term arg = expr(false);
			t2 = tokenizer.readToken();
			if (t2.isType(Tokenizer.RBRA2))
			/*Castagna 06/2011*/
			{
				
				Term b = new Struct("{}", arg);
				map(b, tempStart);
				return b;
			}
			/*Castagna 06/2011*/
            
			throw new InvalidTermException("Missing right braces '{"+arg + "' -> here <-",
					tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            	tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
			/**/
		}
		/*Castagna 06/2011*/
		
		throw new InvalidTermException("Unexpected token '" + t1.seq + '\'',
				tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
		/**/
	}

	
	private Term expr0_list() throws InvalidTermException, IOException {
		Term head = expr(true);
		Tokenizer.Token t = tokenizer.readToken();
		if (",".equals(t.seq))
			return new Struct(head, expr0_list());
		if ("|".equals(t.seq))
			return new Struct(head, expr(true));
		if ("]".equals(t.seq)) {
			tokenizer.unreadToken(t);
			return new Struct(head, Struct.emptyList());
		}
		/*Castagna 06/2011*/
        
		throw new InvalidTermException("The expression '" + head + "' is not followed by either a ',' or '|'  or ']'.",
				tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
		/**/	
	
	}

	
	private LinkedList<Term> expr0_arglist() throws InvalidTermException, IOException {
		Term head = expr(true);
		Tokenizer.Token t = tokenizer.readToken();
		String ts = t.seq;
		if (ts.length()==1) {
			char ts0 = ts.charAt(0);
			if (ts0 == ',') {
				LinkedList<Term> l = expr0_arglist();
				l.addFirst(head);
				return l;
			}
			if (ts0 == ')') {
				tokenizer.unreadToken(t);
				LinkedList<Term> l = new LinkedList<>();
				l.add(head);
				return l;
			}
		}
		/*Castagna 06/2011*/
		
		/*Castagna 06/2011*/
        
		throw new InvalidTermException("The argument '" + head + "' is not followed by either a ',' or ')'.",
				tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
		/**/
	}

	

	static NumberTerm parseInteger(String s) {
		long num = Long.parseLong(s);
		return num > Integer.MIN_VALUE && num < Integer.MAX_VALUE ? new NumberTerm.Int((int) num) : new NumberTerm.Long(num);
	}

	static NumberTerm.Double parseFloat(String s) {
		return new NumberTerm.Double(Double.parseDouble(s));
	}

	static NumberTerm createNumber(String s){
		try {
			return parseInteger(s);
		} catch (Exception e) {
			return parseFloat(s);
		}
	}

	/*Castagna 06/2011*/
	/*
     * Francesco Fabbri
     * 18/04/2011
     * Mapping terms on text
     */
    
    private IdentifiedTerm identifyTerm(int priority, Term term, int offset) {
    	map(term, offset);
    	return new IdentifiedTerm(priority, term);
    }
    
    private void map(Term term, int offset) {
    	if (offsetsMap != null)
    		offsetsMap.put(term, offset);
    }
    
//    public HashMap<Term, Integer> getTextMapping() {
//    	return offsetsMap;
//    }
    
    /*
     * Francesco Fabbri
     * 19/04/2011
     * Offset / line tracking
     */


	/**/
	public int getCurrentLine() {
		return tokenizer.lineno();
	}
    
    /*Castagna 06/2011*/

	public int getCurrentOffset() {
		return tokenizer.tokenOffset();
	}
	

//	public int[] offsetToRowColumn(int offset) {
//    	return tokenizer.offsetToRowColumn(offset);
//	}
    /**/

	/**
	 * @return true if the String could be a prolog atom
	 */
	 public static boolean isAtom(String s) {
		 return atomMatches.test(s);
	 }

	private static final Predicate<String> atomMatches =
			Pattern.compile("(!|[a-z][a-zA-Z_0-9]*)").asMatchPredicate();

    /**
     * This class represents an iterator of terms from Prolog text embedded
     * in a parser. Note that this class resembles more a generator than an
     * iterator type. In fact, both {@link TermIterator#next()} and
     * {@link TermIterator#hasNext()} throws {@link InvalidTermException} if
     * the next term they are trying to return or check for contains a syntax
     * error; this is due to both methods trying to generate the next term
     * instead of just returning it or checking for its existence from a pool
     * of already produced terms.
     */
    static class TermIterator implements Iterator<Term> {

        private final Parser parser;
        private boolean hasNext;
        private Term next;

        TermIterator(Parser p) {
            parser = p;
            //next = parser.nextTerm(true);
            //hasNext = (next != null);
        }

        @Override
        public Term next() {
            if (hasNext) {
                if (next == null) {
                    next = parser.nextTerm(true);
                    if (next == null)
                        throw new NoSuchElementException();
                }
                hasNext = false;
                Term temp = next;
                next = null;
                return temp;
            } else
                if (hasNext()) {
                    hasNext = false;
                    Term temp = next;
                    next = null;
                    return temp;
                }
            throw new NoSuchElementException();
        }

        /**
         * @throws InvalidTermException if, while the parser checks for the
         * existence of the next term, a syntax error is encountered.
         */
        @Override
        public boolean hasNext() {
			if (!hasNext) {
				next = parser.nextTerm(true);
				if (next != null)
					hasNext = true;
			}
			return hasNext;
		}

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}