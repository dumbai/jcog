package jcog.ql.dqn;

import jcog.nn.layer.AbstractLayer;

import java.util.Random;

abstract public class StatelessLayer extends AbstractLayer {

    public StatelessLayer(int i, int o) {
        super(i,o);
    }

    @Override
    public void randomize(Random r) {
    }

}