package jcog.decision;

import java.util.function.UnaryOperator;


public class TestValue implements UnaryOperator<Object> {
    
    private final Object label;
    
    public TestValue(Object label) {
        super();
        this.label = label;
    }

    @Override
    public Object apply(Object what) {
        return label;
    }

}
