package jcog.nn.ntm.memory;

import jcog.nn.ntm.control.UVector;
import jcog.nn.ntm.control.Unit;


/**
 * TODO extend UVector for 'read'
 */
public class ReadData {
    public final HeadSetting head;
    public final Unit[] read;
    private final NTMMemory memory;
    private final int cellWidth;
    private final int cellHeight;

    public ReadData(HeadSetting head, NTMMemory mem) {
        this.head = head;
        memory = mem;
        cellWidth = memory.width;
        cellHeight = memory.height;

        read = new Unit[cellWidth];
        for (int i = 0; i < cellWidth; i++) {
            double temp = 0;
            for (int j = 0; j < cellHeight; j++)
                temp += head.address.value[j] * mem.data[j][i].value;

            read[i] = new Unit(temp);
        }
    }

    /**
     * TODO return UMatrix of ReadData UVector's
     */
    public static ReadData[] vector(NTMMemory memory, HeadSetting[] h) {
        int x = memory.headNum();
        ReadData[] r = new ReadData[x];
        for (int i = 0; i < x; i++)
            r[i] = new ReadData(h[i], memory);
        return r;
    }

    public void backward() {
        UVector a = head.address;
        Unit[][] d = memory.data;
        int w = this.cellWidth, h = this.cellHeight;
        Unit[] r = this.read;
        double[] ag = a.grad;

        for (int i = 0; i < h; i++)
            ag[i] += backward(a.value[i], w, r, d[i]);
    }

    private double backward(double v, int w, Unit[] r, Unit[] d) {
        double g = 0;
        for (int j = 0; j < w; j++) {
            Unit dj = d[j];
            double rg = r[j].grad;
            g += rg * dj.value;
            dj.grad += rg * v;
        }
        return g;
    }

}