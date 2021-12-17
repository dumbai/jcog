package jcog.nn.ntm.memory;

import jcog.activation.DiffableFunction;
import jcog.nn.ntm.control.Unit;
import jcog.nn.ntm.control.UnitFactory;
import jcog.nn.ntm.learn.IWeightUpdater;
import jcog.nn.ntm.memory.address.Head;
import jcog.nn.ntm.memory.address.content.BetaSimilarity;
import jcog.nn.ntm.memory.address.content.ContentAddressing;

import java.lang.ref.WeakReference;

public class NTMMemory {

    public final WeakReference<NTMMemory> parent;

    public final Unit[][] data;
    public final int width, height;
    public final HeadSetting[] heading;
    private final Head[] heads;
    private final BetaSimilarity[][] simPrev;
    public final double[][] add;
    public final double[][] erase;
    private final DiffableFunction act;

    public NTMMemory(int height, int width, int heads, DiffableFunction act) {
        this(null, height, width, new Head[heads], UnitFactory.tensor2(height, width), null, act);
    }

    private NTMMemory(HeadSetting[] heading, int height, int width, Head[] heads, Unit[][] data, NTMMemory parent, DiffableFunction act) {
        this.height = height;
        this.width = width;
        this.data = data;
        this.heading = heading;
        this.parent = new WeakReference(parent);
        this.act = act;

        this.heads = heads;

        simPrev = BetaSimilarity.tensor2(heads.length, height);
        erase = tensor2(heads.length, width);
        add = tensor2(heads.length, width);
    }

    NTMMemory(HeadSetting[] heading, Head[] heads, NTMMemory memory, DiffableFunction act) {
        this(heading, memory.height, memory.width, memory.heads,
                UnitFactory.tensor2(memory.height, memory.width), memory, act);

        double[][] erasures = tensor2(memory.height, memory.width);

        int h = headNum();

        for (int i = 0; i < h; i++) {
            Head d = this.heads[i];
            if (d == null)
                this.heads[i] = d = new Head(memory.getWidth());

            Unit[] eraseVector = d.erasing();
            Unit[] addVector = d.adding();
            double[] erases = erase[i];
            double[] adds = add[i];
            for (int j = 0; j < width; j++) {
                erases[j] = act.valueOf(eraseVector[j].value);
                adds[j] = act.valueOf(addVector[j].value);
            }
        }

        NTMMemory p = parent();
        for (int i = 0; i < height; i++) {

            Unit[] oldRow = p.data[i];
            double[] erasure = erasures[i];
            Unit[] row = data[i];
            for (int j = 0; j < width; j++) {
                Unit oldCell = oldRow[j];
                double erase = 1;
                double add = 0;
                for (int k = 0; k < h; k++) {
                    double addressingValue = this.heading[k].address.value[i];
                    erase *= (1.0 - (addressingValue * this.erase[k][j]));
                    add += addressingValue * this.add[k][j];
                }
                erasure[j] = erase;
                row[j].value += (erase * oldCell.value) + add;
            }
        }
    }

    private static double[][] tensor2(int x, int y) {
        double[][] t = new double[x][];
        for (int i = 0; i < x; i++)
            t[i] = new double[y];
        return t;
    }

    public int heads() { return heads.length; }

    public final NTMMemory parent() {
        return parent.get();
    }

    /**
     * number of heads, even if unallocated
     */
    int headNum() {
        return erase.length;
    }

    private int getWidth() {
        return width;
    }

    public void backward() {
        int H = headNum();
        for (int i = 0; i < H; i++) {
            HeadSetting h = heading[i];
            double[] e = this.erase[i];
            double[] a = this.add[i];
            headSettingGradientUpdate(i, e, a, h);
            eraseAndAddGradientUpdate(i, e, a, h, heads[i]);
        }
        memoryGradientUpdate();
    }

    private void memoryGradientUpdate() {
        int h = headNum();

        NTMMemory p = parent();


        HeadSetting[] heading = this.heading;
        double[][] erase = this.erase;

        int height = this.height;
        int width = this.width;

        for (int i = 0; i < height; i++) {

            Unit[] oldDataVector = p.data[i];
            Unit[] newDataVector = data[i];


            for (int j = 0; j < width; j++) {
                double gradient = 1;

                for (int q = 0; q < h; q++)
                    gradient *= 1 - (heading[q].address.value[i] * erase[q][j]);

                oldDataVector[j].grad += gradient * newDataVector[j].grad;
            }
        }
    }

    private void eraseAndAddGradientUpdate(int headIndex, double[] erase, double[] add, HeadSetting headSetting, Head head) {
        Unit[] addVector = head.adding();
        Unit[] eraseVector = head.erasing();

        int h = headNum();

        NTMMemory p = parent();

        for (int j = 0; j < width; j++) {
            double gradientErase = 0;
            double gradientAdd = 0;

            for (int k = 0; k < height; k++) {
                Unit[] row = data[k];
                double itemGradient = row[j].grad;
                double addressingVectorItemValue = headSetting.address.value[k];

                double e = p.data[k][j].value;
                for (int q = 0; q < h; q++) {
                    if (q != headIndex) {
                        e *= 1 - (heading[q].address.value[k] * this.erase[q][j]);
                    }

                }

                double gradientAddressing = itemGradient * addressingVectorItemValue;

                gradientErase -= gradientAddressing * e;
                gradientAdd   += gradientAddressing;
            }


            double e = erase[j];
            double a = add[j];
            eraseVector[j].grad += gradientErase * e * (1 - e);
            addVector[j].grad   += gradientAdd   * a * (1 - a);
        }
    }

    private void headSettingGradientUpdate(int headIndex, double[] erase, double[] add, HeadSetting headSetting) {
        int h = headNum();

        NTMMemory p = parent();

        for (int j = 0; j < height; j++) {

            Unit[] row = data[j];
            Unit[] oldRow = p.data[j];
            double gradient = 0.0;
            for (int k = 0; k < width; k++) {
                Unit data = row[k];
                double oldDataValue = oldRow[k].value;
                for (int q = 0; q < h; q++) {
                    if (q == headIndex)
                        continue;


                    HeadSetting setting = heading[q];
                    oldDataValue *= (1 - (setting.address.value[j] * this.erase[q][k]));
                }
                gradient += ((oldDataValue * (-erase[k])) + add[k]) * data.grad;
            }
            headSetting.address.grad[j] += gradient;
        }
    }

    ContentAddressing[] getContentAddressing() {
        return ContentAddressing.getVector(headNum(), i -> simPrev[i]);
    }

    public void update(IWeightUpdater u) {
        for (BetaSimilarity[] ss : simPrev) {
            u.update(ss);
        }
        u.update(data);
    }

    public MemoryState state(Head[] h, HeadSetting[] s) {
        int H = h.length;
        ReadData[] r = new ReadData[H];
        HeadSetting[] n = new HeadSetting[H];

        for (int i = 0; i < H; i++)
            r[i] = new ReadData(n[i] = h[i].setting(s, i, act, this), this);

        return new MemoryState(new NTMMemory(n, h, this, act), n, r);
    }


}