package jcog.nn.ntm.memory;

import jcog.nn.ntm.memory.address.Head;
import jcog.nn.ntm.memory.address.content.ContentAddressing;

public class MemoryState {
    public final NTMMemory mem;
    public final HeadSetting[] heading;
    public final ReadData[] read;

    public MemoryState(NTMMemory mem) {
        this(mem, HeadSetting.getVector(mem));
    }

    private MemoryState(NTMMemory mem, HeadSetting[] heading) {
        this(mem, heading, ReadData.vector(mem, heading));
    }

    public MemoryState(NTMMemory mem, HeadSetting[] heading, ReadData[] reading) {
        this.mem = mem;
        this.heading = heading;
        this.read = reading;
    }

    public void backward() {
        for (ReadData readData : read)
            readData.backward();

        mem.backward();
        for (HeadSetting h : mem.heading) {
            h.backward();
        }
    }


    public void backwardFinal() {

        ContentAddressing[] addr = mem.getContentAddressing();

        for (int i = 0; i < read.length; i++) {

            ReadData r = read[i];
            r.backward();

            ContentAddressing a = addr[i];

            a.content.gradAddSelf(r.head.address.grad);
            a.backward();
        }
    }

    public MemoryState forward(Head[] heads) {
        return mem.state(heads, heading);
    }


}