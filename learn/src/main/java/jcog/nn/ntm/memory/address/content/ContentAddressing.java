package jcog.nn.ntm.memory.address.content;

import jcog.nn.ntm.control.UVector;
import jcog.nn.ntm.control.Unit;
import jcog.nn.ntm.memory.NTMMemory;
import jcog.nn.ntm.memory.address.Head;

import java.util.function.Function;

public class ContentAddressing {

    public final BetaSimilarity[] similarities;
    public final UVector content;

    public ContentAddressing(BetaSimilarity[] similarities) {
        this.similarities = similarities;
        content = new UVector(similarities.length);

        double max = similarities[0].value;
        for (BetaSimilarity iterationBetaSimilarity : similarities)
            max = Math.max(max, iterationBetaSimilarity.value);

        double sum = 0;
        for (int i = 0; i < similarities.length; i++) {
            double weight = Math.exp(similarities[i].value - max);
            sum += (content.value[i] = weight);
        }
        content.valueMultiplySelf(1.0 / sum);
    }

    public ContentAddressing(NTMMemory memory, Head head) {
        this(sim(memory, head));
    }

    private static BetaSimilarity[] sim(NTMMemory memory, Head head) {
        BetaSimilarity[] sim = new BetaSimilarity[memory.height];
        Unit[][] memoryData = memory.data;
        int memoryColumnsN = memory.height;
        for (int j = 0; j < memoryColumnsN; j++) {
            sim[j] = new BetaSimilarity(head.getBeta(),
                    new Similarity(new CosineSimilarityFunction(), head.keying(), memoryData[j]));
        }
        return sim;
    }

    public static ContentAddressing[] getVector(Integer x, Function<Integer, BetaSimilarity[]> paramGetter) {
        ContentAddressing[] c = new ContentAddressing[x];
        for (int i = 0; i < x; i++)
            c[i] = new ContentAddressing(paramGetter.apply(i));
        return c;
    }

    public void backward() {
        double gradient = content.sumGradientValueProducts();

        int s = content.size();
        double[] g = content.grad;
        double[] v = content.value;
        for (int i = 0; i < s; i++)
            similarities[i].grad += (g[i] - gradient) * v[i];
    }

}