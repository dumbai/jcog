package jcog.nn.ntm.memory.address;

import jcog.activation.DiffableFunction;
import jcog.nn.ntm.control.Unit;
import jcog.nn.ntm.control.UnitFactory;
import jcog.nn.ntm.memory.HeadSetting;
import jcog.nn.ntm.memory.NTMMemory;
import jcog.nn.ntm.memory.address.content.ContentAddressing;
import org.eclipse.collections.api.block.function.primitive.IntToIntFunction;

public class Head {

    private final Unit[] eraseVector;
    private final Unit[] addVector;
    private final Unit[] keyVector;
    private final Unit beta;
    private final Unit gate;
    private final Unit shift;
    private final Unit gamma;
    private final int width;

    public Head(int memoryWidth) {
        width = memoryWidth;
        keyVector = UnitFactory.vector(memoryWidth);
        addVector = UnitFactory.vector(memoryWidth);
        eraseVector = UnitFactory.vector(memoryWidth);
        beta = new Unit();
        gate = new Unit();
        shift = new Unit();
        gamma = new Unit();
    }

    public static int unitSize(int memoryRowsM) {
        return (3 * memoryRowsM) + 4;
    }

    public static Head[] vector(int length, IntToIntFunction constructorParamGetter) {
        Head[] h = new Head[length];
        for (int i = 0; i < length; i++)
            h[i] = new Head(constructorParamGetter.valueOf(i));
        return h;
    }

    public Unit getBeta() {
        return beta;
    }
    public Unit getGate() {
        return gate;
    }
    public Unit getShift() {
        return shift;
    }
    public Unit getGamma() {
        return gamma;
    }

    public Unit[] keying() {
        return keyVector;
    }
    public Unit[] erasing() {
        return eraseVector;
    }
    public Unit[] adding() {
        return addVector;
    }

    /** HACK multiplex/demultiplex from the arrays */
    public Unit get(int i) {
        if (i < width)
            return eraseVector[i];

        if (i < width * 2)
            return addVector[i - width];

        int width3 = width * 3;
        if (i < width3)
            return keyVector[i - (2 * width)];

        return switch (i - width3) {
            case 0 -> beta;
            case 1 -> gate;
            case 2 -> shift;
            case 3 -> gamma;
            default -> throw new IndexOutOfBoundsException("Index is out of range");
        };

    }

    public HeadSetting setting(HeadSetting[] heading, int i, DiffableFunction act, NTMMemory m) {
        return new HeadSetting(getGamma(),
            new ShiftedAddressing(getShift(),
                new GatedAddressing(getGate(),
                    new ContentAddressing(m, this), heading[i], act), act)
        );
    }
}