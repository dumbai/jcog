package jcog.grammar.evolve;


import jcog.grammar.evolve.inputs.DataSet;
import jcog.grammar.evolve.outputs.Results;
import jcog.random.XoRoShiRo128PlusRandom;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static jcog.grammar.evolve.DataSetTest.noise;

/**
 * Created by me on 11/29/15.
 */
class EvolveGrammarTest {

    /** JSON example from wiki: https://github.com/MaLeLabTs/RegexGenerator/wiki/Annotated-Dataset */
    String j = "{\n" +
            "  \"name\": \"Log/MAC\",\n" +
            "  \"description\": \"\",\n" +
            "  \"regexTarget\": \"\",\n" +
            "  \"examples\": [\n" +
            "    {\n" +
            "    \"string\": \"Jan 12 06:26:19: ACCEPT service http from 119.63.193.196 to firewall(pub-nic), prefix: \\\"none\\\" (in: eth0 119.63.193.196(5c:0a:5b:63:4a:82):4399 -> 140.105.63.164(50:06:04:92:53:44):80 TCP flags: ****S* len:60 ttl:32)\",\n" +
            "    \"match\": [\n" +
            "        { “start\": 119, \"end\": 136 },\n" +
            "        { \"start\": 161, \"end\": 178 }\n" +
            "    ],\n" +
            "    \"unmatch\": [\n" +
            "        {\"start\": 0,\"end\": 119},\n" +
            "        {\"start\": 136,\"end\": 161},\n" +
            "        {\"start\": 178,\"end\": 215}\n" +
            "    ]\n" +
            "    } ] }";

    @Test
    void test1() throws Exception {
        run( DataSetTest.getExampleDataSet());
    }
    @Test
    void test2() throws Exception {
        Random rng = new XoRoShiRo128PlusRandom(1);
        run(DataSetTest.getExampleDataSet2(

                () -> "/*" + noise(2 + rng.nextInt(3)) + "*/",

                "f(x);", "f(y,x);", "f(x);", "f(x,y);", "f(x,y,z);"

                //"xf(/*ab,c*/z, z1);", "gggg(b /* !;*(fs)s! */);"


                ));
    }



    private static void run(DataSet d) throws Exception {
        Results r = EvolveGrammar.run(new SimpleConfig(
                d, 1000, 100)
                .buildConfiguration()
        );


        System.out.println(r.getBestSolution());
        System.out.println(r.getBestSolution().solutionJS);
        System.out.println(r.getBestExtractionsStats());




        
    }

}