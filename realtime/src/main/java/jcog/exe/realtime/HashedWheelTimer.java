package jcog.exe.realtime;

import jcog.Log;
import jcog.Str;
import jcog.TODO;
import jcog.Util;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Hash Wheel Timer, as per the paper:
 * <p>
 * Hashed and hierarchical timing wheels:
 * http:
 * <p>
 * More comprehensive slides, explaining the paper can be found here:
 * http:
 * <p>
 * Hash Wheel timer is an approximated timer that allows performant execution of
 * larger amount of tasks with better performance compared to traditional scheduling.
 *
 * @author Oleksandr Petrov
 */
public class HashedWheelTimer implements AbstractTimer, ScheduledExecutorService, Runnable {

	private static final Logger logger = Log.log(HashedWheelTimer.class);

	static final int THREAD_PRI =
		Thread.NORM_PRIORITY;
		//Thread.MAX_PRIORITY;

	/**
	 * how many epochs to delay while empty before the thread attempts to end (going into a re-activatable sleep mode)
	 */
	private static final int SLEEP_EPOCHS =
			//32;
			1024;

	private static final int SHUTDOWN = Integer.MIN_VALUE;

	/** in nanoseconds */
	private final long resolution;

	private final int wheels;
    private final WheelModel model;
	private final Executor executor;
	private final WaitStrategy waitStrategy;

	private final AtomicInteger cursor = new AtomicInteger(-1);
	private volatile Thread loop;

	/**
	 * Create a new {@code HashedWheelTimer} using the given timer resolution and wheelSize. All times will
	 * rounded up to the closest multiple of this resolution.
	 *
	 *                  for sparse timeouts. Sane default is 512.
	 * @param strategy  strategy for waiting for the next tick
	 * @param exec      Executor instance to submit tasks to
	 */
	public HashedWheelTimer(WheelModel m, WaitStrategy strategy, Executor exec) {
		model = m;
		this.waitStrategy = strategy;

		this.resolution = m.resolution;

		this.executor = exec;

		this.wheels = m.wheels;
	}

	private static Callable<?> constantlyNull(Runnable r) {
		return () -> {
			r.run();
			return null;
		};
	}

	public void join() {
		Thread t = loop;
		if (t!=null) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {

		logger.info("{} restart", this);

		model.restart(this);

		long deadline = System.nanoTime();

		int empties = 0;
        do {
			int c = cursor();

			while ((cursor.weakCompareAndSet(c, c = (c + 1) % wheels))) {

				if (model.run(c) == 0)
					empties++;
				else
					empties = 0;


				deadline = await(deadline + resolution);
			}
		} while (cursor() >= 0 && (!model.isEmpty() || empties < wheels * SLEEP_EPOCHS));

		logger.info("{} {}", this, cursor() == SHUTDOWN ? "off" : "sleep");

		loop = null;
	}

//    private void await() {
//        try {
//            waitStrategy.waitUntil(System.nanoTime() + resolution);
//        } catch (InterruptedException e) {
//            logger.error("interrupted: {}", e);
//            shutdownNow();
//        }
//    }

	/**
	 * TODO call System.nanoTime() less by passing now,then as args to the wait interfce
	 */
	private long await(long deadline) {
		long now = System.nanoTime();
		long sleepTimeNanos = deadline - now;

		if (sleepTimeNanos > 0) {
			//waitStrategy.waitUntil(deadline);
			waitStrategy.waitNanos(sleepTimeNanos);
		} else {
			float lagThreshold = wheels; //in resolutions
			//if fell behind more than N resolutions, adjust
			if (sleepTimeNanos < -resolution * lagThreshold)
				return now;
		}
		return deadline;
	}

	@Override
	public TimedFuture<?> submit(Runnable runnable) {
		return schedule(new Soon.Run(runnable));
	}

	@Override public final <D> TimedFuture<D> schedule(TimedFuture<D> r) {

		if (r instanceof FixedRateTimedFuture)
			_schedule((FixedRateTimedFuture) r);

		if (r.isCancelled() || !model.accept(r, this))
			return null;
		else {
			assertRunning();
			return r;
		}
	}

	private void _schedule(FixedRateTimedFuture fr) {
		long resolution = this.resolution;
		double epoch = resolution * wheels;
		long periodNS = fr.periodNS.get();
		int rounds = Math.min((int) (periodNS / epoch), Integer.MAX_VALUE);
		fr.rounds = rounds;
		fr.offset = Math.max(1, (int)Math.round(((periodNS - rounds * epoch) / resolution)));
	}

	protected static <X> void reject(Future<X> r) {
		r.cancel(false);
		logger.error("reject {}", r);
	}

	final boolean reschedule(TimedFuture<?> r) {
		int o = r.offset(model.resolution);
		if (model.reschedule(idx(cursorActive() + o), r)) {
			return true;
		} else {
			reject(r);
			return false;
		}
	}

	/**
	 * equivalent to model.idx() since its wheels is equal
	 */
	public final int idx(int cursor) {
		return cursor % wheels;
	}

	int cursor() {
		return cursor.getOpaque();
	}

	final int cursorActive() {
		int c = cursor();
		if (c != -1)
			return c;
		else {
			assertRunning();
			return cursor();
		}
	}

//    public final int idxCursor(int delta) {
//        return idx(cursor() + delta);
//    }

	@Override
	public ScheduledFuture<?> schedule(Runnable runnable,
									   long delay,
									   TimeUnit timeUnit) {
		return scheduleOneShot(NANOSECONDS.convert(delay, timeUnit), constantlyNull(runnable));
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit timeUnit) {
		return scheduleOneShot(NANOSECONDS.convert(delay, timeUnit), callable);
	}

	@Override
	public FixedRateTimedFuture scheduleAtFixedRate(Runnable runnable, long delay, long period, TimeUnit unit) {
		return scheduleFixedRate(NANOSECONDS.convert(period, unit), NANOSECONDS.convert(delay, unit),
			runnable);
	}

	@Override
	public FixedDelayTimedFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
		return scheduleFixedDelay(NANOSECONDS.convert(delay, unit),
			NANOSECONDS.convert(initialDelay, unit),
			constantlyNull(runnable));
	}

