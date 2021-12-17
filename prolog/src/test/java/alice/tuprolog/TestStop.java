package alice.tuprolog;

class PrologThread implements Runnable {
	private final Prolog core;
	private final String goal;
	PrologThread(Prolog core, String goal){
		this.core = core;
		this.goal = goal;
	}

	@Override
	public void run(){
		try {
			System.out.println("STARTING...");
			Solution info = core.solve(goal);
			System.out.println(info);
			System.out.println("STOP.");
		} catch (Exception ex){
			ex.printStackTrace();			
		}
	}	
}

public class TestStop {

	public static void main(String... args) throws InvalidTheoryException, InterruptedException {
		
		Prolog core = new Prolog();
		
		Theory th = new Theory(
			"rec(X):- current_thread <- sleep(X), X1 is X + 100, rec(X1).\n"
		);
		core.setTheory(th);
		
		
		new Thread(new PrologThread(core,"rec(100).")).start();
		
		Thread.sleep(2000);
		
		System.out.println("STOPPING...");
		
		core.solveHalt();
		
		Thread.sleep(2000);
		
		System.out.println("OK.");
	}
}