package jcog.grammar.synthesize;

import jcog.grammar.synthesize.util.GrammarUtils;
import jcog.random.XoRoShiRo128PlusRandom;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLInputFactory;
import java.io.StringReader;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;


class XMLTest {

    @Test
    void test1() {
        List<String> examples = List.of(
                "<a xy=\"xy\">xy<a xy=\"xy\">xy<a>xy</a>xy</a>xy</a>",
                "<a>xy<![CDATA[xy]]>xy</a>",
                "<a>xy<!--xy-->xy</a>",
                "<a>123</a>",
                "<a><a>x</a></a>",
                "<a>xy</a>",
                "<a>xy<?xy xy?>xy</a>",
                "<a>xy<a xy=\"xy\"/>xy</a>",
                "<a xy=\"xy\"/>",
                "<a/>"
        );

        XMLInputFactory f = XMLInputFactory.newInstance();//newDefaultFactory();

        Predicate<String> oracle = (q) -> {
            try {
                f.createXMLEventReader(new StringReader(q)).forEachRemaining(r -> {

                });
                return true;
            } catch (Exception e) {

                return false;
            }
        };

        
        GrammarUtils.Grammar grammar = GrammarSynthesis.learn(examples, oracle);

        
        Iterable<String> samples = new GrammarFuzzer.GrammarMutationSampler(grammar, new GrammarFuzzer.SampleParameters(new double[]{
                0.2, 0.2, 0.2, 0.4}, 
                0.8,                                          
                0.1,                                          
                100),
                1000, 20, new XoRoShiRo128PlusRandom(1));

        int pass = 0;
        int count = 0;
        int numSamples = 20;
        for(String sample : samples) {
            RegexSynthesis.logger.info("SAMPLE: {}", sample);
            if(oracle.test(sample)) {
                RegexSynthesis.logger.info("PASS");
                pass++;
            } else {
                RegexSynthesis.logger.info("FAIL");
            }
            //RegexSynthesis.logger.info("");

            if(++count >= numSamples)
                break;
        }
        float rate = (float) pass / numSamples;
        RegexSynthesis.logger.info("PASS RATE: {}", rate);


        assertTrue(rate > 0.7f);

    }
}