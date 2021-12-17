package jcog.markov;


/**
 * Tests the gradual chaining methods of the data structure.
 *
 * @author OEP
 */
public class GradualTest {
    public static void main(String[] args) {
        String[] shortPhrase1 = "foo bar bing bang".split(" ");
        String[] shortPhrase2 = "foo eat bar bang foo".split(" ");

        String[] longPhrase = "a b c d e f g h i j k l m n o p q r s t u v w x y z".split(" ");
        String[] ragamuffin = "i a q a v a z a d a".split(" ");

        MarkovChain<String> longChain = new MarkovChain<>();
        MarkovChain<String> shortChain = new MarkovChain<>();

        longChain.learn(1, longPhrase);
        longChain.learn(1, ragamuffin);
        shortChain.learn(1, shortPhrase1);
        shortChain.learn(1, shortPhrase2);

		MarkovSampler<String> longChainSampler = (MarkovSampler<String>) new MarkovSampler(longChain);
		MarkovSampler<String> shortChainSampler = (MarkovSampler<String>) new MarkovSampler(shortChain);
        String longy;
        while ((longy = longChainSampler.next()) != null) {
            String shorty = shortChainSampler.nextLoop();
            System.out.printf("%s (%s)\n", longy, shorty);
        }

        longChainSampler.reset();

        while ((longy = longChainSampler.next()) != null) {
            System.out.printf("%s", longy);
        }
        System.out.println();
    }
}