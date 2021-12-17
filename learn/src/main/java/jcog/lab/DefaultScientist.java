package jcog.lab;

import jcog.decision.TableDecisionTree;
import jcog.sort.TopFilter;
import jcog.sort.TopN;
import jcog.util.ArrayUtil;
import org.eclipse.collections.api.tuple.primitive.FloatObjectPair;
import tech.tablesaw.api.Row;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/** default implementation intended for generic usage */
public class DefaultScientist<S,E> extends Scientist<S,E> {

    /** TODO TopFilter<..> */
    final Map<String, TopFilter<FloatObjectPair<String>>> best = new HashMap();

    //TODO running PCA aggregation to determine the general relative influence of each variable



    final Random random;

    int maxVars = -1;
    @Deprecated int experimentIterations = 256;

    public DefaultScientist() {
        this(new Random());
    }

    public DefaultScientist(Random random) {
        this.random = random;

        Runtime.getRuntime().addShutdownHook(new Thread(this::conclude));
    }

    protected void conclude() {
        PrintStream out = System.out;
        for (Map.Entry<String, TopFilter<FloatObjectPair<String>>> entry : best.entrySet()) {
            String experiment = entry.getKey();
            TopFilter<FloatObjectPair<String>> b = entry.getValue();
            out.println(experiment);
            for (FloatObjectPair<String> bb : b) {
                out.println(bb.getTwo());
            }
            out.println();
            out.println();
            out.println();
        }
    }

    @Override public int experimentIterations() {
        return experimentIterations;
    }

    @Override
    public void analyze(Optimize<S, E> results) {

        TopFilter<FloatObjectPair<String>> b = bestsTable(results);
        for (Row r : results.data) {
            float score = (float) r.getDouble(0);
            if (score > b.minValueIfFull()) {
                String report = r.toString();
                b.accept(pair(score, report));
            }
        }

        PrintStream out = System.out;

        //current.print(out);

        TableDecisionTree tree = results.tree(2, 6, 0);
        tree.print(out);
        tree.printExplanations(out);


    }


    /** gets the table of bests for the current result's goal */
    public TopFilter<FloatObjectPair<String>> bestsTable(Optimize<S, E> results) {
        return best.computeIfAbsent(results.goal.id, (g) ->
                new TopN<>(new FloatObjectPair[64], FloatObjectPair::getOne)
        );
    }

    /** decide goal for next experiment */
    @Override public Goal<E> goals() {
        return goals.get(random.nextInt(goals.size())); //TODO choose subset of this list by model
    }

    @Override
    public List<Var<S, ?>> vars() {
        if (maxVars < 0 || vars.size() < maxVars) {
            return vars;
        } else  {
            //random subset
            Var[] v = vars.toArray(new Var[0]);
            ArrayUtil.shuffle(v, random);
            return List.of(ArrayUtil.subarray(v, 0, maxVars));
        }
    }

    @Override
    public List<Sensor<E, ?>> sensors() {
        return sensors;
    }

}
