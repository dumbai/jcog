package alice.tuprologx.runtime.tcp;

import alice.tuprolog.Solution;

public class Test
{
    public static void main(String... args)
    {
        if (args.length<2){
            System.err.println("args:  <host> <goal>");
            System.exit(-1);
        }
        try{

            Prolog engine = new Proxy(args[0]);
            /*
            engine.loadLibrary("alice.tuprolog.lib.JavaLibrary");
            engine.addTheory(new Theory(new FileInputStream("test.pl")));
             */
            Solution info=engine.solve(args[1]);
            if (info.isSuccess())
                System.out.println("yes: "+info.getSolution());
            else
                System.out.println("no.");
            
        } catch(Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
    }
}




































