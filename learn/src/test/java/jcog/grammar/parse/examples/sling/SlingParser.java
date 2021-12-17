package jcog.grammar.parse.examples.sling;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.examples.reserved.WordOrReservedState;
import jcog.grammar.parse.examples.track.SeqEx;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.Tokenizer;
import jcog.grammar.parse.tokens.Word;

/**
 * 
 * This class provides a parser for Sling, an imperative
 * language that plots the path of a sling stone.
 *
 * <p>
 * The grammar this class supports is:
 * <blockquote><pre>
 * 
 * statements    = statement statement*;
 * statement     = assignment | forStatement | plotStatement;
 * assignment    = variable '=' expression ';' ; 
 * plotStatement = "plot" expression ';';
 * forStatement  = 
 *     "for" '(' variable ',' expression ',' expression  ')' 
 *     '{' statements '}';
 * <br>
 * variable   = Word;
 * <br>
 * expression       = term (plusTerm | minusTerm)*;
 * plusTerm         = '+' term;
 * minusTerm        = '-' term;
 * term             = element (timesElement | divideElement |
 *                             remainderElement)*;
 * timesElement     = '*' element;
 * divideElement    = '/' element;
 * remainderElement = '%' element;
 * element          = '(' expression ')' | baseElement | 
 *                    negative;
 * <br>
 * negative    = '-' baseElement; 
 * <br>
 * baseElement = 
 *     Num | "pi" | "random" | "s1" | "s2" | "t" | variable | 
 *     oneArg("abs")    | oneArg("ceil")       | 
 *     oneArg("cos")    | oneArg("floor")      | 
 *     oneArg("sin")    | oneArg("tan")        |
 *     twoArgs("polar") | twoArgs("cartesian") | 
 *     twoArgs("scale") | twoArgs("sling");
 * <br>
 * oneArg(i)  = i '(' expression ')';
 * twoArgs(i) = i '(' expression ',' expression ')';
 * 
 * </pre></blockquote>
 * 
 * The following program describes about 10,000 interesting
 * plots:
 *
 * <blockquote><pre>
 *     plot sling(1, 1) + sling(s1, 100*s2);
 * </pre></blockquote>
 *
 * <p>
 * The class <code>SlingIde</code> provides an interactive
 * development environment for Sling.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class SlingParser {
	private Seq expression;
	private Alternation statement;
	private Alternation baseElement;
	WordOrReservedState wors;
	private Tokenizer tokenizer;

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     assignment = variable '=' expression ';' ; 
	 */
    private Parser assignment() {
		SeqEx t = new SeqEx("assignment");
		t.get(variable());
		t.get(new Symbol('=').ok());
		t.get(expression());
		t.get(new Symbol(';').ok());
		t.put(new AssignmentAssembler());
		return t;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     baseElement = 
	 *        Num | "pi" | "random" | "s1" | "s2" | "t" | variable |
	 *       ("abs" |"ceil" |"cos" | "floor" |"sin" |"tan") oneArg |
	 *       ("polar" | "cartesian" | "scale" | "sling") twoArgs;
	 */
    private Parser baseElement() {
		if (baseElement == null) {
			baseElement = new Alternation("base elements");
			baseElement.get(oneArg("abs", new Abs()));
			baseElement.get(twoArg("cartesian", new Cartesian()));
			baseElement.get(oneArg("ceil", new Ceil()));
			baseElement.get(oneArg("cos", new Cos()));
			baseElement.get(oneArg("floor", new Floor()));
			baseElement.get(num());
			baseElement.get(noArgs("random", new Random()));
			baseElement.get(pi());
			baseElement.get(twoArg("polar", new Polar()));
			baseElement.get(s1());
			baseElement.get(s2());
			baseElement.get(scale());
			baseElement.get(oneArg("sin", new Sin()));
			baseElement.get(twoArg("sling", new Sling()));
			baseElement.get(noArgs("t", new T()));
			baseElement.get(oneArg("tan", new Tan()));
			baseElement.get(variable());
		}
		return baseElement;
	}

	/*
	 * Recognize a comma.
	 */
	private static Parser comma() {
		return new Symbol(',').ok();
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     divideElement  = '/' element;
	 */
    private SeqEx divideElement() {
		SeqEx t = new SeqEx();
		t.get(new Symbol('/').ok());
		t.get(element());
		t.put(new FunctionAssembler(new Arithmetic('/')));
		return t;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     element = '(' expression ')' | baseElement | negative;
	 */
    private Parser element() {

		Alternation a = new Alternation("element");
		Seq s = new Seq();
		s.get(new Symbol('(').ok());
		s.get(expression());
		s.get(new Symbol(')').ok());
		a.get(s);
		a.get(baseElement());
		a.get(negative());
		return a;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     expression = term (plusTerm | minusTerm)*;
	 */
    private Parser expression() {

		if (expression == null) {
			expression = new Seq("expression");
			expression.get(term());
			Alternation rest = new Alternation();
			rest.get(plusTerm());
			rest.get(minusTerm());
			expression.get(new Repetition(rest));
		}
		return expression;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     forStatement  = 
	 *       "for" '(' variable ',' expression ',' expression  ')' 
	 *       '{' statements '}';
	 */
    private Parser forStatement() {
		SeqEx t = new SeqEx();
		t.get(reserve("for"));
		t.get(lParen());

		
		t.get(variable());
		t.get(comma());

		
		t.get(expression());
		t.get(comma());

		
		t.get(expression());
		t.get(rParen());

		
		t.get(lBrace());
		t.get(statements());
		t.get(rBrace());
		t.put(new ForAssembler());
		return t;
	}

	/*
	 * Recognize a left brace, and leave it on the stack as
	 * a fence.
	 */
	private static Parser lBrace() {
		return new Symbol('{');
	}

	/*
	 * Recognize a left parenthesis.
	 */
	private static Parser lParen() {
		return new Symbol('(').ok();
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     minusTerm  = '-' term;
	 */
    private SeqEx minusTerm() {
		SeqEx t = new SeqEx();
		t.get(new Symbol('-').ok());
		t.get(term());
		t.put(new FunctionAssembler(new Arithmetic('-')));
		return t;
	}

	/*
	 *  Returns a parser that will recognize the grammar:
	 * 
	 *      negative = '-' baseElement; 
	 */
    private Parser negative() {
		Seq s = new Seq("negative baseElement");
		s.get(new Symbol('-').ok());
		s.get(baseElement());
		s.put(new NegativeAssembler());
		return s;
	}

	/*
	 * Reserves the given name, and creates and returns an 
	 * parser that recognizes the name. Sets the assembler of
	 * the parser to be a <code>FunctionAssembler</code> for
	 * the given function.
	 */
    private Parser noArgs(String name, SlingFunction f) {
		Parser p = reserve(name);
		p.put(new FunctionAssembler(f));
		return p;
	}

	/*
	 * Constructs and returns a parser that recognizes a
	 * number and that uses a <code>NumAssembler</code>.
	 */
    private static Parser num() {
		return new Num().put(new NumAssembler());
	}

	/*
	 * Return a parser that recognizes and stacks a one-
	 * argument function.
	 */
    private Parser oneArg(String name, SlingFunction f) {
		SeqEx t = new SeqEx(name);
		t.get(reserve(name));
		t.get(lParen());
		t.get(expression());
		t.get(rParen());
		t.put(new FunctionAssembler(f));
		return t;
	}

	/*
	 * Returns a parser that recognizes the literal "pi". Sets
	 * the parser's assembler to be a <code>PiAssembler</code>.
	 */
    private Parser pi() {
		ReservedLiteral pi = reserve("pi");
		pi.put(new PiAssembler());
		return pi;
	}

	/*
	 * Return a parser that recognizes the grammar:
	 *
	 *     plotStatement = "plot" expression ';';
	 */
    private Parser plotStatement() {
		SeqEx t = new SeqEx();
		t.get(reserve("plot"));
		t.get(expression());
		t.get(semicolon());
		t.put(new PlotAssembler());
		return t;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     plusTerm  = '+' term;
	 */
    private SeqEx plusTerm() {
		SeqEx t = new SeqEx();
		t.get(new Symbol('+').ok());
		t.get(term());
		t.put(new FunctionAssembler(new Arithmetic('+')));
		return t;
	}

	/*
	 * Recognize a right brace.
	 */
	private static Parser rBrace() {
		return new Symbol('}').ok();
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     remainderElement  = '%' element;
	 */
    private SeqEx remainderElement() {
		SeqEx t = new SeqEx();
		t.get(new Symbol('%').ok());
		t.get(element());
		t.put(new FunctionAssembler(new Arithmetic('%')));
		return t;
	}

	/*
	 * Mark the given word as reserved, meaning users cannot use
	 * the word as a variable. Create a special literal parser
	 * to recognize the word, and return this parser. 
	 */
    private ReservedLiteral reserve(String s) {
		wors().addReservedWord(s);
		ReservedLiteral lit = new ReservedLiteral(s);
		lit.ok();
		return lit;
	}

	/*
	 * Recognize a right parenthesis.
	 */
	private static Parser rParen() {
		return new Symbol(')').ok();
	}

	/*
	 * Recognize the first slider variable.
	 */
    private Parser s1() {
		Parser p = reserve("s1");
		
		p.put(new SliderAssembler(1));
		return p;

	}

	/*
	 * Recognize the second slider variable.
	 */
    private Parser s2() {
		Parser p = reserve("s2");
		p.put(new SliderAssembler(2));
		return p;
	}

	/*
	 * Returns a parser that recognizes scale functions, and
	 * sets the parser's assembler to be a <code>ScaleAssembler
	 * </code>.
	 */
    private Parser scale() {
		SeqEx t = new SeqEx("scale");
		t.get(reserve("scale"));
		t.get(lParen());

		t.get(expression());

		t.get(comma());
		t.get(expression());

		t.get(rParen());

		t.put(new ScaleAssembler());
		return t;
	}

	/*
	 * Recognize a semicolon.
	 */
	private static Parser semicolon() {
		return new Symbol(';').ok();
	}

	/*
	 * Recoginze Sling <code>statements</code>.
	 */
	public Parser start() {
		return statements();
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     statement = assignment | forStatement | plotStatement;
	 */
    Parser statement() {
		if (statement == null) {
			statement = new Alternation("Statement");
			statement.get(assignment());
			statement.get(forStatement());
			statement.get(plotStatement());
		}
		return statement;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     statements = statement statement*;
	 */
    private Parser statements() {
		Seq s = new Seq();
		s.get(statement());
		s.get(new Repetition(statement()));
		return s;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     term = element (timesElement | divideElement | 
	 *                     remainderElement)*;
	 */
    private Parser term() {
		Seq s = new Seq("term");
		s.get(element());
		Alternation a = new Alternation();
		a.get(timesElement());
		a.get(divideElement());
		a.get(remainderElement());
		s.get(new Repetition(a));
		return s;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 * 
	 *     timesElement  = '*' element;
	 */
    private SeqEx timesElement() {
		SeqEx t = new SeqEx();
		t.get(new Symbol('*').ok());
		t.get(element());
		t.put(new FunctionAssembler(new Arithmetic('*')));
		return t;
	}

	/**
	 * Creates a tokenizer that uses a <code>WordOrReservedState
	 * </code> instead of a normal <code>WordState</code>.
	 */
	public Tokenizer tokenizer() {
		if (tokenizer == null) {
			start(); 
			tokenizer = new Tokenizer();
			tokenizer.setCharacterState('a', 'z', wors());
			tokenizer.setCharacterState('A', 'Z', wors());
			tokenizer.setCharacterState(0xc0, 0xff, wors());
		}
		return tokenizer;
	}

	/*
	 * Return a parser that recognizes and stacks a one-
	 * argument function.
	 */
    private Parser twoArg(String name, SlingFunction f) {
		SeqEx t = new SeqEx(name);
		t.get(reserve(name));
		t.get(lParen());
		t.get(expression());
		t.get(comma());
		t.get(expression());
		t.get(rParen());
		t.put(new FunctionAssembler(f));
		return t;
	}

	/*
	 * Recognize a word as a variable.
	 */
    private static Parser variable() {
		return new Word().put(new VariableAssembler());
	}

	/*
	 * Returns a WordOrReservedState object, which is a tokenizer 
	 * state that differentiates reserved words from nonreserved 
	 * words.
	 */
    private WordOrReservedState wors() {
		if (wors == null) {
			wors = new WordOrReservedState();
		}
		return wors;
	}
}
