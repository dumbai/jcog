package jcog.nn.ntm.memory.address.content;

import jcog.nn.ntm.control.Unit;

import java.util.function.BiFunction;

public interface ISimilarityFunction extends BiFunction<Unit[], Unit[], Unit> {

    void differentiate(Unit similarity, Unit[] u, Unit[] v);

}