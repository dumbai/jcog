/*
 * Created on Dec 10, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package alice.tuprolog;

/**
 * @author aricci
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestBug {

    
	public static void main(String... args) throws MalformedGoalException, InvalidTheoryException {

	    
	    String goal = """
                out('can_do(X).
                can_do(Y).
                ').""";
		
		new Prolog().solve(goal);


        Prolog engine = new Prolog();
		engine.addSpyListener(System.out::println);

        String st = """
                p(X).
                test(L1,L2):-
                	findall(p(X),p(X),L1),\s
                	append([a,b],L1,L2).
                """;
        engine.setTheory(new Theory(st));
		Solution info = engine.solve("test(L1,L2).");
		System.out.println(info);
		
	}
}