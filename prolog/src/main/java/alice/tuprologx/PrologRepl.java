package alice.tuprologx;

import alice.tuprolog.*;
import alice.tuprolog.event.*;
import alice.tuprolog.lib.IOLibrary;
import alice.util.Automaton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@SuppressWarnings("serial")
public class PrologRepl extends Automaton implements OutputListener, SpyListener, WarningListener/*Castagna 06/2011*/, ExceptionListener/**/{

	final BufferedReader  stdin;
    final Prolog prolog;


    public PrologRepl(String... args){

        if (args.length>1){
            System.err.println("args: { theory file }");
            System.exit(-1);
        }
        

        prolog = new Prolog();
        /**
         * Added the method setExecution to conform
         * the operation of CUIConsole as that of JavaIDE
         */
        IOLibrary IO = (IOLibrary) prolog.library("alice.tuprolog.lib.IOLibrary");
        IO.setExecutionType(IOLibrary.consoleExecution);


        /***/
        stdin = new BufferedReader(new InputStreamReader(System.in));
        prolog.addQueryListener(System.out::println);
        prolog.addExceptionListener(System.err::println);
        prolog.addOutputListener(this);
        prolog.addSpyListener(this);
        /*Castagna 06/2011*/   
        prolog.addExceptionListener(this);
        /**/
        if (args.length>0) {
            try {
                prolog.setTheory(new Theory(new FileInputStream(args[0])));
            } catch (InvalidTheoryException ex){
                System.err.println("invalid theory - line: "+ex.line);
                System.exit(-1);
            } catch (Exception ex){
                System.err.println("invalid theory.");
                System.exit(-1);
            }
        }
    }

    @Override
    public void boot(){
        become("goalRequest");
    }

    public void goalRequest(){
        String goal="";
        while (goal.isEmpty()){
            System.out.print("\n?- ");
            try {
                goal=stdin.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        solveGoal(goal);
    }
    

    void solveGoal(String goal){

        try {
        	Solution info = prolog.solve(goal);
   
            /*Castagna 06/2011*/        	
        	
        	
            /**/
            if (!info.isSuccess()) {
            	/*Castagna 06/2011*/        		
        		if(info.isHalted())
        			System.out.println("halt.");
        		else
        		/**/ 
                System.out.println("no.");
                become("goalRequest");
            } else
                if (!prolog.hasOpenAlternatives()) {
                    String binds = info.toString();
                    if (binds.isEmpty()) {
                        System.out.println("yes.");
                    } else {
                        System.out.println(solveInfoToString(info) + "\nyes.");
                    }
                    become("goalRequest");
                } else {
                    System.out.print(solveInfoToString(info) + " ? ");
                    become("getChoice");
                }
        } catch (MalformedGoalException ex){
            System.out.println("syntax error in goal:\n"+goal);
            become("goalRequest");
        }
    }
    
    private static String solveInfoToString(Solution result) {
        String s = "";
        try {
            for (Var v: result.getBindingVars()) {
                if ( !v.isAnonymous() && v.isBound() && (!(v.term() instanceof Var) || (!((Var) (v.term())).name().startsWith("_")))) {
                    s += v.name() + " / " + v.term() + '\n';
                }
            }
            /*Castagna 06/2011*/
            if(!s.isEmpty()){
            /**/
                s.substring(0,s.length()-1);    
            }
        } catch (NoSolutionException e) {}
        return s;
    }

    public void getChoice(){
        String choice="";
        try {
            while (true){
                choice = stdin.readLine();
                if (!";".equals(choice) && !choice.isEmpty())
                    System.out.println("\nAction ( ';' for more choices, otherwise <return> ) ");
                else
                    break;
            }
        } catch (IOException ex){}
        if (!";".equals(choice)) {
            System.out.println("yes.");
            prolog.solveEnd();
            become("goalRequest");
        } else {
            try {
                System.out.println();
                Solution info = prolog.solveNext();
                if (!info.isSuccess()){
                    System.out.println("no.");
                    become("goalRequest");
                } else {
                	System.out.print(solveInfoToString(info) + " ? ");
                	become("getChoice");
                }
            }catch (Exception ex){
                System.out.println("no.");
                become("goalRequest");
            }
        }
    }

    @Override
    public void onOutput(OutputEvent e) {
        System.out.print(e.msg);
    }
    @Override
    public void onSpy(SpyEvent e) {
        System.out.println(e.getMsg());
    }
    @Override
    public void onWarning(WarningEvent e) {
        System.out.println(e.getMsg());
    }

    /*Castagna 06/2011*/  
	@Override
    public void onException(ExceptionEvent e) {
    	 System.out.println(e.getException());
    	 e.exception.printStackTrace();
	}
	/**/
	
    public static void main(String... args){
        new Thread(new PrologRepl(args)).start();
    }
}
