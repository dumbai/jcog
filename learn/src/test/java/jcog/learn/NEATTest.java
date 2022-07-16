package jcog.learn;

import jcog.activation.LeakyReluActivation;
import jcog.activation.SigLinearActivation;
import jcog.nn.BackpropRecurrentNetwork;
import jcog.nn.NEAT;
import org.junit.jupiter.api.Test;

import static jcog.learn.MLPTest.xor2Test;

class NEATTest {

    @Test void neatXOR2() {
        xor2Test(new NEAT(
        2, 1, 50, 4*2 /* to be safe until deduplication */));
    }

    @Test void freeformBackpropXOR2_3hidden() {
        freeformBackpropXOR2(3);
    }

    @Test void freeformBackpropXOR2_4hidden() {
        freeformBackpropXOR2(4);
    }
    @Test void freeformBackpropXOR2_5hidden() {
        freeformBackpropXOR2(5);
    }

    private static void freeformBackpropXOR2(int hiddens) {
        BackpropRecurrentNetwork n = new BackpropRecurrentNetwork(2, 1, hiddens, 3);
        xor2Test(n
            .activationFn(LeakyReluActivation.the,
                    //SigmoidActivation.the
                    SigLinearActivation.the
            )
        );
    }
}