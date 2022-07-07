package jcog.exe.realtime;

import java.util.concurrent.Callable;

public class OneTimedFuture<T> extends AbstractTimedCallable<T> {

    private final int offset;

    public OneTimedFuture(int offset, int rounds, Callable<T> callable) {
        super(rounds, callable);
        this.offset = offset;
    }

    @Override
    public final int offset(long resolution) {
        return (int) (offset % resolution);
    }


}
