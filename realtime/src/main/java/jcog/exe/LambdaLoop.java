package jcog.exe;

public class LambdaLoop extends Loop {
    private final Runnable iteration;

    public LambdaLoop(Runnable iteration) {
        this.iteration = iteration;
    }

    @Override
    public String toString() {
        return super.toString() + '[' + iteration + ']';
    }

    @Override
    public boolean next() {
        iteration.run();
        return true;
    }
}
