package jcog.nn.ntm.memory;

import jcog.nn.ntm.control.UVector;
import jcog.nn.ntm.control.Unit;
import jcog.nn.ntm.memory.address.ShiftedAddressing;
import jcog.nn.ntm.memory.address.content.ContentAddressing;


public class HeadSetting {

    public final UVector address;
    public final ShiftedAddressing shift;
    public final Unit gamma;


    public HeadSetting(Unit gamma, ShiftedAddressing shift) {
        this.gamma = gamma;

        this.shift = shift;

        double gammaIndex = getGammaIndex();


        int cellCount = getShiftedVector().length;

        address = new UVector(cellCount);


        double[] addr = address.value;

        Unit[] sv = getShiftedVector();
        int bound = cellCount;
        double sum = 0.0;
        for (int i = 0; i < bound; i++) {
            double v = (addr[i] = Math.pow(sv[i].value, gammaIndex));
            sum += v;
        }

        address.valueMultiplySelf(1.0 / sum);

    }

    public HeadSetting(int memoryColumnsN, ContentAddressing contentAddressing) {
        this(new Unit(), memoryColumnsN, contentAddressing);
    }

    private HeadSetting(Unit gamma, int memoryColumnsN, ContentAddressing contentAddressing) {
        this.gamma = gamma;
        this.shift = null;

        address = new UVector(memoryColumnsN);

        double[] addr = address.value;
        if (memoryColumnsN >= 0) System.arraycopy(contentAddressing.content.value, 0, addr, 0, memoryColumnsN);
    }

    public static HeadSetting[] getVector(NTMMemory m) {
        int x = m.headNum();

        HeadSetting[] h = new HeadSetting[x];
        for (int i = 0; i < x; i++) {
            h[i] = new HeadSetting(
                    m.height,
                    m.getContentAddressing()[i]);
        }
        return h;
    }

    private Unit[] getShiftedVector() {
        return shift.shifted;
    }

    private double getGammaIndex() {
        return Math.log(Math.exp(gamma.value) + 1.0) + 1.0;
    }

    public void backward() {

        Unit[] sv = getShiftedVector();
        int cells = sv.length;

        double[] lns = new double[cells];
        double[] temps = new double[cells];


        double gammaIndex = getGammaIndex();

        double[] addrValue = address.value;
        double[] addrGrad = address.grad;

        for (int i = 0; i < cells; i++) {
            Unit weight = sv[i];
            double weightValue = weight.value;

            double gradient = 0;

            for (int j = 0; j < cells; j++) {
                double v = addrValue[j];
                double g = addrGrad[j];
                gradient += i == j ?
                        g * (1 - v) :
                       -g * v;
            }
            gradient = ((gradient * gammaIndex) / weightValue) * addrValue[i];
            weight.grad += gradient;

            lns[i] = Math.log(weightValue);
            temps[i] = Math.pow(weightValue, gammaIndex);
        }

        double s = 0;
        double lnexp = 0;
        for (int i = 0; i < cells; i++) {
            lnexp += lns[i] * temps[i];
            s += temps[i];
        }

        double lnexps = lnexp / s;
        double g2 = 0;
        for (int i = 0; i < cells; i++) {
            //if (Math.abs(sv[i].value) > EPSILON)
            g2 += addrGrad[i] * (addrValue[i] * (lns[i] - lnexps));
        }

        gamma.grad += g2/(1 + Math.exp(-gamma.value));

        shift.backward();
    }



}