	@Override
	public String toString() {
		return String.format("HashedWheelTimer { Buffer Size: %d, Resolution: %s }",
			wheels,
			Str.timeStr(resolution));
	}

	/**
	 * Executor Delegate, invokes immediately bypassing the timer's ordered scheduling.
	 * Use submit for invokeLater-like behavior
	 */
	@Override
	public final void execute(Runnable r) {
		executor.execute(r);
	}

	@Override
	public void shutdown() {
		cursor.set(Integer.MIN_VALUE);
		if (executor instanceof ExecutorService)
			((ExecutorService) this.executor).shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		cursor.set(Integer.MIN_VALUE);
		return executor instanceof ExecutorService ? ((ExecutorService) this.executor).shutdownNow() : Collections.EMPTY_LIST;
	}

	@Override
	public boolean isShutdown() {
		return cursor() >= 0 &&
			(!(executor instanceof ExecutorService) || ((ExecutorService) this.executor).isShutdown());
	}

	@Override
	public boolean isTerminated() {
		return cursor() >= 0 &&
			(!(executor instanceof ExecutorService) || ((ExecutorService) this.executor).isTerminated());
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return
			(!(executor instanceof ExecutorService) || ((ExecutorService) this.executor).awaitTermination(timeout, unit));
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return ((ExecutorService) this.executor).submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return ((ExecutorService) this.executor).submit(task, result);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return ((ExecutorService) this.executor).invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
										 TimeUnit unit) throws InterruptedException {
		return ((ExecutorService) this.executor).invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return ((ExecutorService) this.executor).invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout,
						   TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return ((ExecutorService) this.executor).invokeAny(tasks, timeout, unit);
	}


	/**
	 * INTERNALS
	 */
	private <V> ScheduledFuture<V> scheduleOneShot(long delayNS, Callable<V> callable) {

		if (delayNS <= resolution / 2) {
			//immediate
			ImmediateFuture<V> f = new ImmediateFuture<>(callable);
			executor.execute(f);
			return f;
		} else if (delayNS < resolution) {
			//round-up
			delayNS = resolution;
		}


		long cycleLen = wheels * resolution;
		int rounds = (int) (((double) delayNS) / cycleLen);
		int firstFireOffset = Util.longToInt(delayNS - rounds * cycleLen);

		return schedule(new OneTimedFuture(Math.max(0, firstFireOffset), rounds, callable));
	}


	public FixedRateTimedFuture scheduleFixedRate(long recurringTimeout,
												  long firstDelay,
												  Runnable callable) {


		return scheduleFixedRate(recurringTimeout, firstDelay, new MyFixedRateRunnable(recurringTimeout, callable));
	}

	private FixedRateTimedFuture scheduleFixedRate(long recurringTimeout, long firstDelay, FixedRateTimedFuture r) {
		assert (recurringTimeout >= resolution) : "Cannot schedule tasks for amount of time less than timer precision.";

		if (firstDelay > 0) {
			scheduleOneShot(firstDelay, () -> {
				schedule(r);
				return null;
			});
		} else {
			schedule(r);
		}

		return r;
	}

