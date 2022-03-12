package jcog.signal.meter;

import jcog.event.ListTopic;
import jcog.event.Topic;

import java.util.function.Supplier;

public class ExplainedCounter<E> extends FastCounter {

    public final Topic<E> why = new ListTopic<>();

    public ExplainedCounter(String name) {
        super(name);
    }


    public void increment(Supplier<E> explainer) {
        increment();

        why.accept(explainer);
    }
}