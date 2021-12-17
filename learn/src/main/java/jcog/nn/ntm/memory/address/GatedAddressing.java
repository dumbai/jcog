package jcog.nn.ntm.memory.address;

import jcog.activation.DiffableFunction;
import jcog.nn.ntm.control.UVector;
import jcog.nn.ntm.control.Unit;
import jcog.nn.ntm.control.UnitFactory;
import jcog.nn.ntm.memory.HeadSetting;
import jcog.nn.ntm.memory.address.content.ContentAddressing;

public class GatedAddressing {
    public final Unit gate;
    public final HeadSetting _oldHeadSettings;
    public final ContentAddressing content;
    public final Unit[] GatedVector;
    public final int _memoryCellCount;

    public final double gt;


    public GatedAddressing(Unit gate, ContentAddressing contentAddressing, HeadSetting oldHeadSettings, DiffableFunction act) {
        this.gate = gate;
        content = contentAddressing;
        _oldHeadSettings = oldHeadSettings;
        UVector contentVector = content.content;
        _memoryCellCount = contentVector.size();
        GatedVector = UnitFactory.vector(_memoryCellCount);

        gt = act.valueOf(this.gate.value);

        for (int i = 0; i < _memoryCellCount; i++)
            GatedVector[i].value = jcog.Util.fma(gt, contentVector.value[i], ((1.0 - gt) * _oldHeadSettings.address.value[i]));
    }

    public void backward() {
        UVector contentVector = content.content;
        double gradient = 0;

        UVector oldAddr = _oldHeadSettings.address;

        double[] oa = oldAddr.grad;
        double[] ov = oldAddr.value;
        double gt = this.gt;
        double oneMinusGT = 1 - gt;
        double[] cv = contentVector.value;
        for (int i = 0; i < _memoryCellCount; i++) {
            double gg = GatedVector[i].grad;
            gradient += (cv[i] - ov[i]) * gg;
            contentVector.gradAddSelf(i, gt * gg);
            oa[i] += oneMinusGT * gg;
        }
        gate.grad += gradient * gt * oneMinusGT;
    }

}