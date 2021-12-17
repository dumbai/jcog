package jcog.tree.radix;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** TODO use StampedLock? */
public class ConcurrentRadixTree<X> extends MyRadixTree<X> {

    private final @Nullable Lock readLock;
    private final Lock writeLock;

    /**
     * essentially a version number which increments each acquired write lock, to know if the tree has changed
     */
    final AtomicInteger writes = new AtomicInteger();

    public ConcurrentRadixTree() {
        this(false);
    }

    /**
     * * @param restrictConcurrency If true, configures use of a {@link ReadWriteLock} allowing
     * *                            concurrent reads, except when writes are being performed by other threads, in which case writes block all reads;
     * *                            if false, configures lock-free reads; allows concurrent non-blocking reads, even if writes are being performed
     * *                            by other threads
     *
     * @param restrict
     */
    public ConcurrentRadixTree(boolean restrictConcurrency) {
        super();
        //TODO if !restrictConcurrency maybe use Lock direct instead of thru ReentrantRWLock
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.writeLock = readWriteLock.writeLock();
        this.readLock = restrictConcurrency ? readWriteLock.readLock() : null;
    }


    @Override
    protected int beforeWrite() {
        return writes.intValue();
    }

    /** @noinspection LockAcquiredButNotSafelyReleased*/
    public final int acquireWriteLock() {
        writeLock.lock();
        return writes.incrementAndGet();
    }

    public final void releaseWriteLock() {
        writeLock.unlock();
    }


    /** @noinspection LockAcquiredButNotSafelyReleased*/
    public final void acquireReadLockIfNecessary() {
        if (readLock != null)
            readLock.lock();
    }

    public final void releaseReadLockIfNecessary() {
        if (readLock != null)
            readLock.unlock();
    }

    public boolean tryAcquireWriteLock() {
        return writeLock.tryLock();
    }
}