	private <V> FixedDelayTimedFuture<V> scheduleFixedDelay(long recurringTimeout,
															long firstDelay,
															Callable<V> callable) {
		assert (recurringTimeout >= resolution) : "Cannot schedule tasks for amount of time less than timer precision.";


		FixedDelayTimedFuture<V> r = new FixedDelayTimedFuture<>(
			callable,
			recurringTimeout, resolution, wheels,
			this::schedule);

		if (firstDelay > resolution) {
			scheduleOneShot(firstDelay, () -> {
				schedule(r);
				return null;
			});
		} else {
			schedule(r);
		}

		return r;
	}


	void assertRunning() {
		if (cursor.weakCompareAndSet(-1, 0))
			start();
	}

	private synchronized void start() {
		if (this.loop != null) {

			//HACK time grap between cursor==-1 and loop==null (final thread stop signal)
			Util.sleepMS(10);

			if (this.loop != null)
				throw new RuntimeException("loop exists");
		}

		Thread t = this.loop = new Thread(this, HashedWheelTimer.class.getSimpleName() + '_' + hashCode());
        boolean daemon = false;
        t.setDaemon(daemon);
		t.setPriority(THREAD_PRI);
		t.start();
	}

	public int size() {
		return model.size();
	}

	public enum WaitStrategy {

		/**
		 * Yielding wait strategy.
		 * <p>
		 * Spins in the loop, until the deadline is reached. Releases the flow control
		 * by means of Thread.yield() call. This strategy is less precise than BusySpin
		 * one, but is more scheduler-friendly.
		 */
		YieldingWait {
			@Override
			public void waitUntil(long deadline) {
//				Thread t = null;
				do {
					Thread.yield();
//					if ((t == null ? (t = Thread.currentThread()) : t).isInterrupted()) {
//						//throw new InterruptedException();
//						throw new RuntimeException();
//					}
				} while (deadline >= System.nanoTime());
			}
		},

		/**
		 * BusySpin wait strategy.
		 * <p>
		 * Spins in the loop until the deadline is reached. In a multi-core environment,
		 * will occupy an entire core. Is more precise than Sleep wait strategy, but
		 * consumes more resources.
		 */
		BusySpinWait {
			@Override
			public void waitUntil(long deadline) {
//				Thread t = null;
				do {
					Thread.onSpinWait();
//					if ((t == null ? (t = Thread.currentThread()) : t).isInterrupted()) {
//						//throw new InterruptedException();
//						throw new RuntimeException("interrupted");
//					}
				} while (deadline >= System.nanoTime());
			}
		},

		/**
		 * Sleep wait strategy.
		 * <p>
		 * Will release the flow control, giving other threads a possibility of execution
		 * on the same processor. Uses less resources than BusySpin wait, but is less
		 * precise.
		 */
		SleepWait {
			@Override
			public void waitUntil(long deadline) {
				Util.sleepNS(deadline - System.nanoTime());
			}

		};

		/**
		 * Wait until the given deadline, deadlineNanoseconds
		 *
		 * @param deadlineNanoseconds deadline to wait for, in milliseconds
		 */
		public abstract void waitUntil(long deadlineNanoseconds);

		void waitNanos(long nanos) {
			LockSupport.parkNanos(nanos);
		}

	}

	private static class ImmediateFuture<V> implements ScheduledFuture<V>, Runnable {
		private Callable<V> callable;
		private Object result = this;

		public ImmediateFuture(Callable<V> callable) {
			this.callable = callable;
		}

		@Override
		public long getDelay(TimeUnit timeUnit) {
			return 0;
		}

		@Override
		public int compareTo(Delayed delayed) {
			throw new TODO();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			Callable<V> c = callable;
			if (c != null) {
				result = null;
				callable = null;
				return true;
			}
			return false;
		}

		@Override
		public boolean isCancelled() {
			return result == this && callable == null;
		}

		@Override
		public boolean isDone() {
			return result != this;
		}

		@Override
		public V get() {
			Object r = this.result;
			return r == this ? null : (V) r;
		}

		@Override
		public V get(long l, TimeUnit timeUnit) {
			return AbstractTimedCallable.poll(this, l, timeUnit);
		}

		@Override
		public void run() {
			try {
				result = callable.call();
			} catch (Exception e) {
				result = e;
			}
		}
	}

	private static class MyFixedRateRunnable extends FixedRateTimedFuture {
		private final Runnable callable;

		MyFixedRateRunnable(long recurringTimeout, Runnable callable) {
			super(1, recurringTimeout);
			this.callable = callable;
		}

		@Override public void run() {
			callable.run();
		}
	}
}