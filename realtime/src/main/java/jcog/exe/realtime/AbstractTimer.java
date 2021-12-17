package jcog.exe.realtime;

import java.util.concurrent.Executor;

public interface AbstractTimer extends Executor {

    <D> TimedFuture<D> schedule(TimedFuture<D> r);

}
