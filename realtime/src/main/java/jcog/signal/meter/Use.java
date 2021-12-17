package jcog.signal.meter;

import jcog.Str;
import jcog.exe.Exe;
import org.HdrHistogram.AtomicHistogram;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** resource stopwatch, useful for profiling */
public class Use {

	static final Logger logger = LoggerFactory.getLogger(Use.class);

	public final String id;

	static final long MAX_TIME_SEC = 10;
	static final long MAX_TIME_NS = MAX_TIME_SEC * 1_000_000_000;
	static final long MIN_TIME_NS = 10;

//	final AtomicLong totalNS = new AtomicLong();
	final Histogram timeNS;

	public Use(String id) {
		this(id, true);
	}

	public Use(String id, boolean concurrent) {
		this.id = id;
		timeNS = concurrent ?
			new AtomicHistogram(MIN_TIME_NS, MAX_TIME_NS, 0)
			:
			new Histogram(MIN_TIME_NS, MAX_TIME_NS, 0);
	}

	public void timeNS(long dur) {
//		totalNS.addAndGet(dur);
		if (dur > MIN_TIME_NS) {
			if (dur < MAX_TIME_NS)
				timeNS.recordValue(dur);
			else
				logger.error("{} excessive duration: {}", id, dur);
		}
	}

	public void reset() {
//		totalNS.set(0);
		timeNS.reset();
	}

	public long count() {
		return timeNS.getTotalCount();
	}

	public final class time implements SafeAutoCloseable {

		final long start;

		time() {
			this.start = System.nanoTime();
		}

		@Override
		public void close() {
			long end = System.nanoTime();
			timeNS(end-start);
		}
	}

	public final SafeAutoCloseable time() {
		return Exe.PROFILE ? get() : NullAutocloseable;
	}

	public final SafeAutoCloseable get() {
		return new time();
	}

	static final SafeAutoCloseable NullAutocloseable = () -> { };

	@Override public String toString() {
		return toString(true, true, true, true);
	}

	public String toString(boolean reset, boolean sum, boolean Mean, boolean count) {
		return Str.toString(copy(reset), sum, Mean, count);
	}

	public final Histogram copy(boolean reset) {
		Histogram timeCopy = timeNS instanceof AtomicHistogram ? timeNS.copy() : timeNS; //TODO dont need to copy to AtomicHistogram, non-atomic is ok

		if (reset) timeNS.reset();

		return timeCopy;
	}
}
