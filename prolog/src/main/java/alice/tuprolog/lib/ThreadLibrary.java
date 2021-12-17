///**
// * @author Eleonora Cau
// *
// */
//
//package alice.tuprolog.lib;
//
//import alice.tuprolog.*;
//
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.locks.ReentrantLock;
//
//
//public class ThreadLibrary extends PrologLib {
//
//	protected final AtomicInteger id = new AtomicInteger();
//
//	public final ConcurrentHashMap<Integer, PrologRun> runners = new ConcurrentHashMap<>();
//	private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
//	public final ConcurrentHashMap<String, TermQueue> queues = new ConcurrentHashMap<>();
//	public static final ThreadLocal<PrologRun> threads = new ThreadLocal<>();
//
//	@Override
//	public void setProlog(Prolog p) {
//        prolog = p;
//		threads.set(p.run);
//	}
//
//
//	public boolean thread_id_1 (Term t) {
//		int id = runner().getId();
//        unify(t,new NumberTerm.Int(id));
//		return true;
//	}
//
//	private static PrologRun runner() {
//		return threads.get(); //prolog.runner();
//	}
//
//
//	public boolean thread_create_2 (Term id, Term goal){
//		return threadCreate(id, goal);
//	}
//
//	/*Aspetta la terminazione del thread di identificatore id e ne raccoglie il risultato,
//	unificando il goal risolto a result. Il thread viene eliminato dal sistema*/
//	public boolean thread_join_2(Term id, Term result) throws PrologError{
//		id = id.term();
//		if (!(id instanceof NumberTerm.Int))
//			throw PrologError.type_error(prolog, 1,
//                    "integer", id);
//		Solution res = join(((NumberTerm.Int)id).intValue());
//		if (res == null) return false;
//		Term status;
//		try {
//			status = res.getSolution();
//		} catch (NoSolutionException e) {
//
//			return false;
//		}
//		try{
//			unify (result, status);
//		} catch (InvalidTermException e) {
//			throw PrologError.syntax_error(prolog,-1, e.line, e.pos, result);
//		}
//		return true;
//	}
//
//	public Solution read(int id) {
//		ThreadedPrologRun er = runner(id);
//		if (er == null || er.isDetached()) return null;
//		/*toSPY
//		 * System.out.println("Thread id "+runnerId()+" - prelevo la soluzione (read) del thread di id: "+er.getId());
//		 */
//		/*toSPY
//		 * System.out.println("Soluzione: "+solution);
//		 */
//		return er.read();
//	}
//
//	public boolean thread_read_2(Term id, Term result) throws PrologError{
//		id=id.term();
//		if (!(id instanceof NumberTerm.Int))
//			throw PrologError.type_error(prolog, 1,
//                    "integer", id);
//		Solution res= read( ((NumberTerm.Int)id).intValue());
//		if (res==null) return false;
//		Term status;
//		try {
//			status = res.getSolution();
//		} catch (NoSolutionException e) {
//
//			return false;
//		}
//		try{
//			unify (result, status);
//		} catch (InvalidTermException e) {
//			throw PrologError.syntax_error(prolog,-1, e.line, e.pos, result);
//		}
//		return true;
//	}
//
//	public boolean thread_has_next_1(Term id) throws PrologError{
//		id=id.term();
//		if (!(id instanceof NumberTerm.Int))
//			throw PrologError.type_error(prolog, 1,
//                    "integer", id);
//		return hasNext(((NumberTerm.Int)id).intValue());
//	}
//
//	public boolean hasNext(int id) {
//		ThreadedPrologRun er = runner(id);
//		return !(er == null || er.isDetached()) && er.hasOpenAlternatives();
//	}
//
//	public boolean thread_next_sol_1(Term id) throws PrologError{
//		id=id.term();
//		if (!(id instanceof NumberTerm.Int))
//			throw PrologError.type_error(prolog, 1,
//                    "integer", id);
//		return nextSolution(((NumberTerm.Int)id).intValue());
//	}
//
//	public boolean nextSolution(int id) {
//		ThreadedPrologRun er = runner(id);
//		/*toSPY
//		 * System.out.println("Thread id "+runnerId()+" - next_solution: risveglio il thread di id: "+er.getId());
//		 */
//		return !(er == null || er.isDetached()) && er.nextSolution();
//	}
//
//
//	public boolean thread_detach_1 (Term id) throws PrologError{
//		id=id.term();
//		if (!(id instanceof NumberTerm.Int))
//			throw PrologError.type_error(prolog, 1,
//                    "integer", id);
//		detach(((NumberTerm.Int)id).intValue());
//		return true;
//	}
//
//	public boolean thread_sleep_1(Term millisecs) throws PrologError{
//		millisecs=millisecs.term();
//		if (!(millisecs instanceof NumberTerm.Int))
//			throw PrologError.type_error(prolog, 1,
//                    "integer", millisecs);
//		long time=((NumberTerm.Int)millisecs).intValue();
//		try {
//			Thread.sleep(time);
//		} catch (InterruptedException e) {
//			System.out.println("ERRORE SLEEP");
//			return false;
//		}
//		return true;
//	}
//
//	public boolean thread_send_msg_2(Term id, Term msg) throws PrologError{
//		id=id.term();
//		if (id instanceof NumberTerm.Int)
//			return sendMsg(((NumberTerm.Int)id).intValue(), msg);
//		if (!id.isAtom() || !id.isAtomic())
//			throw PrologError.type_error(prolog, 1,
//                    "atom, atomic or integer", id);
//		return sendMsg(id.toString(), msg);
//	}
//
//	public  boolean  thread_get_msg_2(Term id, Term msg) throws PrologError{
//		id=id.term();
//		if (id instanceof NumberTerm.Int)
//			return getMsg(((NumberTerm.Int)id).intValue(), msg);
//		if (!id.isAtomic() || !id.isAtom())
//			throw PrologError.type_error(prolog, 1,
//                    "atom, atomic or integer", id);
//		return getMsg(id.toString(), msg);
//	}
//
//	public  boolean  thread_peek_msg_2(Term id, Term msg) throws PrologError{
//		id=id.term();
//		if (id instanceof NumberTerm.Int)
//			return peekMsg(((NumberTerm.Int)id).intValue(), msg);
//		if (!id.isAtomic() || !id.isAtom())
//			throw PrologError.type_error(prolog, 1,
//                    "atom, atomic or integer", id);
//		return peekMsg(id.toString(), msg);
//	}
//
//	public  boolean  thread_wait_msg_2(Term id, Term msg) throws PrologError{
//		id=id.term();
//		if (id instanceof NumberTerm.Int)
//			return waitMsg(((NumberTerm.Int)id).intValue(), msg);
//		if (!id.isAtomic() || !id.isAtom())
//			throw PrologError.type_error(prolog, 1,
//                    "atom, atomic or integer", id);
//		return waitMsg(id.toString(), msg);
//	}
//
//	public  boolean  thread_remove_msg_2(Term id, Term msg) throws PrologError{
//		id=id.term();
//		if (id instanceof NumberTerm.Int)
//			return removeMsg(((NumberTerm.Int)id).intValue(), msg);
//		if (!id.isAtomic() || !id.isAtom())
//			throw PrologError.type_error(prolog, 1,
//                    "atom, atomic or integer", id);
//		return removeMsg(id.toString(), msg);
//	}
//
//	public boolean msg_queue_create_1(Term q) throws PrologError{
//		q= q.term();
//		if (!q.isAtom() || !q.isAtomic())
//			throw PrologError.type_error(prolog, 1,
//                    "atom or atomic", q);
//		return createQueue(q.toString());
//	}
//
//	public boolean msg_queue_destroy_1 (Term q) throws PrologError{
//		q=q.term();
//		if (!q.isAtom() || !q.isAtomic())
//			throw PrologError.type_error(prolog, 1,
//                    "atom or atomic", q);
//		destroyQueue(q.toString());
//		return true;
//	}
//
//	public boolean createQueue(String name) {
//
//		queues.computeIfAbsent(name, (n) -> new TermQueue());
//
//		return true;
//	}
//
//	public void destroyQueue(String name) {
//
//		queues.remove(name);
//
//	}
//
//
//	public ReentrantLock createLock(String name) {
//		return locks.computeIfAbsent(name, (n) -> new ReentrantLock());
//	}
//
//	public boolean destroyLock(String name) {
//		return locks.remove(name)!=null;
//	}
//
//	public void mutexLock(String name) {
//		//while (true) {
//		ReentrantLock mutex = createLock(name);
//
//		mutex.lock();
//		/*toSPY
//		 * System.out.println("Thread id "+runnerId()+ " - mi sono impossessato del lock");
//		 */
//	}
//
//	public boolean mutexTryLock(String name) {
//		ReentrantLock mutex = locks.get(name);
//		return mutex != null && mutex.tryLock();
//		/*toSPY
//		 * System.out.println("Thread id "+runnerId()+ " - provo ad impossessarmi del lock");
//		 */
//	}
//
//	public boolean mutexUnlock(String name) {
//		ReentrantLock mutex = locks.get(name);
//		if (mutex == null) return false;
//		try {
//			mutex.unlock();
//			/*toSPY
//			 * System.out.println("Thread id "+runnerId()+ " - Ho liberato il lock");
//			 */
//			return true;
//		} catch (IllegalMonitorStateException e) {
//			return false;
//		}
//	}
//
//	public boolean isLocked(String name) {
//		ReentrantLock mutex = locks.get(name);
//		return mutex != null && mutex.isLocked();
//	}
//
//	public void unlockAll() {
//
//        for (Map.Entry<String, ReentrantLock> entry : locks.entrySet()) {
//            String k = entry.getKey();
//            ReentrantLock mutex = entry.getValue();
//            boolean unlocked = false;
//            while (!unlocked) {
//                try {
//                    mutex.unlock();
//                } catch (IllegalMonitorStateException e) {
//                    unlocked = true;
//                }
//            }
//        }
//    }
//
//
//	public int queueSize(int id) {
//		return runner(id).msgs.size();
//	}
//
//	public int queueSize(String name) {
//		TermQueue q = queues.get(name);
//		return q == null ? -1 : q.size();
//	}
//
//	public boolean msg_queue_size_2(Term id, Term n) throws PrologError{
//		id=id.term();
//		int size;
//		if (id instanceof NumberTerm.Int)
//			size= queueSize(((NumberTerm.Int)id).intValue());
//		else{
//			if (!id.isAtomic() || !id.isAtom())
//				throw PrologError.type_error(prolog, 1,
//	                    "atom, atomic or integer", id);
//			size= queueSize(id.toString());
//		}
//		if (size<0) return false;
//		return unify(n, new NumberTerm.Int(size));
//	}
//
//	public boolean mutex_create_1(Term mutex) throws PrologError{
//		mutex=mutex.term();
//		if (!mutex.isAtomic() || !mutex.isAtom())
//			throw PrologError.type_error(prolog, 1,
//                    "atom or atomic", mutex);
//		createLock(mutex.toString());
//		return true;
//	}
//
//	public boolean mutex_destroy_1(Term mutex) throws PrologError{
//		mutex=mutex.term();
//		if (!mutex.isAtomic() || !mutex.isAtom())
//			throw PrologError.type_error(prolog, 1,
//                    "atom or atomic", mutex);
//		return destroyLock(mutex.toString());
//	}
//
//	public boolean mutex_lock_1(Term mutex) throws PrologError{
//		mutex=mutex.term();
//		if (!mutex.isAtomic() || !mutex.isAtom())
//			throw PrologError.type_error(prolog, 1,
//                    "atom or atomic", mutex);
//		mutexLock(mutex.toString());
//		return true;
//	}
//
//	public boolean mutex_trylock_1(Term mutex) throws PrologError{
//		mutex=mutex.term();
//		if (!mutex.isAtomic() || !mutex.isAtom())
//			throw PrologError.type_error(prolog, 1,
//                    "atom or atomic", mutex);
//		return mutexTryLock(mutex.toString());
//	}
//
//	public boolean mutex_unlock_1(Term mutex) throws PrologError{
//		mutex=mutex.term();
//		if (!mutex.isAtomic() || !mutex.isAtom())
//			throw PrologError.type_error(prolog, 1,
//                    "atom or atomic", mutex);
//		return mutexUnlock(mutex.toString());
//	}
//
//	public boolean mutex_isLocked_1(Term mutex) throws PrologError{
//		mutex=mutex.term();
//		if (!mutex.isAtomic() || !mutex.isAtom())
//			throw PrologError.type_error(prolog, 1,
//                    "atom or atomic", mutex);
//		return isLocked(mutex.toString());
//	}
//
//	public boolean mutex_unlock_all_0(){
//		unlockAll();
//		return true;
//	}
//
//	@Override
//	public String getTheory(){
//		return
//		"thread_execute(ID, GOAL):- thread_create(ID, GOAL), '$next'(ID). \n" +
//		"'$next'(ID). \n"+
//		"'$next'(ID) :- '$thread_execute2'(ID). \n"+
//		"'$thread_execute2'(ID) :- not thread_has_next(ID),!,false. \n" +
//		"'$thread_execute2'(ID) :- thread_next_sol(ID). \n" +
//		"'$thread_execute2'(ID) :- '$thread_execute2'(ID). \n" +
//
//		"with_mutex(MUTEX,GOAL):-mutex_lock(MUTEX), call(GOAL), !, mutex_unlock(MUTEX).\n" +
//		"with_mutex(MUTEX,GOAL):-mutex_unlock(MUTEX), fail."
//		;
//
//	}
//
//
//
//	public boolean getMsg(String name, Term msg) {
//		PrologRun er = runner();
//		if (er == null) return false;
//		TermQueue queue = queues.get(name);
//		if (queue == null) return false;
//		return queue.get(msg, prolog, er);
//	}
//	public boolean sendMsg(int dest, Term msg) {
//		ThreadedPrologRun er = runner(dest);
//		if (er == null) return false;
//		Term msgcopy = msg.copy(new LinkedHashMap<>(), 0);
//		er.msgs.store(msgcopy);
//		return true;
//	}
//
//	public boolean sendMsg(String name, Term msg) {
//		TermQueue queue = queues.get(name);
//		if (queue == null) return false;
//		Term msgcopy = msg.copy(new LinkedHashMap<>(), 0);
//		queue.store(msgcopy);
//		return true;
//	}
//
//	public boolean getMsg(int id, Term msg) {
//		ThreadedPrologRun er = runner(id);
//		if (er == null) return false;
//		er.msgs.get(msg, prolog, er);
//		return true;
//	}
//
//
//	public boolean waitMsg(int id, Term msg) {
//		ThreadedPrologRun er = runner(id);
//		if (er == null) return false;
//		er.msgs.wait(msg, prolog, er);
//		return true;
//	}
//
//	public boolean waitMsg(String name, Term msg) {
//		PrologRun er = runner();
//		if (er == null) return false;
//		TermQueue queue = queues.get(name);
//		if (queue == null) return false;
//		return queue.wait(msg, prolog, er);
//	}
//
//	public boolean peekMsg(int id, Term msg) {
//		ThreadedPrologRun er = runner(id);
//		if (er == null) return false;
//		return er.msgs.peek(msg, prolog);
//	}
//
//	public boolean peekMsg(String name, Term msg) {
//		TermQueue queue = queues.get(name);
//		if (queue == null) return false;
//		return queue.peek(msg, prolog);
//	}
//
//	public boolean removeMsg(String name, Term msg) {
//		TermQueue queue = queues.get(name);
//		if (queue == null) return false;
//		return queue.remove(msg, prolog);
//	}
//
//	public boolean removeMsg(int id, Term msg) {
//		ThreadedPrologRun er = runner(id);
//		if (er == null) return false;
//		return er.msgs.remove(msg, prolog);
//	}
//
//	/**
//	 * @return L'EngineRunner associato al thread di id specificato.
//	 */
//
//	public ThreadedPrologRun runner(int id) {
//		return (ThreadedPrologRun) runners.get(id);
//	}
//
//
//	public Solution join(int id) {
//		ThreadedPrologRun er = runner(id);
//		if (er == null || er.isDetached()) return null;
//		/*toSPY
//		 * System.out.println("Thread id "+runnerId()+" - prelevo la soluzione (join)");*/
//		Solution solution = er.read();
//		/*toSPY
//		 * System.out.println("Soluzione: "+solution);*/
//		runners.remove(id);
//		return solution;
//	}
//
//	public void detach(int id) {
//		ThreadedPrologRun er = runner(id);
//		if (er != null)
//			er.detach();
//	}
//
//	public boolean threadCreate(Term threadID, Term goal) {
//
//		if (goal == null)
//			return false;
//
//		int id = this.id.incrementAndGet();
//
//		if (goal instanceof Var)
//			goal = goal.term();
//
//		ThreadedPrologRun er = new ThreadedPrologRun(id);
//		er.initialize(this.prolog);
//
//		if (!threadID.unify(this.prolog, new NumberTerm.Int(id)))
//			return false;
//
//		er.setGoal(goal);
//
//
//		runners.put(id, er);
//
//
//
//		Thread t = new Thread(er);
//
//		t.start();
//		return true;
//	}
//
//	public static class ThreadedPrologRun extends PrologRun {
//		public final TermQueue msgs;
//		private boolean detached;
//
//		public ThreadedPrologRun(int id) {
//			super(id);
//			msgs = new TermQueue();
//		}
//
//		@Override
//		public PrologRun initialize(Prolog vm) {
//			detached = false;
//			return super.initialize(vm);
//		}
//
//		@Override
//		public void run() {
//			threads.set(this);
//			super.run();
//		}
//
//
//
//		public void detach() {
//			detached = true;
//		}
//
//		public boolean isDetached() {
//			return detached;
//		}
//
//	}
//
//	public static class TermQueue {
//
//		private final LinkedList<Term> queue = new LinkedList<>();
//
//		public boolean get(Term t, Prolog engine, PrologRun er) {
//			return searchLoop(t, engine, true, true, er);
//		}
//
//		private boolean searchLoop(Term t, Prolog engine, boolean block, boolean remove, PrologRun er) {
//			synchronized (queue) {
//                do {
//                    boolean found = search(t, engine, remove);
//                    if (found)
//						return true;
//
//					er.setSolving(false);
//					try {
//						queue.wait();
//					} catch (InterruptedException e) {
//						break;
//					}
//				} while (block);
//				return false;
//			}
//		}
//
//
//		private boolean search(Term t, Prolog engine, boolean remove) {
//			synchronized (queue) {
//				Iterator<Term> it = queue.iterator();
//				while (it.hasNext()) {
//					if (t.unify(engine, it.next())) {
//						if (remove)
//							it.remove();
//						return true;
//					}
//				}
//				return false;
//			}
//		}
//
//
//		public boolean peek(Term t, Prolog engine) {
//			synchronized (queue) {
//				return search(t, engine, false);
//			}
//		}
//
//		public boolean remove(Term t, Prolog engine) {
//			synchronized (queue) {
//				return search(t, engine, true);
//			}
//		}
//
//		public boolean wait(Term t, Prolog engine, PrologRun er) {
//			return searchLoop(t, engine, true, false, er);
//		}
//
//		public void store(Term t) {
//			synchronized (queue) {
//				queue.addLast(t);
//				queue.notifyAll();
//			}
//		}
//
//		public int size() {
//			synchronized (queue) {
//				return queue.size();
//			}
//		}
//
//		public void clear() {
//			synchronized (queue) {
//				queue.clear();
//			}
//		}
//
//		public boolean isEmpty() {
//			synchronized (queue) {
//				return queue.isEmpty();
//			}
//		}
//	}
//}
